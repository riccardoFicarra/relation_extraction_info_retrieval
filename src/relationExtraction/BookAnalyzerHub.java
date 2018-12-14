package relationExtraction;

import com.pengyifan.nlp.process.anaphoraresolution.AnaphoraResolver;
import com.pengyifan.nlp.process.anaphoraresolution.AnnotatedText;
import com.pengyifan.nlp.process.anaphoraresolution.CorreferencialPair;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class BookAnalyzerHub {


    static ArrayList<Sentence> analyzeBook(String bookName, int anaphora)
    {
        //Data structures
        ArrayList<Sentence> finalSentences = new ArrayList<>();
        ArrayList<String> sentenceArrayList;
        ArrayList<String> processedSentenceArrayList;
        int numSentences = 0;
        String sentenceAsString, sentenceAsPOS, sentenceAsNER;
        DocumentPreprocessor dp = new DocumentPreprocessor(bookName);
        Properties props = new Properties();
        Boolean personPresencePrev = false;     //flag that is true if previous sentence contains NER tagged people
        Boolean pronounPresencePrev = false;    //flag that is true if previous sentence contains NER pronouns
        Boolean pronounPresenceCurr = false;    //flag that is true if current sentence contains NER pronouns
        Boolean done = false;   //flag that is true if current sentence have been resolved, so next iteration it won't again
        String sentencesForAnaphora = null;   // stores the sentences to be Anaphora resolved at current iteration
        HashSet<String> pronouns = readPronouns("./pronouns.txt");
        String[] resultFromAnaphora;

        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        System.out.println("Analyzing book " + bookName);

        // Initialize the POS tagger and the NER classifier
        MaxentTagger POStagger = new MaxentTagger("english-bidirectional-distsim.tagger");
        AbstractSequenceClassifier NERclassifier = CRFClassifier.getClassifierNoExceptions("english.all.3class.distsim.crf.ser.gz");

        //Create arrayList of sentences
        sentenceArrayList = new ArrayList<>();
        processedSentenceArrayList = new ArrayList<>();
        //For each sentence of the book...
        for (List<HasWord> sentenceAsList : dp)
        {
            //Get string version of the sentence (everything is separated by whitespace)
            sentenceAsString = wordListToString(sentenceAsList);
            //Adding to arrayList
            sentenceArrayList.add(sentenceAsString);
        }
        numSentences = sentenceArrayList.size();

        //Now we analyze 2 sentences at a time in order to improve anaphora resolution when necessary
        int i;
        for(i=0; i<numSentences; i++) {

            //Getting NER resolution (Person, Organization, Location)
            sentenceAsNER = NERclassifier.classifyToString(sentenceArrayList.get(i));
            //If the flag is set, then we need to resolve anaphora for this sentence

            /************************************************************************************************/
            /*             Coreference resolution with Hobbs Algorithm  --- The Gabri Way                   */
            /************************************************************************************************/

            //checking if current sentence contains pronouns
            if (containsPronouns(sentenceArrayList.get(i), pronouns)) {
                pronounPresenceCurr = true;
            }
            // if previous sentence had people in it
            if (personPresencePrev == true && anaphora != 0) {
                // if previous sentence had people in it and if current has pronouns in it
                if (pronounPresenceCurr) {
                    //current and previous sentences must be anaphora resolved
                    sentencesForAnaphora = sentenceArrayList.get(i - 1) + " " + sentenceArrayList.get(i);
                    done = true;
                } else {

                    // if previous sentence has both people and pronouns but hasn't been solved yet and current
                    // has no pronouns
                    if (pronounPresencePrev && !done) {
                        //Only previous sentences must be anphora resolved
                        sentencesForAnaphora = sentenceArrayList.get(i - 1);
                        done = false;
                    }
                    // if previous sentence has people and no pronouns and current has no pronouns
                    else {
                        //nothing to be solved
                        sentencesForAnaphora = null;
                        done = false;
                    }
                }
            }
            else {
                done = false;
                if(i != 0) {
                    processedSentenceArrayList.add(i - 1, sentenceArrayList.get(i - 1));
                }
            }

            // executing anaphora resolution
            if (sentencesForAnaphora != null) {
                try {
                    switch (anaphora) {

                        case 1:
                            resultFromAnaphora = ApplyHobbsAlgorithm(sentencesForAnaphora, pipeline);
                            for (int j = 0; j < resultFromAnaphora.length; j++) {
                                processedSentenceArrayList.add(i - 1 + j, resultFromAnaphora[j]);
                            }
                            break;

                        case 2:
                            String resolved = resolveAnaphora(sentencesForAnaphora, pipeline);
                            Reader reader = new StringReader(resolved);
                            dp = new DocumentPreprocessor(reader);
                            List<String> sentenceList = new ArrayList<>();
                            for (List<HasWord> sentence : dp) {
                                String sentenceString = SentenceUtils.listToString(sentence);
                                sentenceList.add(sentenceString);
                            }
                            for (int j = 0; j < sentenceList.size(); j++) {
                                processedSentenceArrayList.add(i - 1 + j, sentenceList.get(j));
                            }
                            break;
                    }
                }
                catch (Exception e){
                    processedSentenceArrayList.add(i - 1, sentenceArrayList.get(i - 1));
                    processedSentenceArrayList.add(i, sentenceArrayList.get(i));
                }

            }

            if(i!=0)
            {
                //checking if current phrase contains people
                if(sentenceAsNER.contains("PERSON"))
                {
                    // this means that next sentence should be processed by anaphora resolver if contains pronouns
                    personPresencePrev = true;
                }
                // updating previous sentence pronoun presence flag
                pronounPresencePrev = pronounPresenceCurr;
            }

            if(i != 0) {
                ///////////////////////////////////////////////////////////////////
                //Now that we have the final sentence, we store it in the correct structures...
                //Getting NER resolution (Person, Organization, Location)
                sentenceAsNER = NERclassifier.classifyToString(processedSentenceArrayList.get(i - 1));
                //POS Tagging (We already have NER)
                try {
                    sentenceAsPOS = POStagger.tagString(processedSentenceArrayList.get(i - 1));
                } catch(OutOfMemoryError e) {
                    System.out.println("Sentence Skipped");
                }
                //Saving into the onject
                Sentence s = new Sentence(sentenceArrayList.get(i - 1), sentenceAsPOS, sentenceAsNER);
                if (s.getAppearingCharacters().size() >= 2)
                    finalSentences.add(s);
            }
        }

        if(processedSentenceArrayList.size() != numSentences) {
            //last sentence must be added
            try {
                Sentence s = new Sentence(sentenceArrayList.get(i - 1),
                        POStagger.tagString(sentenceArrayList.get(i - 1)),
                        NERclassifier.classifyToString(sentenceArrayList.get(i - 1)));
            } catch(OutOfMemoryError e) {
                System.out.println("Sentence skipped");
            }
            if (s.getAppearingCharacters().size() >= 2)
                finalSentences.add(s);
        }

        return finalSentences;
    }


    private static String wordListToString(List<HasWord> sentence)
    {
        StringBuilder builder = new StringBuilder();

        for(HasWord word:sentence)
        {
            builder.append(word.toString()+" ");
        }

        return builder.toString();
    }



    private static String resolveAnaphora(String text, StanfordCoreNLP pipeline) throws Exception{

        Annotation doc = new Annotation(text);
        pipeline.annotate(doc);


        Map<Integer, CorefChain> corefs = doc.get(CorefCoreAnnotations.CorefChainAnnotation.class);
        List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);


        List<String> resolved = new ArrayList<>();

        for (CoreMap sentence : sentences) {

            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);

            for (CoreLabel token : tokens) {

                Integer corefClustId= token.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);
                //System.out.println(token.word() +  " --> corefClusterID = " + corefClustId);


                CorefChain chain = corefs.get(corefClustId);
                //System.out.println("matched chain = " + chain);


                if(chain==null){
                    resolved.add(token.word());
                    //System.out.println("Adding the same word "+token.word());
                }else{

                    int sentINdx = chain.getRepresentativeMention().sentNum -1;
                    //System.out.println("sentINdx :"+sentINdx);
                    CoreMap corefSentence = sentences.get(sentINdx);
                    List<CoreLabel> corefSentenceTokens = corefSentence.get(CoreAnnotations.TokensAnnotation.class);
                    String newwords = "";
                    CorefChain.CorefMention reprMent = chain.getRepresentativeMention();
                    //System.out.println("reprMent :"+reprMent);
                    //System.out.println("Token index "+token.index());
                    //System.out.println("Start index "+reprMent.startIndex);
                    //System.out.println("End Index "+reprMent.endIndex);
                    if (token.index() <= reprMent.startIndex || token.index() >= reprMent.endIndex) {

                        for (int i = reprMent.startIndex; i < reprMent.endIndex; i++) {
                            CoreLabel matchedLabel = corefSentenceTokens.get(i - 1);
                            resolved.add(matchedLabel.word().replace("'s", ""));
                            //System.out.println("matchedLabel : "+matchedLabel.word());
                            newwords += matchedLabel.word() + " ";

                        }
                    }

                    else {
                        resolved.add(token.word());
                        //System.out.println("token.word() : "+token.word());
                    }

                    //System.out.println("converting " + token.word() + " to " + newwords);
                }


                //System.out.println();
                //System.out.println();
                //System.out.println("-----------------------------------------------------------------");

            }

        }


        String resolvedStr ="";
        System.out.println();
        for (String str : resolved) {
            resolvedStr+=str+" ";
        }
        System.out.println(resolvedStr);
        return resolvedStr;

    }

    public static String[] ApplyHobbsAlgorithm(String text, StanfordCoreNLP pipeline) throws Exception{
        Tree tree;
        String treeString, sentenceTreeString  = "";
        AnnotatedText aText;
        AnaphoraResolver u = new AnaphoraResolver();
        List<CorreferencialPair> vet;
        int refereeIndex, refererIndex;
        String[] result;

        System.out.println(text);

        Annotation doc = new Annotation(text);
        pipeline.annotate(doc);

        List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);

        // merging the parse tree of the sentences together
        treeString = "(S1";
        for(CoreMap sentence: sentences) {
            // this is the parse tree of the current sentence
            tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            sentenceTreeString = tree.toString();
            sentenceTreeString = sentenceTreeString.substring(0, sentenceTreeString.length()-1);
            treeString = treeString + " " + sentenceTreeString.replaceAll("\\([\\s]*ROOT", "");
        }
        treeString = treeString + ")";

        // getting the substitution to be executed
        aText = new AnnotatedText(treeString);
        vet = u.resolverV1(aText.getNPList(), aText.getPRPList());

        // converting the sentences in array of strings
        // converting the array of strings in a ArrayList of ArrayList of single words
        ArrayList<ArrayList<String>> elaboration = new ArrayList<>();
        for(String element: sentences.stream().map(CoreMap::toString).collect(Collectors.toList())) {
            elaboration.add((ArrayList<String>)Arrays.stream(element.split(" ")).collect(Collectors.toList()));
        }

        if(text.contains("When we came back to work ")) {
            System.out.println("");
        }

        // going through the reference found in order
        for (int i = 0; i < vet.size(); i ++) {

            System.out.println(vet.get(i).toString());

            //getting the indices referred to the unique big sentence
            if((vet.get(i).getReferee() != null) && (vet.get(i).getReferer() != null)) {
                refereeIndex = vet.get(i).getReferee().getWordIndex();
                refererIndex = vet.get(i).getReferer().getWordIndex();

                //defining to which original sentence the words belong
                // and computing the real indices
                int refereeSentence = 0;
                int refererSentence = 0;
                int tot = 0;
                while(refereeIndex >= tot + elaboration.get(refereeSentence).size()) {
                    tot += elaboration.get(refereeSentence).size();
                    refereeSentence++;
                }
                refereeIndex -= tot;
                tot  = 0;
                while(refererIndex >= tot + elaboration.get(refererSentence).size()) {
                    tot += elaboration.get(refererSentence).size();
                    refererSentence++;
                }
                refererIndex -= tot;


            /*
                Temporary solution: since the substitution of a multiword element will change the index meaning,
                the new position is searched by before and after for 3 position, looking for the word considered
            */
                //finally substituting referer with referee
                for(int j = refererIndex - 3; j < refererIndex - 3 && j < elaboration.get(refererIndex).size(); j++) {
                    if (j >= 0) {
                        if (elaboration.get(refererSentence).get(j).equals(vet.get(i).getReferer().getWord().split(" ")[0])) {
                            elaboration.get(refererSentence).remove(j);
                            refererIndex = j;
                        }
                    }
                }
                for(int j = 0; j < vet.get(i).getReferee().getWord().split(" ").length; j++) {
                    for(int k = refereeIndex - 3; k < refereeIndex - 3 && k < elaboration.get(refereeIndex).size(); j++) {
                        if(k >= 0) {
                            if(elaboration.get(refereeSentence).get(k+j).equals(vet.get(i).getReferee().getWord().split(" ")[j])) {
                                elaboration.get(refererSentence).add(refererIndex + j, elaboration.get(refereeSentence).get(k + j));
                            }
                        }
                    }
                }
            }
        }

        result = new  String[sentences.size()];
        for(int i = 0; i < result.length; i++) {
            result[i] = elaboration.get(i).stream().collect(Collectors.joining(" "));
        }

        return result;
    }


    public static void splittingTest(String text, StanfordCoreNLP pipeline)
    {
        String[] paragraphs = text.split("(?m)(?=^\\s{4})");
        System.out.println(paragraphs.length);
        for(String par:paragraphs)
        {
            try {
                resolveAnaphora(par, pipeline);
            } catch (Exception e) {

            }
        }
    }

    // read a file and store each line a element of a Set [each line is supposed to be s single word pronoun]
    public static HashSet<String> readPronouns(String filepath) {
        //read file into stream, try-with-resources
        HashSet<String> pronouns = new HashSet<>();
        try (Stream<String> stream = Files.lines(Paths.get(filepath))) {
            // adding to the hashset each pronoun read from the file
            stream.map(s -> s.trim()).forEach(pronouns::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pronouns;
    }

    // this function return true if s contains at least one occurrence of the element of p [ p is supposed to store pronouns]
    public static Boolean containsPronouns(String sentence, HashSet<String> p) {
        String[] tokens = sentence.split("[\\s]+");

        for(String s : tokens) {
            if(p.contains(s)) {
                return true;
            }
        }

        return false;
    }
}

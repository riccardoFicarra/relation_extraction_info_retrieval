package relationExtraction;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class BookAnalyzerHub {


    static ArrayList<Sentence> analyzeBook(String bookName)
    {
        //Data structures
        ArrayList<Sentence> finalSentences = new ArrayList<>();
        int numSentences = 0;
        String sentenceAsString, sentenceAsPOS, sentenceAsNER;
        DocumentPreprocessor dp = new DocumentPreprocessor(bookName);
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        System.out.println("Analyzing book " + bookName);


        //String text = new String(Files.readAllBytes(Paths.get("./input.txt")), StandardCharsets.UTF_8);
        //resolveAnaphora(text, pipeline);
        //splittingTest(text, pipeline);


        // Initialize the POS tagger and the NER classifier
        MaxentTagger POStagger = new MaxentTagger("./english-bidirectional-distsim.tagger");
        AbstractSequenceClassifier NERclassifier = CRFClassifier.getClassifierNoExceptions("./english.all.3class.distsim.crf.ser.gz");

        //Create arrayList of sentences
        ArrayList<String> sentenceArrayList = new ArrayList<>();
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
        boolean toResolve = false;
        for(int i=0; i<numSentences; i++)
        {
            String currentFinalSentence;

            //Getting NER resolution (Person, Organization, Location)
            sentenceAsNER = NERclassifier.classifyToString(sentenceArrayList.get(i));
            //If the flag is set, then we need to resolve anaphora for this sentence
            currentFinalSentence = sentenceArrayList.get(i);
            /*if(toResolve==true)
            {
                toResolve = false;
                String tmp = sentenceArrayList.get(i-1) + " " + sentenceArrayList.get(i);
                String resolved = resolveAnaphora(tmp, pipeline);
                Reader reader = new StringReader(resolved);
                dp = new DocumentPreprocessor(reader);
                List<String> sentenceList = new ArrayList<String>();
                for (List<HasWord> sentence : dp)
                {
                    String sentenceString = SentenceUtils.listToString(sentence);
                    sentenceList.add(sentenceString);
                }
                if(sentenceList.size()>=2)
                    currentFinalSentence = sentenceList.get(1);

            }*/
            //currentFinalSentence = resolveAnaphora(sentenceArrayList.get(i), pipeline);

            //If this is not the first sentence, check the NON-RESOLVED version of the phrase for if we need to resolve anaphora in the following phrase
            if(i!=0)
            {
                if(sentenceAsNER.contains("PERSON"))
                {
                    toResolve = true;
                }
            }


            ///////////////////////////////////////////////////////////////////
            //Now that we have the final sentence, we store it in the correct structures...
            //POS Tagging (We already have NER)
            try {
                sentenceAsPOS = POStagger.tagString(currentFinalSentence);
                Sentence s = new Sentence(sentenceArrayList.get(i), sentenceAsPOS, sentenceAsNER);
                if (s.getAppearingCharacters().size() >= 2)
                    finalSentences.add(s);
            } catch (OutOfMemoryError e) {
                System.err.println("Sentence skipped because out of memory");
            }
            //Saving into the onject


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



    private static String resolveAnaphora(String text, StanfordCoreNLP pipeline){

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


    public static void splittingTest(String text, StanfordCoreNLP pipeline)
    {
        String[] paragraphs = text.split("(?m)(?=^\\s{4})");
        System.out.println(paragraphs.length);
        for(String par:paragraphs)
        {
            resolveAnaphora(par, pipeline);
        }
    }

}

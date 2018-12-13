package relationExtraction;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import edu.stanford.nlp.simple.*;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 *
 * @author artur
 */
public class Word implements Serializable {

    private String text, pos, ner;

    public Word(String text, String pos, String ner) {
        this.text = text;
        this.pos = pos;
        this.ner = ner;
    }

    public String getText() {
        return text;
    }

    public String getPos() {
        return pos;
    }

    public String getNer() {
        return ner;
    }

    boolean isNotStopword(String word, HashSet<String> stopWordSet)
    {
        word = word.trim();
        word = word.toLowerCase();
        if(stopWordSet.contains(word))
            return false;
        else
            return true;
    }

    boolean isNotPunctuation(String word)
    {
        word = word.trim();
        if (Pattern.matches("\\p{Punct}", word))
            return false;
        else
            return true;
    }

    boolean isNotNumber(String word)
    {
        word = word.trim();
        if(Pattern.matches("\\d+", word))
            return false;
        else
            return true;
    }

    //This function returns the lemma of the considered word
    String getLemma()
    {

        StanfordCoreNLP pipeline = new StanfordCoreNLP(new Properties(){{
            setProperty("annotators", "tokenize,ssplit,pos,lemma");
        }});

        Annotation tokenAnnotation = new Annotation(this.getText());
        pipeline.annotate(tokenAnnotation);  // necessary for the LemmaAnnotation to be set.
        List<CoreMap> list = tokenAnnotation.get(SentencesAnnotation.class);
        String tokenLemma = list
                .get(0).get(TokensAnnotation.class)
                .get(0).get(LemmaAnnotation.class);


        //String tokenLemma = new edu.stanford.nlp.simple.Sentence(this.getText()).lemma(0);
        System.out.println("I converted " + this.getText() +" to " + tokenLemma);
        return tokenLemma;
    }
}

package relationExtraction;

import java.io.Serializable;
import java.util.HashSet;
import java.util.regex.Pattern;

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
}

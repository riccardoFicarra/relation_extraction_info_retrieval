package relationExtraction;

import java.io.Serializable;

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
}

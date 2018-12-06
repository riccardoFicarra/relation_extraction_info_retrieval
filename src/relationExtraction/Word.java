package relationExtraction;

/**
 *
 * @author artur
 */
public class Word {
    
    String myText, myPOS, myNER;
    
    public Word(String word, String POS, String NER)
    {
        myText = new String(word);
        myPOS = new String(POS);
        myNER = new String(NER);

    }
    
}

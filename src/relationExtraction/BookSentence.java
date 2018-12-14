package relationExtraction;

public class BookSentence {

    private String book;
    private Sentence sentence;

    public BookSentence(String b, Sentence s) {
        book = b;
        sentence = s;
    }

    public Sentence getSentence() {
        return sentence;
    }

    public String getBook() {
        return book;
    }
}

package relationExtraction;



import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        String crFilePath = args[0];
        String booksPath = args[1];    //must end with / or \ (win or unix)
        CharacterRelationParser crp = new CharacterRelationParser(crFilePath);
        HashMap<String, Book> books = crp.parseCharacterRelations();
        //printCharacters(books);
        //checkBookFilenames(books, booksPath);

        for (String title : books.keySet()) {
            try {
                BookAnalyzerHub.analyzeBook(booksPath + title + ".txt");

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    static void printCharacters(HashMap<String, Book> books) {
        books.values().forEach(b -> {
            System.out.println(b.getTitle());
            b.getCharacters().forEach(c -> System.out.println("\t" + c));
        });
    }

    private static void checkBookFilenames(HashMap<String, Book> books, String booksPath) {
        //snippet to check that all books are correctly named
        for (String title : books.keySet().stream().sorted().collect(Collectors.toList())) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(booksPath + title + ".txt"));

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}

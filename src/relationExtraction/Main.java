package relationExtraction;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    /**
     * @param args
     * [0] -> path of character relations file
     * [1] -> path of books folder
     * [3] -> options: p = parse, f = force overwrite, blank = read books from file
     */
    public static void main(String[] args) {
        String crFilePath = args[0];
        String booksPath = args[1];    //must end with / or \ (win or unix)
        String booksOpt = args.length == 3 ? args[2] : "";
        String bookFilename = "books.dat";
        //printCharacters(books);
        //checkBookFilenames(books, booksPath);
        HashMap<String, Book> books;
        if (booksOpt.contains("p")) {
            File booksFile = new File(bookFilename);
            boolean bookExists = booksFile.exists();
            String choice = "";
            if (bookExists && !booksOpt.contains("f")) {
                System.err.println("CAUTION: DO YOU WANT TO OVERWRITE THE FILE? y/n");
                Scanner scanner = new Scanner(System.in);
                choice = scanner.nextLine();
            }
            if (!bookExists || booksOpt.contains("f") || choice.equals("y")) {
                CharacterRelationParser crp = new CharacterRelationParser(crFilePath);
                books = crp.parseCharacterRelations();
                for (Book book : books.values()) {
                    try {
                        book.setSentences(BookAnalyzerHub.analyzeBook(booksPath + book.getTitle() + ".txt"));
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    //break; //uncomment to parse only first book
                }
                System.out.println("Parsing complete, writing to file");
                ObjectIO.writeBooksToFile(bookFilename, books.values());
            }
        } else {
            books = ObjectIO.readBooksFromFile(bookFilename);
            printCharacters(books);
        }
    }

    private static void printCharacters(HashMap<String, Book> books) {
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

package relationExtraction;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    /**
     * @param args in options: p = parse, f = force overwrite
     */
    public static void main(String[] args) {
        String crFilePath = args[0];
        String booksPath = args[1];    //must end with / or \ (win or unix)
        String booksOpt = args.length == 3 ? args[2] : "";
        String bookOutFile = "books";
        int nfile = 3;
        //printCharacters(books);
        //checkBookFilenames(books, booksPath);
        HashMap<String, Book> books;
        if (booksOpt.contains("p")) {
            File booksFile = new File(bookOutFile + "1.dat");
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
                int skip = books.size() / nfile + 1;
                for (int j = 0; j < nfile; j++) {
                    books.values().stream().skip(j).limit(j * skip).forEach(b -> addSentences(b, booksPath));
                    System.out.println("Parsing complete, writing to file");
                    ObjectIO.writeBooksToFile(bookOutFile + j + ".dat", books.values());
                }

            }
        } else {
            books = new HashMap<>();
            for (int j = 0; j < nfile; j++) {
                books.putAll(ObjectIO.readBooksFromFile(bookOutFile + j + ".dat"));
            }
            printCharacters(books);
        }
    }

    private static void addSentences(Book book, String booksPath) {
        try {
            book.setSentences(BookAnalyzerHub.analyzeBook(booksPath + book.getTitle() + ".txt"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
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

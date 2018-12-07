package relationExtraction;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    /**
     * @param args
     * [0] -> path of character relations file
     * [1] -> path of books folder
     * [2] -> options: p = parse, f = force overwrite, b = build Naive Bayes model
     * [3] [only with b option] label type to use in classifier. {affinity, coarse, fine}
     */
    public static void main(String[] args) {
        String crFilePath = args[0];
        String booksPath = args[1];    //must end with / or \ (win or unix)
        String options = args.length == 3 ? args[2] : "";
        String labelType = args.length == 4 ? args[3] : null;
        String bookOutFile = "books";
        String bookInFile = "booksOnly1"; //this should be the same as bookOutFile, splitted for debugging
        int nfile = 3;
        //PARSING FILES
        HashMap<String, Book> books = null;
        if (options.contains("p")) {
            File booksFile = new File(bookOutFile + "1.dat");
            boolean bookExists = booksFile.exists();
            String choice = "";
            if (bookExists && !options.contains("f")) {
                System.err.println("CAUTION: DO YOU WANT TO OVERWRITE THE FILE? y/n");
                Scanner scanner = new Scanner(System.in);
                choice = scanner.nextLine();
            }
            if (!bookExists || options.contains("f") || choice.equals("y")) {
                CharacterRelationParser crp = new CharacterRelationParser(crFilePath);
                books = crp.parseCharacterRelations();
                int skip = books.size() / nfile + 1;
                for (int j = 0; j < nfile; j++) {
                    Collection<Book> bookSlice = books.values().stream().skip(j * 3).limit(skip).collect(Collectors.toCollection(ArrayList::new));
                    //bookSlice.forEach(b -> addSentences(b, booksPath));
                    bookSlice.forEach(b -> System.out.println(b.getTitle()));
                    System.out.println("Parsing complete, writing to file");
                    ObjectIO.writeBooksToFile(bookOutFile + j + ".dat", bookSlice);
                }

            }
        } else {
            books = new HashMap<>();
            for (int j = 0; j < nfile; j++) {
                books.putAll(ObjectIO.readBooksFromFile(bookInFile + j + ".dat"));
                System.out.println("Finished reading file " + j);
            }
            printCharacters(books);
        }
        //NAIVE BAYES MODEL
        if (options.contains("b")) {
            NaiveBayes nbm = new NaiveBayes(labelType);
            nbm.buildModel(books);
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

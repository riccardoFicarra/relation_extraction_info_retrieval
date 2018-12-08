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
     * [2] -> options: p = parse, f = force overwrite, b = build Naive Bayes model, l = load Naive Bayes model from file
     * [3] [only with b or l option] label type to use in classifier. {affinity, coarse, fine}
     */
    public static void main(String[] args) {
        String crFilePath = args[0];
        String booksPath = args[1];    //must end with / or \ (win or unix)
        String options = args.length >= 3 ? args[2] : "";
        String labelType = args.length == 4 ? args[3] : null;
        String processedBooksPath = "processedBooks/";
        String bookOutFile = "booksJson";
        String bookInFile = "booksJson";
        String modelFileName = "NaiveBayesModel.json";
        int nfile = 3;
        //PARSING FILES
        HashMap<String, Book> books = null;
        if (options.contains("p")) {
            File booksFile = new File(processedBooksPath);
            File[] listfiles = booksFile.listFiles();
            boolean bookExists = listfiles != null && listfiles.length > 0;
            String choice = "";
            if (bookExists && !options.contains("f")) {
                System.err.println("CAUTION: DO YOU WANT TO OVERWRITE THE FILE? y/n");
                Scanner scanner = new Scanner(System.in);
                choice = scanner.nextLine();
            }
            if (!bookExists || options.contains("f") || choice.equals("y")) {
                CharacterRelationParser crp = new CharacterRelationParser(crFilePath);
                books = crp.parseCharacterRelations();
                /*books.values().forEach(b -> {
                    System.out.println(b.getTitle());
                    ObjectIO.writeBookToFile(processedBooksPath,b);});*/

                books.values().stream().limit(1).forEach(b -> {
                    b.addSentences(booksPath);
                    ObjectIO.writeBookToFile(processedBooksPath, b);
                });


            }
        } else {
            try {
                books = ObjectIO.readBooksFromFile(processedBooksPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //printCharacters(books);
        }
        // NAIVE BAYES MODEL
        NaiveBayes nbm = null;
        if (options.contains("b")) {
            nbm = new NaiveBayes(labelType);
            nbm.buildModel(books);
            nbm.saveModelToFile(modelFileName);
        } else if (options.contains("l"))
            nbm = new NaiveBayes(modelFileName, labelType);
        nbm.toString();
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

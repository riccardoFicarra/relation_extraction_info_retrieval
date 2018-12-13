package relationExtraction;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    /**
     * @param args [0] -> path of character relations file
     *             [1] -> path of books folder
     *             [2] -> options: p = parse, f = force overwrite, b = build Naive Bayes model with all books, l = load
     *             Naive Bayes model from file
     *             x -> do 10-fold validation
     *             [3] [only with b, x or l option] label type to use in classifier. {affinity, coarse, fine}
     */
    public static void main(String[] args) {

        HashSet<String> stopWordSet;
        String crFilePath = args[0];
        String booksPath = args[1];    //must end with / or \ (win or unix)
        String options = args.length >= 3 ? args[2] : "";
        String labelType = args.length == 4 ? args[3] : null;
        String processedBooksPath = "processedBooks/";
        String bookOutFile = "booksJson";
        String bookInFile = "booksJson";
        String modelFileName = "NaiveBayesModel.json";
        int maxFolds = 3;

        //Initializing Stop Word set
        stopWordSet = OurUtils.prepareStopWordList("./stopwords.txt");

        //PARSING FILES
        HashMap<String, Book> books = null;
        if (options.contains("p")) {
            //Parsing option
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

                books.values().forEach(b -> {
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
            nbm.buildModel(books, stopWordSet);
            nbm.saveModelToFile(modelFileName);
        } else if (options.contains("l")) {
            nbm = new NaiveBayes(modelFileName, labelType);


            for (Book book : books.values()) {
                HashMap<String, HashMap<String, String>> classified = nbm.classifyBook(book);
                System.out.println(book.getTitle());
                for (String char1 : classified.keySet()) {
                    for (String char2 : classified.get(char1).keySet()) {
                        System.out.println(char1 + " " + char2 + " -> " + classified.get(char1).get(char2));
                    }
                }
                book.compareResults(classified);
            }
            System.out.println("Done");
        } else if (options.contains("x")) {

            int skip = books.size() / maxFolds + 1;
            ArrayList<List<Book>> splits = new ArrayList<>(maxFolds);
            for (int fold = 0; fold < maxFolds; fold++)
                splits.add(books.values().stream().skip(fold * skip).limit(skip).collect(Collectors.toList()));
            /*outer key: label1
             inner key: label2
             value = count;
             */
            HashMap<String, HashMap<String, Integer>> confusionMatrix = new HashMap<>();
            String total = "_total";
            for (int fold = 0; fold < maxFolds; fold++) {

                //Model building with only one part of set
                nbm = new NaiveBayes(labelType);
                nbm.buildModel(getHashMap(splits.get(fold)), stopWordSet);

                for (int split = 0; split < splits.size(); split++) {
                    if (split == fold)
                        //skip classification of training set
                        continue;
                    for (Book book : splits.get(split)) {
                        //classifyUpdateConfusionMatrix(book, confusionMatrix, nbm);
                        HashMap<String, HashMap<String, String>> classified = nbm.classifyBook(book);
                        //compute confusion matrix
                        book.compareResultsCumulative(classified, confusionMatrix);
                    }
                }
                System.out.println("Completed fold #" + (fold + 1));
            }
            //confusion matrix is complete
            for (String label : confusionMatrix.keySet()) {
                if (label.equals("NR") || label.equals(total)) continue;
                double precision = (double) confusionMatrix.get(label).get(label) //correctly classified
                        / confusionMatrix.get(total).get(label);   //total classified with that label
                double recall = (double) confusionMatrix.get(label).get(label) //correctly classified
                        / confusionMatrix.get(label).get(total);   //total actually with that label
                double fmeasure = 2 * precision * recall / (precision + recall);
                System.out.println("Label\t" + label + " precision: " + precision + " recall " + recall + " F measure: " + fmeasure);
            }

        } else {


            //----------CLASSIFYING VIRGIN BOOK---------------------------
            //Now we try to classify a virgin book
            String bookPath = "./TrainingBooks/Ghosts.txt";
            Book bookToAnalyze = new Book(bookPath, "---");
            HashMap<String, HashMap<String, String>> classifiedCharacters;

            bookToAnalyze.setSentences(BookAnalyzerHub.analyzeBook(bookPath));
            if (nbm != null) {
                classifiedCharacters = nbm.classifyBook(bookToAnalyze);
                System.out.println(classifiedCharacters);
            }


        }
    }

    private static void classifyUpdateConfusionMatrix(Book book,
                                                      HashMap<String, HashMap<String, Integer>> confusionMatrix,
                                                      NaiveBayes nbm) {
        HashMap<String, HashMap<String, String>> classified = nbm.classifyBook(book);

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

    private static HashMap<String, Book> getHashMap(List<Book> books) {
        HashMap<String, Book> temp = new HashMap<>();
        for (Book book : books)
            temp.put(book.getTitle(), book);
        return temp;
    }
}

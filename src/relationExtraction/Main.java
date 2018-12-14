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
            HashSet<String> listfiles =
                    Arrays.stream(booksFile.listFiles()).map(File::getName).collect(Collectors.toCollection(HashSet::new));
            /*String choice = "";
            if (bookExists && !options.contains("f")) {
                System.err.println("CAUTION: DO YOU WANT TO OVERWRITE THE FILE? y/n");
                Scanner scanner = new Scanner(System.in);
                choice = scanner.nextLine();
            }
            */

            CharacterRelationParser crp = new CharacterRelationParser(crFilePath);
            books = crp.parseCharacterRelations();
            /*books.values().forEach(b -> {
                System.out.println(b.getTitle());
                ObjectIO.writeBookToFile(processedBooksPath,b);});*/
            for (Book book : books.values()) {
                if (!listfiles.contains(book.getTitle() + ".json")) {
                    book.addSentences(booksPath);
                    if (book.getSentences() != null) {
                        ObjectIO.writeBookToFile(processedBooksPath, book);
                    } else {
                        System.err.println("ERROR WITH SENTENCES OF BOOK " + book.getTitle());
                    }
                }
            }
   /*         books.values().stream()
                    .filter(b -> !listfiles.contains(b.getTitle()+".json"))
                    .forEach(b -> {
                        b.addSentences(booksPath);
                        if (b.getSentences() != null) {
                            //only write to file the ones where parsing was completed
                    ObjectIO.writeBookToFile(processedBooksPath, b);
                        }
                    });*/

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

            nFoldValidation(maxFolds, books, nbm, labelType, stopWordSet);

        }

    }


    private static void nFoldValidation(int maxFolds, HashMap<String, Book> books, NaiveBayes nbm, String labelType,
                                        HashSet<String> stopWordSet) {
        int skip = books.size() / maxFolds + 1;
        ArrayList<List<Book>> splits = new ArrayList<>(maxFolds);
        for (int fold = 0; fold < maxFolds; fold++)
            splits.add(books.values().stream().skip(fold * skip).limit(skip).collect(Collectors.toList()));
            /*outer key: label1
             inner key: label2
             value = count;
             */
        HashMap<String, Double> avgPrec = new HashMap<>();
        HashMap<String, Double> avgRec = new HashMap<>();
        HashMap<String, Double> avgFmeasure = new HashMap<>();
        String total = "_total";
        ArrayList<Book> trainingBooks = new ArrayList<>();
        for (int fold = 0; fold < maxFolds; fold++) {

            for (int split = 0; split < splits.size(); split++) {
                if (split == fold)
                    //skip test set during training
                    continue;
                trainingBooks.addAll(splits.get(split));
            }
            //Model building with only one part of set
            nbm = new NaiveBayes(labelType);
            nbm.buildModel(getHashMap(trainingBooks), stopWordSet);

            HashMap<String, HashMap<String, Integer>> confusionMatrix = new HashMap<>();
            for (Book book : splits.get(fold)) {
                HashMap<String, HashMap<String, String>> classified = nbm.classifyBook(book);
                //compute confusion matrix
                book.compareResultsCumulative(classified, confusionMatrix, labelType);
            }
            //confusion matrix is complete
            for (String goldLabel : confusionMatrix.keySet()) {
                if (goldLabel.equals("NR") || goldLabel.equals(total)) continue;
                double precision = 0.0;
                double recall = 0.0;
                double fmeasure = 0.0;
                if (confusionMatrix.get(goldLabel).containsKey(goldLabel)) {
                    precision = (double) confusionMatrix.get(goldLabel).get(goldLabel) //correctly classified
                            / confusionMatrix.get(total).get(goldLabel);   //total classified with that label
                    recall = (double) confusionMatrix.get(goldLabel).get(goldLabel) //correctly classified
                            / confusionMatrix.get(goldLabel).get(total);   //total actually with that label
                    fmeasure = 2 * precision * recall / (precision + recall);
                    //System.out.println("Label\t" + goldLabel + " precision: " + precision + " recall :" +
                    // recall + " F " +
                    //        "measure: " + fmeasure);
                }
                if (!avgPrec.containsKey(goldLabel) || !avgRec.containsKey(goldLabel) || !avgFmeasure.containsKey(goldLabel)) {
                    avgPrec.put(goldLabel, 0.0);
                    avgRec.put(goldLabel, 0.0);
                    avgFmeasure.put(goldLabel, 0.0);
                }
                avgPrec.put(goldLabel, avgPrec.get(goldLabel) + precision);
                avgRec.put(goldLabel, avgRec.get(goldLabel) + recall);
                avgFmeasure.put(goldLabel, avgFmeasure.get(goldLabel) + fmeasure);


            }

            System.out.println("Completed fold #" + (fold + 1));
        }
        for (String label : avgPrec.keySet()) {
            System.out.println("Label\t" + label + " precision: " + avgPrec.get(label) / maxFolds + " recall " +
                    ":" + avgRec.get(label) / maxFolds +
                    " F-measure: " + avgFmeasure.get(label) / maxFolds);
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

    private static HashMap<String, Book> getHashMap(List<Book> books) {
        HashMap<String, Book> temp = new HashMap<>();
        for (Book book : books)
            temp.put(book.getTitle(), book);
        return temp;
    }
}

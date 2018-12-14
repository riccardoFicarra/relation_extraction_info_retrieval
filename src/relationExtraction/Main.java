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

            nFoldValidationSentences(maxFolds, books, nbm, labelType, stopWordSet);

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

    private static void nFoldValidationSentences(int maxFolds, HashMap<String, Book> books, NaiveBayes nbm, String labelType,
                                        HashSet<String> stopWordSet) {

        HashMap<String, ArrayList<BookSentence>> sentencesByLabels = new HashMap<>();


        // filling the hashmap of arraylists, each associated to a class value
        // and containing the sentences labeled with it
        for (Map.Entry<String, Book> entry : books.entrySet()) {
            if (entry.getValue().getSentences() != null) {
                entry.getValue().getSentences().stream().forEach(sentence -> {
                    String r = entry.getValue().getRelationFromSentence(sentence, NaiveBayes.RelationLabel.valueOf(labelType));

                    if (!sentencesByLabels.containsKey(r)) {
                        sentencesByLabels.put(r, new ArrayList<>());
                    }
                    sentencesByLabels.get(r).add(new BookSentence(entry.getKey(), sentence));
                });
            }
        }

        // creating the shuffled indices for randomly selecting the element of the sets of the cross validation
        ArrayList<ArrayList<Integer>> indices = new ArrayList<>();
        // and storing also the size of the set size for each class stored into indices
        ArrayList<Integer> setSizes = new ArrayList<>();
        int i = 0;
        for (ArrayList<BookSentence> list : sentencesByLabels.values()) {
            setSizes.add(list.size() / maxFolds);
            indices.add(i, new ArrayList<>());
            int count = 0;
            for (BookSentence s : list) {
                indices.get(i).add(count);
                count++;
            }
            Collections.shuffle(indices.get(i), new Random());
            i++;
        }


        // flags representing the sentences used fot test
        ArrayList<Boolean[]> flags = new ArrayList<>();
        for(int j = 0; j < sentencesByLabels.size(); j++) {
            flags.add(j, new Boolean[sentencesByLabels.get(j).size()]);
            Arrays.fill(flags.get(j), Boolean.FALSE);
        }

        int[] test_start = new int[sentencesByLabels.size()];
        int[] test_end = new int[sentencesByLabels.size()];

        // for each fold iteration
        for(int fold = 0; i < maxFolds; i++) {
            //compute indices of test sentences
            for(int j = 0; j < sentencesByLabels.size(); j++) {

                Arrays.fill(flags.get(j), Boolean.FALSE);
                test_start[j] = test_end[j];
                test_end[j] = (fold+1)*setSizes.get(j);

                //setting true the flags related to test sentences
                for(int k =test_start[j]; k < test_end[j]; k++) {
                    flags.get(j)[k] = Boolean.TRUE;
                }
            }

            //Model building with only one part of set
            nbm = new NaiveBayes(labelType);
            nbm.buildModelSentences(sentencesByLabels, indices, flags, stopWordSet);

            // classifying the test set
            HashMap<String, HashMap<String, String>> classified = nbm.classifySentences(sentencesByLabels, indices, flags);

            //creating confusion matrix
            HashMap<String, HashMap<String, Integer>> confusionMatrix = new HashMap<>();

            //compute confusion matrix
            compareResultsCumulative(classified, confusionMatrix);

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
                    book.compareResultsCumulative(classified, confusionMatrix);
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

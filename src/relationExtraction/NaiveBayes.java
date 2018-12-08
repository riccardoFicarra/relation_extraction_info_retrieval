package relationExtraction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

class NaiveBayes {

    private RelationLabel relationlabel;
    /**
     * The actual model:
     * Outer key is the label, inner key is the word, value is the probability (multinomial)
     */
    private HashMap<String, HashMap<String, Double>> probabilities;
    enum RelationLabel {affinity, coarse, fine}
    //lap: add 1 smoothing; log: use logs to avoid underflow; sparse: used dataset is sparse
    NaiveBayes(String relationlabel) {
        this.relationlabel = RelationLabel.valueOf(relationlabel);
    }

    NaiveBayes(String filepath, String relationlabel) {
        this.relationlabel = RelationLabel.valueOf(relationlabel);
        readModelFromFile(filepath);
    }

    /*
     * @param books
     * @return outer key is label, value is the list of sentences with that value
     */
   /*
    private HashMap<String, List<Sentence>> buildSentenceDataset(HashMap<String, Book> books) {

        //Dataset Building
        HashMap<String, List<Sentence>> labeledSentences = new HashMap<>();
        for (Book book : books.values()) {
            HashMap<String, List<Sentence>> bookLabeledSentences = book.buildRelationSentence(relationlabel);
            mergeLabeledSentences(labeledSentences, bookLabeledSentences);
        }
        return labeledSentences;
    }*/

    void buildModel(HashMap<String, Book> books, HashSet<String> stopWordSet) {
        HashMap<String, HashMap<String, Double>> probabilities = new HashMap<>();
        HashMap<String, Integer> count = new HashMap<>();
        for (Book book : books.values()) {
            for (Sentence sentence : book.getSentences()) {
                if (sentence.getAppearingCharacters().size() >= 2) {
                    String label = book.getRelationFromSentence(sentence, relationlabel);
                    sentence.getWordList().stream()
                            .filter(w->w.isNotPunctuation(w.getText()))
                            .filter(w->w.isNotNumber(w.getText()))
                            .filter(w->w.isNotStopword(w.getText(),stopWordSet))
                            .map(Word::getText/*additional processing here*/)
                            .forEach(w -> addToModel(probabilities, count, w, label));
                }

            }
        }
        for (String label : probabilities.keySet()) {
            HashMap<String, Double> labelEntry = probabilities.get(label);
            double dividend = count.get(label);
            for (String word : labelEntry.keySet()) {
                //ADD ONE SMOOTHING HERE
                labelEntry.put(word, (labelEntry.get(word) + 1) / (dividend + labelEntry.size()));
            }
        }
        this.probabilities = probabilities;
    }

    private static void addToModel(HashMap<String, HashMap<String, Double>> probabilities, HashMap<String, Integer> count, String w, String label) {
        if (!probabilities.containsKey(label) || !count.containsKey(label)) {
            probabilities.put(label, new HashMap<>());
            count.put(label, 0);
        }
        HashMap<String, Double> labelEntry = probabilities.get(label);
        if (labelEntry.containsKey(w)) {
            labelEntry.put(w, labelEntry.get(w) + 1);
        } else
            labelEntry.put(w, 1.0);
        count.put(label, count.get(label) + 1);
    }

    void saveModelToFile(String filepath) {
        GsonBuilder builder = new GsonBuilder();
        //builder.serializeNulls();
        Gson gson = builder.create();
        // System.out.println(gson.toJson(book));
        try (FileWriter file = new FileWriter(filepath)) {
            file.write(gson.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readModelFromFile(String filename) {
        GsonBuilder builder = new GsonBuilder();
        //builder.serializeNulls();
        Gson gson = builder.create();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            NaiveBayes fromFile = gson.fromJson(br, NaiveBayes.class);
            probabilities = fromFile.probabilities;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double calculateLabelProbability(String labelName)
    {
        HashMap<String, Double>  wordsOfLabelMap;

        //Getting list of words/probabilities for current label
        wordsOfLabelMap = this.probabilities.get(labelName);

        return 0.0;
    }

    private String calculateSentenceLabel(Sentence seentence) {
        return "positive";
    }

    /**
     * outer key: char1
     * inner key: char2
     * value: relation
     */
    HashMap<String, HashMap<String, String>> classifyBook(Book book) {
        /*
         * outer key: char1
         * inner key: char2
         * value: relations
         */
        HashMap<String, HashMap<String, LabelCounter>> counter = new HashMap<>();
        for (Sentence sentence : book.getSentences()) {
            if (sentence.getAppearingCharacters().size() < 2)
                continue;
            String label = this.calculateSentenceLabel(sentence);
            String[] character = new String[2];
            Iterator<String> itr = sentence.getAppearingCharacters().iterator();
            character[0] = itr.next();
            character[1] = itr.next();
            addToCounter(character[0], character[1], label, counter);
        }
        HashMap<String, HashMap<String, String>> classifiedChars = new HashMap<>();
        for (String char1 : counter.keySet()) {
            for (String char2 : counter.get(char1).keySet()) {
                HashMap<String, String> temp = new HashMap<>();
                temp.put(char2, counter.get(char1).get(char2).getMaxLabel());
                classifiedChars.put(char1, temp);
            }
        }
        return classifiedChars;
    }

    private void addToCounter(String c1, String c2, String label, HashMap<String, HashMap<String, LabelCounter>> counter) {
        if (counter.containsKey(c1)) {
            HashMap<String, LabelCounter> char1 = counter.get(c1);
            if (!char1.containsKey(c2)) {
                char1.put(c2, new LabelCounter());
            }
            char1.get(c2).incrementLabel(label);
        } else {
            LabelCounter lc = new LabelCounter();
            lc.incrementLabel(label);
            HashMap<String, LabelCounter> char1 = new HashMap<>();
            char1.put(c2, lc);
            counter.put(c1, char1);
        }
    }


}

class LabelCounter {
    private HashMap<String, Integer> labelcounters = new HashMap<>();

    void incrementLabel(String label) {
        if (labelcounters.containsKey(label)) {
            labelcounters.put(label, labelcounters.get(label) + 1);
        } else
            labelcounters.put(label, 1);
    }

    String getMaxLabel() {
        int max = 0;
        String maxLabel = "";
        for (String label : labelcounters.keySet()) {
            int value = labelcounters.get(label);
            if (value > max) {
                max = value;
                maxLabel = label;
            }
        }
        return maxLabel;
    }

}

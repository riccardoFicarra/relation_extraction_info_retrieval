package relationExtraction;

import java.util.HashMap;
import java.util.HashSet;

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
/*

    private static void mergeLabeledSentences(HashMap<String, List<Sentence>> labeledSentences, HashMap<String, List<Sentence>> bookLabeledSentences) {
        for (String relation : bookLabeledSentences.keySet()) {
            labeledSentences.merge(relation, bookLabeledSentences.get(relation), (l1, l2) -> {
                List<Sentence> newList = new ArrayList<>();
                newList.addAll(l1);
                newList.addAll(l2);
                return newList;
            });
        }
    }
    */


    


}

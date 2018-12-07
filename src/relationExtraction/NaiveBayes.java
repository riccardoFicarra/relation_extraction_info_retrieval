package relationExtraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    void buildModel(HashMap<String, Book> books) {
        HashMap<String, List<Sentence>> dataset = buildSentenceDataset(books);

    }

    /**
     * @param books
     * @return outer key is label, value is the list of sentences with that value
     */
    private HashMap<String, List<Sentence>> buildSentenceDataset(HashMap<String, Book> books) {

        //Dataset Building
        HashMap<String, List<Sentence>> labeledSentences = new HashMap<>();
        for (Book book : books.values()) {
            HashMap<String, List<Sentence>> bookLabeledSentences = book.buildRelationSentence(relationlabel);
            mergeLabeledSentences(labeledSentences, bookLabeledSentences);
        }
        return labeledSentences;
    }

    private HashMap<String, HashMap<String, Double>> train(HashMap<String, List<List<String>>> cleanedLabeledSentences) {
        HashMap<String, HashMap<String, Double>> probabilities = new HashMap<>();
        for (String label : cleanedLabeledSentences.keySet()) {

        }
        return probabilities;
    }


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

}

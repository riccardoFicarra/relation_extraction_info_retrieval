package relationExtraction;

import net.sf.javaml.classification.bayes.NaiveBayesClassifier;
import net.sf.javaml.core.DefaultDataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class NaiveBayes {

    private NaiveBayesClassifier nbc;
    private RelationLabel relationlabel;

    enum RelationLabel {affinity, coarse, fine}
    //lap: add 1 smoothing; log: use logs to avoid underflow; sparse: used dataset is sparse
    NaiveBayes(boolean laplace, boolean log, boolean sparse, String relationlabel) {
        this.nbc = new NaiveBayesClassifier(true, true, true);
        this.relationlabel = RelationLabel.valueOf(relationlabel);
    }

    void buildModel(HashMap<String, Book> books) {
        DefaultDataset dataset = new DefaultDataset();

        //Dataset Building
        HashMap<String, List<Sentence>> labeledSentences = new HashMap<>();
        for (Book book : books.values()) {
            HashMap<String, List<Sentence>> bookLabeledSentences = book.buildRelationSentence(relationlabel);
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

}

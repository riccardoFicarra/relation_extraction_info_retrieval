package relationExtraction;

import net.sf.javaml.classification.bayes.NaiveBayesClassifier;
import net.sf.javaml.core.DefaultDataset;

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
        HashMap<String, List<Sentence>> labeledSentences = new HashMap<>();
        for (Book book : books.values()) {
            labeledSentences.putAll((book.buildRelationSentence(relationlabel)));
        }
    }

}

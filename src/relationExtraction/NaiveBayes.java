package relationExtraction;

import net.sf.javaml.classification.bayes.NaiveBayesClassifier;
import net.sf.javaml.core.DefaultDataset;

import java.util.HashMap;


public class NaiveBayes {

    private NaiveBayesClassifier nbc;

    //lap: add 1 smoothing; log: use logs to avoid underflow; sparse: used dataset is sparse
    NaiveBayes(boolean laplace, boolean log, boolean sparse) {
        this.nbc = new NaiveBayesClassifier(true, true, true);

    }

    void buildModel(HashMap<String, Book> books) {
        DefaultDataset dataset = new DefaultDataset();

    }

}

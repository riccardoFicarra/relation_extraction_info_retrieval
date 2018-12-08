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
    private HashMap<String, Double> labelProbabilities;     //P(label) for each label
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
        HashMap<String, Double> labelProbabilities = new HashMap<>();
        HashMap<String, Integer> count = new HashMap<>();
        double totalNumOfWordOccurencies = 0;       //Word count cumulative for all labels

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

        //Counting cumulative total number of words
        for (String label : probabilities.keySet()) {
            double dividend = count.get(label);     //Count of words with label label

            totalNumOfWordOccurencies = totalNumOfWordOccurencies + dividend;
        }

        for (String label : probabilities.keySet()) {
            HashMap<String, Double> labelEntry = probabilities.get(label);
            double dividend = count.get(label);     //Count of words with label label

            for (String word : labelEntry.keySet()) {
                //ADD ONE SMOOTHING HERE
                labelEntry.put(word, (labelEntry.get(word) + 1) / (dividend + labelEntry.size()));
            }

            //Calculating P(label) for the current label
            labelProbabilities.put(label, (dividend/totalNumOfWordOccurencies));
        }
        this.probabilities = probabilities;
        this.labelProbabilities = labelProbabilities;
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


    //Given a sentence, returns its label
    String calculateSentenceLabel(Sentence sentenceToLabel)
    {
        //NOTE: summing logs instead of multiplying probs, in order to avoid underflow
        HashMap<String, Double>  labelProbability = new HashMap<String, Double>();

        //Iterating through all possible labels
        for(String label : labelProbabilities.keySet())
        {
            //Adding the label probability first
            labelProbability.put(label, Math.log10(labelProbabilities.get(label)));
            //Iterating through all words of the label
            HashMap<String, Double> wordProbs = this.probabilities.get(label);
            for(String word : wordProbs.keySet())
            {
                double tmpValue;
                if(sentenceToLabel.containsWord(word))
                {
                    //If the sentence contains this word, then we add its probability
                    tmpValue = labelProbability.get(label);
                    tmpValue = tmpValue + Math.log10(wordProbs.get(word));
                    labelProbability.put(label, tmpValue);
                }
                else
                {
                    //If the sentence does NOT contain this word, then add 1-probability
                    tmpValue = labelProbability.get(label);
                    tmpValue = tmpValue + Math.log10(1-wordProbs.get(word));
                    labelProbability.put(label, tmpValue);
                }

            }

        }


        //Choosing the label with greatest probability
        double max = 0;
        String maxLabel = "NIENTE";

        for(String label : labelProbability.keySet())
        {
            if(labelProbability.get(label) > max)
            {
                max = labelProbability.get(label);
                maxLabel = label;
            }
        }



        return maxLabel;
    }




}

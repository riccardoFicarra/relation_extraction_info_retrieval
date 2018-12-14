package relationExtraction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

class Book implements Serializable {

    private HashMap<String, HashMap<String, CharacterRelation>> characterRelations = new HashMap<>();

    private String title;
    private String author;
    private ArrayList<Sentence> sentences;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    /**
     * adds character relations between char1 and char2 to a symmetric nested hashmap
     *
     * @param char1   first character in the relation
     * @param char2   second character in the relation
     * @param changes if the relation changes over time or not
     */
    void addCharacterRelation(String char1, String char2, Boolean changes, String affinity, String coarseCategory, String fineCategory, String detail) {
        //for now not dealing with duplicates (relations changing over time)
        CharacterRelation cr = new CharacterRelation(char1, char2, changes, affinity, coarseCategory, fineCategory, detail);
        //relation graph is bidirectional, more efficient retrieval but more expensive space-wise
        addCharacterRelationToMap(char1, char2, cr);
        addCharacterRelationToMap(char2, char1, cr);
    }

    private void addCharacterRelationToMap(String char1, String char2, CharacterRelation cr) {
        if (!this.characterRelations.containsKey(char1)) {
            HashMap<String, CharacterRelation> temp = new HashMap<>();
            temp.put(char2, cr);
            characterRelations.put(char1, temp);
        } else {
            this.characterRelations.get(char1).put(char2, cr);
        }
    }

    /*public HashMap<String, HashMap<String, CharacterRelation>> getCharacterRelations() {
        return characterRelations;
    }
    */

    public HashMap<String, CharacterRelation> getCharacterRelations(String character) {
        return this.characterRelations.getOrDefault(character, null);
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public HashSet<String> getCharacters() {
        return new HashSet<>(characterRelations.keySet());
    }


    public ArrayList<Sentence> getSentences() {
        return sentences;
    }

    /**
     * @param s sentence to analyze
     * @return relation between the two characters in the sentence: for now returns the affinity.
     */
    String getRelationFromSentence(Sentence s, NaiveBayes.RelationLabel relationLabel) {
        //no check on number of characters: use the first two
        String[] character = new String[2];
        Iterator<String> itr = s.getAppearingCharacters().iterator();
        character[0] = itr.next();
        character[1] = itr.next();
        if (characterRelations.containsKey(character[0]) && characterRelations.get(character[0]).containsKey(character[1])) {
            if (relationLabel == NaiveBayes.RelationLabel.affinity)
                return characterRelations.get(character[0]).get(character[1]).getAffinity();
            else if (relationLabel == NaiveBayes.RelationLabel.coarse)
                return characterRelations.get(character[0]).get(character[1]).getCoarseCategory();
            else
                return characterRelations.get(character[0]).get(character[1]).getFineCategory();
        } else
            return null;

    }

    boolean containsCharacterRelation(String character1, String character2) {
        if (characterRelations.containsKey(character1)) {
            return characterRelations.get(character1).containsKey(character2);
        }
        return false;
    }



    void addSentences(String booksPath) {
        try {
            ArrayList<Sentence> sentences = BookAnalyzerHub.analyzeBook(booksPath + this.getTitle() + ".txt");
            recomputeCharacters(sentences);
            //only add sentences with >= 2 characters
            this.sentences = sentences.stream().filter(s -> s.getAppearingCharacters().size() >= 2).collect(Collectors.toCollection(ArrayList::new));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param sentences This function computes all possible pairs between characters recognized by the NER in the book
     *                  and checks if the character names are contained in the characters obtained by the character relations file.
     *                  If the check for a pair of characters is positive and they have a relation between themselves,
     *                  the character name from the book is substituted by the one from the character relations file.
     *                  An hashmap avoids repeating useless work for already found couples.
     */
    private void recomputeCharacters(ArrayList<Sentence> sentences) {

        for (Sentence sentence : sentences) {
            HashSet<String> newCharacters = new HashSet<>();
            ArrayList<String> characters = new ArrayList<>(sentence.getAppearingCharacters());
            for (int i = 0; i < characters.size() - 1; i++) {
                for (int j = i + 1; j < characters.size(); j++) {
                    String char1 = characters.get(i);
                    String char2 = characters.get(j);
                    String relChar1 = "";
                    String relChar2 = "";

                    //For each gold standard character, check if it can be matched with what was found in the sentence
                    String[] decomposedChar1 = char1.split("\\s+");
                    String[] decomposedChar2 = char2.split("\\s+");
                    //  First, try to match the full name...
                    for (String relationCharacter : this.getCharacters()) {
                        if (relationCharacter.indexOf(char1) != -1) {
                            relChar1 = relationCharacter;
                        }
                        if (relationCharacter.indexOf(char2) != -1) {
                            relChar2 = relationCharacter;
                        }
                        if (!relChar1.isEmpty() && !relChar2.isEmpty())
                            break;
                    }
                    //  If we could not match the names, we try to match their composing parts
                    if(relChar1.isEmpty() && decomposedChar1.length>1)
                    {
                        for(String part : decomposedChar1)
                        {
                            for (String relationCharacter : this.getCharacters())
                            {
                                if (relationCharacter.indexOf(part) != -1)
                                {
                                    relChar1 = relationCharacter;
                                    break;
                                }
                            }
                            if(!relChar1.isEmpty())
                                break;
                        }
                    }
                    if(relChar2.isEmpty() && decomposedChar2.length>1)
                    {
                        for(String part : decomposedChar2)
                        {
                            for (String relationCharacter : this.getCharacters())
                            {
                                if (relationCharacter.indexOf(part) != -1)
                                {
                                    relChar2 = relationCharacter;
                                    break;
                                }
                            }
                            if(!relChar2.isEmpty())
                                break;
                        }
                    }
                    //Add the corrected characters names to the new sentence characters map
                    if (!relChar1.equals(relChar2) && this.containsCharacterRelation(relChar1, relChar2)) {
                        newCharacters.add(relChar1);
                        newCharacters.add(relChar2);
                        System.out.println("I substituted '"+char1+"'--->'"+relChar1+"'      and      '"+char2+"'--->'"+relChar2+"'");
                    }
                }
            }
            //Overriding the characters map of the sentence with the substituted one
            sentence.setAppearingCharacters(newCharacters);
        }
    }

    /*returns the confusion matrix of the book
     * outer key: label1
     * inner key: label2
     * value: number of character pairs identified with label1 by the classifier that have label2 in the gold standard*/
    void compareResultsCumulative(HashMap<String, HashMap<String, String>> classified,
                                  HashMap<String, HashMap<String, Integer>> confusionMatrix) {

        for (String char1 : classified.keySet()) {
            HashMap<String, String> char1Entry = classified.get(char1);
            for (String char2 : char1Entry.keySet()) {
                if (this.containsCharacterRelation(char1, char2)) {
                    String goldLabel = this.getCharacterRelations(char1).get(char2).getAffinity();
                    String predictedLabel = char1Entry.get(char2);
                    addToConfusionMatrix(confusionMatrix, goldLabel, predictedLabel);
                }
            }
        }

    }

    /*returns the confusion matrix of the book
     * outer key: label1
     * inner key: label2
     * value: number of character pairs identified with label1 by the classifier that have label2 in the gold standard*/
    HashMap<String, HashMap<String, Integer>> compareResults(HashMap<String, HashMap<String, String>> classified) {
        HashMap<String, HashMap<String, Integer>> confusionMatrix = new HashMap<>();
        for (String char1 : classified.keySet()) {
            HashMap<String, String> char1Entry = classified.get(char1);
            for (String char2 : char1Entry.keySet()) {
                if (this.containsCharacterRelation(char1, char2)) {
                    String goldLabel = this.getCharacterRelations(char1).get(char2).getAffinity();
                    String predictedLabel = char1Entry.get(char2);
                    addToConfusionMatrix(confusionMatrix, goldLabel, predictedLabel);
                }
            }
        }
        return confusionMatrix;

    }

    /*
    outer key: goldLabel
    inner key: predicted label
    the outer HashMap contains an entry (another hashmap) with key "_total" that keeps track of the total number of
    pairs for each predictedLabel.
    the inner hashMaps have an entry (just a kv pair) with key "_total" that keeps track of the total number of pairs
     for each goldLabel.
     */
    private void addToConfusionMatrix(HashMap<String, HashMap<String, Integer>> confusionMatrix, String goldLabel,
                                      String predictedLabel) {
        String total = "_total";
        if (!confusionMatrix.containsKey(total))
            confusionMatrix.put(total, new HashMap<>());
        //init gold label entry if needed
        if (!confusionMatrix.containsKey(goldLabel)) {
            HashMap<String, Integer> goldLabelEntry = new HashMap<>();
            goldLabelEntry.put(total, 0);
            confusionMatrix.put(goldLabel, goldLabelEntry);
        }
        HashMap<String, Integer> goldLabelEntry = confusionMatrix.get(goldLabel);
        HashMap<String, Integer> totalEntry = confusionMatrix.get(total);
        if (!goldLabelEntry.containsKey(predictedLabel)) {
            //if predictedLabel has never been added before
            goldLabelEntry.put(predictedLabel, 0);
        }
        if (!totalEntry.containsKey(predictedLabel)) {
            //also create entry in the predictedLabel total hashmap
            totalEntry.put(predictedLabel, 0);
        }
        //update the counter
        goldLabelEntry.put(predictedLabel, goldLabelEntry.get(predictedLabel) + 1);
        //update counter for total with that goldLabel
        goldLabelEntry.put(total, goldLabelEntry.get(total) + 1);
        //update counter for total with that predictedLabel
        totalEntry.put(predictedLabel, totalEntry.get(predictedLabel) + 1);

    }


    public void setSentences(ArrayList<Sentence> sentences) {
        this.sentences = sentences;
    }
}



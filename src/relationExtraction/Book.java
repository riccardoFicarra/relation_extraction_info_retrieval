package relationExtraction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

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

    public void setSentences(ArrayList<Sentence> sentences) {
        this.sentences = sentences;
    }

    public ArrayList<Sentence> getSentences() {
        return sentences;
    }

    /*
     * @return hashmap with relations as keys and sentences as values
     */
    /*
    HashMap<String, List<Sentence>> buildRelationSentence(NaiveBayes.RelationLabel relationLabel) {
        Map<String, List<Sentence>> relationSentence;
        relationSentence = sentences.parallelStream().filter(this::containsCharacterRelation).collect(Collectors.groupingBy(s -> this.getRelationFromSentence(s, relationLabel)));
        return new HashMap<>(relationSentence);
    }
*/
    /**
     * @param s sentence to analyze
     * @return relation between the two characters in the sentence: for now returns the affinity.
     */
    String getRelationFromSentence(Sentence s, NaiveBayes.RelationLabel relationLabel) {
        Iterator<String> itr = s.getAppearingCharacters().iterator();
        String character1 = itr.next();
        String character2 = itr.next();
        if (relationLabel == NaiveBayes.RelationLabel.affinity)
            return characterRelations.get(character1).get(character2).getAffinity();
        else if (relationLabel == NaiveBayes.RelationLabel.coarse)
            return characterRelations.get(character1).get(character2).getCoarseCategory();
        else
            return characterRelations.get(character1).get(character2).getFineCategory();

    }

    boolean containsCharacterRelation(Sentence s) {
        if (s.getAppearingCharacters().size() != 2)
            return false;
        Iterator<String> itr = s.getAppearingCharacters().iterator();
        String character1 = itr.next();
        String character2 = itr.next();
        if (characterRelations.containsKey(character1)) {
            return characterRelations.get(character1).containsKey(character2);
        }
        return false;
    }
}



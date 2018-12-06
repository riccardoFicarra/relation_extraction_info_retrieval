package relationExtraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

class Book {

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

}



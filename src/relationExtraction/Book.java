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

        if (relationLabel == NaiveBayes.RelationLabel.affinity)
            return characterRelations.get(character[0]).get(character[1]).getAffinity();
        else if (relationLabel == NaiveBayes.RelationLabel.coarse)
            return characterRelations.get(character[0]).get(character[1]).getCoarseCategory();
        else
            return characterRelations.get(character[0]).get(character[1]).getFineCategory();

    }

    private boolean containsCharacterRelation(String character1, String character2) {
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
        HashMap<String, String> alreadyFound = new HashMap<>();
        for (Sentence sentence : sentences) {
            HashSet<String> newCharacters = new HashSet<>();
            ArrayList<String> characters = new ArrayList<>(sentence.getAppearingCharacters());
            for (int i = 0; i < characters.size() - 1; i++) {
                for (int j = i + 1; j < characters.size(); j++) {
                    String char1 = characters.get(i);
                    String char2 = characters.get(j);
                    String relChar1 = "";
                    String relChar2 = "";
                    if (alreadyFound.containsKey(char1) && alreadyFound.containsKey(char2))
                        continue;
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
                    if (!relChar1.equals(relChar2) && this.containsCharacterRelation(relChar1, relChar2)) {
                        alreadyFound.put(char1, relChar1);
                        alreadyFound.put(char2, relChar2);
                        newCharacters.add(relChar1);
                        newCharacters.add(relChar2);
                    }
                }
            }
            sentence.setAppearingCharacters(newCharacters);
        }
    }

}



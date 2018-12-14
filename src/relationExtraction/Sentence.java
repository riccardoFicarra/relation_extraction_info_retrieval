/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package relationExtraction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author artur
 */
public class Sentence implements Serializable {

    private String pureSentence;              //Sentence as a normal string
    private ArrayList<Word> wordList;         //Sentence as a sequence of Word objects
    private HashSet<String> appearingCharacters;          //Set containing the character names appearing in this sentence

    public String getPureSentence() {
        return pureSentence;
    }

    public ArrayList<Word> getWordList() {
        return wordList;
    }

    HashSet<String> getAppearingCharacters() {
        return appearingCharacters;
    }

    void setAppearingCharacters(HashSet<String> appearingCharacters) {
        this.appearingCharacters = appearingCharacters;
    }

    public Sentence(String pureSentence, String sentenceAsPOS, String sentenceAsNER)
    {

        this.appearingCharacters = new HashSet<>();
        //Copying original sentence
        this.pureSentence = pureSentence;
        //Saving list of words
        this.wordList = new ArrayList<>();
        String firstName = "";
        boolean toAdd = false;
        String[] wordArray = pureSentence.split("\\s+");
        String[] POSArray = sentenceAsPOS.split("\\s+");
        String[] NERArray = sentenceAsNER.split("\\s+");
        for(int i=0; i<wordArray.length; i++)
        {
            try
            {
                String[] POSelement = POSArray[i].split("_");
                String[] NERelement = NERArray[i].split("/");
                wordList.add(new Word(wordArray[i], POSelement[1], NERelement[1]));
                //Checking if we need to add a story character
                if(NERelement[1].equals("PERSON"))
                {
                    if(toAdd == false)
                    {
                        //First character name that has been found
                        firstName = NERelement[0];
                        toAdd = true;
                        appearingCharacters.add(NERelement[0]);
                    }
                    else
                    {
                        //Second consecutive character name that has been found: probably it is its surname
                        //toAdd = false;
                        if(!firstName.isEmpty())
                        {
                            appearingCharacters.add(firstName + " " +NERelement[0]);
                            appearingCharacters.remove(firstName);
                            firstName = firstName + " " + NERelement[0];
                        }
                        //appearingCharacters.add(NERelement[0]);
                    }
                    //System.out.println(NERelement[0]+" is a person");
                    //appearingCharacters.add(NERelement[0]);
                }
                else
                {
                    toAdd = false;
                }
            }
            catch(Exception e)
            {
                //In case something goes wrong, just set the POS and NER fields to empty
                System.out.println("Error in sentence Parsing!");
                wordList.add(new Word(wordArray[i], "", ""));
            }
        }
    }
}

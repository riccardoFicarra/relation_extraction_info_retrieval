/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package relationExtraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author artur
 */
public class Sentence {
    
    String myPureSentence;              //Sentence as a normal string
    ArrayList<Word> myWordList;         //Sentence as a sequence of Word objects
    Set myAppearingCharacters;          //Set containing the character names appearing in this sentence
    
    public Sentence(String pureSentence, String sentenceAsPOS, String sentenceAsNER)
    {

        myAppearingCharacters = new HashSet(); 
        //Copying original sentence
        myPureSentence = new String(pureSentence);
        //Saving list of words
        myWordList = new ArrayList<Word>();
        String[] wordArray = pureSentence.split("\\s+");
        String[] POSArray = sentenceAsPOS.split("\\s+");
        String[] NERArray = sentenceAsNER.split("\\s+");
        for(int i=0; i<wordArray.length; i++)
        {
            try
            {
                String[] POSelement = POSArray[i].split("_");
                String[] NERelement = NERArray[i].split("/");
                myWordList.add(new Word(wordArray[i],POSelement[1],NERelement[1]));
                //Checking if we need to add a story character
                if(NERelement[1].equals("PERSON"))
                {
                    //System.out.println(NERelement[0]+" is a person");
                    myAppearingCharacters.add(NERelement[0]);
                }
            }
            catch(Exception e)
            {
                //In case something goes wrong, just set the POS and NER fields to empty
                System.out.println("Errore");
                myWordList.add(new Word(wordArray[i],"Nothing","Nothing"));
            }
        }
    }
}

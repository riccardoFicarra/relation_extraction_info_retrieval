package relationExtraction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

public class OurUtils {


    //This function reads the entire stop word list file and copies each word inside an array list
    public static HashSet<String> prepareStopWordList(String path)
    {
        HashSet<String> stopWordsSet=new HashSet();

        try
        {
            BufferedReader br;
            String line;
            br = new BufferedReader(new FileReader(path));
            //Adding each word of the file to the list
            while ((line = br.readLine()) != null)
            {
                stopWordsSet.add(line);
            }
            System.out.println(stopWordsSet.size() + " elements were added to the Stop Word list");
            return stopWordsSet;
        }
        catch(Exception e)
        {
            System.out.println("Could not find Stopwords file!");
        }
        return null;
    }



}

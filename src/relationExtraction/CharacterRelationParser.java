package relationExtraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CharacterRelationParser {

    private HashMap<String, HashMap<String, String>> characterRelations = new HashMap<>();
    private String path;
    private HashSet<String> filenames;

    CharacterRelationParser(String path) {
        this.path = path;
        try {
            this.filenames = getFilenames(this.path);
        } catch (FileNotFoundException fnfe){
            System.err.println("Folder not found");
        }
    }

    HashMap<String, Book> parseCharacterRelation(){

        HashMap<String, Book> books = new HashMap<>();
        for(String filename : this.filenames){

        }
        return books;
    }

    private static HashSet<String> getFilenames(String path) throws FileNotFoundException {   //this function retrieves the list of filenames
        final File folder = new File(path);
        if (!folder.exists()) {
            throw new FileNotFoundException("Input folder does not exist");
        }
        File[] files = folder.listFiles();   //all filenames are retrieved as an array
        //the array is transformed into a stream, all directory names are eliminated and the filenames are extracted and put into a list
        if (files != null)
            return Stream.of(files).filter((File f) -> !f.isDirectory()).map(File::getName).collect(Collectors.toCollection(HashSet::new));
        else
            return null;
    }
}

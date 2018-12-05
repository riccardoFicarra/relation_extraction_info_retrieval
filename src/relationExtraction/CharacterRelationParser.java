package relationExtraction;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

class CharacterRelationParser {

    private String path;

    CharacterRelationParser(String path) {
        this.path = path;
    }

    HashMap<String, Book> parseCharacterRelations() {

        HashMap<String, Book> books = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.path));
            //skip first line: it's the description.
            br.lines().skip(1).forEach(line -> fillBooks(books, line));
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }


        return books;
    }

    private void fillBooks(HashMap<String, Book> books, String line) {
        String[] fields = line.split("\t");
        //do we need to keep info about annotators?
        String title = fields[2];
        String author = fields[3];
        Book book;
        boolean alreadyPresent = books.containsKey(title + "_" + author);
        if (!alreadyPresent) {
            book = new Book(title, author);
        } else {
            book = books.get(title + "_" + author);
        }
        Boolean changes = fields[2].equals("yes");
        //todo some fields are wrongly split
        String char1 = fields[4].replace(".", "");
        String char2 = fields[5].replace(".", "");
        String affinity = fields[6];
        String coarseCategory = fields[7];
        String fineCategory = fields[8];
        String detail = fields[9];
        book.addCharacterRelation(char1, char2, changes, affinity, coarseCategory, fineCategory, detail);
        if (!alreadyPresent)
            books.put(title + "_" + author, book);
    }

}

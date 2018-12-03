package relationExtraction;

import java.util.HashMap;

class Book {

    HashMap<String, HashMap<String, String>> characterRelations;
    String filename;
    String title;
    String author;

    public Book(String filename) {
        this.filename = filename;
    }
}



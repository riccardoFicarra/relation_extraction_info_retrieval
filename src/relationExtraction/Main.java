package relationExtraction;

import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        String path = args[0];
        CharacterRelationParser crp = new CharacterRelationParser(path);
        HashMap<String, Book> books = crp.parseCharacterRelations();
        for (String book : books.keySet()) {
            //System.out.println("Book: " + book);

        }

        // HOW TO CALL BOOK ANALYZER
        BookAnalyzerHub.analyzeBook("./TrainingBooks/Hamlet.txt");
        System.out.print("");
    }
}

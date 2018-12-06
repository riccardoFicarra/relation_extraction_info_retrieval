package relationExtraction;

import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        String path = args[0];
        CharacterRelationParser crp = new CharacterRelationParser(path);
        HashMap<String, Book> books = crp.parseCharacterRelations();
        printCharacters(books);

        }

        // HOW TO CALL BOOK ANALYZER
        BookAnalyzerHub.analyzeBook("./TrainingBooks/Hamlet.txt");
        System.out.print("");
    }

    static void printCharacters(HashMap<String, Book> books) {
        books.values().forEach(b -> {
            System.out.println(b.getTitle());
            b.getCharacters().forEach(c -> System.out.println("\t" + c));
        });
    }
}

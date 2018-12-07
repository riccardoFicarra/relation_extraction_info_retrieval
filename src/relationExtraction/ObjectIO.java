package relationExtraction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ObjectIO {

    static void writeBookToFile(String filepath, Book book) {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        Gson gson = builder.create();
        // System.out.println(gson.toJson(book));
        try (FileWriter file = new FileWriter(filepath + book.getTitle() + ".json")) {
            file.write(gson.toJson(book));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static HashMap<String, Book> readBooksFromFile(String bookpath) throws FileNotFoundException {
        HashMap<String, Book> books = new HashMap<>();
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        Gson gson = builder.create();
        final File folder = new File(bookpath);
        if (!folder.exists()) {
            throw new FileNotFoundException("Input folder does not exist");
        }
        File[] files = folder.listFiles();
        List<String> filepath = null;
        if (files != null)
            filepath = Stream.of(files).filter((File f) -> !f.isDirectory()).map(File::getPath).collect(Collectors.toList());
        if (filepath != null) {
            for (String filename : filepath) {
                try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                    Book book = gson.fromJson(br, Book.class);
                    if (book != null)
                        books.put(book.getTitle(), book);
                    else
                        System.err.println("Could not open book " + filename);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return books;
    }

}

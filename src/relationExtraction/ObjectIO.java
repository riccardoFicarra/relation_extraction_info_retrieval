package relationExtraction;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;

class ObjectIO {

    static void writeBooksToFile(String filepath, Collection<Book> books) {
        try {
            FileOutputStream fileOut = new FileOutputStream(filepath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            for (Book book : books)
                objectOut.writeObject(book);
            objectOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static HashMap<String, Book> readBooksFromFile(String filepath) {
        HashMap<String, Book> books = new HashMap<>();
        Book book;
        Object o = null;
        FileInputStream fin = null;
        ObjectInputStream ois = null;
        try {
            fin = new FileInputStream(filepath);
            ois = new ObjectInputStream(fin);
            try {
                while (true) {
                    //this cycle exits when all entries in the file are read via exception
                    book = (Book) ois.readObject();
                    books.put(book.getTitle(), book);
                }
            } catch (EOFException e) {
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return books;

    }
}

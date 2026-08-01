import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

interface Greeter {
    String greet(String name);

    default void greetLoudly(String name) {
        System.out.println(greet(name).toUpperCase());
    }
}

class FriendlyGreeter implements Greeter {
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}

interface Catalogable {
    String catalogId();

    default String shortLabel() {
        return "[" + catalogId() + "]";
    }
}

abstract class LibraryItem implements Catalogable{
    protected String title;
    protected String isbn;
    protected int year;

    public LibraryItem(String title, String isbn, int year) {
        this.title = title;
        this.isbn = isbn;
        this.year = year;
    }

    abstract double lateFeePerDay();

    @Override
    public String catalogId() {
        return isbn;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false; 
        if (!(object instanceof LibraryItem)) return false;

        LibraryItem libraryItem = (LibraryItem) object;
        return Objects.equals(isbn, libraryItem.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(isbn);
    }

    @Override
    public String toString() {
        return "Name: " + this.getClass().getSimpleName() + ", Title: " + title + ", ISBN: " + isbn;
    }
}

class Book extends LibraryItem{
    public Book(String title, String isbn, int year) {
        super(title, isbn, year);
    }

    @Override
    double lateFeePerDay() {
        return 0.25;
    }
}

class DVD extends LibraryItem{
    public DVD(String title, String isbn, int year) {
        super(title, isbn, year);
    }
    
    @Override
    double lateFeePerDay() {
        return 1.00;
    }
}

public class java_6AbstractionInterfaces {
    public static void main(String[] args) {
        FriendlyGreeter greeter = new FriendlyGreeter();

        System.out.println(greeter.greet("Someone"));
        greeter.greetLoudly("Someone");
        System.out.println();

        LibraryItem l1 = new Book("Book1", "1234", 2010);
        LibraryItem l2 = new DVD("DVD1", "1334", 2012);
        LibraryItem l3 = new DVD("DVD2", "1234", 2014);
        LibraryItem l4 = new Book("Book4", "1239", 2017);

        HashSet<LibraryItem> library = new HashSet<>();
        library.add(l1);
        library.add(l2);
        library.add(l3);
        library.add(l4);

        System.out.println("Size of Hashset LibraryItem: " + library.size());
        System.out.print("Library Items: \n");
        for (LibraryItem libraryItem : library) {
            System.out.println(" -> " + libraryItem);
        }
        
        System.out.println();
        
        HashMap<LibraryItem, Double> libraryMap = new HashMap<>();
        libraryMap.put(l1, l1.lateFeePerDay());
        libraryMap.put(l2, l2.lateFeePerDay());
        libraryMap.put(l3, l3.lateFeePerDay());
        libraryMap.put(l4, l4.lateFeePerDay());

        LibraryItem queryItem = new DVD("DVD3", "1234", 2026);

        System.out.println("Query Item: ");
        System.out.println(" -> " + queryItem);
        
        double fee = libraryMap.get(queryItem);
        System.out.println("QueryItem Fee (Lookup): " + fee);
    }
}

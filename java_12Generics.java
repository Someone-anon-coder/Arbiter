import java.util.ArrayList;
import java.util.List;

class ScoreBox<T extends Comparable<T>> {
    private T value;

    public ScoreBox(T value) {
        this.value = value;
    }

    public boolean isHigherThan(T other) {
        if (value.compareTo(other) > 0) return true;
        else return false;
    }
}
// Note: If the class were declared as plain class ScoreBox<T> instead of 
// class ScoreBox<T extends Comparable<T>>, the line calling "value.compareTo(other)" 
// would fail to compile. The compiler cannot allow it without the bound because a 
// generic type parameter T defaults to Object (or upper-bounded by Object), and the 
// Object class does not possess a compareTo method. Therefore, the compiler has no 
// guarantee that arbitrary type T will support comparison operations.

public class java_12Generics {
    public static void main(String[] args) {
        // This returns warning when compiled without the operator '<>'
        // Note: java_12Generics.java uses unchecked or unsafe operations.
        // Note: Recompile with -Xlint:unchecked for details.
        // ScoreBox<Integer> intBox = new ScoreBox(50);
        // ScoreBox<String> strBox = new ScoreBox("Banana");

        ScoreBox<Integer> intBox = new ScoreBox<>(50);
        ScoreBox<String> strBox = new ScoreBox<>("Banana");

        System.out.println("Integer (50 > 30): " + intBox.isHigherThan(30));
        System.out.println("Integer (50 > 60): " + intBox.isHigherThan(60));
        
        System.out.println("String (Banana > Apple): " + strBox.isHigherThan("Apple"));
        System.out.println("String (Banana > Cherry): " + strBox.isHigherThan("Cherry"));

        List<Integer> intList = List.of(1, 2, 3);
        List<Double> doubleList = List.of(1.5, 2.5);
        List<Number> numberList = List.of(1, 2.5, 3L);

        System.out.println();
        System.out.println(sumNumbers(intList));     // 6.0
        System.out.println(sumNumbers(doubleList));  // 4.0
        System.out.println(sumNumbers(numberList));  // 6.5

        List<Number> numberTarget = new ArrayList<>();
        List<Object> objectTarget = new ArrayList<>();
        List<Integer> intTarget = new ArrayList<>();

        fillWithZero(numberTarget, 2);
        fillWithZero(objectTarget, 3);
        fillWithZero(intTarget, 1);

        System.out.println();
        System.out.println(numberTarget);  // [0, 0]
        System.out.println(objectTarget);  // [0, 0, 0]
        System.out.println(intTarget);     // [0]
    }

    // If we used ? super Number, the method would not accept List<Integer> or List<Double> 
    // because lower-bounded wildcards only permit supertypes of Number, not subtypes.
    public static double sumNumbers(List<? extends Number> list) {
        double sum = 0.0;
        for (Number value: list) {
            sum += value.doubleValue();
        }

        return sum;
    }

    // If we used ? extends Integer, the method would not accept List<Number> or List<Object> 
    // because upper-bounded wildcards make the collection read-only (preventing additions like list.add()).
    public static void fillWithZero(List<? super Integer> list, int count) {
        for (int i = 0; i < count; i++) {
            list.add(Integer.valueOf(0));
        }
    }
}

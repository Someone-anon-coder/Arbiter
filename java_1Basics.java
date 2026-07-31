import java.util.Scanner;

public class java_1Basics {
    public static void main(String[] args) {
        int intValue = 42;
        double doubleValue = 3.14;
        char charValue = 'A';
        boolean booleanValue = true;

        System.out.println("int value: " + intValue);
        System.out.println("double value: " + doubleValue);
        System.out.println("char value: " + charValue);
        System.out.println("boolean value: " + booleanValue);

        Scanner sc = new Scanner(System.in);

        System.out.print("\nEnter Your Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Your Age: ");
        // int age = sc.nextInt();
        // sc.nextLine();
        int age = Integer.parseInt(sc.nextLine()); // Use parse[datatype] function to ensure no newline mismatch

        System.out.print("Enter Your Height (cm): ");
        double heightCm = Double.parseDouble(sc.nextLine());

        float heightM = (float) (heightCm / 100);
        int age10Years = age + 10;

        System.out.println("\nHeight in Meters: " + String.format("%.2f", heightM));
        System.out.println("Age in 10 years: " + age10Years);
        System.out.println("\n" + name + " is " + age + " years old, will be " + age10Years + " in 10 years, and is " + String.format("%.2f", heightM) + "m tall.");

        sc.close();
    }
}

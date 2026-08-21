import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class java_14StreamsOptional {
    public static void main(String[] args) {
        List<String> words = List.of("apple", "kiwi", "banana", "fig", "cherry", "date");
        
        String str = words.stream()
            .filter(word -> word.length() > 3)
            .map(String::toUpperCase)
            .collect(Collectors.joining(", "));
        
        int total = words.stream()
            .mapToInt(String::length)
            .reduce(0, Integer::sum);
        
        System.out.println(str);
        System.out.println("Total characters: " + total);
        System.out.println();

        List<Transaction> transactions = List.of(
            new Transaction("Alice", "Groceries", 45.20),
            new Transaction("Bob", "Electronics", 299.99),
            new Transaction("Alice", "Electronics", 150.00),
            new Transaction("Carol", "Groceries", 60.00),
            new Transaction("Alice", "Groceries", 12.50),
            new Transaction("Bob", "Groceries", 22.30)
        );
        
        Map<String, List<Transaction>> customerTransactions = transactions.stream()
            .collect(Collectors.groupingBy(Transaction::customer));

        System.out.println("--- Customer Totals ---");
        System.out.printf("Alice = %.2f%n", totalFor(customerTransactions, "Alice"));
        System.out.printf("Bob = %.2f%n", totalFor(customerTransactions, "Bob"));
        System.out.printf("Dave = %.2f%n", totalFor(customerTransactions, "Dave"));

        Map<String, Double> categoryTotals = transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::category,
                Collectors.summingDouble(Transaction::amount)
            ));

        System.out.println("\n--- Category Totals ---");
        categoryTotals.forEach((category, totalCount) -> 
            System.out.printf("%s = %.2f%n", category, totalCount)
        );
    }
    
    record Transaction(String customer, String category, double amount) {}

    public static double totalFor(Map<String, List<Transaction>> byCustomer, String customer) {
        return Optional.ofNullable(byCustomer.get(customer))
            .map(list -> list.stream()
            .mapToDouble(Transaction::amount)
            .sum()
        ).orElseGet(() -> {
            System.out.println("No transactions found for " + customer);
            return 0.0;
        });
    }
}
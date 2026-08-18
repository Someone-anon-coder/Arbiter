import java.util.function.Function;
import java.util.function.Predicate;

class Employee {
    private String name;
    private double salary;

    public Employee(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public boolean isNameNonBlank() {
        return name != null && !name.trim().isEmpty();
    }

    // Static method used for method reference
    public static boolean isSalaryAboveThreshold(Employee employee) {
        return employee.getSalary() > 60000.0;
    }
}

class FormatException extends Exception {
    public FormatException(String message) {
        super(message);
    }
}

@FunctionalInterface
interface EmployeeFormatter {
    String format(Employee employee) throws FormatException;

    default String formatOrDefault(Employee employee, String fallback) {
        try {
            return format(employee);
        } catch (FormatException e) {
            return fallback;
        }
    }
}

public class java_13Lambdas {
    public static void main(String[] args) {
        String[] usernames = new String[] {
            "aayush99", "bob",
            "x", "claude_code",
            "jd", "ninacodes"
        };

        Predicate<String> correctUsername = str -> str.length() >= 5;
        for (int i = 0; i < usernames.length; i++) {
            if (correctUsername.test(usernames[i])) System.out.println(usernames[i]);
        }
        System.out.println();

        Employee[] employees = new Employee[] {
            new Employee("Alice Kim", 72000.0),
            new Employee("bob jones", 45000.0),
            new Employee("CHARLIE NG", 138000.0),
            new Employee("dana park", 61000.0),
            new Employee("Evan Osei", 29500.0),
            // new Employee(null, 61000.0), // Uncommenting this record will trigger exception
        };

        // Arbitrary-object-instance method reference form (Class::instanceMethod)
        Predicate<Employee> nameCheck = Employee::isNameNonBlank; // Used here
        Predicate<Employee> salaryCheck = Employee::isSalaryAboveThreshold; // Static method reference form
        
        // Composed Predicate for Bonus Eligibility
        Predicate<Employee> bonusEligible = salaryCheck.and(nameCheck);

        EmployeeFormatter reportLineFormatter = emp -> {
            if (emp.getName() == null) throw new FormatException("Employee name cannot be null");

            // Arbitrary-object-instance method reference forms used here
            Function<String, String> normalize = String::trim;
            Function<String, String> uppercase = normalize.andThen(String::toUpperCase);
            
            String formattedName = uppercase.apply(emp.getName());
            return String.format("%s - %.1f", formattedName, emp.getSalary());
        };

        System.out.println("=== Bonus Eligible ===");
        for (int i = 0; i < employees.length; i++) {
            Employee employee = employees[i];
            if (bonusEligible.test(employee)) System.out.println(reportLineFormatter.formatOrDefault(employee, "INVALID_RECORD"));
        }
        System.out.println();

        System.out.println("=== All Records ===");
        for (int i = 0; i < employees.length; i++) {
            System.out.println(reportLineFormatter.formatOrDefault(employees[i], "INVALID_RECORD"));
        }
    }
}

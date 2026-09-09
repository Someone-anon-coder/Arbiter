import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.nio.file.Path;
import java.nio.file.Files;

public class java_17FileIoNio {
    record Employee(String name, String department, double salary) {}

    public static void main(String[] args) {
        String fileContent = """
Hello, this is line one.
Line two here.
Line three.
Almost done.
Final line.""";

        String filePath = "Files/greeting.txt";
        int writeCount = 0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line: fileContent.split("\n")) {
                writer.write(line);
                writer.newLine();
                writeCount++;
            }
        } catch (IOException e) {
            System.err.println("Exception Occured: " + e);
        }

        int readCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                readCount++;
                System.out.println(readCount + ": " + line);
            }
        } catch (IOException e) {
            System.err.println("Exception Occured: " + e);
        }

        if (readCount == writeCount) {
            System.out.println("Read " + readCount + " lines, matches " + writeCount + " written.");
        } else {
            System.out.println("Read Count " + readCount + " do not match Write Count " + writeCount);
        }
        System.out.println();

        Employee employee1 = new Employee("Alice Johnson", "Engineering", 95000.0);
        Employee employee2 = new Employee("Bob Smith, Jr.", "Sales", 62000.0);
        Employee employee3 = new Employee("Carol \"CJ\" White", "Marketing", 71000.0);
        Employee employee4 = new Employee("Dave Chen", "Engineering", 88000.0);
        Employee employee5 = new Employee("Eve, Product", "Ops", 99500.0);
        
        Employee[] employees = new Employee[] {employee1, employee2, employee3, employee4, employee5};

        Path dir = Path.of("Files", "csv_reports");
        try {
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path file = dir.resolve("employees.csv");
            StringBuilder content = new StringBuilder();
            
            content.append("name,department,salary\n");
            for (Employee employee: employees) {
                content
                    .append(csvField(employee.name())).append(",")
                    .append(csvField(employee.department())).append(",")
                    .append(employee.salary()).append("\n");
            }
            Files.writeString(file, content.toString());
            List<String> lines = Files.readAllLines(file);
            
            List<List<String>> parsedRecords = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                parsedRecords.add(parseCsvLine(lines.get(i)));
            }

            List<Employee> parsedEmployees = new ArrayList<>();
            for (List<String> record: parsedRecords) {
                if (record.size() != 3) throw new IllegalStateException("Invalid CSV record: " + record);

                String name = record.get(0);
                String department = record.get(1);
                double salary = Double.parseDouble(record.get(2));

                parsedEmployees.add(new Employee(name, department, salary));
            }

            System.out.println("Total Employee Count: " + parsedEmployees.size());
            Map<String, List<Employee>> grouped = parsedEmployees.stream()
                .collect(Collectors.groupingBy(Employee::department));
            
            System.out.println("\nSalary by department: ");
            for (Map.Entry<String, List<Employee>> entry: grouped.entrySet()) {
                String department = entry.getKey();
                List<Employee> departmentEmployees = entry.getValue();

                double totalSalary = departmentEmployees.stream()
                    .mapToDouble(Employee::salary)
                    .sum();
                
                double averageSalary = totalSalary / departmentEmployees.size();
                System.out.printf("%s -> Total: %.2f, Average: %.2f%n", department, totalSalary, averageSalary);
            }

            System.out.println("\nEmployee whose name contains a comma:");
            parsedEmployees.stream()
                .filter(employee -> employee.name().contains(","))
                .map(Employee::name)
                .forEach(System.out::println);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private static String csvField(String value) {
        if (
            value.contains(",") || value.contains("\"") || 
            value.contains("\n") || value.contains("\r")
        ) return "\"" + value.replace("\"", "\"\"") + "\"";

        return value;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();

        boolean insideQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '"') {
                if (insideQuotes && i+1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else insideQuotes = !insideQuotes;
            } else if (current == ',' && !insideQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else field.append(current);
        }

        if (insideQuotes) throw new IllegalArgumentException("Malformed CSV line: unclosed quote");
        fields.add(field.toString());

        return fields;
    }
}
package com.arbiter.learning.json.p2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Problem2 {

    // (a) Data Binding Records
    public record Item(String sku, int quantity, BigDecimal unitPrice) {}
    public record Customer(String id, String name, String email) {}
    public record Order(String id, LocalDate orderDate, Customer customer, List<Item> items, String note) {}

    // Custom Exception for strict boundary wrapped parsing
    public static class OrderParseException extends Exception {
        public OrderParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // (d) Summary Record for Writing
    public record CustomerSummary(
            String id,
            @JsonProperty("customer_name") String name,
            int orderCount,
            BigDecimal total,
            LocalDate latestDate,
            @JsonInclude(JsonInclude.Include.NON_NULL) String email,
            @JsonIgnore int internalRank
    ) {}

    // Configured Mappers (Static, Final, Thread-safe after config)
    private static final ObjectMapper strictMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static final ObjectMapper lenientMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static void main(String[] args) throws Exception {
        Path ordersPath = Path.of("../../Files/json_orders/orders.json");

        // --- (b) Strict Failure (Raw) ---
        System.out.println("--- 1. Strict Failure (Raw) ---");
        try (var reader = Files.newBufferedReader(ordersPath)) {
            strictMapper.readValue(reader, new TypeReference<List<Order>>() {});
        } catch (Exception e) {
            System.out.println(e.getClass().getName());
            System.out.println(e.getMessage());
        }

        // --- (b) Wrapped Checked Exception Boundary ---
        System.out.println("\n--- 2. Wrapped Checked Exception (Caller Handling) ---");
        try {
            // Using the strict mapper under the hood, which will fail and trigger our wrapper
            parseOrdersStrict(ordersPath);
        } catch (OrderParseException e) {
            System.out.println("Caught custom boundary exception: " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage());
            System.out.println("Cause: " + e.getCause().getClass().getName()); // Caller sees Jackson exception as cause
        }

        // --- (b) Lenient Parsing (Successful fallback) ---
        System.out.println("\n--- 3. Lenient Parsing (Successful) ---");
        List<Order> parsedOrders;
        try (var reader = Files.newBufferedReader(ordersPath)) {
            parsedOrders = lenientMapper.readValue(reader, new TypeReference<List<Order>>() {});
            System.out.println("Successfully parsed " + parsedOrders.size() + " orders.");
        }

        // --- (c) Report ---
        System.out.println("\n--- 4. Report ---");
        generateReportAndExport(parsedOrders);

        // --- (e) Two Different Failures ---
        System.out.println("\n--- 5. Two Different Failures ---");
        System.out.println("Failure 1: Malformed JSON");
        try {
            lenientMapper.readValue("{\"id\": \"ORD-9001\", \"quantity\": 3 ", Item.class);
        } catch (Exception e) {
            printHierarchy(e);
        }

        System.out.println("\nFailure 2: Unfit Data");
        try {
            lenientMapper.readValue("{\"sku\": \"A-100\", \"quantity\": \"three\", \"unitPrice\": 19.99}", Item.class);
        } catch (Exception e) {
            printHierarchy(e);
        }
    }

    // A parsing method that bounds Jackson logic and exposes only our domain checked exception
    private static List<Order> parseOrdersStrict(Path path) throws OrderParseException {
        try (var reader = Files.newBufferedReader(path)) {
            return strictMapper.readValue(reader, new TypeReference<List<Order>>() {});
        } catch (IOException e) { // Catches JsonProcessingException (databind/parse) and standard IOException
            throw new OrderParseException("Failed to strictly parse order data from " + path, e);
        }
    }

    private static class CustomerStats {
        String name;
        BigDecimal total = BigDecimal.ZERO;
        LocalDate latest = LocalDate.MIN;
        String email = null;
        int orderCount = 0;
    }

    private static void generateReportAndExport(List<Order> orders) throws IOException {
        Map<String, CustomerStats> stats = new LinkedHashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;
        int totalUnits = 0;
        LocalDate earliestDate = LocalDate.MAX;
        LocalDate latestDateAll = LocalDate.MIN;

        for (Order o : orders) {
            CustomerStats cs = stats.computeIfAbsent(o.customer().id(), k -> {
                CustomerStats newCs = new CustomerStats();
                newCs.name = o.customer().name();
                return newCs;
            });

            cs.orderCount++;
            if (cs.email == null && o.customer().email() != null) {
                cs.email = o.customer().email();
            }
            if (o.orderDate().isAfter(cs.latest)) {
                cs.latest = o.orderDate();
            }

            if (o.orderDate().isBefore(earliestDate)) earliestDate = o.orderDate();
            if (o.orderDate().isAfter(latestDateAll)) latestDateAll = o.orderDate();

            for (Item item : o.items()) {
                BigDecimal lineTotal = item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
                cs.total = cs.total.add(lineTotal);
                grandTotal = grandTotal.add(lineTotal);
                totalUnits += item.quantity();
            }
        }

        System.out.printf("%-15s %-15s %-15s %-15s%n", "Customer", "Orders", "Total", "Latest");
        List<CustomerSummary> summaries = new ArrayList<>();
        int rank = 1;
        
        for (var entry : stats.entrySet()) {
            CustomerStats cs = entry.getValue();
            System.out.printf("%-15s %-15s %-15s %-15s%n",
                    entry.getKey() + " " + cs.name,
                    cs.orderCount,
                    cs.total.setScale(2, RoundingMode.HALF_UP),
                    cs.latest);
            
            summaries.add(new CustomerSummary(
                    entry.getKey(), cs.name, cs.orderCount, 
                    cs.total.setScale(2, RoundingMode.HALF_UP), cs.latest, cs.email, rank++));
        }

        System.out.println("Grand total: " + grandTotal.setScale(2, RoundingMode.HALF_UP));
        System.out.println("Total units across all line items: " + totalUnits);
        System.out.println("Span between the earliest and latest order date in days: " + ChronoUnit.DAYS.between(earliestDate, latestDateAll));

        // (d) Write and Read Tree Model
        Path summaryPath = Path.of("../../Files/json_orders/customer_summary.json");
        Files.createDirectories(summaryPath.getParent());
        
        try (var out = Files.newOutputStream(summaryPath)) {
            lenientMapper.writeValue(out, summaries);
        }
        System.out.println("\n--- Wrote customer_summary.json ---");

        try (var in = Files.newInputStream(summaryPath)) {
            JsonNode root = lenientMapper.readTree(in);
            System.out.println("Using readTree to extract third customer's latest order date: " + 
                    root.get(2).get("latestDate").asText());
        }
    }

    private static void printHierarchy(Throwable t) {
        System.out.println(t.getClass().getName() + ": " + t.getMessage());
        Class<?> clazz = t.getClass().getSuperclass();
        while (clazz != null && clazz != Object.class) {
            System.out.println("  -> " + clazz.getName());
            clazz = clazz.getSuperclass();
        }
        System.out.println("  -> java.lang.Object");
    }
}
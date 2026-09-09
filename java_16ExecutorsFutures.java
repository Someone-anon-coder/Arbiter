import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.*;

public class java_16ExecutorsFutures {
    private static final Map<String, Double> BASE_PRICES = new HashMap<>();
    static {
        BASE_PRICES.put("ORDER-1", 100.0);
        BASE_PRICES.put("ORDER-2", 250.0);
        BASE_PRICES.put("ORDER-3", 75.0);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Integer>> futures = new ArrayList<>();

        for(int i = 1; i <= 5; i++) {
            final int taskNumber = i;
            Future<Integer> future = executor.submit(() -> {
                Thread.sleep(100);
                return taskNumber * taskNumber;
            });

            futures.add(future);
        }
        executor.shutdown();

        List<Integer> results = new ArrayList<>();
        for(Future<Integer> future: futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Main thread was interrupted");
                executor.shutdownNow();
                return;
            } catch (ExecutionException e) {
                System.err.println("Task Failed: " + e.getCause());
                executor.shutdownNow();
                return;
            }
        }
        
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("Executor did not terminate in time.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("Shutdown unit was interrupter");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        int sum = 0;
        
        System.out.println("Results: ");
        for (int i = 0; i < results.size(); i++) {
            int result = results.get(i);
            System.out.println("  Task " + (i+1) + " result: " + result);
            sum += result;
        }
        System.out.println("\nSum of all results: " + sum);
        System.out.println();

        String[] orderIds = {"ORDER-1", "ORDER-2", "ORDER-3"};
        for (String orderId: orderIds) {
            CompletableFuture<Double> basePriceFuture = CompletableFuture.supplyAsync(() -> {
                Double price = BASE_PRICES.get(orderId);

                if (price == null) throw new RuntimeException("Unknown Order ID: " + orderId);
                return price;
            });

            CompletableFuture<Double> discountedPriceFuture = basePriceFuture.thenCompose(basePrice -> applyDiscount(orderId, basePrice));
            CompletableFuture<Double> shippingFeeFuture = getShippingFee(orderId);
            
            CompletableFuture<Double> totalFuture = discountedPriceFuture.thenCombine(
                shippingFeeFuture, (discountedPrice, shippingFee) -> discountedPrice + shippingFee
            );

            CompletableFuture<Double> finalFuture = totalFuture.handle((result, exception) -> {
                if (exception != null) {
                    if ("ORDER-3".equals(orderId)) return 50.0;
                    throw new RuntimeException("Unexpected Failure for: " + orderId, exception);
                }
                
                return result;
            });

            double finalTotal = finalFuture.get();
            System.out.println(orderId + " final total: " + finalTotal);
        }
    }

    public static CompletableFuture<Double> applyDiscount(String orderId, double basePrice) {
        return CompletableFuture.supplyAsync(() -> {
            double discountRate;

            switch (orderId) {
                case "ORDER-1" -> discountRate = 0.10;
                case "ORDER-2" -> discountRate = 0.20;
                case "ORDER-3" -> discountRate = 0.15;
                default -> discountRate = 0.0;
            }

            return basePrice * (1 - discountRate);
        });
    }

    public static CompletableFuture<Double> getShippingFee(String orderId) {
        return CompletableFuture.supplyAsync(() -> {
            if ("ORDER-3".equals(orderId)) throw new RuntimeException("Shipping service unavailable for ORDER-3");

            switch (orderId) {
                case "ORDER-1": return 10.0;
                case "ORDER-2": return 15.0;
                default: return 0.0;
            }
        });
    }
}

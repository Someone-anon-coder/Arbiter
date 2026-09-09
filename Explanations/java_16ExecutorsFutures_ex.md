# java_16ExecutorsFutures — ExecutorService, Future, CompletableFuture

## `shutdown()` + `awaitTermination` after every `Future.get()` is already collected
```java
executor.shutdown();
// ... loop calling future.get() on every submitted Future ...
executor.awaitTermination(5, TimeUnit.SECONDS);
```
`Future.get()` blocks until that specific task finishes. Once the `get()` loop has run over *every* submitted future, the pool has, by construction, already finished all its work by the time `awaitTermination` is reached — so the bounded wait returns `true` almost instantly rather than doing any real waiting. It's still correct (bounded, not unbounded, with a `shutdownNow()` fallback), just not exercising the mechanism it's normally there for. `awaitTermination` earns its keep when you *don't* individually block on every future — e.g. tasks submitted with `execute()` for their side effects, where there's no `Future` to `get()` on and it's the only way to know the pool has actually drained.

## `thenApply` vs `thenCompose`, concretely
```java
CompletableFuture<Double> discountedPriceFuture =
    basePriceFuture.thenCompose(basePrice -> applyDiscount(orderId, basePrice));
```
`applyDiscount` returns `CompletableFuture<Double>`, not `Double`. `thenApply`'s function type is `Function<T, R>` — it has no idea `R` might itself be a future, so it would have wrapped the *returned future* as the stage's result, producing `CompletableFuture<CompletableFuture<Double>>`. `thenCompose`'s function type is `Function<T, CompletableFuture<R>>`, so it flattens the nesting automatically. The rule isn't about which method "sounds right" — it's mechanical: check what the function you're passing returns. A plain value → `thenApply`. A `CompletableFuture` → `thenCompose`.

## `handle()` running on both branches in one place
```java
totalFuture.handle((result, exception) -> {
    if (exception != null) {
        if ("ORDER-3".equals(orderId)) return 50.0;
        throw new RuntimeException("Unexpected Failure for: " + orderId, exception);
    }
    return result;
});
```
`handle()` always runs, with exactly one of `result`/`exception` non-null depending on outcome. That's what let one callback both pass successful totals through untouched *and* substitute `50.0` for `ORDER-3`'s failure. `exceptionally()` can only see the failure branch — it has no hook to also touch or reason about the success case in the same callback, so it couldn't have expressed "recover on failure, pass through unchanged on success" as a single unconditional rule the way `handle()` did here.

## Two independent futures merged, not chained
```java
CompletableFuture<Double> shippingFeeFuture = getShippingFee(orderId); // own supplyAsync
CompletableFuture<Double> totalFuture = discountedPriceFuture.thenCombine(
    shippingFeeFuture, (discountedPrice, shippingFee) -> discountedPrice + shippingFee
);
```
`shippingFeeFuture` doesn't depend on `discountedPriceFuture`'s result — both start from `supplyAsync` independently and can run concurrently. `thenCombine` is specifically for this case: two unrelated pipelines whose results need merging once both finish, as opposed to `thenCompose`/`thenApply`, which model one stage depending on the *previous* stage's output.

## Key takeaway
The nested-future trap with `thenApply`/`thenCompose` isn't a naming quirk — it falls directly out of each method's function signature (`Function<T,R>` vs `Function<T,CompletableFuture<R>>`), and checking what your chained function actually returns settles which one is correct every time. Similarly, `handle()` vs `exceptionally()` isn't just "the fancier one" — `handle()` is the only one of the two that can see and shape the success branch, which is exactly what a pipeline requiring uniform recovery across success/failure needs.

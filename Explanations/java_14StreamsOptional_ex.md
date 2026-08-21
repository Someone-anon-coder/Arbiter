# java_14StreamsOptional — Streams API, laziness, Optional

## A stream pipeline is a description, not an action, until a terminal operation runs it
```java
String str = words.stream()
    .filter(word -> word.length() > 3)
    .map(String::toUpperCase)
    .collect(Collectors.joining(", "));
```
`filter` and `map` are intermediate — each just records a step and hands back a new (still-unexecuted) stream. Nothing touches `words` until `.collect(...)`, the terminal operation, actually pulls elements through the whole chain. Had this pipeline ended at `.map(String::toUpperCase)` with no `.collect(...)`, it would have compiled fine as an unused `Stream<String>` expression and done precisely nothing — not even called `toUpperCase` once. `str`'s value here (`"APPLE, KIWI, BANANA, CHERRY, DATE"`) only exists because `collect` was there to demand it.

## `reduce` folds a stream down to one value using a combining function
```java
int total = words.stream()
    .mapToInt(String::length)
    .reduce(0, Integer::sum);
```
`mapToInt(String::length)` converts to an `IntStream` of each word's length; `reduce(0, Integer::sum)` starts an accumulator at `0` and folds every length into it via `Integer::sum`. This ran over the full, unfiltered `words` list (28 total characters), independent of the filtered/uppercased pipeline above it — the two pipelines don't share state, they're two separate walks over the same source data.

## `Optional.ofNullable(...).map(...).orElseGet(...)` handles absence without ever calling `isPresent()`
```java
return Optional.ofNullable(byCustomer.get(customer))
    .map(list -> list.stream().mapToDouble(Transaction::amount).sum())
    .orElseGet(() -> {
        System.out.println("No transactions found for " + customer);
        return 0.0;
    });
```
`byCustomer.get(customer)` returns `null` for `"Dave"`, since he never appears in the source data — `Optional.ofNullable` wraps that possibly-null result instead of branching on it. `.map(...)` only runs its lambda if a list is actually present, transforming "list of transactions" into "summed amount" while staying inside the `Optional`. `.orElseGet(...)` only invokes its `Supplier` — printing the "No transactions found" line and returning `0.0` — when the chain is genuinely empty. The proof this was really lazy rather than just correct-looking: the message printed exactly once, only for Dave, meaning the `Supplier` never ran for Alice or Bob. Had `orElse(...)` been used with an eagerly-evaluated argument instead, and had that argument itself printed as a side effect, it would have printed for every customer regardless of presence — that's the concrete difference the assignment was testing.

## `groupingBy` with a downstream collector aggregates within each group, not just partitions it
```java
Map<String, Double> categoryTotals = transactions.stream()
    .collect(Collectors.groupingBy(
        Transaction::category,
        Collectors.summingDouble(Transaction::amount)
    ));
```
The single-argument form of `groupingBy` (used earlier for `customerTransactions`) produces `Map<K, List<T>>` — group membership only. Passing a second, *downstream* collector (`Collectors.summingDouble`) changes what each bucket holds: instead of collecting the matching elements into a list, it reduces them in place to a `double` as they're grouped, producing `Map<String, Double>` directly (Groceries = 140.00, Electronics = 449.99) with no separate summing pass over the grouped lists afterward.

## Key takeaway
The two Optional exit methods used here (`map` to transform-if-present, `orElseGet` to lazily supply a fallback only when needed) are the difference between treating `Optional` as a genuine "value or deferred fallback" combinator versus using it as a null-check with a checkbox (`isPresent()` + `get()`). The single-print-for-Dave-only output is what makes the laziness claim verified rather than asserted.

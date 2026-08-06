# java_8MapsSets — Map/Set implementations, choosing the right collection

## `getOrDefault` as the counting idiom
```java
wordHashMap.put(word, wordHashMap.getOrDefault(word, 0) + 1);
```
`getOrDefault(key, 0)` returns the existing count if the key is already present, or `0` if this is the word's first appearance — either way `+ 1` is correct, with no `containsKey` branch needed. This is the standard shape for frequency counting in Java and generalizes to any "accumulate per key" problem.

## `LinkedHashMap` for insertion order, reused twice
```java
LinkedHashMap<String, Integer> eventsLinkedHashMap = new LinkedHashMap<>();
```
Choosing `LinkedHashMap` here (instead of `HashMap`) is what makes both the occurrence-count report *and* the first-seen-order report legitimately correct rather than correct-by-luck: its iteration order is guaranteed to be insertion order, so the very first time each event name is `put` fixes its position permanently, regardless of how many times it's updated afterward. A `HashMap` could have happened to iterate in first-seen order for this particular data by coincidence — it wouldn't be reliable, and wouldn't be a deliberate choice.

## `TreeMap` with a `Comparator` for a non-natural sort key
```java
Map<String, Integer> sortedMap = new TreeMap<>((a, b) -> {
    int severityCompare = Integer.compare(severityLookup.get(a), severityLookup.get(b));
    if (severityCompare != 0) return severityCompare;
    else return a.compareTo(b);
});
```
The map's keys are `String`s, and `String` already has a natural ordering (alphabetical) — but the requirement was to sort by *severity*, an entirely different value looked up from elsewhere. That's precisely the case where `Comparable` (which only gives you one fixed natural ordering per type) isn't enough, and a `Comparator` lambda is required. The tie-break (`a.compareTo(b)` when severities are equal) is what makes `retry` and `timeout` — both severity 5 — resolve deterministically instead of by whatever order a `TreeMap` happened to insert them in.

## `putIfAbsent` to collapse duplicates before comparing
```java
severityLookup.putIfAbsent(eventArray[i], severityArray[i]);
```
Since severity is constant per event name in this dataset, `putIfAbsent` guarantees only the *first* severity seen per name is kept — later duplicate `put`s for the same name are no-ops. This produces exactly the "one severity per distinct name" map the sorting comparator needs, without a separate deduplication pass.

## Key takeaway
Three different Map implementations were used for three different guarantees in the same run: `HashMap` where no order was required (word counts), `LinkedHashMap` where insertion order had to be preserved (first-seen report), and `TreeMap` with a custom `Comparator` where a sorted-by-a-different-field order was required (severity report). Using `HashMap` for either of the latter two would not have failed loudly — it would have silently produced a differently-ordered, wrong report.

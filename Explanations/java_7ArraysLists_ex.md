# java_7ArraysLists — Arrays, ArrayList/LinkedList, Iterator

## 2D array traversal without a shared "row length" assumption
The ring solution loops `for (int j = idx; j < grid[i].length - idx; j++)` — using `grid[i].length`, not a single cached `n`, for the inner bound. For this square grid it doesn't matter, but the habit matters: a 2D array in Java is an array of independently-allocated row arrays, so a truly general traversal always re-reads each row's own length rather than assuming rectangularity.

## Ring extraction by boundary condition, not by construction
Rather than building each ring as a separate structure, the solution scans the whole current sub-square (`idx` to `length - idx`) and only accumulates a cell when it sits on that sub-square's boundary (`i == idx`, `i == length - idx - 1`, or the equivalent column check). This is why the middle ring's inner cells (e.g., row 2 col 2 = `13`) get skipped correctly — they satisfy neither the row-boundary nor column-boundary condition at that ring's `idx`.

## `Iterator.remove()` — removing without invalidating the walk
```java
Iterator<Integer> it = arrayList.iterator();
while (it.hasNext()) {
    Integer element = it.next();
    if (element < 0 || element % 7 == 0) {
        it.remove();
    }
}
```
Calling `arrayList.remove(element)` directly inside a for-each loop over the same list would change the list's `modCount` out from under the iterator the for-each is secretly using, and the next `next()` call would throw `ConcurrentModificationException`. Calling `it.remove()` instead removes *through* the iterator, so the iterator's cursor and the list's modification count stay in agreement — no exception, and critically, no silently skipped element (the bug a naive manual-index-shift removal can produce without throwing anything at all).

## `addFirst`/`addLast` — the operations LinkedList is actually good at
```java
if (element % 2 == 0) linkedList.addFirst(element);
else linkedList.addLast(element);
```
Both are O(1) regardless of list size — each is just relinking a node's `prev`/`next` pointers at whichever end is being touched. This is the genuine LinkedList strength: not "faster in general," but specifically O(1) insertion at either end, which an `ArrayList` can only match at its *back* (inserting at the front of an `ArrayList` is O(n) because everything else has to shift over).

## Key takeaway
The two structures were chosen for what they're actually built to do: `ArrayList` for the filter pass (random-access-friendly, and the only structure the iterator-based removal even needed), `LinkedList` for the front/back-loading pass (playing directly to its O(1)-at-either-end strength, not used arbitrarily).

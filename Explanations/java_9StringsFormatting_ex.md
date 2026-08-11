# java_9StringsFormatting — String immutability, StringBuilder, formatting

## Index math when mutating a `StringBuilder` in place
```java
if (Character.isLowerCase(ch)) {
    value.insert(i + 1, '*');
    insertedMarkers++;
}
```
Inserting at `i + 1` and letting the loop's normal `i++` run means the next iteration lands on the freshly inserted `*` — it gets visited and no-op'd (not lowercase, not a digit), then the iteration after that resumes on the character that was originally next. No explicit skip was needed because the marker itself doesn't match either branch condition. This only works because the marker character was chosen to be inert with respect to the two rules; a marker that happened to be a digit or lowercase letter would have needed an explicit skip.

## The `deleteCharAt` + `i--` pattern for consecutive matches
```java
if (i > 0 && Character.isDigit(value.charAt(i - 1))) {
    value.deleteCharAt(i);
    deletedDigits++;
    i--;
}
```
Deleting index `i` shifts everything after it one position left, so the character that used to be at `i + 1` is now at `i`. Without the `i--`, the loop's `i++` would skip straight over it. The `i--` cancels that skip, so the next iteration re-examines the same index — which is exactly right for a run of 3+ digits, since each deletion needs to re-check whether the *new* character at `i` is also a digit preceded by a digit.

## Why the digit-run check still works after marker insertions
The adjacency check (`value.charAt(i - 1)` is a digit) reads the *current buffer*, not the original string. That's safe here because markers are only ever inserted immediately after lowercase letters — never between two digits of the same run — so no marker ever lands between a digit and its run-neighbor. If the rules had allowed a marker to be inserted inside a digit run, the adjacency check would have needed to look further back than one position.

## `String.format` vs `println` both adding line breaks
```java
String summary = String.format("...%n... \n", ...); // or similar
System.out.println(summary);
```
`String.format`/`printf`-style `\n` (or `%n`) already terminates the line; `println` appends another terminator on top of that. Harmless here, but worth noticing — a format string built for `printf` shouldn't usually also go through `println`, and vice versa.

## Key takeaway
The hard problem's constraint — one `StringBuilder`, mutated by `insert()`/`delete()` in a single pass, not rebuilt via concatenation — forces you to reason about index shifting explicitly rather than let a fresh buffer/string absorb the bookkeeping for you. Getting the offsets right after both insertions (skip forward implicitly) and deletions (step back explicitly) is the actual skill `StringBuilder` exists to support; a second buffer or string concatenation would have sidestepped that reasoning entirely.

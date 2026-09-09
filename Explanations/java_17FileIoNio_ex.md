# java_17FileIoNio — Classic I/O, NIO (Path/Files), CSV quoting

## Classic I/O: two separate try-with-resources blocks, not one
```java
try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) { ... }
...
try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) { ... }
```
Writing and reading were done as two independent try-with-resources blocks against the same path, rather than keeping the written lines around in memory and reusing them for the "read" step. That's the actual round-trip proof the problem asked for — the read pulls real bytes back off disk, so a bug in the write path (wrong line separator, file not flushed before read) would have shown up as a mismatch, not been silently hidden by comparing an in-memory list to itself.

## NIO used for existence/creation, not classic I/O
```java
Path dir = Path.of("Files", "csv_reports");
if (!Files.exists(dir)) Files.createDirectories(dir);
```
`Files.exists` returns a boolean with no exception path for "doesn't exist" — contrast with classic `File`, where you'd typically check `.exists()` on a `File` object with murkier semantics around symlinks and permissions. `Files.createDirectories` creates the full parent chain in one call and is a no-op if the directory is already there, so the guard isn't strictly required for correctness but makes the intent explicit.

## Why `split(",")` would have broken this exact data, concretely
```java
"Bob Smith, Jr.",Sales,62000.0
```
Naive `line.split(",")` on that raw line produces four pieces (`"Bob Smith`, ` Jr."`, `Sales`, `62000.0`) instead of three — the interior comma isn't a separator, but `split` has no notion of "inside quotes" to know that. The submitted parser instead scans character-by-character with an `insideQuotes` flag, so a comma encountered while that flag is true gets appended to the current field instead of ending it:
```java
} else if (current == ',' && !insideQuotes) {
    fields.add(field.toString());
    field.setLength(0);
} else field.append(current);
```
This is the mechanical reason the final report came out right: `Total Employee Count: 5` (not 6, which is what a `split(",")`-based parser would have produced by turning one row into two field-groups), and `Bob Smith, Jr.` / `Eve, Product` both printed as single, whole name fields rather than being cut at the interior comma.

## Doubled-quote escaping, both directions
Write side:
```java
if (value.contains(",") || value.contains("\"") || ...)
    return "\"" + value.replace("\"", "\"\"") + "\"";
```
Read side:
```java
if (current == '"') {
    if (insideQuotes && i+1 < line.length() && line.charAt(i + 1) == '"') {
        field.append('"');
        i++;
    } else insideQuotes = !insideQuotes;
}
```
`Carol "CJ" White` was written as `"Carol ""CJ"" White"` and read back correctly because the parser's one-character lookahead distinguishes "a doubled quote, meaning a literal `"` inside the field" from "a single quote, meaning the quoted section just ended" — the same rule applied symmetrically on both ends is what made the round trip lossless.

## Key takeaway
CSV correctness isn't about the comma — it's about tracking a boolean state (inside/outside quotes) while scanning, which is exactly what `split(",")` cannot do because it operates on the whole string at once with no memory of what came before each comma. NIO's `Files.exists`/`createDirectories`/`readAllLines`/`writeString` collapse what would be several classic-I/O calls (open, check, catch, close) into single calls that return plain values, which is the concrete reason it reads cleaner for whole-file/whole-directory operations.

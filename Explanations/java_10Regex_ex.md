# java_10Regex — Pattern, Matcher

## `matches()` implicitly anchors the whole string
```java
Pattern producPattern = Pattern.compile("^[A-Z]{2}-[0-9]{4}-[A-Z]{3}$");
Matcher m = producPattern.matcher(code);
m.matches();
```
`Matcher.matches()` already requires the entire input to match, so the `^`/`$` anchors here are redundant (harmless, but `find()` is the method that actually needs them to avoid matching a substring). Worth remembering which method already implies full-string matching and which doesn't.

## `find()` in a `while` loop walks every match, not just the first
```java
while (logMatcher.find()) {
    String level = logMatcher.group(2);
    ...
}
```
Each call to `find()` resumes searching from the end of the previous match, so the loop naturally advances through all 6 valid log lines in the block and simply fails to match (and moves on) at the 2 noise lines — no manual filtering needed. This is the behavior `String`'s convenience methods can't give you: `replaceAll`/`split` process the whole input in one shot and don't hand back per-match structured data.

## `MULTILINE` changes what `^`/`$` anchor to
```java
Pattern logPattern = Pattern.compile(regex, Pattern.MULTILINE);
```
Without `MULTILINE`, `^` and `$` only anchor to the start/end of the *entire input*, so a multi-line block would only ever be able to match once (at most), at the very start. With `MULTILINE`, they anchor to the start/end of each *line*, which is what lets `find()` locate a match on every qualifying line independently.

## Capturing groups you don't read are still valid, just unused
```java
// String timestamp = logMatcher.group(1); // Not used, but extracted
```
The regex captured 5 groups, but only `group(2)`/`group(3)`/`group(4)` were actually called. That's not wrong — the report didn't need timestamp or message — but it's worth noting the alternative: `(?:...)` (a non-capturing group) documents "this piece is structurally required but nobody's going to ask for it," which is a small but real signal for anyone reading the pattern later.

## Key takeaway
The hard problem's real test was whether `Pattern`/`Matcher` earned its place over `split`/`replaceAll`: it did, because the task needed (a) the same compiled pattern reused across many lines, (b) more than one match extracted from a single input, and (c) structured sub-fields per match rather than just a yes/no or a full-match string — none of which `String`'s regex convenience methods can do in one pass.

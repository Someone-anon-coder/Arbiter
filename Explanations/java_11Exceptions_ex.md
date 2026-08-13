# java_11Exceptions — try/catch/finally, checked vs. unchecked, custom exceptions, try-with-resources

## `finally` runs regardless of how the `try` block exits
```java
try {
    int result = withdraw(100, 40);
    System.out.println("Result: " + result);
} catch (NegativeAmountException e) {
    System.out.println(e.getMessage());
} finally {
    System.out.println("Attempt finished.");
}
```
`Attempt finished.` prints after both the successful call and the call that threw and was caught — proof that `finally` isn't "the error-handling cleanup block," it's "the always block."

## The parent class you extend decides checked vs. unchecked — there's no separate keyword
```java
class NegativeAmountException extends Exception { ... }        // checked
class DataCorruptionException extends RuntimeException { ... } // unchecked
```
`withdraw(...) throws NegativeAmountException` is required to compile, because `NegativeAmountException` is checked. `process(...) throws DataCorruptionException` compiles too, but only because declaring an unchecked exception is *legal*, not because it's *required* — the compiler would accept `process` without it. Declaring it anyway is fine as self-documentation; just worth knowing the difference between "required" and "allowed."

## Try-with-resources closes in reverse declaration order — even when nothing throws
```java
try (
    TrackedResource tr1 = new TrackedResource("Tracked Resource 1");
    TrackedResource tr2 = new TrackedResource("Tracked Resource 2");
) {
    tr1.process(0);
}
```
Output: `Closing Tracked Resource 2` then `Closing Tracked Resource 1`. `tr2` was declared last, so it's closed first — like popping a stack. This isn't an artifact of exception handling; it happens on the plain success path too.

## Resources close *before* the `catch` block runs, not after
```java
try (tr1; tr2) {
    tr1.process(1); // throws DataFormatException
} catch (DataCorruptionException e) {
    ...
} catch (DataFormatException e) {
    System.out.println(e.getMessage());
}
```
Output order: `Closing Tracked Resource 2`, `Closing Tracked Resource 1`, *then* `Data Format Exception: Code = 1`. The exception propagates out of the `try` body, but both resources are closed as part of unwinding the `try` block itself, before control ever reaches a matching `catch`. This is what makes try-with-resources safe to reason about under exceptions: cleanup isn't contingent on which `catch` (if any) ends up handling the failure.

## Multiple typed `catch` blocks vs. one broad one
```java
} catch (DataCorruptionException e) {
    System.out.println(e.getMessage());
} catch (DataFormatException e) {
    System.out.println(e.getMessage());
}
```
Since `DataCorruptionException` and `DataFormatException` aren't related by inheritance, their `catch` order doesn't matter for compilation (unlike catching a subclass after its own superclass, which *would* be a compile error — unreachable catch). Keeping them separate — rather than one `catch (Exception e)` — is what makes it possible to react differently to a checked, expected-and-declared failure versus an unchecked, programmer-error-shaped one.

## Key takeaway
The hard problem only comes out right if three things hold at once: resources close in reverse order regardless of outcome, that closing happens *before* any `catch` block runs (not folded into it), and each exception type is caught by its own specific type rather than a shared catch-all. Getting the output sequence right (`Closing ... Closing ... Caught ...`) is what actually proves all three, rather than just asserting them.

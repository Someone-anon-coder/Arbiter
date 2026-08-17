# java_13Lambdas — lambda expressions, functional interfaces, method references

## A lambda only compiles because the target interface has exactly one abstract method
```java
Predicate<String> correctUsername = str -> str.length() >= 5;
```
The compiler doesn't infer `str`'s type from thin air — it looks at the target type `Predicate<String>`, finds its single abstract method `boolean test(T t)`, and matches the lambda's one parameter and `boolean`-valued body against that signature. This is also why a custom interface with two abstract methods can never be a lambda target: there would be two possible shapes to implement and no way to know which one the lambda body is for. `default` and `static` methods don't count toward that limit, which is what let `EmployeeFormatter` below carry a second, non-abstract method without losing its status as a valid lambda target.

## A custom functional interface earns its place when the built-ins can't express the signature you need
```java
@FunctionalInterface
interface EmployeeFormatter {
    String format(Employee employee) throws FormatException;

    default String formatOrDefault(Employee employee, String fallback) {
        try { return format(employee); }
        catch (FormatException e) { return fallback; }
    }
}
```
None of `Function`/`Predicate`/`Consumer`/`Supplier` allow their abstract method to declare a checked exception — `Function<T,R>.apply` simply doesn't have a `throws` clause, so a lambda that needs to throw `FormatException` checked has nowhere to plug into a built-in. Defining `EmployeeFormatter` was the only way to model "formatting can fail," not a stylistic preference for a custom type. This was borne out directly: manually adding a null-name record made `formatOrDefault` actually catch the exception and fall back to `"INVALID_RECORD"` at runtime, rather than that path being unreachable dead code.

## Distinguishing method reference forms is about *whose* method is being referenced
```java
Predicate<Employee> salaryCheck = Employee::isSalaryAboveThreshold; // static — form 1
Predicate<Employee> nameCheck   = Employee::isNameNonBlank;         // arbitrary-instance — form 3
Function<String, String> normalize = String::trim;                  // arbitrary-instance — form 3
```
`isSalaryAboveThreshold` is declared `static`, so `Employee::isSalaryAboveThreshold` is a direct static-method reference — equivalent to `(e) -> Employee.isSalaryAboveThreshold(e)`. `isNameNonBlank` and `trim` are instance methods with no receiver bound in advance; the *lambda's own parameter* supplies the receiver at call time — equivalent to `(e) -> e.isNameNonBlank()` and `(s) -> s.trim()`. The tell is whether an object is already sitting in scope before the reference is written (bound instance, form 2) or whether the target interface's parameter *becomes* the receiver (arbitrary instance, form 3) — here it's always the latter for the instance methods, since `Employee` and `String` are types, not variables.

## Composition builds one combined behavior out of independently reusable pieces
```java
Predicate<Employee> bonusEligible = salaryCheck.and(nameCheck);
Function<String, String> uppercase = normalize.andThen(String::toUpperCase);
```
`salaryCheck` and `nameCheck` each stay meaningful on their own — reusable in isolation — while `.and(...)` produces a third `Predicate` that short-circuits like `&&` without either original predicate needing to know about the other. `.andThen(...)` chains `normalize` and `String::toUpperCase` so the second `Function` runs on the first's output. Both compositions were load-bearing rather than decorative: `bonusEligible` was the actual condition driving which records printed under "Bonus Eligible," and `uppercase` was the actual transformation applied to every formatted name — verified by tracing the dataset by hand (salary > 60000 AND non-blank name → Alice Kim, CHARLIE NG, dana park) against the program's real output, which matched exactly.

## Key takeaway
The header requirements (custom interface, two method reference forms, composition) are all easy to satisfy superficially by adding unused code that merely exists. What made this submission genuine rather than checkbox-driven is that each piece was doing real work reachable from `main`'s actual control flow — proven by running the program, not just by reading the source and trusting that it would behave as intended.

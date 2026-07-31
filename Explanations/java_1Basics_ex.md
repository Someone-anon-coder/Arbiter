# java_1Basics — Basics of Java

## JDK / Environment
The JDK provides `javac` (compiler) and `java` (launcher/JVM). Workflow: `javac File.java` produces `File.class`, then `java File` runs it (no extension on either the class name or the `.java`/`.class` suffix when invoking). Every runnable program needs a `public class` whose name matches the file name exactly, containing `public static void main(String[] args)` as the entry point.

Naming note: Java identifiers (and therefore file/class names) cannot contain `-`. Use digits and PascalCase/underscore instead (e.g. `java_1Basics`, not `java_1-basics`).

## Variables and Data Types
Java is statically typed — every variable has a fixed, declared type.

- **Primitives** hold raw values directly: `byte`, `short`, `int`, `long` (whole numbers), `float`, `double` (decimals), `char` (single 16-bit character), `boolean` (true/false). `long` literals need an `L` suffix, `float` literals need an `f` suffix, or they default to `int`/`double` and may not compile or may lose precision.
- **Reference types** (`String`, arrays, any class) hold a reference to an object on the heap. Assigning or passing a reference type copies the reference, not the object — two variables can end up pointing at the same underlying object.

## Operators
- Arithmetic: `+ - * / %`. Integer division truncates (`7 / 2 == 3`).
- Assignment shorthand: `+= -= *= /= %=`.
- Comparison: `== != > < >= <=`. For reference types, `==` compares identity (same object), not content.
- Logical: `&& || !`, both `&&` and `||` short-circuit.
- Increment/decrement: `++`/`--`, prefix applies before the value is used in an expression, postfix after.

## Type Conversion / Casting
- **Widening** (small → large, e.g. `int` → `double`) happens automatically, no data loss.
- **Narrowing** (large → small, e.g. `double` → `int`) requires an explicit cast — `(int) someDouble` — and can lose precision or truncate.
- Mixed-type expressions promote the smaller type upward for that operation (`int + double` evaluates as `double`).

## Input / Output
- Output: `System.out.println` (newline), `System.out.print` (no newline), `System.out.printf`/`String.format` for formatted output (e.g. `%.2f` for 2 decimal places).
- Input: `Scanner` wraps `System.in`. `nextInt()`/`nextDouble()` leave the trailing newline in the buffer, which a following `nextLine()` will immediately consume as an empty string. Two valid fixes: insert an extra `sc.nextLine()` to flush after a token read, or read every input as a line via `nextLine()` and parse it explicitly (`Integer.parseInt`, `Double.parseDouble`) — the latter avoids the hidden-leftover-token problem entirely rather than remembering to clean it up.

## Key takeaway
Cast only when the language requires it (narrowing conversions, or deliberate truncation) — not as a way to demonstrate the concept when the wider type would have worked fine and been more precise.

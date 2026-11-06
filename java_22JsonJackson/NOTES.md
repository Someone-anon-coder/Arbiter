# Session 22: JSON with Jackson Notes

## Execution Environment & Decisions
- **Data File Location:** JSON files are placed relative to the repo root in `Files/json_orders/`.
- **Working Directory:** The Java code dynamically locates the files via NIO `Path.of("../../Files/json_orders/...")` assuming the programs are run from inside their respective project directories (e.g., from `java_22JsonJackson/problem2`).

## Problem 1: Transitive Dependencies and Major Version

**Transitive Dependency Question:**
Even though we only declared `jackson-databind` in our dependencies, `jackson-core` and `jackson-annotations` are downloaded and present in the dependency tree. This is because `jackson-databind` internally relies on both of these artifacts. Maven handles transitive dependencies automatically, so we get them "for free", and our BOM import guarantees all three are locked to compatible versions. Note that the BOM handles the wrinkle where annotations is at `2.22` while core/databind are at `2.22.4`.

**Dependency Tree Evidence:**
```text
[INFO] --- maven-dependency-plugin:3.6.1:tree (default-cli) @ problem1 ---
[INFO] com.arbiter.learning:problem1:jar:1.0-SNAPSHOT
[INFO] \- com.fasterxml.jackson.core:jackson-databind:jar:2.22.4:compile
[INFO]   +- com.fasterxml.jackson.core:jackson-annotations:jar:2.22:compile
[INFO]   \- com.fasterxml.jackson.core:jackson-core:jar:2.22.4:compile

```

**Compiler Release Evidence:**

```text
  major version: 65

```

## Problem 2: Order Processing

### (b) Strict Mapper Failure (Unknown Field)

```text
--- 1. Strict Failure (Raw) ---
com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
Unrecognized field "loyaltyPoints" (class com.arbiter.learning.json.p2.Problem2$Order), not marked as ignorable (5 known properties: "items", "note", "id", "orderDate", "customer")
 at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 41, column: 25] (through reference chain: java.util.ArrayList[3]->com.arbiter.learning.json.p2.Problem2$Order["loyaltyPoints"])

```

### (b-c-d) Program Output (Wrapped Exception, Lenient Report, Summary Generation)

**Tree model path:** To extract the latest date from the third customer summary in the Tree Model step, I used the chained path `root.get(2).get("latestDate").asText()`.

```text
--- 2. Wrapped Checked Exception (Caller Handling) ---
Caught custom boundary exception: OrderParseException
Message: Failed to strictly parse order data from ../../Files/json_orders/orders.json
Cause: com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException

--- 3. Lenient Parsing (Successful) ---
Successfully parsed 7 orders.

--- 4. Report ---
Customer        Orders          Total           Latest         
C-01 Priya Nair 2               194.48          2026-03-02     
C-02 Marco Rossi 2               106.99          2026-03-15     
C-03 Aiko Tanaka 2               324.43          2026-04-21     
C-04 Ben Okafor 1               0.00            2026-01-31     
Grand total: 625.90
Total units across all line items: 36
Span between the earliest and latest order date in days: 80

--- Wrote customer_summary.json ---
Using readTree to extract third customer's latest order date: 2026-04-21

```

### (e) Understanding Exception Layers

**Failure 1 (Malformed JSON):**

```text
com.fasterxml.jackson.core.io.JsonEOFException: Unexpected end-of-input: expected close marker for Object (start marker at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 1])
 at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 34]
  -> com.fasterxml.jackson.core.JsonParseException
  -> com.fasterxml.jackson.core.exc.StreamReadException
  -> com.fasterxml.jackson.core.JsonProcessingException
  -> com.fasterxml.jackson.core.JacksonException
  -> java.io.IOException
  -> java.lang.Exception
  -> java.lang.Throwable
  -> java.lang.Object

```

**Why this happened:** This exception (`JsonEOFException` / `JsonParseException`) originates in the `jackson-core` streaming tokenizer layer. The parser reads raw text characters and encountered an unexpected end-of-file before the JSON object was properly closed with a `}`. It failed immediately, before `jackson-databind` ever looked at your Java types.

**Failure 2 (Unfit Data):**

```text
com.fasterxml.jackson.databind.exc.InvalidFormatException: Cannot deserialize value of type `int` from String "three": not a valid `int` value
 at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 30] (through reference chain: com.arbiter.learning.json.p2.Problem2$Item["quantity"])
  -> com.fasterxml.jackson.databind.exc.MismatchedInputException
  -> com.fasterxml.jackson.databind.JsonMappingException
  -> com.fasterxml.jackson.databind.DatabindException
  -> com.fasterxml.jackson.core.JsonProcessingException
  -> com.fasterxml.jackson.core.JacksonException
  -> java.io.IOException
  -> java.lang.Exception
  -> java.lang.Throwable
  -> java.lang.Object

```

**Why this happened:** This is an `InvalidFormatException` (a subtype of `JsonMappingException`), which originates in the `jackson-databind` layer. The input text was perfectly valid JSON structurally, so the tokenizer succeeded. The failure occurred during data binding because Jackson tried to coerce the JSON string `"three"` into a Java primitive `int` for the `quantity` field, which is not possible since the string is not a numeric representation.

### Why Reuse ObjectMapper?

The `ObjectMapper` caches expensive reflection data—specifically the serializers and deserializers it builds when inspecting Java types. Re-creating the mapper on every call throws away this cache, severely degrading performance. By configuring it once upfront and reusing a static singleton, we incur the reflection cost only once. The instance is thread-safe for all reads and writes once configuration is complete.

**1. Why TypeReference is needed**
Java erases generic type parameters at runtime. If you tell Jackson to deserialize into `List.class`, the generic type is gone; Jackson has no way to know it should build a list of `Order` objects, so it falls back to parsing the elements into generic `LinkedHashMap`s. This causes a `ClassCastException` later in your code.

`TypeReference` bypasses type erasure. By instantiating it as an anonymous subclass (e.g., `new TypeReference<List<Order>>() {}`), the generic type is baked into the subclass's bytecode at compile time. Jackson uses reflection to read that generic signature from the class definition, allowing it to deserialize the exact types you want.

**2. Missing field vs. Explicit null vs. Unknown property**
These represent three distinct situations where the JSON schema and the Java type do not perfectly align:

* **Unknown property:** The JSON contains a key (e.g., `"loyaltyPoints": 120`) that does not exist in your Java class. This is "extra data." By default, Jackson is strict and fails immediately with an `UnrecognizedPropertyException`.
* **Missing field:** The JSON completely lacks a key that your Java class declares. Jackson never encounters the field during parsing, so the Java object simply receives its default initialization: `null` for reference types, `0` for numbers, and `false` for booleans.
* **Explicit null:** The JSON includes the key, but explicitly sets it to null (e.g., `"note": null`). Jackson reads the key and actively sets the Java field to `null`. For Java primitives, Jackson quietly coerces this `null` to `0` or `false` (unless you explicitly enable `FAIL_ON_NULL_FOR_PRIMITIVES`).

For reference types, missing fields and explicit nulls result in the same final state (`null`), making it impossible to tell from the parsed object whether the data was absent or explicitly blanked out.
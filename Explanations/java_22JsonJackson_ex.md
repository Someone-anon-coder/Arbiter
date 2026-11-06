# java_22JsonJackson — JSON with Jackson: binding, strictness, dates, and exceptions

## Three layers, one mapper
Java has no built-in JSON support. Jackson's streaming layer (`jackson-core`) reads tokens; `jackson-databind` adds data binding (JSON ↔ records/POJOs) and the tree model (`JsonNode`). One `ObjectMapper` is configured once (modules, features) and then reused: it caches reflection-built (de)serializers and is thread-safe after configuration. Both solutions keep mappers in `static final` fields.

## Dependency management with the BOM
`jackson-bom` 2.22.4 is imported in `<dependencyManagement>` (scope `import`, type `pom`), so `jackson-databind` and `jackson-datatype-jsr310` carry no versions. The real tree shows `databind` 2.22.4, `core` 2.22.4 and `annotations` **2.22** (annotations is versioned major.minor only since 2.20). `core` and `annotations` arrive transitively through `databind`'s own pom.

## Why `TypeReference`
`List<Order>.class` doesn't exist because generics are erased; `List.class` alone would give a list of `LinkedHashMap`s and a later `ClassCastException`. `new TypeReference<List<Order>>() {}` is an anonymous subclass, and a subclass records its type arguments in the class file, so Jackson can read them back reflectively.

## Missing vs. explicit null vs. unknown
| Situation | Example in `orders.json` | Result |
|---|---|---|
| Unknown property | `loyaltyPoints` on ORD-1004 | Strict mapper throws `UnrecognizedPropertyException`; lenient mapper skips it |
| Missing field | no `email` for C-04 | Component is `null` (primitives would be `0`/`false`) |
| Explicit null | `"note": null` on ORD-1002 | Component is `null`; indistinguishable from missing on a `String` |

Strict vs. lenient is an API-design choice: strict catches drift in input you control; lenient tolerates feeds that add fields. The wrapped `OrderParseException` is a checked boundary: callers handle one domain type (with path context and the Jackson exception as cause) rather than depending on Jackson.

## Two different failures
`{"id": "ORD-9001", "quantity": 3` is not JSON: the tokenizer in `jackson-core` hits end-of-input with an open object, so `JsonEOFException` (a `JsonParseException`, under `StreamReadException`) is thrown before any Java type is consulted. `{"sku": ..., "quantity": "three", ...}` is valid JSON, so tokenizing succeeds; binding then fails converting `"three"` to `int`: `InvalidFormatException` (a `MismatchedInputException` / `JsonMappingException`, under `DatabindException`). Both extend `JsonProcessingException`, which extends `IOException`, so one `catch (IOException)` covers file and JSON failures.

## Annotations and features used
| Annotation / feature | What it does |
|---|---|
| `@JsonProperty("customer_name")` | Writes the component under a different JSON name |
| `@JsonIgnore` (on `internalRank`) | Excludes the property from the JSON entirely |
| `@JsonInclude(NON_NULL)` | Omits the property when null (C-04 has no `email` key) |
| `@JsonIgnoreProperties(ignoreUnknown = true)` | Per-type lenient reading (the solution used the mapper-wide setting instead) |
| `FAIL_ON_UNKNOWN_PROPERTIES` (default true) | Mapper-wide strict/lenient switch for unknown keys |
| `JavaTimeModule` | Teaches Jackson `LocalDate`; without it, `InvalidDefinitionException` |
| `WRITE_DATES_AS_TIMESTAMPS` disabled | `"2026-04-21"` instead of `[2026,4,21]` |
| `TypeReference<T>` | Carries a generic type past erasure |
| `readTree` / `JsonNode` | Navigate JSON with no POJO (`root.get(2).get("latestDate")`) |

## Key takeaway
Binding errors split by who gives up: the tokenizer (text isn't JSON) or the data binder (JSON doesn't fit the type). That split decides which exception you see and where a wrapper exception belongs.

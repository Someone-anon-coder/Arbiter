# java_6AbstractionInterfaces — Abstraction, Interfaces, equals/hashCode/toString

## Interfaces: abstract + default together
`Greeter` declares `greet` with no body (implicitly `public abstract`) and `greetLoudly` with a body (`default`). `FriendlyGreeter` only had to implement `greet` — `greetLoudly` came along for free, inherited as-is, and still called `greet(name)` polymorphically on whatever object it was invoked on.

## Abstract class implementing an interface
`LibraryItem` is `abstract` and `implements Catalogable`. It satisfies `catalogId()` itself (returning `isbn`), but leaves `lateFeePerDay()` abstract — a method with no sensible shared implementation, deferred to each subclass. `Book` and `DVD` each provide their own fixed fee. This is the pattern from Step 1: interface for the contract (`Catalogable`, shared with anything catalogable), abstract class for shared identity and partial implementation (`LibraryItem`, specific to library items).

## equals()/hashCode() — same field, both methods
`LibraryItem.equals()` and `hashCode()` both derive from `isbn` alone, and nothing else. That's what made the pair internally consistent: two objects with equal `isbn` are guaranteed to produce equal hash codes, so a hash-based collection can never accidentally route them to different buckets.

## The proof, not just the assertion
Four `LibraryItem`s went into a `HashSet`, two of them (`l1`, a `Book`; `l3`, a `DVD`) sharing isbn `1234` as a deliberate by-value duplicate — different object, different subclass, different title/year, equal only by the overridden `equals()`. The set's size (3, not 4) and printed contents (isbn `1234` appearing once) showed the duplicate was actually detected, not just theoretically handled.

The `HashMap` case pushed further: `put()` on an already-equal key overwrites the *value* rather than creating a second entry, so the final fee stored under isbn `1234` ended up being the later `put`'s value. Then a **third**, never-stored `DVD` object with the same isbn was used to `get(...)` from the map and still successfully returned that value. That lookup only works if `hashCode()` placed the query object in the same bucket as the stored key *and* `equals()` then matched them — it's the mechanism itself being exercised, not a printed claim about it.

## Key takeaway
`equals()` and `hashCode()` must be derived from the *same* fields, or a hash-based collection's bucketing and its equality check disagree with each other — and that disagreement is exactly what causes duplicates to silently slip past `HashSet.add()` or a `HashMap.get()` to return `null` for a key that's logically already present.

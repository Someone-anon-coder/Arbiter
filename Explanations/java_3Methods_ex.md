# java_3Methods — Methods

## Method declaration, parameters, return values
A method's signature is `<modifiers> <returnType> <name>(<params>)`. Every path through a non-`void` method must return a value of the declared type — the compiler enforces this. Parameters are local to the call; each invocation gets its own copies.

## Method overloading
Multiple methods can share a name in the same class as long as their parameter lists differ (count, types, or order). The compiler resolves which one to call at compile time based on the argument types at the call site. Return type alone is not enough to distinguish an overload.

## Varargs
`type... name` lets a method accept zero or more arguments of that type, treated as an array (`type[]`) inside the method body. A method may have at most one varargs parameter, and it must be last in the parameter list. Confirmed in this session: `describe(String label, int... value)` correctly handled both zero-argument and five-argument calls, printing `"(none)"` for the empty case.

## Recursion
A method calling itself, with a base case that stops the recursion and a recursive case that moves an argument closer to that base case. This session's hard problem required recursing over a jagged 2D array with no loops at all — solved with two cooperating recursive methods: one recursing row-by-row, calling a second that recurses column-by-column within a single row, summing as the call stack unwinds.

## Pass-by-value semantics
Java is *always* pass-by-value — for primitives, the value is copied; for reference types (arrays, objects), the reference itself is copied, not the object it points to. This produces two distinct behaviors that must not be conflated:

1. **Mutating through the reference** (e.g. `arr[0] = 0`, or `runningMax[0] = newMax`) is visible to the caller, because the caller's reference and the parameter's copied reference point at the same heap object.
2. **Reassigning the parameter** (e.g. `arr = new int[]{...}`) only repoints the local copy of the reference — the caller's variable still points at the original object, completely unaffected.

This session's hard problem proved point 2 directly rather than just asserting it: `flattenAndSumBroken` reassigned its `runningMax` parameter to a brand-new array before using it, and afterward the caller's original array in `main` was shown to still hold `Integer.MIN_VALUE` — untouched, because the reassignment only ever affected the method's own local copy of the reference.

## Key takeaway
"Reference passed by value" means the *pointer* is copied, not the object. Writing through that pointer reaches the shared object; overwriting the pointer itself does not reach back to the caller. This distinction is the foundation for reasoning correctly about mutation in all future object/collection work.

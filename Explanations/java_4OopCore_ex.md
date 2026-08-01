# java_4OopCore — Object-Oriented Programming (Core)

## Classes and objects
A class is a blueprint (fields + methods); an object is a concrete instance created with `new`, holding its own copy of the instance fields. A variable of a class type holds a *reference* to the object, not the object itself — the same reference semantics already seen with arrays.

## Constructors and `this`
A constructor initializes an object's fields at creation time. `this` disambiguates the field from a same-named parameter (`this.title = title`). Writing any constructor removes the compiler's free no-arg default.

## Encapsulation
Fields are declared `private`; access from outside the class goes only through `public` getters/setters. This session's `Book.setPages` demonstrated the actual point of a setter over a bare public field: it rejected `-5` silently, leaving the original value (`37`) intact rather than corrupting it or crashing.

## Static vs. instance members
Instance fields (`ownerName`, `balance`, `accountId`) belong to each `BankAccount` object separately — three accounts, three independent balances. Static fields (`totalBalance`, `accountCounter`) belong to the class itself: one shared copy, updated from every instance's constructor and every `deposit`/`withdraw` call, read back through a `static` method (`getTotalBankBalance()`) that needs no object to call.

This session proved the shared-state behavior directly, not just by asserting it: after `account1.deposit(1000)`, `account2.withdraw(2000)` (succeeds), and a second `account2.withdraw(2000)` (rejected — exceeds the now-lower balance), the printed static total (`9000.0`) matched the hand-summed individual balances (`3000 + 1000 + 5000`) — confirming the static field tracked changes made *through every instance*, not just the one most recently touched. `accountId` values (`1, 2, 3`) were also shown to be assigned uniquely and sequentially by a static counter incremented once per construction, independent of any instance data.

## Key takeaway
Instance state answers "what does *this* object hold"; static state answers "what does the *class* hold, shared across all objects." A static method has no implicit object to operate on (no `this`), which is exactly why `main` — and `getTotalBankBalance()` here — can be called without ever constructing anything first.

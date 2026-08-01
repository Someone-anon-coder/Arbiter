# java_5Inheritance — Inheritance & Polymorphism

## Inheritance
`Circle extends Shape` and `Manager`/`Contractor`/`Intern extends Employee` establish "is-a" relationships. Each subclass constructor chains to its superclass constructor via `super(...)` as its first action (`Manager`, `Contractor`, `Intern` all call `super(name, baseSalary)` before touching their own fields), which is how the inherited `name`/`baseSalary` state actually gets initialized without the subclass needing direct access to set it itself.

## Overriding vs. inheriting unchanged
`Circle.area()`, `Manager.calculatePay()`, and `Contractor.calculatePay()` all use `@Override` to redefine behavior with the exact same signature as their superclass's version. `Intern` deliberately declares no `calculatePay()` at all — it simply inherits `Employee`'s version untouched, and that was intentional, not an oversight, per the problem statement.

## Dynamic dispatch — the actual point of this session
`Shape circle = new Circle(5.0)` has declared type `Shape` but runtime type `Circle`. Calling `circle.area()` printed `Circle`'s message ("Circle Area: 78.53...") and returned `Circle`'s computed value — not `Shape`'s generic "no area" default — proving the call resolved based on what the object actually *is*, not what the reference is declared as.

The hard problem pushed this further: an `Employee[]` held a deliberately shuffled mix of `Manager`, `Contractor`, and `Intern` instances, and the traversal loop called `.calculatePay()` on each element using only the `Employee`-typed array reference — no `instanceof`, no casting anywhere. Each employee's pay still came out using its own subclass's formula (`Manager`'s per-report bonus, `Contractor`'s hourly formula, `Intern`'s inherited default), and the hand-summed total (333000) matched the printed total exactly. That correctness was only possible because the JVM looks up the actual object's class at the moment `calculatePay()` is called, every time — the loop code itself never needed to know or check which subclass each element actually was.

## Key takeaway
Overriding resolves at runtime (dynamic dispatch, based on the object's actual type); overloading (Session 3) resolves at compile time (based on the declared types of the arguments at the call site). A base-typed collection calling an overridden method on each element is the concrete mechanism behind "program to a supertype, not a specific implementation" — the same idea Spring later relies on when it hands back a proxy typed as an interface.

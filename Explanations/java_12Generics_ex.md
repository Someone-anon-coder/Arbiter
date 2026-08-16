# java_12Generics — generic classes/methods, bounded type parameters, wildcards & PECS

## A bound is what lets you call a method the compiler otherwise can't guarantee exists
```java
class ScoreBox<T extends Comparable<T>> {
    private T value;
    public boolean isHigherThan(T other) {
        return value.compareTo(other) > 0;
    }
}
```
Drop the bound to plain `class ScoreBox<T>` and `value.compareTo(other)` stops compiling: `cannot find symbol: method compareTo(T)`, because an unbounded `T` is only known to be an `Object`, and `Object` has no `compareTo`. The bound is a promise to the compiler — "whatever `T` ends up being, it will have this method" — checked once at the class declaration, not re-derived from how the class happens to be used later.

## `? extends T` — read-only, because the compiler won't guess the exact subtype
```java
public static double sumNumbers(List<? extends Number> list) {
    double sum = 0.0;
    for (Number n : list) sum += n.doubleValue();   // fine — every element widens to Number
    return sum;
}
```
This accepts `List<Integer>`, `List<Double>`, `List<Number>` — anything whose elements are `Number` or narrower. Swap the bound to `? super Number` and both `sumNumbers(intList)` and `sumNumbers(doubleList)` fail at the *call site* — a lower-bounded wildcard only accepts `Number` or a **supertype** of it, so `List<Integer>` (a subtype's list) is not a `List<? super Number>` at all. This was verified directly: compiling that variant produces `incompatible types: List<Integer> cannot be converted to List<? super Number>` for both calls, not a guess about what "should" happen.

## `? super T` — write-only, because a read can only be typed as `Object`
```java
public static void fillWithZero(List<? super Integer> list, int count) {
    for (int i = 0; i < count; i++) list.add(Integer.valueOf(0));  // fine — Integer always fits
}
```
This accepts `List<Integer>`, `List<Number>`, `List<Object>` — anything whose elements are `Integer` or broader, so adding an `Integer` is always safe regardless of which exact supertype it turns out to be. Swap the bound to `? extends Integer` and the method body itself stops compiling: `list.add(Integer.valueOf(0))` fails with `Integer cannot be converted to CAP#1` — an upper-bounded wildcard captures some *unknown* subtype of `Integer`, and the compiler can't verify an `Integer` literal actually matches that unknown subtype. On top of that, the call sites for `numberTarget` and `objectTarget` also fail to compile against `List<? extends Integer>`, since neither `List<Number>` nor `List<Object>` is a list of "some subtype of Integer." Both failures were reproduced directly, not asserted.

## PECS in one line
`sumNumbers` only *reads* from its list → **P**roducer → `? extends T`. `fillWithZero` only *writes* into its list → **C**onsumer → `? super T`. The two methods needed opposite wildcard directions because they play opposite roles with respect to the list — PECS describes the parameter's role in that specific method, not a fixed property of `List<Integer>` itself, which is exactly why the same kind of list (`List<Integer>`, `List<Number>`, etc.) shows up as a valid argument to both methods for different reasons.

## Key takeaway
Getting the right output isn't proof the wildcard direction was necessary — it's proof only when the *wrong* direction is shown to fail, and specifically fails for the reason PECS predicts (rejected caller for `? extends` used where a producer needed `? super`'s breadth; rejected caller *and* rejected method body for `? extends` used where a consumer needed `? super`'s breadth). Compiling both wrong-direction variants independently confirmed the reasoning was real, not incidental to the given call sites.

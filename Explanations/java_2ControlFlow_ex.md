# java_2ControlFlow — Control Flow

## if / else if / else
A chain of boolean checks evaluated top to bottom; the first `true` condition's block runs and the rest are skipped. `else if` is not a separate keyword — it's an `else` whose body is itself another `if`. Conditions must be `boolean` expressions.

## switch — classic form
Dispatches on a value against `case` labels. Falls through to the next case unless a `break` stops it — this is intentional and lets multiple cases share one body (stacked `case` labels with no code between them).

## switch — arrow form
`case X -> ...` isolates each arm; no fall-through, no `break` needed. Can be used as a statement or as an expression that yields a value. `continue` used inside an arrow-form switch body still targets the nearest *enclosing loop* — the switch does not intercept it (only `break` is switch-scoped).

## for / while / do-while
- `for (init; condition; update)` — best when iteration count/index is known up front.
- `while (condition) { ... }` — condition checked before each iteration; may run zero times.
- `do { ... } while (condition);` — condition checked after each iteration; always runs at least once.

## break / continue
`break` exits the nearest enclosing loop or switch immediately. `continue` skips to the next iteration's condition/update check, skipping the remaining body.

## Labeled loops
A label (`outer:` before a loop) lets `break label;` or `continue label;` target that specific loop from inside a nested loop, instead of only affecting the innermost one. Essential for "stop everything the moment X is found" logic in nested scans — confirmed in this session's grid-scan problem, where `break found;` from inside a `switch` inside the inner `for` loop stopped both loops at once, skipping the rest of the grid entirely.

## Key takeaway
`continue` and `break` are loop-scoped by default (`break` is also switch-scoped), which is why a bare `break` inside a `switch` nested in a loop only exits the switch — reaching the enclosing loop from there requires a label.

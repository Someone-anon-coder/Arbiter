# java_18StacksQueuesLinkedList — Custom Stack, Queue (head+tail linked list), Big-O

## Why the queue needs a tail pointer, concretely
```java
public void insertAtTail(T value) {
    Node newNode = new Node(value);
    if (tail == null) {
        head = newNode;
        tail = newNode;
    } else {
        tail.next = newNode;
        tail = newNode;
    }
    size++;
}
```
Without a `tail` field, the only way to find "the last node" is to start at `head` and follow `.next` until one is `null` — an O(n) walk that gets slower every time the queue grows. Here, `enqueue` never touches anything but the `tail` reference itself, so adding the 50th re-enqueued task costs exactly the same as adding the 1st. This is the thing the round-robin scheduler problem was actually built to expose: task `D` gets re-enqueued twice and task `C` once, so a queue with an accidentally-O(n) `enqueue` would still print the *same* completion order and turnaround times — the bug is invisible in the output and only visible by reading `insertAtTail` directly, which is why the evaluation checked the method body rather than trusting the trace.

## The tail-nulling edge case
```java
public T removeHead() {
    ...
    head = head.next;
    if (head == null) tail = null;
    size--;
    return value;
}
```
When the last node is dequeued, `head` becomes `null` — but `tail` still points at the node that was just removed unless this is handled explicitly. Left unhandled, the *next* `enqueue` would do `tail.next = newNode` on a node that's no longer reachable from `head`, silently detaching the new node from the list while `size`/`tail` both look fine. The submitted code guards this correctly (`if (head == null) tail = null`).

## Amortized O(1) vs. plain O(1)
```java
public void push(T value) {
    if (size == data.length) resize();
    data[size] = value;
    size++;
}
```
`resize()` copies the whole backing array — O(n) — but it only fires when the array is completely full, and each resize doubles the capacity, so the total copying work across `n` pushes stays proportional to `n`, not `n²`. That's what "amortized O(1)" means: any *individual* `push` might be the expensive one, but averaged over many calls the cost per call is constant. `pop`/`peek` have no such asterisk — they only ever touch `data[size-1]`.

## Big-O summary — operations implemented in this file

| Structure | Operation | Complexity | Why |
|---|---|---|---|
| `Stack` (array-backed) | `push` | O(1) amortized | Occasional O(n) resize, but doubling keeps total copy work linear in total pushes |
| `Stack` | `pop` / `peek` | O(1) | Direct index at `size-1`/`size`, no scan |
| `SingleLinkedList` | `insertAtHead` | O(1) | Only touches `head` |
| `SingleLinkedList` | `insertAtTail` | O(1) | Only touches `tail` — no walk |
| `SingleLinkedList` | `removeHead` | O(1) | Only touches `head` (and `tail` only in the empties-the-list case) |
| `Queue` (list-backed) | `enqueue` | O(1) | Delegates to `insertAtTail` |
| `Queue` | `dequeue` | O(1) | Delegates to `removeHead` |

## Key takeaway
A linked-list queue's reputation for O(1) enqueue/dequeue is not automatic — it's earned specifically by maintaining `tail` and keeping it correct through the empty-list edge cases (first insert, last removal). Drop the tail pointer, or forget to null it out when the list empties, and the queue either silently degrades to O(n) enqueue or corrupts its own structure — either way, the bug hides behind correct-looking output unless the implementation itself is read.

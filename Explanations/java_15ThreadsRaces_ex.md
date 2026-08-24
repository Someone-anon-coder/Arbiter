# java_15ThreadsRaces — Thread/Runnable, synchronized/locks, race conditions & deadlocks

## `start()` launches a new thread; the interleaved output is the proof
```java
Thread print1To10Task = new Thread(print1To10);
Thread printEvenTask = new Thread(new PrintEven());
print1To10Task.start();
printEvenTask.start();
```
Calling `.start()` (never `.run()` directly) on 4 separate `Thread` objects wrapping 4 separate `Runnable`s is what actually forks execution. The printed trace shows lines from `Thread-0`, `Thread-1`, `Thread-2`, `Thread-3` genuinely interleaved — e.g. `Thread-2` prints several odd numbers in a row before `Thread-1` gets scheduled at all. That interleaving is only possible because 4 independent threads are really running concurrently and being scheduled by the JVM/OS; had `.run()` been called instead, every task would execute fully, in order, on the main thread, and the trace would show one task's entire output before the next task's first line.

## Starting all threads before joining any of them is what makes them concurrent
```java
print1To10Task.start();
printEvenTask.start();
printOddTask.start();
print5Multiple.start();

print1To10Task.join();
printEvenTask.join();
printOddTask.join();
print5Multiple.join();
```
All four `.start()` calls happen first, then all four `.join()` calls. This ordering matters: if `.join()` were interleaved with `.start()` (start thread 1, join thread 1, start thread 2, join thread 2, ...), each thread would have to finish before the next one even began, collapsing the whole thing back into sequential execution. `.join()` still does its job here — it blocks `main` until each thread's `run()` has returned — but only pays off as a *lifecycle-management* tool, not a concurrency-preventing one, when every thread is already running by the time any `join()` is called.

## `counter++` under multiple threads is not one operation — it's read, modify, write
```java
public void run() {
    for (int i = 0; i < incrementCount; i++) {
        counter++;   // unsynchronized: read, add 1, write — as three separate steps
    }
}
```
With 4 threads each doing 100,000 of these, the final count came out wrong on most runs (e.g. 135072 instead of 400000) — not because Java's `+` is broken, but because two threads can both read the same value before either writes back, so one thread's increment is silently overwritten by the other's. This is the exact three-step read/modify/write race described in Step 1, now observed directly: the loss is proportional to how often the three steps from different threads interleave, which is why it shows up reliably at 400,000 total increments but wouldn't necessarily show up at, say, 10.

## `synchronized` on a shared lock object serializes the read-modify-write, restoring correctness
```java
private final Object lockCounter = new Object();

public void run() {
    for (int i = 0; i < incrementCount; i++) {
        synchronized (lockCounter) {
            counter++;
        }
    }
}
```
Because all 4 threads in a given run are constructed around the *same* `IncrementCounterSynchronized` instance, they're all contending for the same `lockCounter` object's monitor. Only one thread can be inside the `synchronized` block — and therefore mid read-modify-write on `counter` — at any instant. Every one of 20 test trials came out at exactly 400000: with the three steps forced to happen as an indivisible unit per thread, there's no window left for another thread's read to land between another thread's modify and write.

## Deadlock: opposite lock-acquisition order creates circular wait
```java
// Thread A                              // Thread B
synchronized (lockA) {                   synchronized (lockB) {
    Thread.sleep(50);                        Thread.sleep(50);
    synchronized (lockB) { ... }             synchronized (lockA) { ... }
}                                         }
```
Thread A grabs `lockA`, Thread B grabs `lockB` (the `sleep(50)` widens the window so this reliably happens on both sides before either tries for its second lock). Then A blocks waiting for `lockB` — held by B — while B blocks waiting for `lockA` — held by A. Neither will ever release what it's holding, because release only happens on the way *out* of the block, which requires acquiring the second lock first. `join(2000)` followed by `isAlive()` on both threads is how the deadlock is proven without hanging the program forever: both threads reporting `isAlive() == true` after the timeout is direct evidence they're still blocked, not just "possibly slow."

## The fix is consistent ordering, not fewer locks
```java
// Thread A                              // Thread B
synchronized (lockC) {                   synchronized (lockC) {
    Thread.sleep(50);                        Thread.sleep(50);
    synchronized (lockD) { ... }             synchronized (lockD) { ... }
}                                         }
```
Both threads now acquire `lockC` before `lockD` — same order, every time. Whichever thread gets `lockC` first now simply forces the other to wait its turn for `lockC` (there's no scenario where one thread holds `lockD` while waiting on `lockC`, since `lockD` is never grabbed first by either thread), so circular wait can't form. The two threads end up serialized rather than running fully concurrently, but "runs correctly and a bit sequentially" is a very different outcome from "hangs forever" — confirmed by an elapsed time of ~102ms (dominated by the two `sleep(50)` calls happening one after another) against a 2000ms timeout, with both threads reporting `isAlive() == false` afterward.

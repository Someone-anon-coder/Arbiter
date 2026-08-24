class Print1To10 implements Runnable {
    public int sum = 0;

    public void run() {
        for (int i = 1; i <= 10; i++) {
            System.out.println("Thread Name: " + Thread.currentThread().getName() + ", task value: " + i);
            sum += i;
        }
    }
}

class PrintEven implements Runnable {
    public void run() {
        for (int i = 2; i <= 20; i += 2) {
            System.out.println("Thread Name: " + Thread.currentThread().getName() + ", task value: " + i);
        }
    }
}

class PrintOdd implements Runnable {
    public void run() {
        for (int i = 1; i < 20; i += 2) {
            System.out.println("Thread Name: " + Thread.currentThread().getName() + ", task value: " + i);
        }    
    }
}

class IncrementCounter {
    protected int counter = 0;
    public int incrementCount = 100000;

    public int getCounterValue() {
        return counter;
    }
}

class IncrementCounterUnsynchronized extends IncrementCounter implements Runnable{
    public void run() {
        for (int i = 0; i < incrementCount; i++) {
            counter++;
        }
    }
}

class IncrementCounterSynchronized extends IncrementCounter implements Runnable {
    private final Object lockCounter = new Object();
    
    public void run() {
        for (int i = 0; i < incrementCount; i++) {
            synchronized (lockCounter) {
                counter++;
            }
        }
    }
}

public class java_15ThreadsRaces {
    public static void main(String[] args) {
        Print1To10 print1To10 = new Print1To10();

        Thread print1To10Task = new Thread(print1To10);
        Thread printEvenTask = new Thread(new PrintEven());
        Thread printOddTask = new Thread(new PrintOdd());
        
        Thread print5Multiple = new Thread(() -> {
            for (int i = 5; i <= 50; i += 5) {
                System.out.println("Thread Name: " + Thread.currentThread().getName() + ", task value: " + i);
            }
        });

        print1To10Task.start();
        printEvenTask.start();
        printOddTask.start();
        print5Multiple.start();

        try {
            print1To10Task.join();
            printEvenTask.join();
            printOddTask.join();
            print5Multiple.join();
        } catch (InterruptedException e) {
            System.out.println("Exception Occurred: " + e);
        } 

        boolean print1To10TaskCompleted = print1To10Task.isAlive();
        boolean printEvenTaskCompleted = printEvenTask.isAlive();
        boolean printOddTaskCompleted = printOddTask.isAlive();
        boolean print5MultipleCompleted = print5Multiple.isAlive();

        System.out.println(
            !print1To10TaskCompleted && 
            !printEvenTaskCompleted &&
            !printOddTaskCompleted &&
            !print5MultipleCompleted ? "All tasks complete. Result of Print1To10Numbers: " + print1To10.sum : "Tasks still pending"
        );

        System.out.println();
        System.out.println("unsynchronized [5+ runs, showing some incorrect] Unsynchronized Run + ==================== +");
        for (int i = 0; i < 5; i++) {
            IncrementCounterUnsynchronized unsynchronizedIncrement = new IncrementCounterUnsynchronized();
            int incrementCountUnsynchronized = unsynchronizedIncrement.incrementCount;

            Thread unsyncThread1 = new Thread(unsynchronizedIncrement);
            Thread unsyncThread2 = new Thread(unsynchronizedIncrement);
            Thread unsyncThread3 = new Thread(unsynchronizedIncrement);
            Thread unsyncThread4 = new Thread(unsynchronizedIncrement);

            unsyncThread1.start();
            unsyncThread2.start();
            unsyncThread3.start();
            unsyncThread4.start();

            try {
                unsyncThread1.join();
                unsyncThread2.join();
                unsyncThread3.join();
                unsyncThread4.join();
            } catch (InterruptedException e) {
                System.out.println("Exception Occurred: " + e);
            }

            System.out.println("Counter Value Expected (4 threads times " + incrementCountUnsynchronized + "): " + 4 * incrementCountUnsynchronized);
            System.out.println("Counter Value Actual (Run " + (i+1) + "): " + unsynchronizedIncrement.getCounterValue());
        }
        System.out.println(" + ==================== + Unsynchronized Run ");

        // This works because synchronized locks another thread to interrupt 3 step process of increment
        // ReentrantLock was not used due to not requiring much of the methods available by it and synchronized can handle this task efficiently
        System.out.println("\nfixed [5+ runs, all exactly correct] Synchronized Run + ==================== +");
        for (int i = 0; i < 5; i++) {
            IncrementCounterSynchronized synchronizedIncrement = new IncrementCounterSynchronized();
            int incrementCountSynchronized = synchronizedIncrement.incrementCount;

            Thread syncThread1 = new Thread(synchronizedIncrement);
            Thread syncThread2 = new Thread(synchronizedIncrement);
            Thread syncThread3 = new Thread(synchronizedIncrement);
            Thread syncThread4 = new Thread(synchronizedIncrement);

            syncThread1.start();
            syncThread2.start();
            syncThread3.start();
            syncThread4.start();

            try {
                syncThread1.join();
                syncThread2.join();
                syncThread3.join();
                syncThread4.join();
            } catch (InterruptedException e) {
                System.out.println("Exception Occurred: " + e);
            }

            System.out.println("Counter Value Expected (4 threads times " + incrementCountSynchronized + "): " + 4 * incrementCountSynchronized);
            System.out.println("Counter Value Actual (Run " + (i+1) + "): " + synchronizedIncrement.getCounterValue());
        }
        System.out.println(" + ==================== + Synchronized Run ");

        final Object lockA = new Object();
        final Object lockB = new Object();

        System.out.println();
        System.out.println("=== 1. DEMONSTRATING DEADLOCK ===");
        Thread deadlockIncorrectA = new Thread(() -> {
            try {
                synchronized (lockA) {
                    System.out.println(Thread.currentThread().getName() + " acquired Lock A");
                    Thread.sleep(50);
                    
                    synchronized (lockB) {
                        System.out.println(Thread.currentThread().getName() + " acquired Lock B");
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Exception Occured: " + e);
            }
        });

        Thread deadlockIncorrectB = new Thread(() -> {
            try {
                synchronized (lockB) {
                    System.out.println(Thread.currentThread().getName() + " acquired Lock B");
                    Thread.sleep(50);
                    
                    synchronized (lockA) {
                        System.out.println(Thread.currentThread().getName() + " acquired Lock A");
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Exception Occured: " + e);
            }
        });

        deadlockIncorrectA.setDaemon(true);
        deadlockIncorrectB.setDaemon(true);

        deadlockIncorrectA.start();
        deadlockIncorrectB.start();

        try {
            deadlockIncorrectA.join(2000);
            deadlockIncorrectB.join(2000);
        } catch (InterruptedException e) {
            System.out.println("Exception Occured: " + e);
        } 

        boolean thread1Alive = deadlockIncorrectA.isAlive();
        boolean thread2Alive = deadlockIncorrectB.isAlive();
        
        System.out.println("Timeout expired. Thread 1 alive? " + thread1Alive);
        System.out.println("Timeout expired. Thread 2 alive? " + thread2Alive);
        System.out.println(thread1Alive && thread2Alive ? "-> Result: Deadlock successfully occurred!\n" : "-> Result: Threads finished.\n");

        final Object lockC = new Object();
        final Object lockD = new Object();
        // NOTE: not using locks A and B as they are deadlocked until main is completed,
        // Therefore using the same locks will show the same output and not provide a solution to deadlock

        System.out.println("=== 2. FIXING THE DEADLOCK (Consistent Order) ===");
        Thread deadlockCorrectA = new Thread(() -> {
            try {
                synchronized (lockC) {
                    System.out.println(Thread.currentThread().getName() + " acquired Lock C");
                    Thread.sleep(50);
                    
                    synchronized (lockD) {
                        System.out.println(Thread.currentThread().getName() + " acquired Lock D");
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Exception Occured: " + e);
            }
        });

        Thread deadlockCorrectB = new Thread(() -> {
            try {
                synchronized (lockC) {
                    System.out.println(Thread.currentThread().getName() + " acquired Lock C");
                    Thread.sleep(50);
                    
                    synchronized (lockD) {
                        System.out.println(Thread.currentThread().getName() + " acquired Lock D");
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Exception Occured: " + e);
            }
        });

        long startTime = System.currentTimeMillis();

        deadlockCorrectA.start();
        deadlockCorrectB.start();

        try {
            deadlockCorrectA.join(2000);
            deadlockCorrectB.join(2000);
        } catch (InterruptedException e) {
            System.out.println("Exception Occured: " + e);
        } 

        long elapsedTime = System.currentTimeMillis() - startTime;
        boolean fixedThread1Alive = deadlockCorrectA.isAlive();
        boolean fixedThread2Alive = deadlockCorrectB.isAlive();
        
        System.out.println("Elapsed time: " + elapsedTime + " ms");
        System.out.println("Timeout expired. Thread 1 alive? " + fixedThread1Alive);
        System.out.println("Timeout expired. Thread 2 alive? " + fixedThread2Alive);
        System.out.println(fixedThread1Alive && fixedThread2Alive ? "-> Result: Deadlock successfully occurred!\n" : "-> Result: Threads finished.\n");
    }
}

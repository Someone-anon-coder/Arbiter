class Stack<T> {
    private Object[] data;
    private int size;

    public Stack() {
        data = new Object[4];
        size = 0;
    }

    // Time complexity: O(1) amortized.
    // A resize can take O(n), but resizing happens only occasionally.
    public void push(T value) {
        if (size == data.length) resize();

        data[size] = value;
        size++;
    }

    // Time complexity: O(1)
    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) throw new IllegalStateException("Cannot pop from empty stack");

        size--;
        T value = (T) data[size];
        data[size] = null;

        return value;
    }

    // Time complexity: O(1)
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new IllegalStateException("Cannot peek at an empty stack");
        return (T) data[size-1];
    }

    public boolean isEmpty() { return size == 0; }

    // Time complexity: O(n)
    public void resize() {
        Object[] newData = new Object[data.length * 2];
        for (int i = 0; i < size; i++) newData[i] = data[i];

        data = newData;
    }
}

class Task {
    String name;
    int burstTime;
    int remainingTime;
    int completionTime;

    Task (String name, int burstTime) {
        this.name = name;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.completionTime = 0;
    }
}

class SingleLinkedList<T> {
    private class Node {
        T value;
        Node next;

        Node(T value) {
            this.value = value;
            this.next = null;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    // Time complexity: O(1)
    public void insertAtHead(T value) {
        Node newNode = new Node(value);

        newNode.next = head;
        head = newNode;

        if (tail == null) tail = newNode;
        size++;
    }

    // Time complexity: O(1)
    // Because we maintain a tail pointer.
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

    public boolean isEmpty() { return head == null; }
    public int size() { return size; }

    // Time complexity: O(1)
    public T removeHead() {
        if (head == null) throw new IllegalStateException("Cannot remove from empty list");

        T value = head.value;
        head = head.next;

        if (head == null) tail = null;

        size--;
        return value;
    }

    public T getHead() {
        if (head == null) throw new IllegalStateException("Cannot get head from empty list");
        return head.value;
    }
}

class Queue<T> {
    private SingleLinkedList<T> list = new SingleLinkedList<>();

    /*
    * enqueue:
    * Insert at the tail of the linked list.
    *
    * Time complexity: O(1)
    *
    * Why?
    * The linked list maintains a tail pointer, so we already
    * know exactly where the new node belongs. We don't have
    * to walk through the list.
    */
    public void enqueue(T value) { list.insertAtTail(value); }
    
    /*
    * dequeue:
    * Remove and return the head of the linked list.
    *
    * Time complexity: O(1)
    *
    * Why?
    * The head pointer directly identifies the front element.
    * Moving head to head.next requires only a constant amount
    * of work.
    */
    public T dequeue() {
        if (list.isEmpty()) throw new IllegalStateException("Cannot dequeue an empty queue");
        return list.removeHead();
    }

    public T peek() {
        if (list.isEmpty()) throw new IllegalStateException("Cannot peek at an empty queue");
        return list.getHead();
    }

    public boolean isEmpty() { return list.isEmpty(); }
}

public class java_18StacksQueuesLinkedList {
    public static void main(String[] args) {
        String[] tests = {
            "(a + b) * [c - d]",
            "{[()()]}",
            "([)]",
            "(a + b]",
            "((a + b)",
            ")("
        };

        for (String test: tests) {
            System.out.println(test + " -> " + isBalanced(test));
        }

        final int QUANTUM = 4;

        Task taskA = new Task("A", 7);
        Task taskB = new Task("B", 3);
        Task taskC = new Task("C", 9);
        Task taskD = new Task("D", 5);

        Task[] tasks = {taskA, taskB, taskC, taskD};
        Queue<Task> queue = new Queue<>();

        for (Task task: tasks) queue.enqueue(task);

        int currentTime = 0;
        System.out.println();
        System.out.println("Round-robin trace:");
        System.out.println("------------------");

        while (!queue.isEmpty()) {
            Task currentTask = queue.dequeue();
            
            int executionTime = Math.min(currentTask.remainingTime, QUANTUM);
            int startTime = currentTime;

            currentTime += executionTime;
            currentTask.remainingTime -= executionTime;

            System.out.println("Time " + startTime + "-" + currentTime + ": Task " + currentTask.name + " ran for " + executionTime);
            if (currentTask.remainingTime == 0) {
                currentTask.completionTime = currentTime;
                System.out.println("  -> Task " + currentTask.name + " completed at time " + currentTask.completionTime);
            } else queue.enqueue(currentTask);
        }

        System.out.println();
        for (int time = 1; time <= currentTime; time++) {
            for (Task task: tasks) {
                if (task.completionTime == time) System.out.println(task.name);
            }
        }
        
        System.out.println();
        System.out.println("Turnaround times:");
        System.out.println("-----------------");

        for (Task task: tasks) {
            // All tasks arrive at time 0,
            // so turnaround time = completion time.
            int turnaroundTime = task.completionTime;

            System.out.println("Task " + task.name + ": completion time = " + task.completionTime + ", turnaround time = " + turnaroundTime);
        }
    }

    public static boolean isBalanced(String s) {
        Stack<Character> stack = new Stack<>();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            
            if (c == '(' || c == '[' || c == '{') stack.push(c);
            else if (c == ')' || c == ']' || c == '}') {
                if (stack.isEmpty()) return false;
                char opening = stack.pop();

                if (!matches(opening, c)) return false;
            }
        }

        return stack.isEmpty();
    }

    public static boolean matches(char opening, char closing) {
        return (opening == '(' && closing == ')')
            || (opening == '[' && closing == ']')
            || (opening == '{' && closing == '}');
    }
}

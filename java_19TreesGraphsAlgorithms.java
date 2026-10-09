/**
 * TIME COMPLEXITIES:
 * 
 * BST Insert: O(h) where h is the height of the tree (Average O(log n), Worst O(n)). 
 * Justification: Insertion traverses down a single path from root to a leaf based on comparisons.
 * 
 * BST Search: O(h) where h is the height of the tree (Average O(log n), Worst O(n)). 
 * Justification: Search makes one comparison per level, discarding the other subtree each time.
 * 
 * BST In-order Traversal: O(n) where n is the number of nodes. 
 * Justification: The traversal must visit every node exactly once to read out all values.
 * 
 * Graph BFS: O(V + E) where V is vertices and E is edges. 
 * Justification: Each vertex is enqueued/dequeued at most once, and each edge is checked exactly twice (once from each endpoint).
 * 
 * Merge Sort: O(n log n) where n is the array length. 
 * Justification: The array is recursively halved log2(n) times, and merging the halves takes linear O(n) time at each level.
 * 
 * Binary Search: O(log n) where n is the array length. 
 * Justification: The search space is halved at every step by checking the middle element, requiring at most log2(n) steps.
 * 
 * WHY BINARY SEARCH ON THE (B) ARRAY BY NAME ALONE WOULD BE INVALID:
 * Binary search relies on the array being strictly ordered by the target attribute so it can safely discard half the range. 
 * The array from (b) is ordered primarily by distance. Alphabetical names are scattered across different distance tiers, 
 * breaking the sorted assumption. Checking a middle element's name would provide no reliable information about which 
 * half contains the target name.
**/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class java_19TreesGraphsAlgorithms {
    public static void main(String[] args) {
        System.out.println("--- PROBLEM 1: BINARY SEARCH TREE ---");
        BST bst = new BST();
        
        int[] inserts = {50, 30, 70, 20, 40, 60, 80, 35, 65, 30};
        for (int val : inserts) {
            bst.insert(val);
        }

        int[] searches = {60, 35, 90, 10, 50, 65};
        System.out.print("Search queries: ");
        for (int val : searches) {
            System.out.print(val + (bst.search(val) ? "(found) " : "(not found) "));
        }
        System.out.println();

        System.out.print("In-order traversal: ");
        List<Integer> inOrderList = new ArrayList<>();
        
        bst.inOrder(bst.root, inOrderList);
        for (int val : inOrderList) {
            System.out.print(val + " ");
        }
        
        System.out.println();
        System.out.println("Strictly ascending check: " + bst.verifyAscending(inOrderList));

        System.out.println("\n--- PROBLEM 2: GRAPH, BFS, SORT, SEARCH ---");
        Graph graph = new Graph();
        
        String[] allNodes = {"Hub", "Alpha", "Zed", "Mira", "Kilo", "Echo", "Nova", "Bolt", "Quill", "Rook", "Dune", "Sable", "Tau", "Vex"};
        for (String node : allNodes) {
            graph.addNode(node);
        }

        String[] edges = {
            "Kilo-Nova", "Hub-Zed", "Quill-Bolt", "Mira-Echo", "Dune-Sable", "Hub-Alpha",
            "Zed-Kilo", "Nova-Bolt", "Rook-Quill", "Echo-Nova", "Alpha-Kilo", "Hub-Mira",
            "Tau-Dune", "Mira-Bolt", "Sable-Tau"
        };
        
        for (String edge : edges) {
            String[] parts = edge.split("-");
            graph.addEdge(parts[0], parts[1]);
        }

        // (a) BFS
        Map<String, Integer> distances = graph.bfs("Hub");
        System.out.println("(a) Reachable nodes from Hub: " + distances.size());

        // (b) Two-key sort
        Pair[] reachablePairs = new Pair[distances.size()];
        int idx = 0;
        for (Map.Entry<String, Integer> entry : distances.entrySet()) {
            reachablePairs[idx++] = new Pair(entry.getKey(), entry.getValue());
        }

        Sorter.mergeSort(reachablePairs, new ComparatorTwoKey());
        System.out.println("(b) Sorted by distance, then name:");
        for (Pair p : reachablePairs) {
            System.out.print("[" + p.name + ":" + p.distance + "] ");
        }
        System.out.println();

        // (c) Distance queries via binary search
        // Build and sort second array by name only for valid binary search
        Pair[] nameSortedPairs = new Pair[distances.size()];
        idx = 0;
        for (Map.Entry<String, Integer> entry : distances.entrySet()) {
            nameSortedPairs[idx++] = new Pair(entry.getKey(), entry.getValue());
        }
        Sorter.mergeSort(nameSortedPairs, new ComparatorByName());

        System.out.println("(c) Distance queries:");
        String[] queries = {"Kilo", "Rook", "Hub", "Dune", "Vex", "Ghost", "Zed", "Alpha"};
        for (String q : queries) {
            Integer dist = BinarySearch.search(nameSortedPairs, q);
            if (dist == null) {
                System.out.println("  " + q + ": not reachable");
            } else {
                System.out.println("  " + q + ": " + dist);
            }
        }
    }
}

class BST {
    class Node {
        int value;
        Node left;
        Node right;
        Node(int value) { this.value = value; }
    }

    Node root;

    public void insert(int value) {
        if (root == null) {
            root = new Node(value);
            return;
        }
        Node current = root;
        while (true) {
            if (value == current.value) {
                return;
            } else if (value < current.value) {
                if (current.left == null) {
                    current.left = new Node(value);
                    break;
                }
                current = current.left;
            } else {
                if (current.right == null) {
                    current.right = new Node(value);
                    break;
                }
                current = current.right;
            }
        }
    }

    public boolean search(int value) {
        Node current = root;
        while (current != null) {
            if (value == current.value) return true;
            if (value < current.value) current = current.left;
            else current = current.right;
        }
        return false;
    }

    public void inOrder(Node node, List<Integer> result) {
        if (node == null) return;
        inOrder(node.left, result);
        result.add(node.value);
        inOrder(node.right, result);
    }

    public boolean verifyAscending(List<Integer> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) >= list.get(i + 1)) {
                return false;
            }
        }
        return true;
    }
}

class Graph {
    Map<String, List<String>> adjList = new HashMap<>();

    public void addNode(String name) {
        if (!adjList.containsKey(name)) {
            adjList.put(name, new ArrayList<>());
        }
    }

    public void addEdge(String a, String b) {
        adjList.get(a).add(b);
        adjList.get(b).add(a);
    }

    public Map<String, Integer> bfs(String start) {
        Map<String, Integer> distances = new HashMap<>();
        MyQueue<String> queue = new MyQueue<>();
        
        distances.put(start, 0);
        queue.enqueue(start);

        while (!queue.isEmpty()) {
            String current = queue.dequeue();
            int currentDist = distances.get(current);

            for (String neighbor : adjList.get(current)) {
                if (!distances.containsKey(neighbor)) {
                    distances.put(neighbor, currentDist + 1);
                    queue.enqueue(neighbor);
                }
            }
        }
        return distances;
    }
}

class MyQueue<T> {
    private class QNode {
        T data;
        QNode next;
        QNode(T data) { this.data = data; }
    }
    private QNode head, tail;

    public void enqueue(T item) {
        QNode newNode = new QNode(item);
        if (tail != null) tail.next = newNode;
        tail = newNode;
        if (head == null) head = tail;
    }

    public T dequeue() {
        if (head == null) return null;
        T item = head.data;
        head = head.next;
        if (head == null) tail = null;
        return item;
    }

    public boolean isEmpty() {
        return head == null;
    }
}

class Pair {
    String name;
    int distance;
    Pair(String name, int distance) {
        this.name = name;
        this.distance = distance;
    }
}

interface CustomComparator {
    int compare(Pair p1, Pair p2);
}

class ComparatorTwoKey implements CustomComparator {
    public int compare(Pair p1, Pair p2) {
        if (p1.distance != p2.distance) {
            return p1.distance - p2.distance;
        }
        return p1.name.compareTo(p2.name);
    }
}

class ComparatorByName implements CustomComparator {
    public int compare(Pair p1, Pair p2) {
        return p1.name.compareTo(p2.name);
    }
}

class Sorter {
    public static void mergeSort(Pair[] arr, CustomComparator comp) {
        if (arr.length <= 1) return;
        
        int mid = arr.length / 2;
        Pair[] left = new Pair[mid];
        Pair[] right = new Pair[arr.length - mid];

        for (int i = 0; i < mid; i++) left[i] = arr[i];
        for (int i = mid; i < arr.length; i++) right[i - mid] = arr[i];

        mergeSort(left, comp);
        mergeSort(right, comp);
        merge(arr, left, right, comp);
    }

    private static void merge(Pair[] result, Pair[] left, Pair[] right, CustomComparator comp) {
        int i = 0, j = 0, k = 0;
        while (i < left.length && j < right.length) {
            if (comp.compare(left[i], right[j]) <= 0) {
                result[k++] = left[i++];
            } else {
                result[k++] = right[j++];
            }
        }
        while (i < left.length) result[k++] = left[i++];
        while (j < right.length) result[k++] = right[j++];
    }
}

class BinarySearch {
    public static Integer search(Pair[] sortedArray, String targetName) {
        int left = 0;
        int right = sortedArray.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            int cmp = sortedArray[mid].name.compareTo(targetName);

            if (cmp == 0) {
                return sortedArray[mid].distance;
            } else if (cmp < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return null;
    }
}

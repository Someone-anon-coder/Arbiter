# java_19TreesGraphsAlgorithms — BST, Graph + BFS, Merge Sort, Binary Search, Big-O

## Why in-order traversal of a BST is sorted
```java
public void inOrder(Node node, List<Integer> result) {
    if (node == null) return;
    inOrder(node.left, result);
    result.add(node.value);
    inOrder(node.right, result);
}
```
The BST rule says everything in a node's left subtree is smaller and everything in its right subtree is larger. In-order visits left, then the node, then right, so by the time a node is added, every smaller value has already been added and every larger one comes after. Applied recursively at every node, the output is ascending. Nothing is sorted at read time — the ordering was built in at insert time. The `verifyAscending` check in the submission compares adjacent elements, which proves it for the real data instead of just asserting it.

## The degenerate case
Inserting `1, 2, 3, 4, 5` in order sends every value right, producing a linked list with height n. Search and insert cost O(height), so they degrade from O(log n) to O(n). In-order output is still sorted — only speed is lost. Self-balancing trees (AVL, red-black) exist to prevent this; they are out of scope here.

## BFS and "unreachable" as absence
```java
if (!distances.containsKey(neighbor)) {
    distances.put(neighbor, currentDist + 1);
    queue.enqueue(neighbor);
}
```
The queue's FIFO order processes every distance-k node before any distance-(k+1) node, so the first time a node is discovered is via a shortest path by edge count. The `distances` map doubles as the "seen" set (which stops cycles looping forever). Unreachable nodes are never put in the map, so they have no entry at all — better than a sentinel like `-1` or `0`, which could be mistaken for a real distance.

## Merge sort and why one comparator handles two keys
Split in half, sort each half recursively, then merge by repeatedly taking the smaller front element. The comparator decides "smaller": distance first, then name only on a tie. Because the sort takes the comparator as a parameter, the same routine produced both the (distance, name) array and the name-only array. Using `<= 0` in the merge keeps equal elements in original order (stability).

## Why binary search needs the matching sort order
Binary search discards half the range based on one comparison, which is only valid if the array is ordered by the very key being compared. The (distance, name) array scatters names across distance tiers, so a mid-element's name says nothing about which half holds the target. Hence the second array sorted by name.

## Big-O summary — operations implemented in this file

| Structure / algorithm | Operation | Complexity | Why |
|---|---|---|---|
| BST | `insert` | O(h): avg O(log n), worst O(n) | One root-to-leaf path; h = n if inserted in sorted order |
| BST | `search` | O(h): avg O(log n), worst O(n) | One comparison per level, one subtree discarded each time |
| BST | in-order traversal | O(n) | Visits every node once |
| BST | `verifyAscending` | O(n) | One pass over adjacent pairs |
| Graph (adjacency list) | `addNode` / `addEdge` | O(1) average | HashMap lookup plus list append |
| Graph | BFS | O(V + E) | Each vertex enqueued/dequeued once; each edge examined once per endpoint (assumes O(1) average HashMap operations) |
| Merge sort | sort | O(n log n) | log₂ n levels of halving, O(n) merge work per level; O(n) extra space |
| Binary search | search | O(log n) | Range halves each step; requires sorted-by-key input |
| Linear search (contrast) | search | O(n) | No ordering assumption, checks one by one |

## Key takeaway
Each structure here earns its complexity from a property it maintains: the BST from its ordering rule (and loses it when unbalanced), BFS from its queue discipline, merge sort from halving plus a linear merge, binary search from sortedness on the exact key being searched.

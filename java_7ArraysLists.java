import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class java_7ArraysLists {
    public static void main(String[] args) {
        int[][] grid = new int[][] {
            {1,  2,  3,  4,  5 }, 
            {6,  7,  8,  9,  10},
            {11, 12, 13, 14, 15},
            {16, 17, 18, 19, 20},
            {21, 22, 23, 24, 25},
        };

        int n = grid.length;
        int ringCount = 0;
        
        if (n % 2 == 0) {
            ringCount = n / 2;
        } else {
            ringCount = (n + 1) / 2;
        }

        int total = 0;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                total += grid[i][j];
            }
        }

        int idx = 0;
        int grandTotal = 0;
        while(idx < ringCount) {
            int sum = 0;
            System.out.println("Ring: " + idx);
            
            for (int i = idx; i < grid.length - idx; i++) {
                for (int j = idx; j < grid[i].length - idx; j++) {
                    if ((i == idx) || (i == grid.length - (idx + 1))) {
                        System.out.println("Element: " + grid[i][j]);
                        sum += grid[i][j];
                    } else if ((j == idx) || (j == grid[i].length - (idx + 1))) {
                        System.out.println("Element: " + grid[i][j]);
                        sum += grid[i][j];
                    }
                }
            }
            System.out.println("Ring " + idx + " total: " + sum + "\n");
            idx++;
            grandTotal += sum;
        }

        System.out.println("Total by travelsal: " + total);
        System.out.println("Grand Total by rings: " + grandTotal);
        System.out.println();

        ArrayList<Integer> arrayList = new ArrayList<>(List.of(
            -25, -23, -15, -8,
            -2, 1, 1, 7,
            15, 27, 27, 39,
            41, 42, 47, 50, 
            22, 14, 14, 49
        ));

        Iterator<Integer> it = arrayList.iterator();
        while(it.hasNext()) {
            Integer element = it.next();

            if (element < 0 || element % 7 == 0) {
                it.remove();
            }
        }

        System.out.print("(Part A) Elements of arraylist: ");
        for (Integer i : arrayList) {
            System.out.print(i + " ");
        }
        System.out.println();

        LinkedList<Integer> linkedList = new LinkedList<>();
        Iterator<Integer> it2 = arrayList.iterator();
        while(it2.hasNext()) {
            Integer element = it2.next();
            
            if (element % 2 == 0) {
                linkedList.addFirst(element);
            } else {
                linkedList.addLast(element);
            }
        }

        System.out.print("(Part B) Elements of linkedlist: ");
        for (Integer i : linkedList) {
            System.out.print(i + " ");
        }
        System.out.println();

        System.out.println("(Part C) First Element: " + linkedList.getFirst() + ", Last Element: " + linkedList.getLast());
    }
}

public class java_3Methods {
    public static void main(String[] args) {
        describe(1);
        describe(2, 3);
        describe("No Value");
        describe("5 Values", 1, 2, 3, 4, 5);

        System.out.println();
        int[][] grid = {{1, 2}, {-3, 4, 5}, {6}, {7, -8, 9, 0}};
        int[] runningMax = { Integer.MIN_VALUE };

        int sum = flattenAndSum(grid, runningMax);
        System.out.println(sum);
        System.out.println(runningMax[0]);

        System.out.println();
        int[] runningMaxBroken = { Integer.MIN_VALUE };
        int sumBroken = flattenAndSumBroken(grid, runningMaxBroken);
        
        System.out.println(sumBroken);
        System.out.println(runningMaxBroken[0]);
        System.out.println("\nExplanation: Java passes object references BY VALUE. " +
                           "Reassigning the parameter reference (e.g., 'runningMax = new int[]{...}') " +
                           "only changes the local variable inside the method scope and does not " +
                           "affect the original array reference held by the caller in main. " +
                           "To mutate caller data, we must modify the contents of the existing array via index (runningMax[0] = ...).");
    }

    public static void describe(int n) {
        System.out.println("Single int: " + n);
    }

    public static void describe(int a, int b) {
        System.out.println("Two ints: " + a + " and " + b);
    }

    public static void describe(String label, int... value) {
        if (value.length == 0) {
            System.out.println(label + ": (none)");
        } else {
            System.out.print(label + ": ");
            for (int i = 0; i < value.length; i++) {
                System.out.print(value[i] + " ");
            }
            System.out.println();
        }
    }

    public static int flattenAndSum(int[][] grid, int[] runningMax) {
        return recurseRows(grid, 0, runningMax);
    }

    private static int recurseRows(int[][] grid, int r, int[] runningMax) {
        if (r >= grid.length) {
            return 0;
        }

        int currentRowSum = recurseCols(grid[r], 0, runningMax);
        int remainingRowSum = recurseRows(grid, r + 1, runningMax);

        return currentRowSum + remainingRowSum;
    }

    private static int recurseCols(int[] row, int c, int[] runningMax) {
        if (c >= row.length) {
            return 0;
        }

        int currentValue = row[c];

        if (currentValue > runningMax[0]) {
            runningMax[0] = currentValue;
        }

        return currentValue + recurseCols(row, c + 1, runningMax);
    }

    public static int flattenAndSumBroken(int[][] grid, int[] runningMax) {
        runningMax = new int[] {99999};
        return recurseRows(grid, 0, runningMax);
    }
}

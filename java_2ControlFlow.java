public class java_2ControlFlow {
    public static void main(String[] args) {
        int[] scores = {15, 27, 88, 43, 65, 32, 77, 91, 56};

        for (int i = 0; i < scores.length; i++) {
            if (scores[i] <= 100 && scores[i] >= 90) System.out.println("Score: " + scores[i] + " -> Grade: A");
            else if (scores[i] <= 89 && scores[i] >= 80) System.out.println("Score: " + scores[i] + " -> Grade: B");
            else if (scores[i] <= 79 && scores[i] >= 70) System.out.println("Score: " + scores[i] + " -> Grade: C");
            else if (scores[i] <= 69 && scores[i] >= 60) System.out.println("Score: " + scores[i] + " -> Grade: D");
            else if (scores[i] < 60) System.out.println("Score: " + scores[i] + " -> Grade: F");
            else System.out.println("Invalid Score");
        }

        System.out.println();
        int score = 15;
        // int score = 88; 
        // int score = 77;
        // int score = 93;
        // int score = 65;

        if (score <= 100 && score >= 90) System.out.println("Score: " + score + " -> Grade: A");
        else if (score <= 89 && score >= 80) System.out.println("Score: " + score + " -> Grade: B");
        else if (score <= 79 && score >= 70) System.out.println("Score: " + score + " -> Grade: C");
        else if (score <= 69 && score >= 60) System.out.println("Score: " + score + " -> Grade: D");
        else if (score < 60) System.out.println("Score: " + score + " -> Grade: F");
        else System.out.println("Invalid Score");

        System.out.println();
        int[][] rooms = {{0, 0, 1, 1}, {1, 0, 0, 1}, {1, 0, 1, 2}, {1, 0, 0, 0}};

        found:
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                switch (rooms[row][col]) {
                    case 0 -> { 
                        continue; 
                    }
                    case 1 -> { 
                        System.out.println("Obstacle at (" + row + "," + col + ")"); 
                    }
                    case 2 -> { 
                        System.out.println("\nTarget found at (" + row + "," + col + ")"); 
                        break found;
                    }
                    default -> { break; }
                }
            }
        }
        System.out.println("Scan complete");
    }
}

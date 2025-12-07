import java.util.*;

public class Solution02 {
    static List<String> grid;
    static int rows, cols;
    static Map<String, Long> memo;
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        grid = new ArrayList<>();
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            grid.add(line);
        }
        scanner.close();
        
        rows = grid.size();
        cols = grid.isEmpty() ? 0 : grid.get(0).length();
        memo = new HashMap<>();
        
        // Find starting position S
        int startRow = -1, startCol = -1;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < grid.get(r).length(); c++) {
                if (grid.get(r).charAt(c) == 'S') {
                    startRow = r;
                    startCol = c;
                    break;
                }
            }
            if (startRow != -1) break;
        }
        
        long timelines = countPaths(startRow, startCol);
        System.out.println(timelines);
    }
    
    static long countPaths(int row, int col) {
        // Base case: reached bottom of grid - this is one complete timeline
        if (row >= rows - 1) {
            return 1;
        }
        
        // Check bounds
        if (col < 0 || col >= cols) {
            return 0;
        }
        
        // Check memo
        String key = row + "," + col;
        if (memo.containsKey(key)) {
            return memo.get(key);
        }
        
        long totalPaths = 0;
        
        // Move down to next row
        int nextRow = row + 1;
        
        if (nextRow < rows && col < grid.get(nextRow).length()) {
            char cell = grid.get(nextRow).charAt(col);
            
            if (cell == '^') {
                // Hit a splitter - split into left and right paths
                totalPaths += countPaths(nextRow, col - 1); // Left
                totalPaths += countPaths(nextRow, col + 1); // Right
            } else {
                // Empty space - continue straight down
                totalPaths += countPaths(nextRow, col);
            }
        }
        
        memo.put(key, totalPaths);
        return totalPaths;
    }
}

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class Solution01 {
    static class Range {
        long start;
        long end;
        
        Range(long start, long end) {
            this.start = start;
            this.end = end;
        }
        
        boolean contains(long id) {
            return id >= start && id <= end;
        }
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Read fresh ID ranges
        List<Range> freshRanges = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) break; // Blank line separates ranges from IDs
            
            String[] parts = line.split("-");
            long start = Long.parseLong(parts[0]);
            long end = Long.parseLong(parts[1]);
            freshRanges.add(new Range(start, end));
        }
        
        // Read available ingredient IDs and count fresh ones
        int freshCount = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            
            long id = Long.parseLong(line);
            
            // Check if this ID is in any fresh range
            boolean isFresh = false;
            for (Range range : freshRanges) {
                if (range.contains(id)) {
                    isFresh = true;
                    break;
                }
            }
            
            if (isFresh) {
                freshCount++;
            }
        }
        
        scanner.close();
        System.out.println(freshCount);
    }
}

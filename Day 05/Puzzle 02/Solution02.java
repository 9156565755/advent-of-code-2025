import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Solution02 {
    static class Range implements Comparable<Range> {
        long start;
        long end;
        
        Range(long start, long end) {
            this.start = start;
            this.end = end;
        }
        
        @Override
        public int compareTo(Range other) {
            return Long.compare(this.start, other.start);
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
        
        scanner.close();
        
        // Merge overlapping ranges and count total IDs
        long totalFreshIds = countFreshIds(freshRanges);
        System.out.println(totalFreshIds);
    }
    
    private static long countFreshIds(List<Range> ranges) {
        if (ranges.isEmpty()) return 0;
        
        // Sort ranges by start position
        Collections.sort(ranges);
        
        // Merge overlapping ranges
        List<Range> merged = new ArrayList<>();
        Range current = ranges.get(0);
        
        for (int i = 1; i < ranges.size(); i++) {
            Range next = ranges.get(i);
            
            // Check if ranges overlap or are adjacent
            if (next.start <= current.end + 1) {
                // Merge: extend current range to include next range
                current.end = Math.max(current.end, next.end);
            } else {
                // No overlap: save current and start new range
                merged.add(current);
                current = next;
            }
        }
        // Don't forget to add the last range
        merged.add(current);
        
        // Count total IDs in all merged ranges
        long count = 0;
        for (Range range : merged) {
            count += (range.end - range.start + 1);
        }
        
        return count;
    }
}

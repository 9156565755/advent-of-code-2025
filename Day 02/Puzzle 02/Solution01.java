import java.util.Scanner;

public class Solution01 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Read the entire input (could be multiple lines)
        StringBuilder input = new StringBuilder();
        while (scanner.hasNextLine()) {
            input.append(scanner.nextLine().trim());
        }
        scanner.close();
        
        // Parse ranges separated by commas
        String[] ranges = input.toString().split(",");
        
        long totalSum = 0;
        
        for (String range : ranges) {
            range = range.trim();
            if (range.isEmpty()) continue;
            
            // Parse start and end of range
            String[] parts = range.split("-");
            long start = Long.parseLong(parts[0]);
            long end = Long.parseLong(parts[1]);
            
            // Check each ID in the range
            for (long id = start; id <= end; id++) {
                if (isInvalid(id)) {
                    totalSum += id;
                }
            }
        }
        
        System.out.println(totalSum);
    }
    
    // Check if an ID is invalid (repeated pattern)
    private static boolean isInvalid(long id) {
        String str = String.valueOf(id);
        int len = str.length();
        
        // Must have even length to be repeated twice
        if (len % 2 != 0) {
            return false;
        }
        
        // Check if first half equals second half
        int half = len / 2;
        String firstHalf = str.substring(0, half);
        String secondHalf = str.substring(half);
        
        return firstHalf.equals(secondHalf);
    }
}

import java.util.Scanner;

public class Solution01 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        long totalJoltage = 0;
        
        while (scanner.hasNextLine()) {
            String bank = scanner.nextLine().trim();
            if (bank.isEmpty()) continue;
            
            int maxJoltage = findMaxJoltage(bank);
            totalJoltage += maxJoltage;
        }
        
        scanner.close();
        System.out.println(totalJoltage);
    }
    
    // Find the maximum joltage for a single bank
    private static int findMaxJoltage(String bank) {
        int maxJoltage = 0;
        int len = bank.length();
        
        // Try all pairs of positions (i, j) where i < j
        for (int i = 0; i < len - 1; i++) {
            for (int j = i + 1; j < len; j++) {
                // Form the two-digit number from positions i and j
                int digit1 = bank.charAt(i) - '0';
                int digit2 = bank.charAt(j) - '0';
                int joltage = digit1 * 10 + digit2;
                
                maxJoltage = Math.max(maxJoltage, joltage);
            }
        }
        
        return maxJoltage;
    }
}

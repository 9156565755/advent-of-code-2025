import java.util.Scanner;

public class SafeCracker {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        int currentPosition = 50;
        int zeroCount = 0;
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            
            // Skip empty lines
            if (line.isEmpty()) {
                continue;
            }
            
            // Parse direction and distance
            char direction = line.charAt(0);
            int distance = Integer.parseInt(line.substring(1));
            
            // Rotate the dial
            if (direction == 'L') {
                currentPosition = (currentPosition - distance) % 100;
            } else if (direction == 'R') {
                currentPosition = (currentPosition + distance) % 100;
            }
            
            // Handle negative modulo in Java
            if (currentPosition < 0) {
                currentPosition += 100;
            }
            
            // Count if we land on 0
            if (currentPosition == 0) {
                zeroCount++;
            }
        }
        
        scanner.close();
        System.out.println(zeroCount);
    }
}

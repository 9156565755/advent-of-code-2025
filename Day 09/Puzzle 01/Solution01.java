import java.util.*;

public class Solution01 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<int[]> redTiles = new ArrayList<>();
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            
            String[] parts = line.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            redTiles.add(new int[]{x, y});
        }
        scanner.close();
        
        long maxArea = 0;
        
        // Try all pairs of red tiles as opposite corners
        for (int i = 0; i < redTiles.size(); i++) {
            for (int j = i + 1; j < redTiles.size(); j++) {
                int[] tile1 = redTiles.get(i);
                int[] tile2 = redTiles.get(j);
                
                // Calculate rectangle dimensions (inclusive of corner tiles)
                long width = Math.abs(tile2[0] - tile1[0]) + 1;
                long height = Math.abs(tile2[1] - tile1[1]) + 1;
                long area = width * height;
                
                maxArea = Math.max(maxArea, area);
            }
        }
        
        System.out.println(maxArea);
    }
}

import java.util.*;

public class Solution01 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int totalPresses = 0;
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            
            Machine machine = parseMachine(line);
            int minPresses = findMinPresses(machine);
            totalPresses += minPresses;
        }
        scanner.close();
        
        System.out.println(totalPresses);
    }
    
    static class Machine {
        boolean[] target;
        List<Set<Integer>> buttons;
        
        Machine(boolean[] target, List<Set<Integer>> buttons) {
            this.target = target;
            this.buttons = buttons;
        }
    }
    
    static Machine parseMachine(String line) {
        // Parse target configuration [.##.]
        int start = line.indexOf('[');
        int end = line.indexOf(']');
        String targetStr = line.substring(start + 1, end);
        boolean[] target = new boolean[targetStr.length()];
        for (int i = 0; i < targetStr.length(); i++) {
            target[i] = targetStr.charAt(i) == '#';
        }
        
        // Parse buttons (0,3,4)
        List<Set<Integer>> buttons = new ArrayList<>();
        int pos = end + 1;
        while (pos < line.length()) {
            int openParen = line.indexOf('(', pos);
            if (openParen == -1) break;
            int closeParen = line.indexOf(')', openParen);
            
            String buttonStr = line.substring(openParen + 1, closeParen);
            Set<Integer> button = new HashSet<>();
            for (String num : buttonStr.split(",")) {
                button.add(Integer.parseInt(num.trim()));
            }
            buttons.add(button);
            pos = closeParen + 1;
        }
        
        return new Machine(target, buttons);
    }
    
    static int findMinPresses(Machine machine) {
        int numLights = machine.target.length;
        int numButtons = machine.buttons.size();
        
        // Try all possible combinations of button presses (2^numButtons possibilities)
        int minPresses = Integer.MAX_VALUE;
        
        for (int mask = 0; mask < (1 << numButtons); mask++) {
            boolean[] state = new boolean[numLights];
            int presses = 0;
            
            // Apply each button if its bit is set in mask
            for (int i = 0; i < numButtons; i++) {
                if ((mask & (1 << i)) != 0) {
                    presses++;
                    // Toggle lights affected by this button
                    for (int light : machine.buttons.get(i)) {
                        if (light < numLights) {
                            state[light] = !state[light];
                        }
                    }
                }
            }
            
            // Check if this matches target
            if (Arrays.equals(state, machine.target)) {
                minPresses = Math.min(minPresses, presses);
            }
        }
        
        return minPresses;
    }
}

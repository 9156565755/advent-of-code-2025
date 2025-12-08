import java.util.*;

public class Solution01 {
    static class Point {
        int x, y, z;
        
        Point(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    
    static class Edge implements Comparable<Edge> {
        int u, v;
        double distance;
        
        Edge(int u, int v, double distance) {
            this.u = u;
            this.v = v;
            this.distance = distance;
        }
        
        @Override
        public int compareTo(Edge other) {
            return Double.compare(this.distance, other.distance);
        }
    }
    
    static class UnionFind {
        int[] parent;
        int[] size;
        
        UnionFind(int n) {
            parent = new int[n];
            size = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                size[i] = 1;
            }
        }
        
        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // Path compression
            }
            return parent[x];
        }
        
        boolean union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            
            if (rootX == rootY) return false; // Already in same circuit
            
            // Union by size
            if (size[rootX] < size[rootY]) {
                parent[rootX] = rootY;
                size[rootY] += size[rootX];
            } else {
                parent[rootY] = rootX;
                size[rootX] += size[rootY];
            }
            return true;
        }
        
        int getSize(int x) {
            return size[find(x)];
        }
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Point> points = new ArrayList<>();
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            
            String[] parts = line.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            points.add(new Point(x, y, z));
        }
        scanner.close();
        
        int n = points.size();
        
        // Calculate all pairwise distances
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double dist = distance(points.get(i), points.get(j));
                edges.add(new Edge(i, j, dist));
            }
        }
        
        // Sort edges by distance
        Collections.sort(edges);
        
        // Process the 1000 shortest edges (not 1000 successful connections)
        UnionFind uf = new UnionFind(n);
        
        for (int i = 0; i < Math.min(1000, edges.size()); i++) {
            Edge edge = edges.get(i);
            uf.union(edge.u, edge.v); // Try to connect, may or may not succeed
        }
        
        // Find all circuit sizes
        Map<Integer, Integer> circuitSizes = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = uf.find(i);
            circuitSizes.put(root, uf.getSize(root));
        }
        
        // Get the three largest circuit sizes
        List<Integer> sizes = new ArrayList<>(circuitSizes.values());
        Collections.sort(sizes, Collections.reverseOrder());
        
        // Multiply the three largest
        long result = 1;
        for (int i = 0; i < Math.min(3, sizes.size()); i++) {
            result *= sizes.get(i);
        }
        
        System.out.println(result);
    }
    
    static double distance(Point a, Point b) {
        long dx = a.x - b.x;
        long dy = a.y - b.y;
        long dz = a.z - b.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}

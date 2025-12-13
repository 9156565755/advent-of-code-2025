import java.io.*;
import java.util.*;

/**
 * Day 12 - Part 1: Count how many regions can fit the required presents.
 *
 * Approach:
 *  - Parse shapes (polyominoes in #/. grids) and region queries.
 *  - Precompute unique orientations (rotations + optional flip).
 *  - For each region: generate all placements for each used shape as bitmasks (long[] blocks).
 *  - Backtracking: place pieces one by one (most constrained first), with:
 *      * overlap test via bitwise AND
 *      * area pruning (remaining area must fit in remaining free cells)
 *      * memoization keyed by (idx, hash) using an incremental XOR hash
 *      * symmetry reduction for identical pieces (enforce nondecreasing placement indices)
 */
public class Solution01 {

    // ---------- Data structures ----------

    static final class Shape {
        final int index;
        final List<Orientation> orientations;
        final int area;
        Shape(int index, List<Orientation> orientations, int area) {
            this.index = index;
            this.orientations = orientations;
            this.area = area;
        }
    }

    static final class Orientation {
        final int w, h;
        final int[] cells; // (x,y) pairs packed as y*w + x in this orientation's own bbox
        Orientation(int w, int h, int[] cells) {
            this.w = w;
            this.h = h;
            this.cells = cells;
        }
    }

    static final class Placement {
        final long[] bits;  // occupancy bits in the region
        final long hash;    // XOR hash contribution of occupied cells
        final int area;     // number of occupied cells
        Placement(long[] bits, long hash, int area) {
            this.bits = bits;
            this.hash = hash;
            this.area = area;
        }
    }

    static final class PieceType {
        final int area;
        final List<Placement> placements;
        PieceType(int area, List<Placement> placements) {
            this.area = area;
            this.placements = placements;
        }
    }

    static final class RegionQuery {
        final int W, H;
        final int[] counts;
        RegionQuery(int w, int h, int[] counts) {
            this.W = w; this.H = h; this.counts = counts;
        }
    }

    // ---------- Parsing ----------

    public static void main(String[] args) throws Exception {
        List<String> lines = readAllLines();

        // Parse shapes until the first "WxH:" line.
        Map<Integer, List<String>> shapeGrids = new HashMap<>();
        List<RegionQuery> queries = new ArrayList<>();

        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) { i++; continue; }
            if (isRegionLine(line)) break;

            if (line.matches("\\d+:")) {
                int idx = Integer.parseInt(line.substring(0, line.length() - 1));
                i++;
                List<String> grid = new ArrayList<>();
                while (i < lines.size()) {
                    String r = lines.get(i).trim();
                    if (r.isEmpty()) break;
                    if (isRegionLine(r) || r.matches("\\d+:")) break;
                    grid.add(r);
                    i++;
                }
                shapeGrids.put(idx, grid);
            } else {
                i++; // skip anything unexpected
            }
        }

        // Parse region queries.
        while (i < lines.size()) {
            String line = lines.get(i).trim();
            i++;
            if (line.isEmpty()) continue;
            if (!isRegionLine(line)) continue;

            String[] parts = line.split(":");
            String[] wh = parts[0].trim().split("x");
            int W = Integer.parseInt(wh[0]);
            int H = Integer.parseInt(wh[1]);

            String rhs = parts.length > 1 ? parts[1].trim() : "";
            int[] counts = rhs.isEmpty() ? new int[0] : parseIntList(rhs);

            queries.add(new RegionQuery(W, H, counts));
        }

        // Build shapes array by max index
        int maxIdx = shapeGrids.keySet().stream().mapToInt(x -> x).max().orElse(-1);
        Shape[] shapes = new Shape[maxIdx + 1];
        for (int idx = 0; idx <= maxIdx; idx++) {
            List<String> grid = shapeGrids.get(idx);
            if (grid == null) continue;
            shapes[idx] = buildShape(idx, grid);
        }

        int ok = 0;
        for (RegionQuery q : queries) {
            if (canFitRegion(q, shapes)) ok++;
        }
        System.out.println(ok);
    }

    private static List<String> readAllLines() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        List<String> out = new ArrayList<>();
        for (String s; (s = br.readLine()) != null; ) out.add(s);
        return out;
    }

    private static boolean isRegionLine(String s) {
        return s.matches("\\d+x\\d+\\s*:.*") || s.matches("\\d+x\\d+\\s*:");
    }

    private static int[] parseIntList(String s) {
        String[] parts = s.trim().split("\\s+");
        int[] a = new int[parts.length];
        for (int i = 0; i < parts.length; i++) a[i] = Integer.parseInt(parts[i]);
        return a;
    }

    // ---------- Shape orientation generation ----------

    private static Shape buildShape(int index, List<String> grid) {
        List<int[]> cells = new ArrayList<>();
        int h = grid.size();
        int w = grid.stream().mapToInt(String::length).max().orElse(0);

        for (int y = 0; y < h; y++) {
            String row = grid.get(y);
            for (int x = 0; x < row.length(); x++) {
                if (row.charAt(x) == '#') cells.add(new int[]{x, y});
            }
        }

        int area = cells.size();

        // Generate 8 transforms (4 rotations * optional reflection), keep uniques.
        Map<String, Orientation> uniq = new LinkedHashMap<>();
        for (int reflect = 0; reflect <= 1; reflect++) {
            for (int rot = 0; rot < 4; rot++) {
                int[] xs = new int[area];
                int[] ys = new int[area];

                for (int i = 0; i < area; i++) {
                    int x = cells.get(i)[0];
                    int y = cells.get(i)[1];

                    if (reflect == 1) x = -x;

                    // rotate around origin (0,0)
                    int rx = x, ry = y;
                    for (int r = 0; r < rot; r++) {
                        int nx = -ry;
                        int ny = rx;
                        rx = nx; ry = ny;
                    }

                    xs[i] = rx;
                    ys[i] = ry;
                }

                // normalize to minX=minY=0
                int minX = Arrays.stream(xs).min().orElse(0);
                int minY = Arrays.stream(ys).min().orElse(0);
                int maxX = Arrays.stream(xs).max().orElse(0);
                int maxY = Arrays.stream(ys).max().orElse(0);

                for (int i = 0; i < area; i++) {
                    xs[i] -= minX;
                    ys[i] -= minY;
                }

                int ow = maxX - minX + 1;
                int oh = maxY - minY + 1;

                int[] packed = new int[area];
                for (int i = 0; i < area; i++) packed[i] = ys[i] * ow + xs[i];
                Arrays.sort(packed);

                String key = ow + "x" + oh + ":" + Arrays.toString(packed);
                uniq.putIfAbsent(key, new Orientation(ow, oh, packed));
            }
        }

        return new Shape(index, new ArrayList<>(uniq.values()), area);
    }

    // ---------- Region solving ----------

    private static boolean canFitRegion(RegionQuery q, Shape[] shapes) {
        int W = q.W, H = q.H;
        int totalCells = W * H;
        int blocks = (totalCells + 63) >>> 6;

        // Random per-cell values for incremental XOR hashing (Zobrist-style). :contentReference[oaicite:1]{index=1}
        long[] cellRand = new long[totalCells];
        SplittableRandom rnd = new SplittableRandom(0xC0FFEE);
        for (int i = 0; i < totalCells; i++) cellRand[i] = rnd.nextLong();

        // Build PieceTypes for shapes used in this query, generate placements in this region.
        int shapeCount = shapes.length;
        int[] counts = q.counts;
        if (counts.length < shapeCount) counts = Arrays.copyOf(counts, shapeCount);

        Map<Integer, PieceType> typeByShape = new HashMap<>();
        List<PieceType> pieces = new ArrayList<>();
        int requiredArea = 0;

        for (int si = 0; si < shapeCount; si++) {
            int c = counts[si];
            if (c <= 0) continue;
            Shape s = shapes[si];
            if (s == null) continue;

            PieceType type = typeByShape.computeIfAbsent(si, k -> new PieceType(s.area, genPlacements(s, W, H, blocks, cellRand)));
            if (type.placements.isEmpty()) return false; // impossible: a required piece has no placement

            for (int t = 0; t < c; t++) pieces.add(type);
            requiredArea += c * s.area;
        }

        if (requiredArea > totalCells) return false; // basic area check

        // Sort pieces: most constrained first.
        pieces.sort((a, b) -> {
            int cmp = Integer.compare(a.placements.size(), b.placements.size());
            if (cmp != 0) return cmp;
            return Integer.compare(b.area, a.area);
        });

        // Suffix area for pruning.
        int n = pieces.size();
        int[] suffixArea = new int[n + 1];
        for (int i = n - 1; i >= 0; i--) suffixArea[i] = suffixArea[i + 1] + pieces.get(i).area;

        // Memo: failed hashes per depth.
        @SuppressWarnings("unchecked")
        HashSet<Long>[] failed = new HashSet[n + 1];
        for (int i2 = 0; i2 <= n; i2++) failed[i2] = new HashSet<>();

        long[] occ = new long[blocks];
        int[] chosenPlacementIndex = new int[n]; // for identical-piece symmetry reduction

        return dfs(0, 0L, 0, pieces, occ, suffixArea, totalCells, failed, chosenPlacementIndex);
    }

    private static List<Placement> genPlacements(Shape s, int W, int H, int blocks, long[] cellRand) {
        int totalCells = W * H;
        List<Placement> out = new ArrayList<>();

        for (Orientation o : s.orientations) {
            if (o.w > W || o.h > H) continue;

            for (int y0 = 0; y0 <= H - o.h; y0++) {
                for (int x0 = 0; x0 <= W - o.w; x0++) {
                    long[] bits = new long[blocks];
                    long h = 0L;

                    for (int cell : o.cells) {
                        int oy = cell / o.w;
                        int ox = cell % o.w;

                        int x = x0 + ox;
                        int y = y0 + oy;
                        int idx = y * W + x;

                        if (idx < 0 || idx >= totalCells) throw new AssertionError();

                        bits[idx >>> 6] |= 1L << (idx & 63);
                        h ^= cellRand[idx];
                    }

                    out.add(new Placement(bits, h, s.area));
                }
            }
        }

        return out;
    }

    private static boolean fits(long[] occ, long[] placeBits) {
        for (int i = 0; i < occ.length; i++) {
            if ((occ[i] & placeBits[i]) != 0L) return false;
        }
        return true;
    }

    private static void applyXor(long[] occ, long[] placeBits) {
        for (int i = 0; i < occ.length; i++) occ[i] ^= placeBits[i];
    }

    private static boolean dfs(
            int idx,
            long hash,
            int usedCells,
            List<PieceType> pieces,
            long[] occ,
            int[] suffixArea,
            int totalCells,
            HashSet<Long>[] failed,
            int[] chosenPlacementIndex
    ) {
        if (idx == pieces.size()) return true;

        // If we've already proven this (idx, hash) fails, skip.
        if (failed[idx].contains(hash)) return false;

        // Area pruning: remaining pieces must fit in remaining free cells.
        int remainingFree = totalCells - usedCells;
        if (suffixArea[idx] > remainingFree) {
            failed[idx].add(hash);
            return false;
        }

        PieceType cur = pieces.get(idx);

        // Symmetry reduction for identical consecutive pieces:
        // enforce nondecreasing placement indices within a run of identical PieceTypes.
        int start = 0;
        if (idx > 0 && pieces.get(idx) == pieces.get(idx - 1)) {
            start = chosenPlacementIndex[idx - 1];
        }

        List<Placement> pls = cur.placements;
        for (int p = start; p < pls.size(); p++) {
            Placement pl = pls.get(p);
            if (!fits(occ, pl.bits)) continue;

            // place
            applyXor(occ, pl.bits);
            chosenPlacementIndex[idx] = p;

            if (dfs(idx + 1, hash ^ pl.hash, usedCells + pl.area, pieces, occ, suffixArea, totalCells, failed, chosenPlacementIndex)) {
                return true;
            }

            // unplace
            applyXor(occ, pl.bits);
        }

        failed[idx].add(hash);
        return false;
    }
}

package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.math.Vector2;
import java.util.*;

public class AStarPathFinder {

    private Node[][] grid;
    private int width;
    private int height;

    private float cellSize = 32f;

    public AStarPathFinder(int[][] collisionMap) {
        buildGrid(collisionMap);
    }

    private void buildGrid(int[][] collisionMap) {
        height = collisionMap.length;
        width = collisionMap[0].length;
        grid = new Node[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean walkable = collisionMap[y][x] == 0;

                Vector2 pos = new Vector2(
                        x * cellSize + cellSize * 0.5f,
                        y * cellSize + cellSize * 0.5f
                );

                grid[y][x] = new Node(x, y, pos, walkable);
            }
        }
    }

    private void clearNodes() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Node n = grid[y][x];
                n.gCost = Float.MAX_VALUE;
                n.hCost = 0f;
                n.fCost = 0f;
                n.parent = null;
            }
        }
    }

    public List<Vector2> findPath(Vector2 startPos, Vector2 targetPos) {
        clearNodes();

        Node start = worldToNode(startPos);
        Node target = worldToNode(targetPos);

        if (start == null || target == null) return Collections.emptyList();
        if (!start.walkable || !target.walkable) return Collections.emptyList();

        PriorityQueue<Node> open = new PriorityQueue<>();
        Set<Node> closed = new HashSet<>();

        start.gCost = 0f;
        open.add(start);

        int safetyCounter = 0;

        while (!open.isEmpty()) {
            if (++safetyCounter > 1000) {
                return Collections.emptyList();
            }

            Node current = open.poll();

            if (current == target) {
                return buildPath(current);
            }

            closed.add(current);

            for (Node next : getNeighbors(current)) {
                if (!next.walkable || closed.contains(next)) continue;

                float newCost = current.gCost + distance(current, next);

                if (newCost < next.gCost) {
                    next.gCost = newCost;
                    next.hCost = distance(next, target);
                    next.fCost = next.gCost + next.hCost;
                    next.parent = current;

                    if (!open.contains(next)) {
                        open.add(next);
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    private List<Node> getNeighbors(Node node) {
        List<Node> result = new ArrayList<>(4);

        int x = node.gridX;
        int y = node.gridY;

        if (y + 1 < height) result.add(grid[y + 1][x]);
        if (x + 1 < width)  result.add(grid[y][x + 1]);
        if (y - 1 >= 0)     result.add(grid[y - 1][x]);
        if (x - 1 >= 0)     result.add(grid[y][x - 1]);

        return result;
    }

    private float distance(Node a, Node b) {
        return Math.abs(a.gridX - b.gridX) + Math.abs(a.gridY - b.gridY);
    }

    private List<Vector2> buildPath(Node end) {
        List<Vector2> path = new ArrayList<>();
        Node cur = end;

        while (cur != null) {
            path.add(cur.worldPosition);
            cur = cur.parent;
        }

        Collections.reverse(path);

        if (!path.isEmpty()) {
            path.remove(0); // remove start position
        }

        return path;
    }

    private Node worldToNode(Vector2 pos) {
        int x = (int) (pos.x / cellSize);
        int y = (int) (pos.y / cellSize);

        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        }

        return grid[y][x];
    }

    public void setWalkable(int x, int y, boolean walkable) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        grid[y][x].walkable = walkable;
    }

    private static class Node implements Comparable<Node> {
        int gridX, gridY;
        Vector2 worldPosition;
        boolean walkable;

        float gCost = Float.MAX_VALUE;
        float hCost;
        float fCost;

        Node parent;

        Node(int x, int y, Vector2 pos, boolean walkable) {
            this.gridX = x;
            this.gridY = y;
            this.worldPosition = pos;
            this.walkable = walkable;
        }

        @Override
        public int compareTo(Node o) {
            return Float.compare(this.fCost, o.fCost);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Node)) return false;
            Node other = (Node) obj;
            return gridX == other.gridX && gridY == other.gridY;
        }

        @Override
        public int hashCode() {
            return gridX * 31 + gridY;
        }
    }
}

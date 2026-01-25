package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.math.Vector2;
import java.util.*;

public class AStarPathFinder {
    private Node[][] grid;
    private int gridWidth;
    private int gridHeight;
    private float cellSize = 32f;

    public AStarPathFinder(int[][] collisionMap) {
        initializeGrid(collisionMap);
    }

    private void initializeGrid(int[][] collisionMap) {
        gridHeight = collisionMap.length;
        gridWidth = collisionMap[0].length;
        grid = new Node[gridHeight][gridWidth];

        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                boolean walkable = collisionMap[y][x] == 0;

                Vector2 worldPos = new Vector2(x * cellSize + cellSize/2, y * cellSize + cellSize/2);
                grid[y][x] = new Node(x, y, worldPos, walkable);
            }
        }
    }

    private void resetNodes() {
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                Node node = grid[y][x];
                node.gCost = Float.MAX_VALUE;
                node.hCost = 0;
                node.fCost = 0;
                node.parent = null;
            }
        }
    }

    public List<Vector2> findPath(Vector2 startWorldPos, Vector2 targetWorldPos) {
        resetNodes();

        Node startNode = worldToNode(startWorldPos);
        Node targetNode = worldToNode(targetWorldPos);

        if (startNode == null || targetNode == null || !startNode.walkable || !targetNode.walkable) {
            return new ArrayList<>();
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();

        startNode.gCost = 0;
        startNode.hCost = 0;

        openSet.add(startNode);

        int loopCount = 0;
        int maxSteps =1000;

        while (!openSet.isEmpty()) {
            loopCount++;
            if (loopCount > maxSteps) {
                return new ArrayList<>();
            }

            Node currentNode = openSet.poll();

            if (currentNode.equals(targetNode)) {
                return reconstructPath(currentNode);
            }

            closedSet.add(currentNode);

            for (Node neighbor : getNeighbors(currentNode)) {
                if (!neighbor.walkable || closedSet.contains(neighbor)) {
                    continue;
                }

                float newCostToNeighbor = currentNode.gCost + getDistance(currentNode, neighbor);

                if (newCostToNeighbor < neighbor.gCost) {
                    neighbor.gCost = newCostToNeighbor;
                    neighbor.hCost = getDistance(neighbor, targetNode);
                    neighbor.fCost = neighbor.gCost + neighbor.hCost;
                    neighbor.parent = currentNode;

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        int[][] directions = {{0,1}, {1,0}, {0,-1}, {-1,0}};

        for (int[] dir : directions) {
            int checkX = node.gridX + dir[0];
            int checkY = node.gridY + dir[1];

            if (checkX >= 0 && checkX < gridWidth && checkY >= 0 && checkY < gridHeight) {
                neighbors.add(grid[checkY][checkX]);
            }
        }

        return neighbors;
    }

    private float getDistance(Node a, Node b) {
        float dstX = Math.abs(a.gridX - b.gridX);
        float dstY = Math.abs(a.gridY - b.gridY);
        return dstX + dstY;
    }

    private List<Vector2> reconstructPath(Node endNode) {
        List<Vector2> path = new ArrayList<>();
        Node currentNode = endNode;

        while (currentNode != null) {
            path.add(0, currentNode.worldPosition);
            currentNode = currentNode.parent;
        }

        if (!path.isEmpty()) {
            path.remove(0);
        }

        return path;
    }

    private Node worldToNode(Vector2 worldPos) {
        int gridX = (int)(worldPos.x / cellSize);
        int gridY = (int)(worldPos.y / cellSize);

        if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
            return grid[gridY][gridX];
        }
        return null;
    }

    public void updateWalkable(int gridX, int gridY, boolean walkable) {
        if (gridY >= 0 && gridY < gridHeight && gridX >= 0 && gridX < gridWidth) {
            grid[gridY][gridX].walkable = walkable;
        }
    }

    private static class Node implements Comparable<Node> {
        int gridX, gridY;
        Vector2 worldPosition;
        boolean walkable;
        float gCost = Float.MAX_VALUE;
        float hCost;
        float fCost;
        Node parent;

        Node(int gridX, int gridY, Vector2 worldPosition, boolean walkable) {
            this.gridX = gridX;
            this.gridY = gridY;
            this.worldPosition = worldPosition;
            this.walkable = walkable;
        }

        @Override
        public int compareTo(Node other) {
            return Float.compare(this.fCost, other.fCost);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return gridX == node.gridX && gridY == node.gridY;
        }

        @Override
        public int hashCode() {
            return 31 * gridX + gridY;
        }
    }
}
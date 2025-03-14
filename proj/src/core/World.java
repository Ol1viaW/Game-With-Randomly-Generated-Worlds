package core;

import org.checkerframework.checker.units.qual.A;
import tileengine.TETile;
import tileengine.Tileset;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World {
    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);
    private static final int WIDTH = 70;
    private static final int HEIGHT = 30;

    public static class Point{
        private int x;
        private int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public double distance(Point other) {
            int dx = this.x - other.getX();
            int dy = this.y - other.getY();
            return (Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)));
        }
    }

    private int numberOfRooms;

    // A list that stores all the bottom left corners of the rooms.
    private List<Point> roomBottomLeftCorners = new ArrayList<>();

    // A list that stores all the widths of rooms.
    private List<Integer> roomWidths = new ArrayList<>();

    // A list that stores all the heights of rooms.
    private List<Integer> roomHeights = new ArrayList<>();

    /**
     * Randomly generates the number of the rooms.
     * We set the minimal and maximal number of the rooms to be 8 and 12.
     */
    public void generateNumberOfRooms() {
        final int minRoom = 12;
        final int maxRoom = 15;
        this.numberOfRooms = minRoom + RANDOM.nextInt(maxRoom - minRoom + 1);
    }

    /**
     * Randomly generates the rooms based on the number of rooms we generated earlier.
     */
    public void generateRooms() {
        int currentNumberOfRooms = 0;
        int minLengthOfSide = 3;
        int maxLengthOfSide = 7;
        int minX = 1; // leave some spaces for walls
        int maxX = WIDTH - 1 - maxLengthOfSide; // leave some spaces for wall & locate within the window
        int minY = 1;
        int maxY = HEIGHT - 1 - maxLengthOfSide;
        while (currentNumberOfRooms < numberOfRooms) {
            int bottomLeftCornerX = minX + RANDOM.nextInt(maxX - minX + 1);
            int bottomLeftCornerY = minY + RANDOM.nextInt(maxY - minY + 1);
            int roomWidth = minLengthOfSide + RANDOM.nextInt(maxLengthOfSide - minLengthOfSide + 1);
            int roomHeight = minLengthOfSide + RANDOM.nextInt(maxLengthOfSide - minLengthOfSide + 1);
            Point bottomLeftCorner = new Point(bottomLeftCornerX, bottomLeftCornerY);
            roomBottomLeftCorners.add(bottomLeftCorner);
            roomWidths.add(roomWidth);
            roomHeights.add(roomHeight);
            currentNumberOfRooms++;
        }
    }

    /**
     * Build rooms based on roomLeftCorners, roomWidths, roomHeights we got earlier.
     * @param tiles
     */
    public void buildRooms(TETile[][] tiles) {
        for (int i = 0; i < numberOfRooms; i++) {
            int BottomLeftX = roomBottomLeftCorners.get(i).getX();
            int BottomLeftY = roomBottomLeftCorners.get(i).getY();
            int width = roomWidths.get(i);
            int height = roomHeights.get(i);
            for (int x = BottomLeftX; x < BottomLeftX + width; x++) {
                for (int y = BottomLeftY; y < BottomLeftY + height; y++) {
                    tiles[x][y] = Tileset.FLOOR;
                }
            }
        }
    }

    /**
     * Build hallways in the order of room generation [need to be optimized]
     * @param tiles
     */
    public void buildHallways(TETile[][] tiles) {
        for (int i = 0; i < numberOfRooms - 1; i++) {
            int x_1 = roomBottomLeftCorners.get(i).getX();
            int y_1 = roomBottomLeftCorners.get(i).getY();
            int x_2 = roomBottomLeftCorners.get(i + 1).getX();
            int y_2 = roomBottomLeftCorners.get(i + 1).getY();
            int xMin = Math.min(x_1, x_2);
            int xMax = Math.max(x_1, x_2);
            int yMin = Math.min(y_1, y_2);
            int yMax = Math.max(y_1, y_2);
            if ((x_1 <= x_2 && y_1 <= y_2) || (x_1 >= x_2 && y_1 >= y_2)) {
                for (int x = xMin; x <= xMax; x++) {
                    tiles[x][yMin] = Tileset.FLOOR;
                }
                for (int y = yMin; y <= yMax; y++) {
                    tiles[xMax][y] = Tileset.FLOOR;
                }
            } else {
                for (int x = xMax; x >= xMin; x--) {
                    tiles[x][yMin] = Tileset.FLOOR;
                }
                for (int y = yMin; y <= yMax; y++) {
                    tiles[xMin][y] = Tileset.FLOOR;
                }
            }
        }
    }

    /**
     * Build walls by checking nearby tiles.
     * @param tiles
     */
    public void buildWalls(TETile[][] tiles) {
        int[] dx = {-1, 1, 0, 0, -1, -1, 1, 1};
        int[] dy = {0, 0, -1, 1, -1, 1, -1, 1};
        final int numberOfDirections = 8;
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (tiles[x][y] == Tileset.NOTHING) {
                    for (int i = 0; i < numberOfDirections; i++) {
                        if (x + dx[i] >= 0 && x + dx[i] < WIDTH && y + dy[i] >= 0 && y + dy[i] < HEIGHT) {
                            if (tiles[x + dx[i]][y + dy[i]] == Tileset.FLOOR) {
                                tiles[x][y] = Tileset.WALL;
                            }
                        }
                    }
                }
            }
        }
    }
}

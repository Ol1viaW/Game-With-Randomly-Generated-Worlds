// @source I used the lecture slides, the project intro videos to help me understand the tasks
// @ source I used the algorithms we discussed in class to figure out how to traverse and connect all the hallways
// @ source I used https://www.geeksforgeeks.org/generating-random-numbers-in-java/ to look over how random number generating
// in java works
// @ source I used chatgpt to figure out how to work with large seed numbers (adding the L)

package core;

import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter;
import org.checkerframework.checker.units.qual.A;
import tileengine.TETile;
import tileengine.Tileset;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World2 {

    public static long seed;
    private static Random RANDOM;
    private static final int WIDTH = 70;
    private static final int HEIGHT = 30;
    private int avatarX;
    private int avatarY;

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

    public World2(long seed) {
        World2.seed = seed;
        RANDOM = new Random(seed);
        //System.out.println(seed);
    }
    /**
     * Randomly generates the number of the rooms.
     * We set the minimal and maximal number of the rooms to be 8 and 12.
     */
    public void generateNumberOfRooms() {
        final int minRoom = 15;
        final int maxRoom = 18;
        this.numberOfRooms = minRoom + RANDOM.nextInt(maxRoom - minRoom + 1);
    }

    /**
     * Randomly generates the rooms based on the number of rooms we generated earlier.
     */
    public void generateRooms() {
        int currentNumberOfRooms = 0;
        int minLengthOfSide = 3;
        int maxLengthOfSide = 6;
        int minX = 1;
        int maxX = WIDTH - 1 - maxLengthOfSide;
        int minY = 1;
        int maxY = HEIGHT - 1 - maxLengthOfSide;

        //1 for each wall + 1 space
        //i tried just doing 2 but i found this a little clearer visually
        int roomPadding = 3;

        while (currentNumberOfRooms < numberOfRooms) {
            int bottomLeftCornerX = minX + RANDOM.nextInt(maxX - minX + 1);
            int bottomLeftCornerY = minY + RANDOM.nextInt(maxY - minY + 1);
            int roomWidth = minLengthOfSide + RANDOM.nextInt(maxLengthOfSide - minLengthOfSide + 1);
            int roomHeight = minLengthOfSide + RANDOM.nextInt(maxLengthOfSide - minLengthOfSide + 1);
            Point bottomLeftCorner = new Point(bottomLeftCornerX, bottomLeftCornerY);

            boolean overlapFound = false;
            for (int i = 0; i < roomBottomLeftCorners.size(); i++) {
                Point existingBottomLeft = roomBottomLeftCorners.get(i);
                int existingWidth = roomWidths.get(i);
                int existingHeight = roomHeights.get(i);

                int newRoomRight = bottomLeftCornerX + roomWidth + roomPadding;
                int newRoomTop = bottomLeftCornerY + roomHeight + roomPadding;
                int existingRoomRight = existingBottomLeft.getX() + existingWidth + roomPadding;
                int existingRoomTop = existingBottomLeft.getY() + existingHeight + roomPadding;

                if (!(newRoomRight <= existingBottomLeft.getX() || bottomLeftCornerX >= existingRoomRight ||
                        newRoomTop <= existingBottomLeft.getY() || bottomLeftCornerY >= existingRoomTop)) {
                    overlapFound = true;
                    break;
                }
            }

            if (!overlapFound) {
                roomBottomLeftCorners.add(bottomLeftCorner);
                roomWidths.add(roomWidth);
                roomHeights.add(roomHeight);
                currentNumberOfRooms++;
            }
        }
    }

    /**
     * Build rooms based on roomLeftCorners, roomWidths, roomHeights we got earlier.
     * @param tiles
     */
    public void buildRooms(TETile[][] tiles) {
        for (int i = 0; i < numberOfRooms; i++) {
            int bottomLeftX = roomBottomLeftCorners.get(i).getX();
            int bottomLeftY = roomBottomLeftCorners.get(i).getY();
            int width = roomWidths.get(i);
            int height = roomHeights.get(i);


            //for debugging easier it's easier if i first build walls around room to see hallways
            for (int x = bottomLeftX - 1; x <= bottomLeftX + width; x++) {
                for (int y = bottomLeftY - 1; y <= bottomLeftY + height; y++) {
                    if (x == bottomLeftX - 1 || x == bottomLeftX + width || y == bottomLeftY - 1 || y == bottomLeftY + height) {
                        tiles[x][y] = Tileset.WALL;
                    }
                }
            }


            for (int x = bottomLeftX; x < bottomLeftX + width; x++) {
                for (int y = bottomLeftY; y < bottomLeftY + height; y++) {
                    tiles[x][y] = Tileset.FLOOR;
                }
            }
        }
    }

    /**
     * Build hallways in the order of room generation [need to be optimized]
     * @param tiles
     * I switched to not building L hallway since it looks like that happens already and when I add it,
     * it gets harder to read
     */
    public void buildHallways5(TETile[][] tiles) {
        List<Point> roomCenters = new ArrayList<>();
        for (int i = 0; i < numberOfRooms; i++) {
            int centerX = roomBottomLeftCorners.get(i).getX() + roomWidths.get(i) / 2;
            int centerY = roomBottomLeftCorners.get(i).getY() + roomHeights.get(i) / 2;
            roomCenters.add(new Point(centerX, centerY));
        }

        boolean[] connected = new boolean[numberOfRooms];
        List<Point> hallwayPath = new ArrayList<>();

        /*
        connected[0] = true;
         */
        //not always start with room 0, start with the most bottom left point
        int mostLeftBottom = Integer.MAX_VALUE;
        int mostLeftBottomIndex = -1;
        for (int i = 0; i < numberOfRooms; i++) {
            int current = roomCenters.get(i).getX() + roomCenters.get(i).getY();
            if (current < mostLeftBottom) {
                mostLeftBottom = current;
                mostLeftBottomIndex = i;
            }
        }
        connected[mostLeftBottomIndex] = true;

        int roomsConnected = 1;

        boolean[] hasBeenStarterBefore = new boolean[numberOfRooms];

        while (roomsConnected < numberOfRooms) {
            int minDistance = Integer.MAX_VALUE;
            int closestRoom = -1;
            Point closestRoomCenter = null;
            Point currentRoomCenter = null;
            int currentRoomIdx = -1;


            for (int i = 0; i < numberOfRooms; i++) {
                if (connected[i] && !hasBeenStarterBefore[i]) {
                    currentRoomCenter = roomCenters.get(i);
                    currentRoomIdx = i;
                    //System.out.println(i);
                    hasBeenStarterBefore[i] = true;
                    break;
                }
            }

            for (int i = 0; i < numberOfRooms; i++) {
                if (!connected[i]) {
                    Point candidate = roomCenters.get(i);
                    int distance = (int) currentRoomCenter.distance(candidate);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestRoom = i;
                        closestRoomCenter = candidate;
                    }
                }
            }

            if (closestRoomCenter != null && currentRoomCenter != null) {
                int startX = currentRoomCenter.getX();
                int startY = currentRoomCenter.getY();
                int endX = closestRoomCenter.getX();
                int endY = closestRoomCenter.getY();

                if (Math.abs(startX - endX) == 1 && startY == endY) {
                    for (int x = Math.min(startX, endX); x <= Math.max(startX, endX); x++) {
                        tiles[x][startY] = Tileset.FLOOR;
                    }
                } else if (Math.abs(startY - endY) == 1 && startX == endX) {
                    for (int y = Math.min(startY, endY); y <= Math.max(startY, endY); y++) {
                        tiles[startX][y] = Tileset.FLOOR;
                    }
                } else {
                    if (startX != endX) {
                        for (int x = Math.min(startX, endX); x <= Math.max(startX, endX); x++) {
                            tiles[x][startY] = Tileset.FLOOR;
                        }
                    }

                    if (startY != endY) {
                        for (int y = Math.min(startY, endY); y <= Math.max(startY, endY); y++) {
                            tiles[endX][y] = Tileset.FLOOR;
                        }
                    }
                }
            }

            connected[closestRoom] = true;
            roomsConnected++;

            //System.out.println(closestRoom);
        }
        /*
        //L shaped hallway of farthest connected room
        int maxDistance = Integer.MIN_VALUE;
        Point farthestRoom1 = null;
        Point farthestRoom2 = null;

        for (int i = 0; i < numberOfRooms; i++) {
            for (int j = i + 1; j < numberOfRooms; j++) {
                Point room1 = roomCenters.get(i);
                Point room2 = roomCenters.get(j);
                int distance = (int) room1.distance(room2);
                if (distance > maxDistance) {
                    maxDistance = distance;
                    farthestRoom1 = room1;
                    farthestRoom2 = room2;
                }
            }
        }

        if (farthestRoom1 != null && farthestRoom2 != null) {
            int startX = farthestRoom1.getX();
            int startY = farthestRoom1.getY();
            int endX = farthestRoom2.getX();
            int endY = farthestRoom2.getY();

            if (startX != endX) {
                for (int x = Math.min(startX, endX); x <= Math.max(startX, endX); x++) {
                    tiles[x][startY] = Tileset.FLOOR;
                }
            }

            if (startY != endY) {
                for (int y = Math.min(startY, endY); y <= Math.max(startY, endY); y++) {
                    tiles[endX][y] = Tileset.FLOOR;
                }
            }
        }

         */
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

    public void calculateTileAreas(TETile[][] tiles) {
        int floorArea = 0;
        int wallArea = 0;
        int totalArea = 0;

        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[x].length; y++) {
                totalArea++;
                if (tiles[x][y] == Tileset.FLOOR) {
                    floorArea++;
                } else if (tiles[x][y] == Tileset.WALL) {
                    wallArea++;
                }
            }
        }
        double num = floorArea + wallArea;
        double den = totalArea;
        double result = num/den;
        System.out.println("Covered Percentage: " + result + "%");
    }

    /**
     * Build hallways in the order of room generation [need to be optimized]
     * @param tiles
     */

    public void buildHallways(TETile[][] tiles) {
        List<Point> roomCenters = new ArrayList<>();
        for (int i = 0; i < numberOfRooms; i++) {
            int centerX = roomBottomLeftCorners.get(i).getX() + roomWidths.get(i) / 2;
            int centerY = roomBottomLeftCorners.get(i).getY() + roomHeights.get(i) / 2;
            roomCenters.add(new Point(centerX, centerY));
        }

        boolean[] connected = new boolean[numberOfRooms];
        List<Point> hallwayPath = new ArrayList<>();

        connected[0] = true;
        int roomsConnected = 1;

        while (roomsConnected < numberOfRooms) {
            int minDistance = Integer.MAX_VALUE;
            int closestRoom = -1;
            Point closestRoomCenter = null;
            Point currentRoomCenter = null;
            int currentRoomIdx = -1;

            for (int i = 0; i < numberOfRooms; i++) {
                if (connected[i]) {
                    currentRoomCenter = roomCenters.get(i);
                    currentRoomIdx = i;
                    break;
                }
            }

             for (int i = 0; i < numberOfRooms; i++) {
                if (!connected[i]) {
                    Point candidate = roomCenters.get(i);
                    int distance = (int) currentRoomCenter.distance(candidate);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestRoom = i;
                        closestRoomCenter = candidate;
                    }
                }
            }

            if (closestRoomCenter != null && currentRoomCenter != null) {
                int startX = currentRoomCenter.getX();
                int startY = currentRoomCenter.getY();
                int endX = closestRoomCenter.getX();
                int endY = closestRoomCenter.getY();

                if (Math.abs(startX - endX) == 1 && startY == endY) {
                    for (int x = Math.min(startX, endX); x <= Math.max(startX, endX); x++) {
                        tiles[x][startY] = Tileset.FLOOR;
                    }
                } else if (Math.abs(startY - endY) == 1 && startX == endX) {
                    for (int y = Math.min(startY, endY); y <= Math.max(startY, endY); y++) {
                        tiles[startX][y] = Tileset.FLOOR;
                    }
                } else {
                    if (startX != endX) {
                        for (int x = Math.min(startX, endX); x <= Math.max(startX, endX); x++) {
                            tiles[x][startY] = Tileset.FLOOR;
                        }
                    }

                    if (startY != endY) {
                        for (int y = Math.min(startY, endY); y <= Math.max(startY, endY); y++) {
                            tiles[endX][y] = Tileset.FLOOR;
                        }
                    }
                }
            }
            connected[closestRoom] = true;
            roomsConnected++;
        }
    }

    public void buildHallwaysL(TETile[][] tiles) {
        List<Point> roomCenters = new ArrayList<>();
        for (int i = 0; i < numberOfRooms; i++) {
            int centerX = roomBottomLeftCorners.get(i).getX() + roomWidths.get(i) / 2;
            int centerY = roomBottomLeftCorners.get(i).getY() + roomHeights.get(i) / 2;
            roomCenters.add(new Point(centerX, centerY));
        }

        boolean[] connected = new boolean[numberOfRooms];
        List<Point> hallwayPath = new ArrayList<>();

        connected[0] = true;
        int roomsConnected = 1;

        while (roomsConnected < numberOfRooms) {
            int maxDistance = Integer.MIN_VALUE;
            int farthestRoom = -1;
            Point farthestRoomCenter = null;
            Point currentRoomCenter = null;
            int currentRoomIdx = -1;

            for (int i = 0; i < numberOfRooms; i++) {
                if (connected[i]) {
                    currentRoomCenter = roomCenters.get(i);
                    currentRoomIdx = i;
                    break;
                }
            }

            for (int i = 0; i < numberOfRooms; i++) {
                if (!connected[i]) {
                    Point candidate = roomCenters.get(i);
                    int distance = (int) currentRoomCenter.distance(candidate);
                    if (distance > maxDistance) {
                        maxDistance = distance;
                        farthestRoom = i;
                        farthestRoomCenter = candidate;
                    }
                }
            }

            if (farthestRoomCenter != null && currentRoomCenter != null) {
                int startX = currentRoomCenter.getX();
                int startY = currentRoomCenter.getY();
                int endX = farthestRoomCenter.getX();
                int endY = farthestRoomCenter.getY();

                if (startX != endX) {
                    for (int x = Math.min(startX, endX); x <= Math.max(startX, endX); x++) {
                        tiles[x][startY] = Tileset.FLOOR;
                    }
                }
                if (startY != endY) {
                    for (int y = Math.min(startY, endY); y <= Math.max(startY, endY); y++) {
                        tiles[endX][y] = Tileset.FLOOR;
                    }
                }
            }
            connected[farthestRoom] = true;
            roomsConnected++;
        }
    }

    public void setAvatarPosition(int x, int y) {
        if (x == -1 && y == -1) {
            Point firstRoomCenter = roomBottomLeftCorners.get(0);
            avatarX = firstRoomCenter.getX() + roomWidths.get(0) / 2;
            avatarY = firstRoomCenter.getY() + roomHeights.get(0) / 2;
        } else {
            avatarX = x;
            avatarY = y;
        }
    }

    public void moveAvatarUp(TETile[][] tiles) {
        if (avatarY + 1 < HEIGHT && tiles[avatarX][avatarY + 1] != Tileset.WALL) {
            tiles[avatarX][avatarY] = Tileset.FLOOR;
            avatarY++;
        }
    }

    public void moveAvatarDown(TETile[][] tiles) {
        if (avatarY - 1 >= 0 && tiles[avatarX][avatarY - 1] != Tileset.WALL) {
            tiles[avatarX][avatarY] = Tileset.FLOOR;
            avatarY--;
        }
    }

    public void moveAvatarLeft(TETile[][] tiles) {
        if (avatarX - 1 >= 0 && tiles[avatarX - 1][avatarY] != Tileset.WALL) {
            tiles[avatarX][avatarY] = Tileset.FLOOR;
            avatarX--;
        }
    }

    public void moveAvatarRight(TETile[][] tiles) {
        if (avatarX + 1 < WIDTH && tiles[avatarX + 1][avatarY] != Tileset.WALL) {
            tiles[avatarX][avatarY] = Tileset.FLOOR;
            avatarX++;
        }
    }

    public void drawAvatar(TETile[][] tiles) {
        tiles[avatarX][avatarY] = Tileset.FLOOR;
        tiles[avatarX][avatarY] = Tileset.AVATAR;
    }

    public static long getSeed() {
        return seed;
    }

    public int getAvatarX() {
        return avatarX;
    }

    public int getAvatarY() {
        return avatarY;
    }

    /**
     * Get the blind version (i.e. tiles out of horizon being Tileset.NOTHING)  of 'tiles'.
     * @param tiles
     * @return blindTiles
     */
    public TETile[][] getBlindTiles(TETile[][] tiles) {
        TETile[][] blindTiles = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                blindTiles[x][y] = tiles[x][y];
            }
        }
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (Math.abs(x - avatarX) + Math.abs(y - avatarY) > 5) {
                    blindTiles[x][y] = Tileset.NOTHING;
                }
            }
        }
        return blindTiles;
    }


    public void drawLight(TETile[][] tiles, boolean light1, boolean light2, boolean light3) {
        Point light1Corr = roomBottomLeftCorners.get(0);
        Point light2Corr = roomBottomLeftCorners.get(1);
        Point light3Corr = roomBottomLeftCorners.get(2);
        boolean[] light = {light1, light2, light3};
        Point[] lightCorr = {light1Corr, light2Corr, light3Corr};
        final int numberOfLight = 3;
        for (int i = 0; i < numberOfLight; i++) {
            if (light[i]) {
                for (int x = lightCorr[i].getX(); x < lightCorr[i].getX() + roomWidths.get(i); x++) {
                    for (int y = lightCorr[i].getY(); y < lightCorr[i].getY() + roomHeights.get(i); y++) {
                        int intensityAttenuation = Math.max(x - lightCorr[i].getX(), y - lightCorr[i].getY());
                        if (intensityAttenuation == 0) {
                            tiles[x][y] = new TETile('●', Color.white, new Color(58, 92, 171),
                                    "Dark", 1 );
                        } else if (intensityAttenuation == 1) {
                            tiles[x][y] = new TETile('·', Color.white, new Color(41, 68, 134),
                                    "Sub-Dark", 2 );
                        } else if (intensityAttenuation == 2) {
                            tiles[x][y] = new TETile('·', Color.white, new Color(30, 45, 95),
                                    "Moderate", 3);
                        } else if (intensityAttenuation == 3) {
                            tiles[x][y] = new TETile('·', Color.white, new Color(22, 27, 66),
                                    "Sub-Light", 4);
                        } else if (intensityAttenuation == 4) {
                            tiles[x][y] = new TETile('·', Color.white, new Color(14, 17, 44),
                                    "Light", 5);
                        }
                    }
                }
            } else {
                for (int x = lightCorr[i].getX(); x < lightCorr[i].getX() + roomWidths.get(i); x++) {
                    for (int y = lightCorr[i].getY(); y < lightCorr[i].getY() + roomHeights.get(i); y++) {
                        if (x == lightCorr[i].getX() && y == lightCorr[i].getY()) {
                            tiles[x][y] = new TETile('●', Color.white, Color.black, "TurnOff", 6 );
                        } else {
                            tiles[x][y] = Tileset.FLOOR;
                        }
                    }
                }
            }
        }
    }


}

//@source I used https://www.geeksforgeeks.org/scanner-class-in-java/
//@source I used https://www.geeksforgeeks.org/stringbuilder-class-in-java-with-examples/
//@source I used  https://stackoverflow.com/questions/18104869/how-can-i-turn-stringbuilder-variable-to-a-long
//@source I used https://stackoverflow.com/questions/32294644/what-is-the-difference-between-scanner-next-and-system-in-read
//@source I used https://introcs.cs.princeton.edu/java/stdlib/javadoc/StdDraw.html
//@source I used https://stackoverflow.com/questions/11680714/standard-draw-is-key-pressed-skips-stages
//@source I used http://siever.info/cs141/StdDraw.html
//@source I used chatgpt for '' vs "" and for an exception error not handling message

package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;
import utils.FileUtils;
import java.io.IOException;
import java.util.Scanner;
import static core.World2.seed;

public class Main {
    private static final int WIDTH = 70;
    private static final int HEIGHT = 30;

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        boolean running = true;
        menu();

        while (running) {
            if (StdDraw.hasNextKeyTyped()) {
                char user = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (user == 'n') {
                    newGame();
                }
                if (user == 'l') {
                    System.out.println("Loading saved game");
                    if (FileUtils.fileExists("save.txt")) {
                        String saved = FileUtils.readFile("save.txt");
                        String[] values = saved.split(",");
                        int x = Integer.parseInt(values[0]);
                        int y = Integer.parseInt(values[1]);
                        long s = Long.parseLong(values[2]);
                        boolean sight = Boolean.parseBoolean(values[3]);
                        boolean light1 = Boolean.parseBoolean(values[4]);
                        boolean light2 = Boolean.parseBoolean(values[5]);
                        boolean light3 = Boolean.parseBoolean(values[6]);
                        startGame(s, x, y, sight, light1, light2, light3);
                    }
                }
                if (user == 'q') {
                    System.exit(0);
                    running = false;
                }
            }
        }
    }

    private static void menu() {
        StdDraw.setCanvasSize(500, 500);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        StdDraw.text(0.5, 0.75, "CS61B: BYOW");
        StdDraw.text(0.5, 0.6, "New Game (N)");
        StdDraw.text(0.5, 0.5, "Load Game (L)");
        StdDraw.text(0.5, 0.4, "Quit (Q)");
    }

    private static void newGame() {
        StdDraw.setCanvasSize(500, 500);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        StdDraw.text(0.5, 0.75, "CS61B: BYOW");
        StdDraw.text(0.5, 0.6, "Enter seed followed by S");
        double startX = 0.3;
        double spacing = 0.025;

        boolean enteringSeed = true;
        StringBuilder seedBuilder = new StringBuilder();
        while (enteringSeed) {
            if (StdDraw.hasNextKeyTyped()) {
                char input = StdDraw.nextKeyTyped();
                if (Character.isDigit(input)) {
                    seedBuilder.append(input);
                    StdDraw.setPenColor(StdDraw.GREEN);
                    double x = startX + seedBuilder.length() * spacing;
                    StdDraw.text(x, 0.3, String.valueOf(input));
                } else if (input == 'S' || input == 's') {
                    enteringSeed = false;
                }
            }
        }
        long seed = Long.parseLong(seedBuilder.toString());
        startGame(seed, -1, -1, true, true, true, true);
    }

    private static void startGame(long seed, int a, int b, boolean sight,
                                  boolean light1, boolean light2, boolean light3) {
        // build your own world!
        // initialize the tile rendering engine with a window of size WIDTH x HEIGHT
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        // initialize tiles
        TETile[][] tiles = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }

        World2 world = new World2(seed);
        world.generateNumberOfRooms();
        world.generateRooms();
        world.buildRooms(tiles);
        world.buildHallways5(tiles);
        world.buildWalls(tiles);
        //world.calculateTileAreas(tiles);
        ter.renderFrame(tiles);
        world.setAvatarPosition(a, b);
        boolean colon = false;

        while (true) {
            world.drawLight(tiles, light1, light2, light3);
            world.drawAvatar(tiles);
            if (!sight) {
                TETile[][] blindTiles = world.getBlindTiles(tiles);
                ter.renderFrame(blindTiles);
            } else {
                ter.renderFrame(tiles);
            }
            if (StdDraw.hasNextKeyTyped()) {
                char move = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (move == ':') {
                    colon = true;
                } else if (colon && move == 'q') {
                    int avatarX = world.getAvatarX();
                    int avatarY = world.getAvatarY();
                    String content = avatarX + "," + avatarY + "," + seed + "," + sight + ","
                            + light1 + "," + light2 + "," + light3;
                    FileUtils.writeFile("save.txt", content);
                    System.exit(0);
                } else {
                    colon = false;
                }

                if (move == 'w') {
                    world.moveAvatarUp(tiles);
                } else if (move == 'a') {
                    world.moveAvatarLeft(tiles);
                } else if (move == 's') {
                    world.moveAvatarDown(tiles);
                } else if (move == 'd') {
                    world.moveAvatarRight(tiles);
                } else if (move == 'p') {
                    sight = !sight;
                } else if (move == 'm') {
                    light1 = !light1;
                } else if (move == 'n') {
                    light2 = !light2;
                } else if (move == 'b') {
                    light3 = !light3;
                }
            }
        }
    }
}

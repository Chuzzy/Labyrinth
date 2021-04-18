package BackEnd;

import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import static BackEnd.TileType.*;

/**
 * This class is a file reader class which will take in a given level file format text file, verify it
 * and create the gameboard, along with the fixed tiles in their correct locations, the players in their correct
 * locations, and the amount and type of each floor/action tile that will populate the silk bag.
 *
 * @author Christian Sanger, Atif Ishaq, Joshua Oladitan and Reuben MacDonald
 * @version 1.0
 */

public class FileReader {

    /*
    These static variables hold information about the number of players and the number
    of different tile types.
     */
    private static final int NUM_OF_TILE_TYPES = TileType.values().length;
    public static final int MAX_NUM_OF_PLAYERS = 4;

    /**
     * This method takes in the given level format file,
     * and create a gameboard and players for that game.
     *
     * @param filename    The name of the level file format text file.
     * @param silkBagSeed The seed used for this game.
     * @param gameLogic The instance of game logic to assign to computer players.
     * @param computerIndices Zero-based indices of the computer players.
     * @return pair where first element is the gameboard and second is the players.
     * @throws Exception issue with gameboard file
     */
    public static Pair<Gameboard, Player[]> gameSetup(String filename, int silkBagSeed, GameLogic gameLogic, int... computerIndices) throws Exception {
        int rand;
        int tileRand;
        int nonFixedCounter = 0;
        int nonFixedMax = -1;
        String fixed;
        boolean nonFixed = false;
        String unFix;
        File input = new File("Gameboards\\" + filename);
        if (!input.exists()) {
            throw new FileNotFoundException(filename);
        }
        Scanner in = new Scanner(input);
        Scanner currentLine;
        SilkBag silkBag = new SilkBag(silkBagSeed);

        //// board config
        currentLine = new Scanner(in.nextLine());
        int width = currentLine.nextInt();
        int height = currentLine.nextInt();
        Gameboard gameboard = new Gameboard(width, height, silkBag);

        //// Creating players
        Player[] players = new Player[MAX_NUM_OF_PLAYERS];
        Coordinate[] playerPos = new Coordinate[MAX_NUM_OF_PLAYERS];
        gameboard.setNumOfPlayers(MAX_NUM_OF_PLAYERS);
        for (int i = 0; i < MAX_NUM_OF_PLAYERS; i++) {
            String nextLine = in.nextLine();
            currentLine = new Scanner(nextLine);
            int x = currentLine.nextInt();
            int y = currentLine.nextInt();
            playerPos[i] = new Coordinate(x, y);
            gameboard.setPlayerPos(i, new Coordinate(x, y));
            int finalI = i;
            if (Arrays.stream(computerIndices).anyMatch(index -> index == finalI)) {
                players[i] = new ComputerPlayer(silkBag, gameboard, gameLogic, i);
            } else {
                players[i] = new Player(silkBag, gameboard);
            }
        }

        //// Filling SilkBag
        int[] tileTypeCount = new int[NUM_OF_TILE_TYPES];
        // Reading how many of each tile
        for (int tileType = 0; tileType < NUM_OF_TILE_TYPES; tileType++) {
            currentLine = new Scanner(in.nextLine());
            tileTypeCount[tileType] = currentLine.nextInt();
        }
        // putting them in the bag
        // for each tile type
        for (int tileType = 0; tileType < NUM_OF_TILE_TYPES; tileType++) {
            int numberOfThisTile = tileTypeCount[tileType];
            // for each tile that need to be added to silkbag
            for (int i = 0; i < numberOfThisTile; i++) {
                Tile newTile = Tile.createTile(TileType.values()[tileType]);
                silkBag.insertTile(newTile);
            }
        }

        //// Fill with random tiles
        Random r = new Random(silkBagSeed);
        ArrayList<Coordinate> slideLocations = gameboard.getSlideLocations();

        while (gameboard.isBoardNotFull()) {
            Coordinate toSlide = null;
            if (slideLocations.size() == 0) {
                throw new Exception("No slide locations");
            }
            for (int x = 0; x <= (gameboard.getWidth())-1; x++) {
                for (int y = 0; y <= (gameboard.getHeight())-1; y++) {
                    tileRand = r.nextInt(3);
                    FloorTile tile = null;
                    switch (tileRand) {
                        case 0:
                            tile = new FloorTile(STRAIGHT);
                            break;
                        case 1:
                            tile = new FloorTile(CORNER);
                            break;
                        case 2:
                            tile = new FloorTile(T_SHAPE);
                            break;
                    }
                    tile.setRotation(Rotation.values()[r.nextInt(4)]);
                    Coordinate coord = new Coordinate(x, y);
                    gameboard.placeTile(tile, coord);
                }
            }
        }
        currentLine = new Scanner(in.nextLine());
        while (nonFixedCounter != nonFixedMax) {
            try{
                currentLine = new Scanner(in.nextLine());
                fixed = currentLine.next().toUpperCase();
                try {
                    nonFixedMax = Integer.parseInt(fixed);
                    if (nonFixed == false) {
                        currentLine = new Scanner(in.nextLine());
                        nonFixed = true;
                        fixed = currentLine.next().toUpperCase();
                    }
                    TileType tileType = TileType.valueOf(fixed);
                    int x = currentLine.nextInt();
                    int y = currentLine.nextInt();
                    Coordinate location = new Coordinate(x, y);
                    int rotationInt = currentLine.nextInt();
                    Rotation rotation = Rotation.values()[rotationInt];
                    FloorTile tile = new FloorTile(tileType, rotation);
                    gameboard.placeTile(tile, location);
                    nonFixedCounter += 1;
                } catch (NumberFormatException e) {
                    if (nonFixed == true) {
                        TileType tileType = TileType.valueOf(fixed);
                        int x = currentLine.nextInt();
                        int y = currentLine.nextInt();
                        Coordinate location = new Coordinate(x, y);
                        int rotationInt = currentLine.nextInt();
                        Rotation rotation = Rotation.values()[rotationInt];
                        FloorTile tile = new FloorTile(tileType, rotation);
                        gameboard.placeTile(tile, location);
                        nonFixedCounter += 1;
                    } else {
                        TileType tileType = TileType.valueOf(fixed);
                        int x = currentLine.nextInt();
                        int y = currentLine.nextInt();
                        Coordinate location = new Coordinate(x, y);
                        int rotationInt = currentLine.nextInt();
                        Rotation rotation = Rotation.values()[rotationInt];
                        FloorTile tile = new FloorTile(tileType, rotation);
                        gameboard.placeFixedTile(tile, location);
                    }
                }
            } catch (Exception e) {
                nonFixedCounter=-1;
            }
        }

        // Place players back in correct location as they would have been shifted when tiles have been placed
        for (int i = 0; i < MAX_NUM_OF_PLAYERS; i++) {
            for (int j = 0; j < 4; j++) {
                gameboard.setPlayerPos(i, playerPos[i]);
            }
        }

        return new Pair<>(gameboard, players);
    }

    /*
     * This method takes in the custom given level format file, and checks to see that the file exists.
     *
     * @param gameBoard The name of the level file format text file.
     * @return in The scanner that iterates through the file.
     * @throws Exception if cannot create gameSetup
     */
//    public static Pair<Gameboard, Player[]> customGameSetup(String gameBoard) throws Exception {
//        return customGameSetup(gameBoard, (new Random()).nextInt());
//    }

}
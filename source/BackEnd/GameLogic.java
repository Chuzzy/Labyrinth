package BackEnd;

import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;

import static BackEnd.Phase.*;
import static BackEnd.TileType.BACKTRACK;
import static BackEnd.TileType.DOUBLE_MOVE;

/**
 * Controls the flow of game, lets the UI know what choices the player has
 * and the state of the game. Also lets the UI tell it the User choices, basically
 * acts and an API for the front end.
 *
 * @author Christian Sanger
 * @version 1.0
 */
public class GameLogic {
    private final int seed;
    // Games parts
    Gameboard gameboard;
    Player[] players;
    int numberOfPlayers;
    int currentPlayerNo;
    Player currentPlayer;
    // Current phase of the game, Draw|Floor|Action|Move
    Phase phase;

    // Special flags
    // True if player gets two moves.
    boolean doubleMove;
    private GameSave gameSaver;

	/**
	 * Creates a new game from the given board file
	 *
	 * @param boardFile Paths to board file
     * @param gameSaver The game server to use
	 * @throws Exception if issue with board file.
	 */
	public void newGame(String boardFile, GameSave gameSaver) throws Exception {
	    boolean custom=false;
		doubleMove = false;
		currentPlayerNo = 0;
		phase = DRAW;
		numberOfPlayers = 4;
		Pair<Gameboard, Player[]> gameItems = FileReader.gameSetup(boardFile, seed);
		gameboard = gameItems.getKey();
		players = gameItems.getValue();
		currentPlayer = players[currentPlayerNo];
		this.gameSaver = gameSaver;
	}

    /**
     * Returns the current phase of the game
     *
     * @return current Phase
     * @see Phase
     */
    public Phase getGamePhase() {
        return this.phase;
    }

    /**
     * Draws a tile for the current player.
     */
    public void draw() {
        gameSaver.draw();
        currentPlayer.drawTile();
        if (currentPlayer.isHolding() instanceof FloorTile) {
            // If floorTile you may play it
            phase = FLOOR;
        } else {
            // Else skip to action phase
            phase = ACTION;
        }
    }

    /**
     * Returns the last drawn card of the current player.
     *
     * @return the tile the player is holding.
     */
    public Tile drawnCard() {
        return players[currentPlayerNo].isHolding();
    }

    /**
     * Gets the current slide locations that the player
     * can push a tile in. All slide locations will be outside
     * of board.
     * ie. to push in from the left to the right on the top row
     * location (-1,0) would be used.
     *
     * @return list of every allowed slide location
     */
    public ArrayList<Coordinate> getSlideLocations() throws Exception {

        return gameboard.getSlideLocations();
    }

    /**
     * Used to tell the game where the user would like to slide
     * a tile in.
     *
     * @param tile     tile to be played
     * @param location where the tile should be played.
     * @throws Exception if silkbag is empty
     */
    public void floor(FloorTile tile, Coordinate location) throws Exception {
        gameSaver.playFloorTile(location, tile);
        assert (tile.getType().equals(currentPlayer.isHolding().getType()));
        if (tile != null) {
            players[currentPlayerNo].playFloorTile(location, tile);
        }
        phase = ACTION;
    }

    /**
     * Returns an array detailing which players have already been backtracked.
     * The array is such that if player i has been backtracked then
     * {@code array[i] == true}.
     *
     * @return boolean array representing which players have been backtracked.
     */
    public boolean[] getPlayersThatCanBeBacktracked() {
        boolean[] result = new boolean[numberOfPlayers];
        for (int i = 0; i < numberOfPlayers; i++) {
            result[i] = !players[i].hasBeenBacktracked();
        }
        return result;
    }

    /**
     * Gets the location of all players.
     *
     * @return array of coordinates containing the current locations of each player
     */
    public Coordinate[] getPlayerLocations() {
        Coordinate[] result = new Coordinate[getNumberOfPlayers()];
        for (int i = 0; i < getNumberOfPlayers(); i++) {
            result[i] = gameboard.getPlayerPos(i);
        }
        return result;
    }

    /**
     * Plays an Action tile at location.
     *
     * @param tile       the tile to play
     * @param coordinate where to play the tile (if its played at a location)
     * @param playerNo   which player this card effect (if it does)
     */
    public void action(ActionTile tile, Coordinate coordinate, int playerNo) throws Exception {
        // If tile is null then player didn't/can't play an action card.
        gameSaver.playActionTile(coordinate, tile, playerNo);
        if (tile != null) {
            if (tile.getType() == DOUBLE_MOVE) {
                doubleMove = true;
            }
            if (tile.getType() == BACKTRACK) {
                players[playerNo].setBeenBackTracked();
            }
            players[currentPlayerNo].playActionTile(coordinate, tile, playerNo);
        }
        if (gameboard.isPlayerOnGoal() != -1) {
            phase = WIN;
        } else {
            phase = MOVE;
        }
    }

    /**
     * Returns all Action tiles the current player can use
     *
     * @return array of all playable action tiles.
     */
    public ActionTile[] getActionCards() {
        return currentPlayer.getInventory().toArray(new ActionTile[0]);
    }

    /**
     * Moves the current player to another location.
     *
     * @param location the new location for the current player
     */
    public void move(Coordinate location) throws Exception {
        gameSaver.playerMove(location);
        gameboard.setPlayerPos(currentPlayerNo, location);
        if (gameboard.isPlayerOnGoal() != -1) {
            phase = WIN;
        } else if (doubleMove) {
            doubleMove = false;
        } else {
            phase = DRAW;
            currentPlayerNo = (currentPlayerNo + 1) % getNumberOfPlayers();
            currentPlayer = players[currentPlayerNo];
        }
        gameboard.ticTiles();
    }

    /**
     * Creates an empty game logic class.
     * To initialise the game, use the
     * {@code newGame} method.
     * @see #newGame(String, GameSave)
     *
     * @param seed seed used with random generator
     */
    public GameLogic(int seed) {
        this.seed = seed;
    }

    /**
     * Gets the floor tile at a given location on the board.
     *
     * @param location the location of the tile
     * @return tile at location.
     */
    public FloorTile getTileAt(Coordinate location) {
        return gameboard.tileAt(location);
    }

    /**
     * Gets the width of the board.
     *
     * @return the width of the board
     */
    public int getWidth() {
        return gameboard.getWidth();
    }

    /**
     * Gets the height of the board.
     *
     * @return the height of the board.
     */
    public int getHeight() {
        return gameboard.getHeight();
    }

    /**
     * Gets how many player are currently playing
     *
     * @return number of players
     */
    public int getNumberOfPlayers() {
        return gameboard.getNumOfPlayers();
    }

    /**
     * Sets how many players are currently playing
     *
     * @param numberOfPlayers number of players
     */
    public void setNumberOfPlayers(int numberOfPlayers) {
        gameboard.setNumOfPlayers(numberOfPlayers);
    }


    /**
     * Gets the index of the player whose turn it is.
     *
     * @return the current player's index
     */
    public int getPlayersTurn() {
        return currentPlayerNo;
    }

    /**
     * gets all move locations that it is valid for the current player to move to.
     *
     * @return all valid move locations
     */
    public Coordinate[] getMoveLocations() throws Exception {
        return gameboard.getMoveDirections(currentPlayerNo).toArray(new Coordinate[0]);
    }

    /**
     * Saves the game to file
     *
     * @throws IOException if error with file
     */
    public void saveGame() throws IOException {
        gameSaver.saveToFile();
    }

    /**
     * Gets whether the game is saved.
     *
     * @return true if the game is saved; false otherwise
     */
    public boolean isGameSaved() {
        return gameSaver.isGameSaved();
    }

    /**
     * Gets the winner of the game.
     *
     * @return zero-based index of the winning player,
     * or -1 if there is no winner.
     */
    public int getWinner() throws Exception {
        return gameboard.isPlayerOnGoal();
    }

    /**
     * Clears the history of the gameSaver.
     */
    public void emptyGameSaver() {
        gameSaver.emptyGameSaveString();
    }
}

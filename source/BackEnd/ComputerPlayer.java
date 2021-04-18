package BackEnd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import com.sun.scenario.animation.shared.TimerReceiver;
import org.javatuples.Triplet;

/**
 * Represents a computer player.
 * @author Samuel Fuller
 */
public class ComputerPlayer extends Player {

    private final Random rng;
    private final GameLogic gameLogic;
    private final int myPlayerIndex;

    /**
     * Create a computer player and give them the silk bag, gameboard and game logic references.
     *
     * @param silkBag   Reference to the game's silk bag object.
     * @param gameboard Reference to the game's gameboard object.
     * @param gameLogic Reference to the game's logic object.
     * @param myPlayerIndex The zero-based index of this player.
     */
    public ComputerPlayer(SilkBag silkBag, Gameboard gameboard, GameLogic gameLogic, int myPlayerIndex) {
        super(silkBag, gameboard);
        this.gameLogic = gameLogic;
        rng = new Random();
        this.myPlayerIndex = myPlayerIndex;
    }

    /**
     * Calculates where to deploy the floor tile.
     * @return the coordinates the computer wishes to deploy the floor tile at
     */
    public Coordinate calculateFloorTilePlacement() {
        ArrayList<Coordinate> slideLocations = gameLogic.getSlideLocations();
        if (slideLocations.size() == 0) {
            return null;
        }

        if (rng.nextBoolean()) {
            // Move me closer to the goal
            Coordinate distanceFromGoal = getDistanceFromGoal();
            // Determine if there's a slide location on my axis
            for (Coordinate slideLocation : slideLocations) {
                if (slideLocation.getX() == gameboard.getPlayerPos(myPlayerIndex).getX()) {
                    // Push down if I need to go down
                    if (distanceFromGoal.getY() > 0 && 0 > slideLocation.getY()) {
                        return slideLocation;
                        // Push up if I need to go up
                    } else if (distanceFromGoal.getY() < 0 && 0 < slideLocation.getY()) {
                        return slideLocation;
                    }
                } else if (slideLocation.getY() == gameboard.getPlayerPos(myPlayerIndex).getY()) {
                    // Push left if I need to go left
                    if (distanceFromGoal.getX() > 0 && 0 > slideLocation.getX()) {
                        return slideLocation;
                    } else if (distanceFromGoal.getX() < 0 && 0 < slideLocation.getX()) {
                        return slideLocation;
                    }
                }
            }
        } else {
            int closestOpponent = getOpponentClosestToGoal();
            Coordinate distanceFromGoal = getDistanceFromGoal(closestOpponent);
            // Determine if there's a slide location on their axis
            for (Coordinate slideLocation : slideLocations) {
                if (slideLocation.getX() == gameboard.getPlayerPos(myPlayerIndex).getX()) {
                    // Push down if I need to go down
                    if (distanceFromGoal.getY() > 0 && 0 < slideLocation.getY()) {
                        return slideLocation;
                        // Push up if I need to go up
                    } else if (distanceFromGoal.getY() < 0 && 0 > slideLocation.getY()) {
                        return slideLocation;
                    }
                } else if (slideLocation.getY() == gameboard.getPlayerPos(myPlayerIndex).getY()) {
                    // Push left if I need to go left
                    if (distanceFromGoal.getX() > 0 && 0 < slideLocation.getX()) {
                        return slideLocation;
                    } else if (distanceFromGoal.getX() < 0 && 0 > slideLocation.getX()) {
                        return slideLocation;
                    }
                }
            }
        }
        // At this point, it's not possible to move closer to the goal.
        // Therefore pick a random location to slide into
        return slideLocations.get(rng.nextInt(slideLocations.size()));
    }

    /**
     * Returns the shortest distance between this computer player and a goal tile.
     * @return the shortest distance as a coordinate
     */
    private Coordinate getDistanceFromGoal() {
        return getDistanceFromGoal(myPlayerIndex);
    }

    /**
     * Returns the shortest distance between a player and a goal tile.
     * @param playerIndex the player to test
     * @return shortest distance as a coordinate
     */
    private Coordinate getDistanceFromGoal(int playerIndex) {
        ArrayList<Coordinate> goalPositions = gameboard.checkGoalTiles();
        Coordinate shortest = null;
        int shortestDistance = 0;
        for (Coordinate goalPos : goalPositions) {
            Coordinate diff = goalPos.difference(gameboard.getPlayerPos(playerIndex));
            if (shortest == null || shortestDistance > diff.absoluteSum()) {
                shortest = diff;
                shortestDistance = diff.absoluteSum();
            }
        }
        return shortest;
    }

    /**
     * Returns the shortest distance between a position and a goal tile
     * @param position the position to test
     * @return the shortest distance as a coordinate
     */
    private Coordinate getDistanceFromGoal(Coordinate position) {
        ArrayList<Coordinate> goalPositions = gameboard.checkGoalTiles();
        Coordinate shortest = null;
        int shortestDistance = 0;
        for (Coordinate goalPos : goalPositions) {
            Coordinate diff = goalPos.difference(position);
            if (shortest == null || shortestDistance > diff.absoluteSum()) {
                shortest = diff;
                shortestDistance = diff.absoluteSum();
            }
        }
        return shortest;
    }

    /**
     * Returns the index of the opponent who is closest to the goal.
     * @return the opponent's index
     */
    private int getOpponentClosestToGoal() {
        if (gameLogic.numberOfPlayers == 2) {
            return myPlayerIndex == 1 ? 0 : 1;
        } else {
            int closestOppIndex = -1;
            int lowestDistance = 0;
            for (int i = 0; i < gameLogic.numberOfPlayers; i++) {
                if (i == myPlayerIndex) {
                    continue;
                }
                int distanceFromGoal = getDistanceFromGoal(i).absoluteSum();
                if (closestOppIndex == -1 || lowestDistance > distanceFromGoal) {
                    closestOppIndex = i;
                    lowestDistance = distanceFromGoal;
                }
            }
            return closestOppIndex;
        }
    }

    /**
     * Calculates a move for the action phase.
     * @return triplet containing the action tile to use, the coordinates to place it, and the index of the player it
     * affects.
     */
    public Triplet<ActionTile, Coordinate, Integer> calculateActionTile() {
        Triplet<ActionTile, Coordinate, Integer> doNothing = new Triplet<>(null, null, -1);
        ArrayList<ActionTile> myTiles = getInventory();
        // 10 percent chance to skip action tile phase
        if (myTiles.size() == 0 || rng.nextInt(100) < 10) {
            System.out.println("I'm not going to bother placing action tile");
            return doNothing;
        }

        int randomChoice = rng.nextInt(myTiles.size());
        for (int i = 0; i < myTiles.size(); i++) {
            ActionTile currentTile = myTiles.get((randomChoice + i) % myTiles.size());
            switch (currentTile.type) {
                case FIRE:
                    return new Triplet<>(currentTile, getFireTileDeploymentLocation(), 0);
                case FROZEN:
                    int opp = getOpponentClosestToGoal();
                    return new Triplet<>(currentTile, gameLogic.getPlayerLocations()[opp], 0);
                case BACKTRACK:
                    if (ableToUseBacktrack()) {
                        int opponentToBacktrack;
                        while ((opponentToBacktrack = rng.nextInt(gameLogic.getNumberOfPlayers())) != myPlayerIndex);
                        return new Triplet<>(currentTile, null, opponentToBacktrack);
                    }
                    break;
                case DOUBLE_MOVE:
                    return new Triplet<>(currentTile, null, 0);
                default:
                    throw new IllegalStateException("How has a floor tile gotten into my inventory?");
            }
        }
        System.out.println("I have no use for an action tile");
        return doNothing;
    }

    /**
     * Finds a good spot to place an fire tile.
     * @return a coordinate to deploy a fire or ice tile
     */
    private Coordinate getFireTileDeploymentLocation() {
        Coordinate deploymentLocation;
        do {
            deploymentLocation = new Coordinate(rng.nextInt(gameboard.getWidth()), rng.nextInt(gameboard.getHeight()));
        } while (!canDeployFireTile(deploymentLocation.getX(), deploymentLocation.getY()));
        return deploymentLocation;
    }

    /**
     * Determines whether a file tile can be deployed on this coordinate.
     * @param getX the x coordinate
     * @param getY the y coordinate
     * @return true if the fire tile is not obstructed by a player
     */
    private boolean canDeployFireTile(int getX, int getY) {
        for (int shiftX = -1; shiftX <= 1; shiftX++) {
            for (int shiftY = -1; shiftY <= 1; shiftY++) {
                Coordinate locationToCheck = new Coordinate(getX + shiftX, getY + shiftY);
                for (Coordinate person : gameLogic.getPlayerLocations()) {
                    if (person.equals(locationToCheck)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines whether or not it's possible to use a backtrack action tile.
     * It is possible if an opponent can be backtracked.
     * @return true if a backtrack action tile can be used
     */
    private boolean ableToUseBacktrack() {
        boolean[] backtrackStatus = gameLogic.getPlayersThatCanBeBacktracked();
        for (int i = 0; i < backtrackStatus.length; i++) {
            if (i != myPlayerIndex && backtrackStatus[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates where the computer should move.
     * @return the coordinates of the square it wants to move to
     * @throws IllegalStateException if the computer cannot move
     */
    public Coordinate calculateMovePhase() {
        Coordinate[] moveLocations = gameLogic.getMoveLocations();
        if (moveLocations.length == 0) {
            System.out.println("Computer " + myPlayerIndex + " is stuck!");
            return gameLogic.getPlayerLocations()[myPlayerIndex];
        }
        if (moveLocations.length == 1) {
            System.out.println("Computer " + myPlayerIndex + " has only one place to go.");
            return moveLocations[0];
        }
        // Move towards the goal, if possible
        Coordinate bestMove = null; // The move that takes me closest to a goal tile
        int bestMoveDistance = 0; // The distance between a goal and the current best move
        for (Coordinate moveLoc : moveLocations) {
            int distanceFromGoal = getDistanceFromGoal(moveLoc).absoluteSum();
            if (bestMove == null || bestMoveDistance > distanceFromGoal) {
                bestMove = moveLoc;
                bestMoveDistance = distanceFromGoal;
            }
        }
        return bestMove;
    }
}

package FrontEnd;

import BackEnd.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.util.Duration;
import javafx.util.Pair;
import org.javatuples.Triplet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static BackEnd.Rotation.*;
import static BackEnd.TileType.*;

/***
 * Use to control the GameScreen scene.
 * @author Chrisitan Sanger
 * @author Sam Harry (Version 1.1)
 */
public class GameScreenController extends StateLoad {

    /* These final variables are used for the game's Sound Effects (SFX) */

    private final String DRAW_SFX = "Assets\\SFX\\draw.mp3";
    private final AudioClip DRAW_AUDIO = new AudioClip(new File(DRAW_SFX).toURI().toString());
    private final String SKIP_SFX = "Assets\\SFX\\skip.mp3";
    private final AudioClip SKIP_AUDIO = new AudioClip(new File(SKIP_SFX).toURI().toString());
    private final String WIN_SFX = "Assets\\SFX\\gamewin.mp3";
    private final AudioClip WIN_AUDIO = new AudioClip(new File(WIN_SFX).toURI().toString());
    private final String MOVEMENT_SFX = "Assets\\SFX\\movement.mp3";
    private final AudioClip MOVEMENT_AUDIO = new AudioClip(new File(MOVEMENT_SFX).toURI().toString());
    private final String FLOOR_SFX = "Assets\\SFX\\floor.mp3";
    private final AudioClip FLOOR_AUDIO = new AudioClip(new File(FLOOR_SFX).toURI().toString());
    private final String BACKTRACK_SFX = "Assets\\SFX\\backtrack.mp3";
    private final AudioClip BACKTRACK_AUDIO = new AudioClip(new File(BACKTRACK_SFX).toURI().toString());
    private final String FIRE_SFX = "Assets\\SFX\\fire.mp3";
    private final AudioClip FIRE_AUDIO = new AudioClip(new File(FIRE_SFX).toURI().toString());
    private final String DOUBLEMOVE_SFX = "Assets\\SFX\\doublemove.mp3";
    private final AudioClip DOUBLEMOVE_AUDIO = new AudioClip(new File(DOUBLEMOVE_SFX).toURI().toString());
    private final String ICE_SFX = "Assets\\SFX\\ice.mp3";
    private final AudioClip ICE_AUDIO = new AudioClip(new File(ICE_SFX).toURI().toString());
    private final String RETURN_SFX = "Assets\\SFX\\return.mp3";
    private final AudioClip RETURN_AUDIO = new AudioClip(new File(RETURN_SFX).toURI().toString());
    private final String MAIN_MENU_SFX = "Assets\\SFX\\mainmenu.mp3";
    private final AudioClip MAIN_MENU_AUDIO = new AudioClip(new File(MAIN_MENU_SFX).toURI().toString());

    @FXML
    private VBox cards;
    @FXML
    private Pane tiles;
    @FXML
    private Pane players;
    @FXML
    private Pane controls;
    @FXML
    private Button drawButton;
    @FXML
    private Pane fixed;
    @FXML
    private Pane profile;
    @FXML
    private Pane profilePic;
    @FXML
    private VBox confirmation;
    @FXML
    private StackPane boardArea;
    @FXML
    private Text profileName;
    @FXML
    private Text gameStateText;

    private int width;
    private int height;
    public Phase phase;
    private GameLogic gameLogic;
    public static int tileWidth;
    private Profile[] profiles = new Profile[4];
    private double[] playerRotations = {0, 0, 0, 0};
    private Random rng = new Random();

    /***
     * Gets all resources for gameScreen
     * @param url Url for resources
     * @param rb pack of resources
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (getInitData() != null) {
            try {
                if (getInitData().get("isLoadedFile").equals("true")) {
                    loadGame();
                } else {
                    startNewGame();
                }
                int width = (int) Screen.getPrimary().getBounds().getHeight();
                int height = (int) Screen.getPrimary().getBounds().getHeight();
                int maxTileWidth = width / (gameLogic.getWidth() + 3);
                int maxTileHeight = height / (gameLogic.getHeight() + 3);
                tileWidth = Math.min(maxTileHeight, maxTileWidth);
                boardArea.setTranslateY(tileWidth);
                updateBoard();
                mainLoop();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
    The flow though this window goes like this
    mainLoop() : will check the current phase and update the visuals
        depending on the phase it will make a selection of controls visible
    onSomeButton() : when a button is pressed i.e. drawButton, it will have an
        onDrawButton called which will send the user choice to gameLogic the
        call back to mainLoop.
    mainLoop() -> show buttons -> wait for call back from button -> mainLoop()
     */
    private void mainLoop() throws IOException {
        // Update current player

        for (int i = 0; i < gameLogic.getNumberOfPlayers(); i++) {
            String profileName = getInitData().get("Profile" + (i));
            profiles[i] = Profile.readProfile(profileName);
        }

        profile.getChildren().clear();
        profilePic.getChildren().clear();
        profile.getChildren().add(Assets.getProfile(profiles[gameLogic.getPlayersTurn()])); //the profile image
        profileName.setText(profiles[gameLogic.getPlayersTurn()].getName()); //sets the current players turn to text
        profilePic.getChildren().add(Assets.getCar(gameLogic.getPlayersTurn())); //adds car next to player
        try {
            updateBoard();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        phase = gameLogic.getGamePhase();
        hideAllControls();
        if (gameLogic.getCurrentTurnPlayer() instanceof ComputerPlayer) {
            gameStateText.setText("The computer is thinking...");
            ComputerPlayer computerPlayer = (ComputerPlayer) gameLogic.getCurrentTurnPlayer();
            Timeline computerThinkTL = new Timeline();
            computerThinkTL.getKeyFrames().add(new KeyFrame(Duration.seconds(2), actionEvent -> {
                try {
                    gameLogic.draw();
                    DRAW_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
                    if (gameLogic.getGamePhase() == Phase.FLOOR) {
                        cards.getChildren().removeIf(Objects::isNull);
                    }
                    if (gameLogic.drawnCard() != null) {
                        gameStateText.setText("Computer got a " + gameLogic.drawnCard().getType() + " tile");
                        cards.getChildren().add(Assets.createCard(gameLogic.drawnCard()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

            computerThinkTL.getKeyFrames().add(new KeyFrame(Duration.seconds(4), actionEvent -> {
                if (gameLogic.getGamePhase() == Phase.FLOOR) {
                    FloorTile drawnTile = (FloorTile) gameLogic.drawnCard();
                    drawnTile.setRotation(getRandomRotation());
                    Coordinate computerMove = computerPlayer.calculateFloorTilePlacement();
                    if (computerMove == null) {
                        gameStateText.setText("The computer was not able to place the floor tile it drew");
                    } else {
                        gameLogic.floor(drawnTile, computerMove);
                        gameStateText.setText("The computer placed a floor tile");
                    }
                    updateBoard();
                } else {
                    gameStateText.setText("The computer drew an action tile");
                    hideAllControls();
                    ActionTile[] tiles = gameLogic.getActionCards();
                    // Add the recently drawn action tile, grayed out
                    if (gameLogic.drawnCard() != null) {
                        Node drawnCard = Assets.createCard(gameLogic.drawnCard());
                        ColorAdjust dim = new ColorAdjust();
                        dim.setBrightness(0.6);
                        dim.setSaturation(0.5);
                        drawnCard.setEffect(dim);
                        drawnCard.setOnMouseEntered((e) -> {
                        });
                        drawnCard.setOnMouseExited((e) -> {
                        });
                        cards.getChildren().add(drawnCard);
                    }
                    // Add the remaining action tiles
                    for (ActionTile tile : tiles) {
                        final Node vCard = Assets.createCard(tile);
                        vCard.setOnMouseEntered((e) -> {
                        });
                        vCard.setOnMouseExited((e) -> {
                        });
                        cards.getChildren().add(vCard);
                    }
                }
            }));


            computerThinkTL.getKeyFrames().add(new KeyFrame(Duration.seconds(4), actionEvent -> {
                hideAllControls();
                ActionTile[] tiles = gameLogic.getActionCards();
                // Add the recently drawn action tile, grayed out
                if (gameLogic.drawnCard() != null) {
                    Node drawnCard = Assets.createCard(gameLogic.drawnCard());
                    ColorAdjust dim = new ColorAdjust();
                    dim.setBrightness(0.6);
                    dim.setSaturation(0.5);
                    drawnCard.setEffect(dim);
                    drawnCard.setOnMouseEntered((e) -> {
                    });
                    drawnCard.setOnMouseExited((e) -> {
                    });
                    cards.getChildren().add(drawnCard);
                }
                // Add the remaining action tiles
                for (ActionTile tile : tiles) {
                    final Node vCard = Assets.createCard(tile);
                    vCard.setOnMouseEntered((e) -> {
                    });
                    vCard.setOnMouseExited((e) -> {
                    });
                    cards.getChildren().add(vCard);
                }
                gameStateText.setText("The computer is\nusing action tile");
            }));

            computerThinkTL.getKeyFrames().add(new KeyFrame(Duration.seconds(6), actionEvent -> {
                Triplet<ActionTile, Coordinate, Integer> computerMove = computerPlayer.calculateActionTile();
                if (computerMove.getValue0() == null) {
                    if (computerPlayer.getInventory().size() == 0) {
                        gameStateText.setText("The computer has no\naction tiles to use");
                    } else {
                        gameStateText.setText("The computer skips using\nan action tile");
                    }
                } else {
                    gameStateText.setText("The computer uses a " + computerMove.getValue0().getType());
                }
                gameLogic.action(computerMove.getValue0(), computerMove.getValue1(), computerMove.getValue2());
                updateBoard();
            }));

            computerThinkTL.getKeyFrames().add(new KeyFrame(Duration.seconds(8), actionEvent -> {
                Coordinate destination = computerPlayer.calculateMovePhase();
                if (gameLogic.isDoubleMove()) {
                    gameLogic.move(destination);
                    destination = computerPlayer.calculateMovePhase();
                }
                gameLogic.move(destination);
                updateBoard();
                gameStateText.setText("The computer\nfinished moving");
            }));

            computerThinkTL.getKeyFrames().add(new KeyFrame(Duration.seconds(10), actionEvent -> {
                try {
                    mainLoop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            computerThinkTL.play();
        } else {
            switch (phase) {
                case DRAW:
                    drawButton.setOnAction((e2) -> {
                        try {
                            onDrawButton();
                            e2.consume();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    drawButton.setText("Draw");
                    drawButton.setVisible(true);
                    gameStateText.setText(profiles[gameLogic.getPlayersTurn()].getName() + ", you must draw a tile");
                    break;
                case FLOOR:
                    try {
                        gameStateText.setText(profiles[gameLogic.getPlayersTurn()].getName() + ", you must place a floor tile");
                        setupFloorPhase();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ACTION:
                    try {
                        setupActionPhase();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MOVE:
                    try {
                        setupMovePhase();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case WIN:
                    try {
                        setupWinScreen();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    System.out.print("Invalid game State");
            }
        }
    }

    private Rotation getRandomRotation() {
        return switch (rng.nextInt(4)) {
            case 0 -> UP;
            case 1 -> RIGHT;
            case 2 -> DOWN;
            case 3 -> LEFT;
            default -> throw new IllegalStateException("Unexpected value when choosing random direction.");
        };
    }

    private void setupWinScreen() throws Exception {
        WindowLoader wl = new WindowLoader(drawButton);
        getInitData().put("Winner", gameLogic.getWinner() + "");
        wl.load("WinScreen", getInitData());
        WIN_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
    }

    private void setupFloorPhase() throws Exception {
        ArrayList<Coordinate> locations = gameLogic.getSlideLocations();
        if (locations.size() == 0) {
            gameLogic.floor(null,null);
        }

        for (Coordinate coordinate : locations) {
            ImageView arrow = Assets.makeArrow();
            final Rotation direction;
            final int where;
            if (coordinate.getX() == -1) {
                arrow.setRotate(0);
                direction = Rotation.RIGHT;
                where = coordinate.getY();
            } else if (coordinate.getY() == -1) {
                arrow.setRotate(90);
                direction = Rotation.DOWN;
                where = coordinate.getX();
            } else if (coordinate.getX() == width) {
                arrow.setRotate(180);
                direction = Rotation.LEFT;
                where = coordinate.getY();
            } else if (coordinate.getY() == height) {
                arrow.setRotate(270);
                direction = UP;
                where = coordinate.getX();
            } else {
                direction = Rotation.DOWN;
                where = 2;
                // Invalid Push location
                assert (false);
            }
            arrow.setFitWidth(tileWidth);
            arrow.setFitHeight(tileWidth);
            arrow.setTranslateX(coordinate.getX() * tileWidth);
            arrow.setTranslateY(coordinate.getY() * tileWidth);

            arrow.setOnMouseClicked((e) -> {
                FloorTile playerTileChoice = (FloorTile) gameLogic.drawnCard();
                Node choiceCard = cards.getChildren().get(0).lookup("#image");
                double rotation = choiceCard.getRotate();
                if (rotation >= 0) {
                    playerTileChoice.setRotation(UP);
                }
                if (rotation >= 90) {
                    playerTileChoice.setRotation(RIGHT);
                }
                if (rotation >= 180) {
                    playerTileChoice.setRotation(DOWN);
                }
                if (rotation >= 270) {
                    playerTileChoice.setRotation(LEFT);
                }
                shiftTiles(direction, where, playerTileChoice);
                try {
                    FLOOR_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
                    gameLogic.floor(playerTileChoice, coordinate);
                    mainLoop();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

            });
            controls.getChildren().add(arrow);
        }
        cards.getChildren().removeIf(Objects::isNull);
        if (gameLogic.drawnCard() != null) {
            cards.getChildren().add(Assets.createCard(gameLogic.drawnCard()));
        }
    }

    private void shiftTiles(Rotation direction, int location, FloorTile newTile) {
        // Add in new tile.
        Coordinate newTileLocation;
        switch (direction) {
            case UP:
                newTileLocation = new Coordinate(location, height);
                break;
            case LEFT:
                newTileLocation = new Coordinate(width, location);
                break;
            case DOWN:
                newTileLocation = new Coordinate(location, -1);
                break;
            case RIGHT:
                newTileLocation = new Coordinate(-1, location);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
        Pane tileView = Assets.getFloorTileImage(newTile, newTileLocation);
        tiles.getChildren().add(tileView);

        // Shift players
        applyToAll(players, player -> {
            int x = ((int) player.getTranslateX()) / tileWidth;
            int y = ((int) player.getTranslateY()) / tileWidth;
            TranslateTransition smooth = new TranslateTransition();
            RotateTransition rotate = new RotateTransition();
            smooth.setNode(player);
            rotate.setNode(player);
            smooth.setDuration(new Duration(200));
            switch (direction) {
                case RIGHT:
                    if (y == location) {
                        if (x >= width - 1) {

                            smooth.setToX(0);
                            smooth.setDuration(new Duration(600));
                            //tiles.getChildren().remove(player);
                            //tiles.getChildren().add(player);
                        } else {
                            smooth.setByX(tileWidth);
                        }
                    }
                    break;
                case LEFT:
                    if (y == location) {
                        if (x <= 0) {
                            smooth.setToX(tileWidth * (width - 1));
                            smooth.setDuration(new Duration(600));
                        } else {
                            smooth.setByX(-tileWidth);
                        }
                    }
                    break;
                case DOWN:
                    if (x == location) {
                        if (y == height - 1) {
                            smooth.setToY(0);
                            smooth.setDuration(new Duration(600));

                        } else {
                            smooth.setByY(tileWidth);
                        }
                    }
                    break;
                case UP:
                    if (x == location) {
                        if (y == 0) {
                            smooth.setToY(tileWidth * (height - 1));
                            smooth.setDuration(new Duration(600));
                        } else {
                            smooth.setByY(-tileWidth);
                        }
                    }
            }
            smooth.play();
            return 0;
        });
        // Shift all tiles

        applyToAll(tiles, tile -> {
            int x = ((int) tile.getTranslateX()) / tileWidth;
            int y = ((int) tile.getTranslateY()) / tileWidth;
            TranslateTransition smooth = new TranslateTransition();
            smooth.setNode(tile);
            smooth.setDuration(new Duration(200));
            FadeTransition smoothVanish = new FadeTransition(new Duration(200));
            smoothVanish.setFromValue(100);
            smoothVanish.setToValue(0);
            switch (direction) {
                case RIGHT:
                    if (y == location) {
                        smooth.setByX(tileWidth);
                        if (x >= width - 1) {
                            smoothVanish.setNode(tile);
                        }
                    }
                    break;
                case DOWN:
                    if (x == location) {
                        smooth.setByY(tileWidth);
                        if (y == height - 1) {
                            smoothVanish.setNode(tile);
                        }
                    }
                    break;
                case LEFT:
                    if (y == location) {
                        smooth.setByX(-tileWidth);
                        if (x <= 0) {
                            smoothVanish.setNode(tile);
                        }
                    }
                    break;
                case UP:
                    if (x == location) {
                        smooth.setByY(-tileWidth);
                        if (y <= 0) {
                            smoothVanish.setNode(tile);
                        }
                    }
            }

            smooth.play();
            smoothVanish.play();
            return 0;

        });
    }

    private void updateBoard() {
        tiles.setPrefWidth((width + 4) * tileWidth);
        tiles.setPrefHeight((height + 4) * tileWidth);
        controls.setPrefWidth((width + 4) * tileWidth);
        controls.setPrefHeight((height + 4) * tileWidth);
        players.setPrefHeight((height + 4) * tileWidth);
        players.setPrefWidth((width + 4) * tileWidth);
        fixed.setPrefWidth((width + 4) * tileWidth);
        fixed.setPrefHeight((width + 4) * tileWidth);

        tiles.getChildren().clear();
        fixed.getChildren().clear();
        players.getChildren().clear();
        controls.getChildren().clear();

        // showing the tiles
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                // Tile from the game board.
                FloorTile tile = gameLogic.getTileAt(new Coordinate(x, y));
                if(tile == null) {
                    System.out.println(x + " " + y);
                }
                // What is going to be shown on screen
                // Get correct image
                if (tile != null) {
                    Node tileView = Assets.getFloorTileImage(tile, x, y);
                    tiles.getChildren().add(tileView);
                }

            }

        }
        // showing the player locations
        for (int i = 0; i < gameLogic.getPlayerLocations().length; i++) {
            Coordinate location = gameLogic.getPlayerLocations()[i];
            ImageView playerView = Assets.getPlayer(i);
            playerView.setTranslateX(location.getX() * tileWidth);
            playerView.setTranslateY(location.getY() * tileWidth);
            playerView.setRotate(playerRotations[i]);
            players.getChildren().add(playerView);
        }

    }

    /**
     * Clears the game and starts a new one with given starting board
     *
     * @throws Exception if issue with board file.
     */
    public void startNewGame() throws Exception {
        gameLogic = new GameLogic(Integer.parseInt(getInitData().get("Seed")));
        GameSave gameSave = new GameSave(getInitData());
        gameLogic.newGame(getInitData().get("Board"), gameSave, getComputerPlayerIndices());
        gameLogic.setNumberOfPlayers(Integer.parseInt(getInitData().get("PlayerCount")));
        width = gameLogic.getWidth();
        height = gameLogic.getHeight();
    }

    /**
     * Determines which profiles in the initData are computer players.
     * @return zero-based indices of the computer players as integer array
     */
    private int[] getComputerPlayerIndices() {
        HashSet<Integer> computerPlayers = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            String profileName = getInitData().get("Profile" + i);
            if (profileName != null && profileName.startsWith("Computer ")) {
                computerPlayers.add(i);
            }
        }
        // Turns the HashSet into a primitive int array
        int[] array = new int[computerPlayers.size()];
        int i = 0;
        for (Integer computerPlayer : computerPlayers) {
            array[i++] = computerPlayer;
        }
        return array;
    }

    private void loadGame() throws Exception {
        Pair<GameLogic, Profile[]> result = GameLoad.loader(getInitData());
        gameLogic = result.getKey();
        Profile[] profiles = result.getValue();
        for (int i = 0; i < profiles.length; i++) {
            getInitData().put("Profile" + i, profiles[i].getName());
        }
        width = gameLogic.getWidth();
        height = gameLogic.getHeight();
        gameLogic.setNumberOfPlayers(Integer.parseInt(getInitData().get("PlayerCount")));
        gameLogic.emptyGameSaver();
    }

    /**
     * hides all controls ready for when mainloop shows the correct ones.
     */
    private void hideAllControls() {
        // Draw controls
        drawButton.setVisible(false);
        cards.getChildren().clear();
        controls.getChildren().clear();
    }

    private void setupActionPhase() throws Exception {
        cards.getChildren().clear();
        ActionTile[] tiles = gameLogic.getActionCards();
        // Add a skip button

        drawButton.setText("Skip");
        drawButton.setVisible(true);
        drawButton.setOnAction(e1 -> {
            try {
                skipActionOnCLick();
                e1.consume();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        if (gameLogic.drawnCard() != null) {
            Node drawnCard = Assets.createCard(gameLogic.drawnCard());
            ColorAdjust dim = new ColorAdjust();
            dim.setBrightness(0.6);
            dim.setSaturation(0.5);
            drawnCard.setEffect(dim);
            drawnCard.setOnMouseClicked((e) -> {
            });
            drawnCard.setOnMouseEntered((e) -> {
            });
            drawnCard.setOnMouseExited((e) -> {
            });
            cards.getChildren().add(drawnCard);
        }
        for (ActionTile tile : tiles) {
            gameStateText.setText(profiles[gameLogic.getPlayersTurn()].getName() + ", you can use an action card!");
            final Node vCard = Assets.createCard(tile);
            vCard.setOnMouseClicked((e) -> {
            });
            cards.getChildren().add(vCard);
            switch (tile.getType()) {
                case DOUBLE_MOVE:
                    vCard.setOnMouseClicked((e) -> {
                        vCard.setEffect(new Bloom(0.03));
                        vCard.setOnMouseClicked(e2 -> {
                            try {
                                doubleMoveAction();
                                DOUBLEMOVE_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        });
                    });
                    break;
                case BACKTRACK:
                    vCard.setOnMouseClicked((e) -> {
                        hideAllControls();
                        // Get all players that can be back tracked
                        boolean[] validPlayers = gameLogic.getPlayersThatCanBeBacktracked();
                        for (int i = 0; i < gameLogic.getNumberOfPlayers(); i++) {
                            if (validPlayers[i]) {
                                // For each valid player
                                final int playerNumber = i;
                                // make them glow
                                Node fakePlayer = Assets.getPlayer(i);
                                fakePlayer.setTranslateX(gameLogic.getPlayerLocations()[i].getX() * tileWidth);
                                fakePlayer.setTranslateY(gameLogic.getPlayerLocations()[i].getY() * tileWidth);
                                fakePlayer.setRotate(playerRotations[playerNumber]);
                                fakePlayer.setEffect(new Bloom(0.1));
                                controls.getChildren().add(fakePlayer);
                                // Set them into an active button
                                fakePlayer.setOnMouseClicked(e2 -> {
                                    hideAllControls();
                                    try {
                                        gameLogic.action(new ActionTile(BACKTRACK), null, playerNumber);
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    }
                                    try {
                                        updateBoard();
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    }
                                    try {
                                        mainLoop();
                                        BACKTRACK_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
                                    } catch (IOException ioException) {
                                        ioException.printStackTrace();
                                    }
                                });
                            }

                        }
                    });

                    break;
                case FIRE:
                    vCard.setOnMouseClicked((e) -> {
                        hideAllControls();
                        Node fire = Assets.getFireEffect();
                        controls.getChildren().add(fire);
                        FIRE_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
                        controls.setOnMouseMoved((e2) -> {
                            LocationSelectOnClick(fire, e2, FIRE);
                        });

                    });
                    break;
                case FROZEN:
                    vCard.setOnMouseClicked((e) -> {
                        hideAllControls();
                        Node frozen = Assets.getFrozenEffect();
                        controls.getChildren().add(frozen);
                        ICE_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
                        controls.setOnMouseMoved((e2) -> {
                            LocationSelectOnClick(frozen, e2, FROZEN);
                        });
                    });
                    break;
            }
        }
    }

    /**
     * Sets up what happens when a fire / frozen card is clicked
     * so that now you can choose a location on the board
     *
     * @param card     the card that has been clicked on
     * @param e2       the mouse event
     * @param tileType what tile type it is.
     */
    private void LocationSelectOnClick(Node card, MouseEvent e2, TileType tileType) {
        int getX = (int) (e2.getX() / tileWidth);
        int getY = (int) (e2.getY() / tileWidth);
        if (getX < 1) {
            getX = 1;
        }
        if (getX > width - 2) {
            getX = width - 2;
        }
        if (getY < 1) {
            getY = 1;
        }
        if (getY > height - 2) {
            getY = height - 2;
        }
        if (tileType == FIRE) {
            // If person on location end method
            for (int shiftX = -1; shiftX <= 1; shiftX++) {
                for (int shiftY = -1; shiftY <= 1; shiftY++) {
                    Coordinate locationToCheck = new Coordinate(getX + shiftX, getY + shiftY);
                    for (Coordinate person : gameLogic.getPlayerLocations()) {
                        if (person.equals(locationToCheck)) {
                            return;
                        }
                    }
                }
            }
        }
        final int x = getX;
        final int y = getY;
        card.setTranslateX((getX - 1) * tileWidth);
        card.setTranslateY((getY - 1) * tileWidth);
        card.setOnMouseClicked((e3) -> {
            try {
                gameLogic.action(new ActionTile(tileType), new Coordinate(x, y), -1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mainLoop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void setupMovePhase() throws Exception {

        TranslateTransition walk = new TranslateTransition();
        RotateTransition rotate = new RotateTransition();
        int currentPlayer = gameLogic.getPlayersTurn();
        Coordinate initLocation = gameLogic.getPlayerLocations()[currentPlayer];

        Coordinate[] validLocations = gameLogic.getMoveLocations();
        if (validLocations.length == 0) {
            // No where to move;
            gameStateText.setText(profiles[currentPlayer].getName() + ", you cannot move! End your turn");
            gameLogic.move(gameLogic.getPlayerLocations()[currentPlayer]);
            playerRotations[currentPlayer] = playerRotations[currentPlayer];
            mainLoop();
        }
        for (Coordinate coordinate : validLocations) {
            // Create pointer.
            final ImageView pointer = Assets.getLocationArrow();
            // Move to correct location.
            pointer.setTranslateX(coordinate.getX() * tileWidth);
            pointer.setTranslateY(coordinate.getY() * tileWidth);
            gameStateText.setText(profiles[currentPlayer].getName() + ", select a location to " +
                    "move to");
            pointer.setOnMouseClicked(e -> {
                Node currentPlayerNode = players.getChildren().get(gameLogic.getPlayersTurn());
                try {
                    gameLogic.move(coordinate);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                controls.getChildren().clear();
                rotate.setDuration(new Duration(800));

                Coordinate location = gameLogic.getPlayerLocations()[currentPlayer];
                if (location.getX() > initLocation.getX()) {
                    rotate.setToAngle(270);
                    playerRotations[currentPlayer] = 270;
                }
                if (location.getX() < initLocation.getX()) {
                    rotate.setToAngle(90);
                    playerRotations[currentPlayer] = 90;
                }
                if (location.getY() < initLocation.getY()) {
                    rotate.setToAngle(180);
                    playerRotations[currentPlayer] = 180;
                }
                if (location.getY() > initLocation.getY()) {
                    rotate.setToAngle(0);
                    playerRotations[currentPlayer] = 0;
                }
                rotate.setNode(currentPlayerNode);
                rotate.setOnFinished((e2) -> {
                    try {
                        currentPlayerNode.setRotate((rotate.getToAngle()));
                        walk.setToX(coordinate.getX() * tileWidth);
                        walk.setToY(coordinate.getY() * tileWidth);
                        walk.setNode(currentPlayerNode);
                        walk.setDuration(new Duration(500));
                        walk.play();
                        removeAll("locationarrow");
                        walk.setOnFinished((e3) -> {
                            try {
                                MOVEMENT_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
                                playerRotations[currentPlayer] = rotate.getToAngle();

                                mainLoop();
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        });
                    } catch (Exception rotateException) {
                        rotateException.printStackTrace();
                    }
                });
                rotate.play();
            });
            controls.getChildren().add(pointer);
        }

    }

    /**
     * Called when Draw button is pressed
     */
    public void onDrawButton() throws IOException {
        gameStateText.setText(null); //sets the current player draw tile text to nothing
        gameLogic.draw();
        DRAW_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
        mainLoop();
    }

    /***
     * Quits to main menu unless the game is unsaved.
     */
    public void onQuitButton() {
        if (gameLogic.isGameSaved()) {
            WindowLoader wl = new WindowLoader(drawButton);
            wl.load("MenuScreen", getInitData());
            RETURN_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
        } else {
            confirmation.setVisible(true);
            confirmation.setMouseTransparent(false);
        }
    }

    /**
     * Saves the game and quits to main menu
     */
    public void onYes() {
        try {
            gameLogic.saveGame();
            MAIN_MENU_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Game NOT saved");
        }
        System.out.println("Game Saved");
        WindowLoader wl = new WindowLoader(drawButton);
        wl.load("MenuScreen", getInitData());
    }

    /**
     * quits to main menu without saving
     */
    public void onNo() {
        WindowLoader wl = new WindowLoader(drawButton);
        wl.load("MenuScreen", getInitData());
        MAIN_MENU_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
    }

    /***
     * Starts save game window.
     */
    public void onSaveButton() {
        try {
            gameLogic.saveGame();
            MAIN_MENU_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Game NOT saved");
        }
        System.out.println("Game Saved");
    }

    public void onAboutMenuButton() {
        try {
            gameLogic.saveGame();
            // Mark this save file as a "loaded" file
            // so when returning to the game screen
            // the GameScreenController will load file from disk
            getInitData().put("isLoadedFile", "true");
            WindowLoader wl = new WindowLoader(drawButton);
            wl.load("/HowToPlay/InGameHowToPlay/InGameHowToPlay", getInitData());
            MAIN_MENU_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Game NOT saved");
        }
        System.out.println("Game Saved");
    }


    private void applyToAll(Pane group, Function<Node, Integer> f) {
        for (Object o : group.getChildren()) {
            if (o == null) {
                continue;
            }
            Node n = (Node) o;
            if (n.getId() == null) {
                continue;
            }
            f.apply(n);
        }
    }

    private void removeAll(String id) {
        tiles.getChildren().removeIf(Objects::isNull);
        tiles.getChildren().removeIf(n -> {
            if (n.getId() == null) {
                return false;
            } else {
                return n.getId().contains(id);
            }
        });
    }

    private void skipActionOnCLick() throws Exception {
        gameLogic.action(null, null, 0);
        mainLoop();
        SKIP_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
    }

    private void doubleMoveAction() throws Exception {
        gameLogic.action(new ActionTile(DOUBLE_MOVE), null, 0);
        for (Node player : players.getChildren()) {
            player.setEffect(new Bloom(999));
            player.setOnMouseClicked(e3 -> {
            });
        }
        mainLoop();
    }
}

package FrontEnd;

import BackEnd.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Screen;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * @author Liam B, Samuel Fuller
 * @version 1.0
 * @since 1.0
 */
public class LevelEditorController extends StateLoad {

    @FXML
    private Slider straightSlider;
    @FXML
    private TextField straightInBox;
    @FXML
    private Slider tshapeSlider;
    @FXML
    private TextField tshapeInBox;
    @FXML
    private Slider conerSlider;
    @FXML
    private TextField cornerInBox;
    @FXML
    private Slider fireSlider;
    @FXML
    private TextField fireInBox;
    @FXML
    private Slider iceSlider;
    @FXML
    private TextField iceInBox;
    @FXML
    private Slider doublemoveSlider;
    @FXML
    private TextField doublemoveInBox;
    @FXML
    private Slider backtrackSlider;
    @FXML
    private TextField backtrackInBox;


    @FXML
    private ImageView straightImage;
    @FXML
    private ImageView goalImage;
    @FXML
    private ImageView cornerImage;
    @FXML
    private ImageView tshapeImage;
    @FXML
    private RadioButton fixRB;
    @FXML
    private RadioButton rotateRB;
    @FXML
    private RadioButton removeRB;
    @FXML
    private ImageView p1Image;
    @FXML
    private ImageView p2Image;
    @FXML
    private ImageView p3Image;
    @FXML
    private ImageView p4Image;

    @FXML
    private VBox errorMsgBox;
    @FXML
    private Text errorMsgText;

    @FXML
    private GridPane boardGridPane;

    private final Pane[] playerSpawnPanes = new Pane[4];
    /**
     * Contains the image views within each pane.
     * Each value within this HashMap is an array where:
     *
     * element 0 is the tile ImageView,
     * element 1 is the fixed ImageView, and
     * element 2 is the car ImageView.
     */
    private final HashMap<Pane, ImageView[]> imageViews = new HashMap<>();

    private GameboardEditor editor;
    private CustomBoard customBoard;

    /**
     * Sets up the level editor
     * @param location  The location used to resolve relative paths for the root object, or
     *                  <tt>null</tt> if the location is not known.
     * @param resources The resources used to localize the root object, or <tt>null</tt> if
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // initialize is called twice, once automatically by JavaFX and again
        // in WindowLoader.load (line 59). The second time around getInitData
        // will actually return a HashMap. Hence we must check to see
        // if getInitData isn't null before we can retrieve data from it.
        if (getInitData() != null) {
            if (getInitData().get("Custom Board Is New").equals("true")) {
                int customWidth = Integer.parseInt(getInitData().get("Custom Board Width"));
                int customHeight = Integer.parseInt(getInitData().get("Custom Board Height"));
                customBoard = new CustomBoard();
                customBoard.setXSize(customWidth);
                customBoard.setYSize(customHeight);
            } else {
                String customBoardName = getInitData().get("Custom Board Name");
                customBoard = GameboardEditor.loadFile("./Gameboards/" + customBoardName + ".txt");
            }
            editor = new GameboardEditor(customBoard);
            // Bunch of calculations to work out the proper size of the tile
            int screenWidth = (int) Screen.getPrimary().getBounds().getHeight();
            int screenHeight = (int) Screen.getPrimary().getBounds().getHeight();
            int boardWidth = customBoard.getXSize();
            int boardHeight = customBoard.getYSize();
            int maxTileWidth = screenWidth / (boardWidth + 9);
            int maxTileHeight = screenHeight / (boardHeight + 9);
            int tileSize = Math.min(maxTileHeight, maxTileWidth);

            // Loop through the rows of the board
            for (int y = 0; y < boardHeight; y++) {
                // Add row constraints to this row
                boardGridPane.getRowConstraints().add(new RowConstraints(tileSize, tileSize, tileSize));
                // Loop through the columns of the row
                for (int x = 0; x < boardWidth; x++) {
                    // Add column constraints to this column
                    boardGridPane.getColumnConstraints().add(new ColumnConstraints(tileSize, tileSize, tileSize));

                    StackPane pane = new StackPane();
                    imageViews.put(pane, new ImageView[3]);

                    FloorTile currentTile = customBoard.getTileAt(x, y);
                    if (currentTile != null) {
                        // A tile exists here; create an image view containing the tile's picture
                        String tileName = currentTile.getType().name().toLowerCase();
                        setPaneTileImage(pane, tileName, tileSize, currentTile.getRotation());
                        setPaneFixedImage(pane, currentTile.isFixed(), tileSize);
                    } else {
                        // No tile exists; create an image view with an empty tile
                        setPaneEmptyImage(pane, tileSize);
                    }

                    // Add the pane containing the images to the board grid pane
                    // at the given column and row
                    boardGridPane.add(pane, x, y);

                    // Lambda expressions require variables to be final, so copy the
                    // coordinates to final variables before reading them
                    final int finalX = x;
                    final int finalY = y;
                    final Coordinate coordinates = new Coordinate(x, y);

                    // Add an event handler when something gets dragged on top of the pane
                    pane.setOnDragOver((DragEvent event) -> {
                        if (event.getGestureSource() != pane && event.getDragboard().hasString()) {
                            String dbContent = event.getDragboard().getString();
                            // Make sure a tile or car is being dragged onto this pane
                            if (dbContent.equals("straight") || dbContent.equals("t_shape") ||
                                    dbContent.equals("corner") || dbContent.equals("goal")) {
                                event.acceptTransferModes(TransferMode.COPY);
                            } else if (dbContent.startsWith("player ")) {
                                // Make sure this isn't a goal tile they're being dragged on to
                                FloorTile thisTile = customBoard.getTileAt(finalX, finalY);
                                if (thisTile == null || thisTile.getType() != TileType.GOAL) {
                                    // Make sure this isn't a player they're being dragged on to
                                    if (paneIsNotPlayerSpawn(pane)) {
                                        event.acceptTransferModes(TransferMode.MOVE);
                                    }
                                }
                            }
                        }
                        event.consume();
                    });

                    // Add an event handler when something gets dropped on top of the pane
                    pane.setOnDragDropped((DragEvent event) -> {
                        Dragboard db = event.getDragboard();
                        if (db.hasString()) {
                            // Determine if this is a player or tile we're dealing with
                            if (db.getString().startsWith("player ")) {
                                // Get the number of the player that got dragged in
                                int playerNum = Integer.parseInt(db.getString().substring(7)) - 1;

                                if (editor.setPlayerPosition(playerNum, coordinates)) {
                                    moveCarImage(playerNum, pane, tileSize);
                                }

                                System.out.printf("Moved player %d to (%d, %d)%n", playerNum + 1, finalX, finalY);
                            } else {
                                // Get the name of the new tile
                                String newTileName = db.getString();

                                // Stick it in the board editor
                                TileType tileType = TileType.valueOf(newTileName.toUpperCase());

                                FloorTile tileToAdd = new FloorTile(tileType);
                                tileToAdd.setLocation(coordinates);



                                // Adds tile if it was added successfully
                                if (editor.putTile(tileToAdd)) {
                                    // Remove all other images and add this new tile to the pane
                                    setPaneTileImage(pane, newTileName, tileSize, Rotation.UP);
                                    setPaneFixedImage(pane, tileToAdd.isFixed(), tileSize);
                                    System.out.printf("The tile at (%d, %d) is now a %s%n", finalX, finalY, newTileName);
                                }
                            }
                        } else {
                            event.setDropCompleted(false);
                        }
                        event.consume();
                    });

                    // Add an event handler when the pane is clicked
                    pane.setOnMouseClicked((MouseEvent event) -> {

                        if (fixRB.isSelected()) {
                            // Fix or unfix this tile
                            FloorTile tile = customBoard.getTileAt(finalX, finalY);
                            if (tile != null) {
                                if (tile.getType() != TileType.GOAL) {
                                    tile.setFixedBool(!tile.isFixed());
                                    setPaneFixedImage(pane, tile.isFixed(), tileSize);
                                    System.out.printf(
                                            tile.isFixed() ?
                                            "Tile (%d, %d) was fixed%n" :
                                            "Tile (%d, %d) was unfixed%n",
                                            finalX, finalY
                                    );
                                } else {
                                    System.out.printf("Goal tile on (%d, %d) can't be unfixed%n", finalX, finalY);
                                }

                            }
                        } else if (rotateRB.isSelected()) {
                            // Rotate this tile
                            FloorTile tile = customBoard.getTileAt(finalX, finalY);
                            if (tile != null) {
                                tile.setRotation(tile.getRotation().clockwise());
                                String tileName = tile.getType().toString().toLowerCase();
                                setPaneTileImage(pane, tileName, tileSize, tile.getRotation());

                                System.out.printf("Tile (%d, %d) was rotated to %s%n",
                                        finalX, finalY, tile.getRotation()
                                );
                            }
                        } else if (removeRB.isSelected()) {
                            // Remove this tile
                            if (customBoard.getTileAt(finalX, finalY) != null) {
                                setPaneEmptyImage(pane, tileSize);
                                setPaneFixedImage(pane, false, tileSize);
                                editor.removeTileOnPosition(coordinates);
                                System.out.printf("Tile (%d, %d) was removed%n", finalX, finalY);
                            }
                        }
                    });

                    // Check to see if a player spawn point is here
                    for (int i = 0; i < 4; i++) {
                        Coordinate spawnPoint = customBoard.getPlayerSpawnPoint(i);
                        if (spawnPoint != null && spawnPoint.equals(coordinates)) {
                            // Put a car here
                            moveCarImage(i, pane, tileSize);
                        }
                    }
                }
            }

            straightSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                    straightInBox.setText(String.valueOf(Math.round((Double) newValue))));

            conerSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                    cornerInBox.setText(String.valueOf(Math.round((Double) newValue))));

            tshapeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                    tshapeInBox.setText(String.valueOf(Math.round((Double) newValue))));


            fireSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                    fireInBox.setText(String.valueOf(Math.round((Double) newValue))));

            iceSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                    iceInBox.setText(String.valueOf(Math.round((Double) newValue))));

            backtrackSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                    backtrackInBox.setText(String.valueOf(Math.round((Double) newValue))));

            doublemoveSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                    doublemoveInBox.setText(String.valueOf(Math.round((Double) newValue))));

            straightSlider.setValue(customBoard.getNumOfTileTypes(TileType.STRAIGHT));
            conerSlider.setValue((customBoard.getNumOfTileTypes(TileType.CORNER)));
            tshapeSlider.setValue((customBoard.getNumOfTileTypes(TileType.T_SHAPE)));
            fireSlider.setValue((customBoard.getNumOfTileTypes(TileType.FIRE)));
            iceSlider.setValue((customBoard.getNumOfTileTypes(TileType.FROZEN)));
            backtrackSlider.setValue((customBoard.getNumOfTileTypes(TileType.BACKTRACK)));
            doublemoveSlider.setValue((customBoard.getNumOfTileTypes(TileType.DOUBLE_MOVE)));

            // Ensure all input is numeric only
            setNumericInputOnly(straightInBox);
            setNumericInputOnly(cornerInBox);
            setNumericInputOnly(tshapeInBox);
            setNumericInputOnly(fireInBox);
            setNumericInputOnly(iceInBox);
            setNumericInputOnly(backtrackInBox);
            setNumericInputOnly(doublemoveInBox);
        }
    }

    /**
     * Forces a text field to accept numeric input only.
     * @param t The text field to modify
     */
    private void setNumericInputOnly(TextField t) {
        t.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                t.setText(oldValue);
            }
        });
    }

    /**
     * Determines whether the given pane is one that is also a player spawn point.
     * @param pane The pane to check.
     * @return True if the pane is not a player spawn point; false otherwise.
     */
    private boolean paneIsNotPlayerSpawn(Pane pane) {
        for (Pane spawnPane : playerSpawnPanes) {
            if (pane.equals(spawnPane)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets the ImageView within a pane to be an empty tile.
     * @param pane The pane to be emptied
     * @param size The size of the empty image
     */
    private void setPaneEmptyImage(Pane pane, int size) {
        setPaneTileImage(pane, "empty", size, Rotation.UP);
    }

    /**
     * Sets the ImageView within a pane to contain a floor tile.
     * @param pane The pane to change the ImageView of
     * @param newTile The name of the new floor tile
     * @param size The size of the tile image
     * @param rotation The rotation of the tile image
     */
    private void setPaneTileImage(Pane pane, String newTile, int size, Rotation rotation) {
        // Remove the already existing ImageView
        ImageView currentImageView = imageViews.get(pane)[0];
        if (currentImageView != null) {
            pane.getChildren().remove(currentImageView);
            imageViews.get(pane)[0] = null;
        }
        // Create and add the new ImageView
        ImageView newImg = createTileImageView(newTile, size, rotation);
        pane.getChildren().add(0, newImg);
        imageViews.get(pane)[0] = newImg;
    }

    /**
     * Shows or hides a fixed icon on a pane.
     * @param pane The pane to show a fixed icon on
     * @param isFixed True to show a fixed icon, false to hide it
     * @param size The size of the fixed icon.
     */
    private void setPaneFixedImage(Pane pane, boolean isFixed, int size) {
        if (isFixed) {
            if (imageViews.get(pane)[1] == null) {
                ImageView fixedImage = createTileImageView("fixed", size);
                pane.getChildren().add(fixedImage);
                imageViews.get(pane)[1] = fixedImage;
            }
        } else {
            ImageView fixedImageView = imageViews.get(pane)[1];
            if (fixedImageView != null) {
                pane.getChildren().remove(fixedImageView);
                imageViews.get(pane)[1] = null;
            }
        }
    }

    /**
     * Helper function that moves the car to a new pane.
     * @param player Zero-based index of the player's car to move.
     * @param newPane The pane to move it to.
     * @param size    The size of the ImageView.
     */
    public void moveCarImage(int player, Pane newPane, int size) {
        // First, remove the car from the old pane
        if (playerSpawnPanes[player] != null) {
            ImageView oldCarImg = imageViews.get(playerSpawnPanes[player])[2];
            playerSpawnPanes[player].getChildren().remove(oldCarImg);
            imageViews.get(playerSpawnPanes[player])[2] = null;
        }
        // Place the car in the new pane
        ImageView carImg = createTileImageView("player" + (1 + player), size);
        imageViews.get(newPane)[2] = carImg;
        newPane.getChildren().add(carImg);
        playerSpawnPanes[player] = newPane;
    }

    /**
     * Creates an ImageView for use as a tile on the game board.
     *
     * @param name The name of the image to use.
     * @param size The fit width and height of the ImageView.
     * @return An ImageView with the tile's image
     * and its fit size set to the provided size.
     */
    private ImageView createTileImageView(String name, int size) {
        return createTileImageView(name, size, Rotation.UP);
    }

    /**
     * Creates an ImageView for use as a tile on the game board with the specified rotation.
     *
     * @param name     The name of the image to use.
     * @param size     The fit width and height of the ImageView.
     * @param rotation The orthogonal rotation of the tile.
     * @return An ImageView with the tile's image rotated to the specified direction
     * and its fit size set to the provided size.
     */
    private ImageView createTileImageView(String name, int size, Rotation rotation) {
        ImageView tileImg = new ImageView(Assets.get(name));
        tileImg.setFitWidth(size);
        tileImg.setFitHeight(size);
        tileImg.setRotate(rotation.degrees());
        return tileImg;
    }

    /**
     * Attempts to set the custom board's silk bag contents
     * using values from the TextFields. If an error occurs
     * the error message box is shown.
     */
    private void setSilkBagData() {
        if (straightInBox.getText().equals("")) {
            straightInBox.setText("10");
        }
        if (cornerInBox.getText().equals("")) {
            cornerInBox.setText("10");
        }
        if (tshapeInBox.getText().equals("")) {
            tshapeInBox.setText("10");
        }
        if (iceInBox.getText().equals("")) {
            iceInBox.setText("10");
        }
        if (fireInBox.getText().equals("")) {
            fireInBox.setText("10");
        }
        if (backtrackInBox.getText().equals("")) {
            backtrackInBox.setText("10");
        }
        if (doublemoveInBox.getText().equals("")) {
            doublemoveInBox.setText("10");
        }
        try {
            customBoard.setSilkBagMapElement(TileType.STRAIGHT, Integer.parseInt(straightInBox.getText()));
            customBoard.setSilkBagMapElement(TileType.CORNER, Integer.parseInt(cornerInBox.getText()));
            customBoard.setSilkBagMapElement(TileType.T_SHAPE, Integer.parseInt(tshapeInBox.getText()));
            customBoard.setSilkBagMapElement(TileType.GOAL, 0);
            customBoard.setSilkBagMapElement(TileType.FIRE, Integer.parseInt(fireInBox.getText()));
            customBoard.setSilkBagMapElement(TileType.FROZEN, Integer.parseInt(iceInBox.getText()));
            customBoard.setSilkBagMapElement(TileType.BACKTRACK, Integer.parseInt(backtrackInBox.getText()));
            customBoard.setSilkBagMapElement(TileType.DOUBLE_MOVE, Integer.parseInt(doublemoveInBox.getText()));
        } catch (NumberFormatException ex) {
            showErrorMsgBox("Silk bag tile count must be an integer above zero");
        }
    }

    /**
     * Helper method which starts a drag and drop event.
     *
     * @param source   The source of the dragging.
     * @param tileName The name of the tile being dragged.
     */
    private void startDragAndDrop(Node source, String tileName) {
        unselectActionRadioButtons();
        hideErrorMsgBox();
        Dragboard db = source.startDragAndDrop(TransferMode.ANY);
        ClipboardContent content = new ClipboardContent();
        content.putString(tileName);
        db.setContent(content);
        System.out.println("Starting a drag event for " + content.getString());
    }

    /**
     * Enables drag detection for the straight tile image
     * @param event The source of the drag event.
     */
    public void onMouseDragStraightTile(MouseEvent event) {
        event.setDragDetect(true);
    }

    /**
     * Begins a drag event for a straight tile.
     */
    public void onDragStraightTile() {
        startDragAndDrop(straightImage, "straight");
    }

    /**
     * Enables drag detection for the t-shape tile image.
     * @param event The source of the drag event.
     */
    public void onMouseDragTShapeTile(MouseEvent event) {
        event.setDragDetect(true);
    }

    /**
     * Begins a drag event for a t-shape tile.
     */
    public void onDragTShapeTile() {
        startDragAndDrop(tshapeImage, "t_shape");
    }

    /**
     * Enables drag detection for the corner tile image.
     * @param event The source of the drag event.
     */
    public void onMouseDragCornerTile(MouseEvent event) {
        event.setDragDetect(true);
    }

    /**
     * Begins a drag event for a corner tile.
     */
    public void onDragCornerTile() {
        startDragAndDrop(cornerImage, "corner");
    }

    /**
     * Enables drag detection for the goal tile image.
     * @param event The source of the drag event.
     */
    public void onMouseDragGoalTile(MouseEvent event) {
        event.setDragDetect(true);
    }

    /**
     * Begins a drag event for a goal tile.
     */
    public void onDragGoalTile() {
        startDragAndDrop(goalImage, "goal");
    }

    /**
     * Enables drag detection for player 1's spawn point image.
     * @param event The source of the drag event.
     */
    public void onMouseDragPlayer1(MouseEvent event) {
        event.setDragDetect(true);
    }

    /**
     * Begins a drag event for player 1's spawn point.
     */
    public void onDragPlayer1() {
        startDragAndDrop(p1Image, "player 1");
    }

    /**
     * Enables drag detection for player 2's spawn point image.
     * @param event The source of the drag event.
     */
    public void onMouseDragPlayer2(MouseEvent event) {
        event.setDragDetect(true);
    }

    /**
     * Begins a drag event for player 2's spawn point.
     */
    public void onDragPlayer2() {
        startDragAndDrop(p2Image, "player 2");
    }

    /**
     * Enables drag detection on player 3's spawn point image.
     * @param event The source of the drag event.
     */
    public void onMouseDragPlayer3(MouseEvent event) {
        event.setDragDetect(true);
    }

    /**
     * Begins a drag event for player 3's spawn point.
     */
    public void onDragPlayer3() {
        startDragAndDrop(p3Image, "player 3");
    }

    /**
     * Enables drag detection on player 4's spawn point image.
     * @param event The source of the drag event.
     */
    public void onMouseDragPlayer4(MouseEvent event) {
        event.setDragDetect(true);
    }

    /**
     * Begins a drag event for player 4's spawn point.
     */
    public void onDragPlayer4() {
        startDragAndDrop(p4Image, "player 4");
    }

    /**
     * Hides the error message box and selects the fix tile radio button.
     * Called when the fix tile radio button is selected.
     */
    public void onFixRB() {
        hideErrorMsgBox();
        fixRB.setSelected(true);
    }

    /**
     * Hides the error message box and selects the rotate tile radio button.
     * Called when the rotate radio button is selected.
     */
    public void onRotateRB() {
        hideErrorMsgBox();
        rotateRB.setSelected(true);
    }

    /**
     * Hides the error message box and selects the remove tile radio button.
     * Called when the remove radio button is selected.
     */
    public void onRemoveRB() {
        hideErrorMsgBox();
        removeRB.setSelected(true);
    }

    /**
     * Unselects the radio buttons.
     */
    public void unselectActionRadioButtons() {
        fixRB.setSelected(false);
        rotateRB.setSelected(false);
        removeRB.setSelected(false);
    }

    /**
     * Shows the error message box.
     * @param errorMessage The message to display.
     */
    public void showErrorMsgBox(String errorMessage) {
        errorMsgText.setText(errorMessage);
        errorMsgBox.setVisible(true);
    }

    /**
     * Hides the error message box.
     */
    public void hideErrorMsgBox() {
        errorMsgBox.setVisible(false);
        errorMsgText.setText("");
    }

    /**
     * Exits the level editor without saving.
     * Called when Exit is clicked.
     */
    public void onExitButton() {
        WindowLoader wl = new WindowLoader(straightSlider);
        wl.load("MenuScreen", getInitData());
    }

    /**
     * Attempts to save and exit the editor, or
     * displays the error message that occurs.
     * Called when Save &amp; Exit is clicked.
     */
    public void onSaveExitButton() {
        setSilkBagData();
        editor.setFileName("./Gameboards/Custom" + getInitData().get("Custom Board Name") + ".txt");
        try {
            editor.saveFile();
            WindowLoader wl = new WindowLoader(straightSlider);
            wl.load("MenuScreen", getInitData());
        } catch (IllegalStateException ex) {
            System.out.println(ex.getMessage());
            showErrorMsgBox(ex.getMessage());
        }
    }

}

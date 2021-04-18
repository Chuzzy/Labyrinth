package FrontEnd;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.media.AudioClip;

import java.io.File;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * This class is used to control the GameSetup scene.
 *
 * @author Christian Sanger.
 * @version 1.0
 */

public class GameSetupController extends StateLoad {
    public ChoiceBox selectProfile;
    /* These final variables are used for the game's Sound Effects (SFX) */
    URL location;
    ResourceBundle resources;
    private final String MAIN_MENU_SFX = "Assets\\SFX\\mainmenu.mp3";
    private final AudioClip MAIN_MENU_AUDIO = new AudioClip(new File(MAIN_MENU_SFX).toURI().toString());
    private final String RETURN_SFX = "Assets\\SFX\\return.mp3";
    private final AudioClip RETURN_AUDIO = new AudioClip(new File(RETURN_SFX).toURI().toString());
    private final String ERROR_SFX = "Assets\\SFX\\error.mp3";
    private final AudioClip ERROR_AUDIO = new AudioClip(new File(ERROR_SFX).toURI().toString());


    @FXML
    private Button backButton;

    @FXML
    private TextField saveName;

    @FXML
    private ChoiceBox<String> selectGameBoard;

    @FXML
    private ToggleGroup playerCount;

    @FXML private Button customBoardBtn;

    boolean isCustomBoard = false;
    /**
     * Populates the choice box with the available gameboards when the page is initialized.
     *
     * @param location  location of the resources
     * @param resources bundle of the resources for the method
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String[] custom;
        String boardName = "";
        if (selectGameBoard.getValue() == null) {
            this.location = location;
            this.resources = resources;
            String[] gameBoards;
            File gameBoardLocation = new File("Gameboards");
            gameBoards = gameBoardLocation.list();
            if (gameBoards != null) {
                for (String gameBoard : gameBoards) {
                    gameBoard = gameBoard.substring(0, gameBoard.length() - 4);
                    custom = gameBoard.split("Custom");
                    if (isCustomBoard == true) {
                        if (custom[0].equals("")) {
                            if (custom.length > 2) {
                                for (int i = 1; i < custom.length; i++) {
                                    if (custom[i].equals("")) {
                                        boardName = boardName + "Custom";
                                    } else {
                                        boardName = boardName + custom[i];
                                    }
                                }
                                selectGameBoard.getItems().add(boardName);
                            } else {
                                selectGameBoard.getItems().add(custom[1]);
                            }
                        }
                    } else {
                        if (!custom[0].equals("")) {
                            selectGameBoard.getItems().add(gameBoard);
                        }
                    }
                }
                selectGameBoard.getSelectionModel().selectFirst();
            }
            saveName.setPromptText("Enter game name");
        }
    }

    /**
     * Toggles between custom and prebuilt maps
     */
    public void onCustomButton() {
        isCustomBoard = !isCustomBoard;
        if (isCustomBoard) {
            customBoardBtn.setText("Use Prebuilt");
        } else {
            customBoardBtn.setText("Use Custom");
        }
        while (selectGameBoard.getItems().toArray().length != 0) {
            selectGameBoard.getItems().remove(0);
        }
        selectGameBoard.setValue(null);
        initialize(location, resources);
    }

    /**
     * Returns to main menu
     */
    public void onBackButton() {
        WindowLoader wl = new WindowLoader(backButton);
        wl.load("MenuScreen", getInitData());
        RETURN_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
    }

    /**
     * This copies the game board file, appends the seed for the silk bag and Continues to GameScreen.
     */
    public void onStartButton() {
        WindowLoader wl = new WindowLoader(backButton);
        getInitData().put("Seed", "" + ((new Random()).nextInt()));
        String gameBoard;
        if(isCustomBoard){
            gameBoard = "Custom" + selectGameBoard.getValue() + ".txt";
        }else {
            gameBoard = selectGameBoard.getValue() + ".txt";
        }
        getInitData().put("Board", gameBoard);
        getInitData().put("PlayerCount", ((RadioButton) playerCount.getSelectedToggle()).getText());
        getInitData().put("LoadFile", saveName.getText() + ".sav");
        getInitData().put("isLoadedFile", "false");
        String gameSaveName = saveName.getText();
        getInitData().put("SaveFile", gameSaveName);
        if ((gameSaveName.equals(""))) {
            ERROR_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
            saveName.setStyle("-fx-prompt-text-fill:red;");
            return;
        }
        File gameSaveFile = new File("SaveData\\GameSave\\" + gameSaveName + ".sav");
        if (gameSaveFile.exists()) {
            ERROR_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
            saveName.clear();
            saveName.setPromptText("Game already exists.");
            saveName.setStyle("-fx-prompt-text-fill:red;");
            return;
        }
        wl.load("PickPlayer", getInitData());
        MAIN_MENU_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
    }
}

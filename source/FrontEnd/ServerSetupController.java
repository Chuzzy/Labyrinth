package FrontEnd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerSetupController extends StateLoad {
    URL location;
    ResourceBundle resources;

    public ChoiceBox<String> selectProfile;
    public ChoiceBox<String> selectGameBoard;
    public Button customBoardBtn;
    @FXML
    private Button backButton;
    boolean isCustomBoard = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File playerLocation = new File("SaveData\\UserData\\");
        String[] players = playerLocation.list();
        selectProfile.getItems().clear();
        assert players != null;
        for (String player : players) {
            selectProfile.getItems().addAll(player.substring(0, player.length() - 4));
        }
        selectProfile.setValue(selectProfile.getItems().get(0));

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
                    if (isCustomBoard) {
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

    public void onStartButton() {
        WindowLoader wl = new WindowLoader(backButton);
        getInitData().put("OnlineBoard", selectGameBoard.getValue());
        getInitData().put("OnlineServerName", selectProfile.getValue());
        wl.load("GameScreenServer", getInitData());
    }

    /**
     * Returns to main menu
     */
    public void onBackButton() {
        WindowLoader wl = new WindowLoader(backButton);
        wl.load("MenuScreen", getInitData());
    }

}

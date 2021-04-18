package FrontEnd;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientSetupController extends StateLoad {
    URL location;
    ResourceBundle resources;

    public TextField hostAddressText;
    public ChoiceBox<String> selectProfile;
    @FXML
    private Button backButton;

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

    }

    public void onStartButton() {
        WindowLoader wl = new WindowLoader(backButton);
        getInitData().put("OnlineHostAddress", hostAddressText.getText());
        getInitData().put("OnlineProfileName", hostAddressText.getText());
        wl.load("GameScreenClient", getInitData());
    }

    /**
     * Returns to main menu
     */
    public void onBackButton() {
        WindowLoader wl = new WindowLoader(backButton);
        wl.load("MenuScreen", getInitData());
    }
}

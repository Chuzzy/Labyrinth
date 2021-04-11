package FrontEnd.FXML.HowToPlay.InGameHowToPlay;

import FrontEnd.StateLoad;
import FrontEnd.WindowLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.media.AudioClip;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class InGameHowToPlayController extends StateLoad {

    private final String START_SFX = "Assets\\SFX\\start.mp3";
    private final AudioClip START_AUDIO = new AudioClip(new File(START_SFX).toURI().toString());
    private final String ERROR_SFX = "Assets\\SFX\\error.mp3";
    private final AudioClip ERROR_AUDIO = new AudioClip(new File(ERROR_SFX).toURI().toString());


    @FXML
    private Button newGameButton;
    private WindowLoader wl;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    /**
     * This method is is used to create a new button for tiles. Also has sound effects when
     * pressed.
     */
    public void onTiles() {
        wl = new WindowLoader(newGameButton);
        wl.load("/HowToPlay/InGameHowToPlay/Tiles", getInitData());
    }

    /**
     * This method is is used to create a new button for tiles. Also has sound effects when
     * pressed.
     */
    public void onTurns() {
        wl = new WindowLoader(newGameButton);
        wl.load("/HowToPlay/InGameHowToPlay/Turns", getInitData());
    }

    /**
     * This method is is used to create a new button for Movement. Also has sound effects when
     * pressed.
     */
    public void onMovement() {
        wl = new WindowLoader(newGameButton);
        wl.load("/HowToPlay/InGameHowToPlay/Movement", getInitData());
    }

    /**
     * This method is is used to create a new button for ActionTiles. Also has sound effects when
     * pressed.
     */
    public void onActionTiles() {
        wl = new WindowLoader(newGameButton);
        wl.load("/HowToPlay/InGameHowToPlay/InGameActionTiles", getInitData());
    }

    /**
     * This method is is used to create a new button for HowToPlay. Also has sound effects when
     * pressed.
     */
    public void onBackToGame() {
        WindowLoader wl = new WindowLoader(newGameButton);
        wl.load("GameScreen", getInitData());
    }

    /**
     * This method is is used to create a new button for Basics. Also has sound effects when
     * pressed.
     */
    public void onBasics() {
        wl = new WindowLoader(newGameButton);
        wl.load("/HowToPlay/InGameHowToPlay/Basics", getInitData());
    }

    public void onNewGame() {
        wl = new WindowLoader(newGameButton);
        wl.load("GameSetup", getInitData());
    }

    public void onBack() {
        wl = new WindowLoader(newGameButton);
        wl.load("/HowToPlay/InGameHowToPlay/InGameHowToPlay", getInitData());
    }
}

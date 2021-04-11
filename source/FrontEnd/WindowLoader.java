package FrontEnd;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

/***
 * This class is used to load new windows.
 * @author Christian Sanger
 */
public class WindowLoader {

    private static final String fileLocation = "FrontEnd/FXML/";
    private static Stage w; // Reference to the primary stage
    private  static Stage overLayStage;

    Rectangle2D inSize = Screen.getPrimary().getBounds(); //Gets the size of the scree
    double height = inSize.getHeight() * 0.8; //Stage height - initially 80%
    double width = inSize.getWidth() * 0.8; //Stage Width - initially 80%

    /***
     * Creates a window loader from a given node.
     * @param window A node in the window that the new scene is to be loaded into.
     */
    public WindowLoader(Node window) {
        w = (Stage) window.getScene().getWindow();
        height = w.getHeight();
        width = w.getWidth();
    }

    /**
     * Creates a new window loader that can change the current scene in this window
     *
     * @param primaryStage primary stage as reference to which window to change
     */
    public WindowLoader(Stage primaryStage) {
        w = primaryStage;
    }

    /***
     * swaps the scene for the given scene. Window should be the scene file name
     * i.e. to swap to MenuScreen.fxml use "MenuScreen"
     *
     * @param window scene name
     * @param initData state of application
     */
    public void load(String window, HashMap<String, String> initData) {
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader();
            if (window.startsWith("/")) {
                window = window.substring(1);
            }
            System.out.println("Loading " + fileLocation + window + ".fxml");
            loader.setLocation(Objects.requireNonNull(getClass().getClassLoader().getResource(fileLocation + window + ".fxml")));
            root = loader.load();
            StateLoad controller = loader.getController();
            controller.setInitData(initData);
            controller.initialize(null, null);
        } catch (IOException e) {
            System.out.println(window + " Failed to load due to " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        if (root == null) {
            System.out.print("Scene loading failed, " + window + " could not be loaded");
            System.exit(1);
        } else {
            if (w.getScene() == null) {
                w.setFullScreen(false);
                w.setHeight(height);
                w.setWidth(width);
                w.setScene(new Scene(root));
            } else {
                w.setFullScreen(false);
                w.setHeight(height);
                w.setWidth(width);
                w.getScene().setRoot(root);
            }
        }
        System.out.println("Loaded " + fileLocation + window + ".fxml");
    }

    /**
     * Maximizes the current window.
     */
    public void setWindowFull() {
        w.setMaximized(true);
    }
}

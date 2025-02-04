package FrontEnd;

import BackEnd.Profile;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.AudioClip;
import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

/***
 * A controller class for profiles.fxml which allows a user to show the player profiles saved, create new player
 * profiles, delete player profiles and view player profiles.
 * It is loaded by clicking from it in the MenuScreen and allows the user to return to the MenuScreen with an action.
 * @author Zhan Zhang
 * @version 1.0
 */
public class ProfilesController extends StateLoad {

	/* These final variables are used for the game's Sound Effects (SFX) */

	private final String MAIN_MENU_SFX = "Assets\\SFX\\mainmenu.mp3";
	private final AudioClip MAIN_MENU_AUDIO = new AudioClip(new File(MAIN_MENU_SFX).toURI().toString());
	private final String RETURN_SFX = "Assets\\SFX\\return.mp3";
	private final AudioClip RETURN_AUDIO = new AudioClip(new File(RETURN_SFX).toURI().toString());
	private final String ERROR_SFX = "Assets\\SFX\\error.mp3";
	private final AudioClip ERROR_AUDIO = new AudioClip(new File(ERROR_SFX).toURI().toString());

	@FXML
	public Button viewButton;
	@FXML
	public Label playerRecord;
	@FXML
	public ImageView playerIcon;
	@FXML
	private Button backButton;
	@FXML
	private TextField input;
	@FXML
	private ListView<String> playerList;

	int currentIndex = 0;
	final File iconImage0 = new File("Assets\\icon0.png");
	final Image icon0 = new Image(iconImage0.toURI().toString());
	final File iconImage1 = new File("Assets\\icon1.png");
	final Image icon1 = new Image(iconImage1.toURI().toString());
	final File iconImage2 = new File("Assets\\icon2.png");
	final Image icon2 = new Image(iconImage2.toURI().toString());
	final File iconImage3 = new File("Assets\\icon3.png");
	final Image icon3 = new Image(iconImage3.toURI().toString());

	/**
	 * Called to initialize a controller after its root element has been
	 * completely processed.
	 *
	 * @param location  The location used to resolve relative paths for the root object, or
	 *                  <tt>null</tt> if the location is not known.
	 * @param resources The resources used to localize the root object, or <tt>null</tt> if
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		File userDataFolder = new File("SaveData\\UserData\\");
		String[] children = userDataFolder.list();
		playerIcon.setImage(icon0);
		if(playerList.getSelectionModel().getSelectedItem() == null) {
			if (children != null) {
				for (String filename : children) {
					playerList.getItems().addAll(filename.substring(0, filename.length() - 4));
				}
				playerList.getSelectionModel().selectFirst();
			}
		}
	}

	/**
	 * the action on the button back, back to the menus screen.
	 */
	public void onBackButton() {
		WindowLoader wl = new WindowLoader(backButton);
		wl.load("MenuScreen", getInitData());
		RETURN_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
	}

	/**
	 * try to create a user file with name typed, create one in UserData if there is not a file with
	 * initialized data and turn text field to red if there is one.
	 *
	 * @throws IOException when FileWriter failed to write the file.
	 */
	public void createFile() throws IOException {
		String newName = input.getText();
		File user = new File("SaveData\\UserData\\" + newName + ".txt");

		if (user.exists() && !user.isDirectory()) {
			input.setStyle("-fx-border-color: red");
		}
		if (user.exists() && !user.isDirectory() || newName.isEmpty() || newName.equals("Computer")) {
			input.setStyle("-fx-border-color: red");
			ERROR_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));

		} else {
			input.setStyle("-fx-border-color: default");
			playerList.getItems().addAll(newName);
			PrintWriter newUser = new PrintWriter(new FileWriter("SaveData\\UserData\\" + newName + ".txt"));
			newUser.write("0 0 0 icon" + currentIndex);
			newUser.close();
			MAIN_MENU_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
		}
	}

	/**
	 * Deletes the file selected in the list, and turns the text field back to white if it is not.
	 */
	public void deleteFile() {
		String newName = playerList.getSelectionModel().getSelectedItem();
		File user = new File("SaveData\\UserData\\" + newName + ".txt");
		if (user.delete()) {
			input.setStyle("-fx-border-color: default");
			playerList.getItems().remove(newName);

			MAIN_MENU_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));

		} else {
			ERROR_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
			input.setStyle("-fx-border-color: red");
		}
	}

	/**
	 * View the data saved in the file with same name as selected in the view list, when that file doesn't exist,
	 * print an error message.
	 */
	public void viewData() {
		String playerPicked = playerList.getSelectionModel().getSelectedItem();
		input.setStyle("-fx-border-color: default");
		try {
			Profile profile = Profile.readProfile(playerPicked);
			playerRecord.setText(
					"This player has " +
							profile.getWins() +
							" wins, " +
							profile.getLosses() +
							" losses and a win streak of " +
							profile.getWinStreak() +
							".");
			MAIN_MENU_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
		} catch (IOException noPlayerFound) {
			ERROR_AUDIO.play(Double.parseDouble(getInitData().get("SFXVol")));
			playerRecord.setText("Please select a player.");
		}
	}

	/**
	 * This button switch the player icon which will save in player profile created.
	 */
	public void nextIcon() {
		currentIndex++;
		if (currentIndex == 0) {
			playerIcon.setImage(icon0);
		} else if (currentIndex == 1) {
			playerIcon.setImage(icon1);
		} else if (currentIndex == 2) {
			playerIcon.setImage(icon2);
		} else if (currentIndex == 3) {
			playerIcon.setImage(icon3);
		} else if (currentIndex == 4) {
			currentIndex =0;
			playerIcon.setImage(icon0);
		}
	}
}


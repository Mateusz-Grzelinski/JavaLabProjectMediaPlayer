package RemotePlayerController;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import mediacommunication.*;
import remotefilechooser.RemoteFileChooser;

/**
 * FXML Controller class hanldes all UI logic and keybindings (same as in
 * master) almost every UI event will trigger syncing process handled by @
 * MediaClient class
 *
 * @DataForSync has all fields that need to be kept synchronized
 *
 * @author mat
 */
public class RemotePlayerFXMLController implements Initializable {

	DataForSync sync;
	MediaClient mediaClient;

	@FXML
	private Slider slider;
	@FXML
	private Slider seekslider;
	@FXML
	private Text title;
		

	private final double rateStep = 0.25;
	private final double skipStep = 5;
	private Scene scene;

	/**
	 * Initializes the controller class.
	 *
	 * @param url
	 * @param rb
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		sync = new DataForSync();
	}

	void initData(Scene scene, Stage stage, MediaClient mediaClient) {
		this.scene = scene;
		this.mediaClient = mediaClient;
		mediaClient.addIncomingDataListener(new  IncomingDataListener() {
			@Override
			public void SyncDataIncoming(DataForSync incoming) {
				System.out.println("synching");
				seekslider.setMax(incoming.getSeekSliderMaxVal());
				seekslider.setValue(incoming.getCurrentTime());
				slider.setValue(incoming.getCurrentVolume()*100);
				setTitleBox(incoming.getCurrentTitle());
			}
		});
		mediaClient.initialSyncWithServer();
		setKeyConstrains(stage);
		setSeekSliderEvents();
		setVolumeSliderEvents();
	}

	private void setKeyConstrains(Stage stage) {

		scene.setOnKeyPressed((KeyEvent keyEvent) -> {
			if (keyEvent.getCode() != null) {
				switch (keyEvent.getCode()) {
					case LEFT:
						keyEvent.consume();
						skipBackwards();
						break;
					case RIGHT:
						keyEvent.consume();
						skipForwards();
						break;
					case SPACE:
						keyEvent.consume();
						toggleMediaStatus();
						break;
					case ESCAPE:
						if (sync.isFullscreen()) {
							sync.setFullscreen(false);
							send("exit fullscreen with esc");
						} else {
							stage.close();
						}
					default:
						break;
				}
			}
		});
	}

	private void toggleMediaStatus() {
		sync.setPlaying(!sync.isPlaying());
		send("toggle playing");
	}

	private void setSeekSliderEvents() {
//		seekslider.valueProperty().addListener(new ChangeListener<Number>() {
//			@Override
//			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
//				sync.setCurrentTime((double) t1);
//				send("seek slider");
//			}
//		});
		seekslider.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent t) {
				sync.setCurrentTime(seekslider.getValue());
				send("seek slider");
			}
		});
	}

	private void setVolumeSliderEvents() {
//		slider.valueProperty().addListener(new InvalidationListener() {
//			@Override
//			public void invalidated(Observable o) {
//				sync.setCurrentVolume(slider.getValue());
//				send("volume slider");
//			}
//		});
		slider.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent t) {
				sync.setCurrentVolume(slider.getValue());
				send("volume slider");
			}
		});
	}

	@FXML
	private void handleButtonOpen(ActionEvent event) {
		RemoteFileChooser remoteFileChooser;
		remoteFileChooser = new RemoteFileChooser();
		try {
			Stage stage = new Stage();
			remoteFileChooser.start(stage);
			mediaClient.sendOpenRequest();
			remoteFileChooser.initData(stage, mediaClient, sync);
		} catch (IOException ex) {
			System.err.println("Could not create new stage");
		}
	}

	@FXML
	private void handleButtonPlay(ActionEvent event) {
		sync.setPlaying(true);
		sync.setCurrentRate(1);
		send("play");
	}

	@FXML
	private void handleButtonStop(ActionEvent event) {
		sync.setPlaying(false);
		sync.setCurrentTime(0);
		seekslider.setValue(0);
		send("stop");
	}

	@FXML
	private void handleButtonPause(ActionEvent event) {
		sync.setPlaying(false);
		send("pause");
	}

	@FXML
	private void handleButtonRateUp(ActionEvent event) {
		sync.setCurrentRate(sync.getCurrentRate() + rateStep);
		send("rate up");
	}

	@FXML
	private void handleButtonRateDown(ActionEvent event) {
		sync.setCurrentRate(sync.getCurrentRate() - rateStep);
		send("rate down");
	}

	@FXML
	private void handleButtonSkipForward(ActionEvent event) {
		skipForwards();
	}

	void skipForwards() {
		seekslider.setValue(seekslider.getValue() + skipStep);
		sync.setSkip(skipStep);
		send("skip forward");
	}

	@FXML
	private void handleButtonSkipBackwards(ActionEvent event) {
		skipBackwards();
	}

	void skipBackwards() {
		seekslider.setValue(seekslider.getValue() - skipStep);
		sync.setSkip(-skipStep);
		send("skip backwards");
	}

	@FXML
	private void handleButtonFullscreen(ActionEvent event) {
		sync.setFullscreen(!sync.isFullscreen());
		send("fullscreen on");
	}

	@FXML
	private void handleButtonConnect() {
		System.out.println("Reconnecting...");
		mediaClient.stop();
		mediaClient.connectToServer();
		mediaClient.initialSyncWithServer();
	}

	private void setTitleBox(String text){
		title.setText(text);
	}

	private void send(String message) {
		try {
			System.out.println(sync);
			mediaClient.sendObject(sync);
			sync.reset();
		} catch (IOException ex) {
			System.out.println(ex);
			System.err.println("Failed to sync " + message);
		}
	}

}

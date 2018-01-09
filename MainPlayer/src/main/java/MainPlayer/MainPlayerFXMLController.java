package MainPlayer;

import com.sun.javafx.beans.event.AbstractNotifyListener;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import mediacommunication.DataForSync;
import mediacommunication.IncomingDataListener;
import mediacommunication.MediaServer;

/**
 * FXML Controller class handles almost all kebindings and all UI logic uses
 *
 * @DataForSync class for syncing with controller
 *
 * @author mat
 */
public class MainPlayerFXMLController implements Initializable {

	@FXML
	private MediaView mediaView;
	@FXML
	private Slider slider;
	@FXML
	private Slider seekslider;
	@FXML
	private BorderPane borderpane;
	@FXML
	private Pane bottompane;

	private MediaPlayer mediaPlayer;
	private String path;
	private final double rateStep = 0.25;
	private final double skipStep = 5;
	private Scene scene;
	private Stage stage;
//	private File file;

	private MediaServer server;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// no need for this
	}

	void initData(Scene scene, Stage stage, MediaServer server) {
		this.scene = scene;
		this.stage = stage;
		this.server = server;
		hideBottompaneOnFullscreen();
		setServerDataSyncListener();
		setKeyConstrains();
	}

	private void hideBottompaneOnFullscreen() {
		stage.fullScreenProperty().addListener(new AbstractNotifyListener() {
			@Override
			public void invalidated(Observable o) {
				if (stage.isFullScreen()) {
					borderpane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
					bottompane.setVisible(false);
				} else {
					borderpane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
					bottompane.setVisible(true);
				}
			}
		});
	}

	/**
	 * The wole idea of syncing is wrong. Instead of syncing whole object, only
	 * single variables should be send
	 */
	private synchronized void setServerDataSyncListener() {
		this.server.addNewDataListener(new IncomingDataListener() {
			@Override
			public void SyncDataIncoming(DataForSync incoming) {
//				System.out.println("Syncing new data :)");
				System.out.println(incoming.getCurrentTitle());
				if (!incoming.getCurrentTitle().isEmpty()) {
					incoming.reset();
					path = incoming.getCurrentTitle();
					openFile(new File(path));
				}
				if (mediaPlayer != null) {
					if (incoming.isPlaying()) {
						mediaPlayer.play();
					} else {
						mediaPlayer.pause();
					}
					mediaPlayer.setRate(incoming.getCurrentRate());
					mediaPlayer.setVolume(incoming.getCurrentVolume() / 100);
					syncVolumeSliderWithMediaVolume();
//					workaround: do not sync time when not needed
					if (incoming.getCurrentTime() != -1) {
						mediaPlayer.seek(Duration.seconds(incoming.getCurrentTime()));
					}
//					get skip can be >0 or <0 or =0. Doesn't matter
					double skippedTime
						= mediaPlayer.getCurrentTime().toSeconds()
						+ incoming.getSkip();
					mediaPlayer.seek(Duration.seconds(skippedTime));
					seekslider.setValue(skippedTime);

				}
			}
		});

	}

	/**
	 * Left, right arrow and esc key
	 */
	private void setKeyConstrains() {
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {
				keyEvent.consume();
				if (null != keyEvent.getCode()) {
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
						default:
							break;
					}
				}
			}
		});
	}

	/**
	 * Toggle play/pause
	 */
	private void toggleMediaStatus() {
		if (mediaPlayer != null) {
			if (mediaPlayer.getStatus() == mediaPlayer.getStatus().PLAYING) {
				mediaPlayer.pause();
			} else {
				mediaPlayer.play();
			}
		}

	}

	/**
	 * Choose and open mp4 file
	 *
	 * @param event
	 */
	@FXML
	private void handleButtonOpen(ActionEvent event) {
		File file = ChooseFile();
		openFile(file);
	}

	private void openFile(File file) {
		if (file != null) {
			path = file.toURI().toString();
		}
		if (mediaPlayer != null) {
			mediaPlayer.dispose();
		}
		if (path != null) {
//			assign chosen file to mediaView
			Media media = new Media(path);
			mediaPlayer = new MediaPlayer(media);
			mediaView.setMediaPlayer(mediaPlayer);
		}
//			set auto resizing for mediaView
		DoubleProperty width = mediaView.fitWidthProperty();
		DoubleProperty hight = mediaView.fitHeightProperty();
		width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
		hight.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));

		setVolumeSliderEvents();
		syncVolumeSliderWithMediaVolume();
		setSeekSliderEvents();
		setSeekSliderMaxVal();
	}

	/**
	 * Only choose file with dialog window
	 *
	 * @return
	 */
	private File ChooseFile() {
		//		System.out.println("Open file");
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter filter;
		//		label.setText("Hello World!");
		filter = new FileChooser.ExtensionFilter("select a file (.mp4)", "*.mp4");
		fileChooser.getExtensionFilters().add(filter);
		File file = fileChooser.showOpenDialog(null);
		return file;
	}

	/**
	 * Seek slider will change position according o current time od media. Media
	 * time can be changed by clicking on slider TODO: disable position changing
	 * when slider is clicked
	 */
	private void setSeekSliderEvents() {
//		main functionality: seek with slider
		mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
			@Override
			public void changed(ObservableValue<? extends Duration> ov, Duration t, Duration t1) {
				seekslider.setValue(t1.toSeconds());
			}
		});

//		auto play after seeking
		seekslider.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent t) {
				mediaPlayer.seek(Duration.seconds(seekslider.getValue()));
			}
		});
	}

	/**
	 * Set slider max value to duration of media Workaround: totaj duration is
	 * known only after some time after media is loaded, so binding bust be
	 * user. Generates error.
	 */
	private void setSeekSliderMaxVal() {
//		set maximum value of slider (must be bindig, not single assigment)
//		causes exception to happen
		try {
			seekslider.maxProperty().bind(Bindings.createDoubleBinding(
				() -> mediaPlayer.getTotalDuration().toSeconds(),
				mediaPlayer.totalDurationProperty()));
		} catch (NullPointerException ex) {
			System.err.println("Error in reading metadata.");
		}
	}


	/**
	 * Change media volume with slider
	 */
	private void setVolumeSliderEvents() {
		slider.valueProperty().addListener((Observable o) -> {
			mediaPlayer.setVolume(slider.getValue() / 100);
		});
	}

	/**
	 * Sync volume slider wih media
	 */
	private void syncVolumeSliderWithMediaVolume() {
		slider.setValue(mediaPlayer.getVolume() * 100);
	}

	@FXML
	private void handleButtonPlay(ActionEvent event) {
		if (mediaPlayer != null) {
			mediaPlayer.play();
			mediaPlayer.setRate(1);
		}
	}

	@FXML
	private void handleButtonStop(ActionEvent event) {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
	}

	@FXML
	private void handleButtonPause(ActionEvent event) {
		if (mediaPlayer != null) {
			mediaPlayer.pause();
		}
	}

	@FXML
	private void handleButtonRateUp(ActionEvent event) {
		if (mediaPlayer != null) {
			double rate = mediaPlayer.getCurrentRate();
			mediaPlayer.setRate(rate + rateStep);
		}
	}

	@FXML
	private void handleButtonRateDown(ActionEvent event) {
		if (mediaPlayer != null) {
			double rate = mediaPlayer.getCurrentRate();
			mediaPlayer.setRate(rate - rateStep);
		}
	}

	@FXML
	private void handleButtonSkipForward(ActionEvent event) {
		skipForwards();
	}

	void skipForwards() {
		if (mediaPlayer != null) {
			double skippedTime = mediaPlayer.getCurrentTime().toSeconds() + skipStep;
			mediaPlayer.seek(Duration.seconds(skippedTime));
			seekslider.setValue(skippedTime);
		}
	}

	@FXML
	private void handleButtonSkipBackwards(ActionEvent event) {
		skipBackwards();
	}

	void skipBackwards() {
		if (mediaPlayer != null) {
			double skippedTime = mediaPlayer.getCurrentTime().toSeconds() - skipStep;
			mediaPlayer.seek(Duration.seconds(skippedTime));
			seekslider.setValue(skippedTime);
		}
	}

	@FXML
	private void handleButtonFullscreen(ActionEvent event) {
		stage.setFullScreen(true);
	}

	public DataForSync inititialClientSync() {
		DataForSync initialData = new DataForSync();
		if (mediaPlayer != null) {
			initialData.setPlaying(MediaPlayer.Status.PLAYING == mediaPlayer.getStatus());
			initialData.setCurrentRate(mediaPlayer.getCurrentRate());
			initialData.setCurrentTime(mediaPlayer.getCurrentTime().toSeconds());
			initialData.setCurrentVolume(mediaPlayer.getVolume());
		}
		if (path == null) {
			initialData.setCurrentTitle("None");
		} else {
			initialData.setCurrentTitle(path);
		}
		initialData.setFullscreen(stage.isFullScreen());
		initialData.setSeekSliderMaxVal(seekslider.getMax());
		return initialData;
	}

}

package MainPlayer;

import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mediacommunication.DataForSync;
import mediacommunication.IncomingDataListener;
import mediacommunication.MediaServer;

/**
 * class starts UI for media player and server (new thread) sets keybindinngs
 * for double click(full screen mode) esc as leaving server is used for remote
 * playing control handles also stopping of the server
 *
 * @author mat
 */
public class MainPlayerUI extends Application {

	private Scene scene;
	private MediaServer server;
	private Thread serverThread;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
//		Static fxml loader (can not be used for keybinding reason)
//		Parent root = FXMLLoader.load(getClass().getResource("../fxml/MainPlayerFXML.fxml"));
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/MainPlayerFXML.fxml"));
		Parent root = loader.load();
		scene = new Scene(root);

//		pass data to controller
		MainPlayerFXMLController controler = loader.getController();
		initServer(6789, controler);
		controler.initData(scene, stage, server);

//		set double click/button as full screen trigger
		setFullscreenMediaView(stage);
//		set full screen when called remotely
		setFullscreenListener(stage);
		setCloseOnESC(stage);
		setStopServerOnExit(stage);

		stage.setScene(scene);
		stage.setTitle("Remote Video Player");
		stage.show();
	}

	private void setFullscreenMediaView(Stage stage) throws IOException {
		scene.setOnMouseClicked((MouseEvent doubleClicked) -> {
			if (doubleClicked.getClickCount() == 2) {
				stage.setFullScreen(true);
			}
		});
	}

	private void setCloseOnESC(Stage stage) {
//		while full screen, do not exit
		stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
		scene.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
			ev.consume();
			if (stage.isFullScreen()) {
				stage.setFullScreen(false);
			} else if (ev.getCode() == KeyCode.ESCAPE) {
				if (server != null) {
					server.stopServer();
				}
				stage.close();
			}
		});
	}

	/**
	 * Stop server on closing window
	 *
	 * @param stage
	 */
	private void setStopServerOnExit(Stage stage) {
		stage.setOnCloseRequest((WindowEvent t) -> {
			if (server != null) {
				server.stopServer();
			}
			Platform.exit();
		});
	}

	/**
	 * starts server at given port
	 *
	 * @param port
	 */
	private void initServer(int port, MainPlayerFXMLController controler) {
		server = new MediaServer(port, controler);
		serverThread = new Thread((Runnable) server);
		serverThread.start();
	}

	/**
	 * set listener for remote calling full screen
	 *
	 * @param stage
	 */
	private void setFullscreenListener(Stage stage) {
		server.addNewDataListener(new IncomingDataListener() {
			@Override
			public void SyncDataIncoming(DataForSync incoming) {
//				must be called from runLater to avoid IllegalStateException
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						stage.setFullScreen(incoming.isFullscreen());
					}
				});
			}
		});
	}

}

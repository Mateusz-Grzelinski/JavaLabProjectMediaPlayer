package RemotePlayerController;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mediacommunication.MediaClient;

/**
 * main client UI runner
 *
 * @author mat
 */
public class RemoteControllerUI extends Application {

	private Scene scene;
	private MediaClient mediaClient;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {

//		Parent root = FXMLLoader.load(getClass().getResource("../fxml/RemotePlayerFXML.fxml"));
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/RemotePlayerFXML.fxml"));
		Parent root = loader.load();
		scene = new Scene(root);

		initClient("localhost", 6789);

		RemotePlayerFXMLController controler = loader.getController();
		controler.initData(scene, stage, mediaClient);

		stage.setScene(scene);
		stage.setTitle("Remote Video Controller");
		stage.show();
	}

	private void initClient(String localhost, int port) {
		mediaClient= new MediaClient(localhost, port);
	}


}

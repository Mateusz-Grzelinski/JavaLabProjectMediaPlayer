/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package remotefilechooser;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mediacommunication.DataForSync;
import mediacommunication.MediaClient;

/**
 *
 * @author mat
 */
public class RemoteFileChooser extends Application {

	private Scene scene;
	private RemoteFileChooserFXMLController controler;

	public void initData(Stage stage, MediaClient mediaClient, DataForSync sync) throws IOException {
		this.controler.initData(scene, stage, mediaClient, sync);
	}


	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/RemoteFileChooserFXML.fxml"));
		Parent root = loader.load();
		this.scene = new Scene(root);
		this.controler = loader.getController();

//		controler.setKeybindings(scene, stage);

		stage.setScene(scene);
		stage.setTitle("Remote Video Controller");
		stage.show();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

}

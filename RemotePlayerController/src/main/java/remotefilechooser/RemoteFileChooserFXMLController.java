/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package remotefilechooser;

import com.sun.javafx.beans.event.AbstractNotifyListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import mediacommunication.DataForSync;
import mediacommunication.MediaClient;

/**
 * FXML Controller class
 *
 * @author mat
 */
public class RemoteFileChooserFXMLController implements Initializable {

	boolean selected = false;
	@FXML
	private Text path;
	@FXML
	private ListView listview;
	private MediaClient mediaClient;
	private DataForSync sync;
	private File[] files;
	private Stage stage;
	private Scene scene;
	private boolean firstrun;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		path.setText("waiting for server...");
	}

	public void addItems(File[] remoteFiles) {
		path.setText("Choose file to open");
		resetItems();
		System.out.println("received files: " + Arrays.toString(remoteFiles));
		for(File file: remoteFiles){
			listview.getItems().add(file.toString());
		}

	}

	public void resetItems() {
		listview.getItems().clear();
	}

	@FXML
	private void handleButtonSelect() { 
		sync.setOpenrequest(true);
		ObservableList selectedIndices;
		selectedIndices = listview.getSelectionModel().getSelectedIndices();
		int index = (int) selectedIndices.get(0);

		String title = files[index].toString();
		sync.setCurrentTitle(title);

		System.out.println(sync.getCurrentTitle());
		
		if (title.contains(".mp4")){
			sync.setOpenrequest(false);
			stage.close();
		}
		if(firstrun){
			sync.setOpenrequest(true);
		}
		try {
			mediaClient.getRemoteDirContents(title);
			mediaClient.sendObject(sync);
		} catch (IOException ex) {
			System.err.println("Cancle button not sent");
		}


	}

	@FXML
	private void handleButtonCancel() {
		stage.close();
		sync.setOpenrequest(false);
		try {
			mediaClient.getRemoteDirContents("");
			mediaClient.sendObject(sync);
			stage.close();
		} catch (IOException ex) {
			System.err.println("Cancle button not sent");
		}

	}

	@FXML
	private void handleButtonBack() {

	}

	void initData(Scene scene, Stage stage, MediaClient mediaClient, DataForSync sync) throws IOException {
		this.sync = sync;
		this.mediaClient = mediaClient;
		this.files = mediaClient.getRemoteDirContents("");
		mediaClient.sendObject(sync);
		this.stage = stage;
		this.scene = scene;
		this.firstrun = true;
		setKeybindings();
		addItems(files);
	}

	void setKeybindings() {
//		stage.onCloseRequestProperty().addListener(new AbstractNotifyListener() {
//			@Override
//			public void invalidated(Observable o) {
//				sync.setOpenrequest(false);
//			}
//		});
	}

}

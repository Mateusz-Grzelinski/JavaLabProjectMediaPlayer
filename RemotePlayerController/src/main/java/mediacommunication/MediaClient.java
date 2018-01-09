/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mediacommunication;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * source:
 * https://stackoverflow.com/questions/5680259/using-sockets-to-send-and-receive-data
 *
 * @author mat
 */
public class MediaClient {

	private int port = 6789;
	private String hostname = "localhost";
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private Socket clientSocket;
	private DataForSync initialData;
	private final List<IncomingDataListener> newDataListener;

	public MediaClient(String hostname, int port) {
		this.newDataListener = new ArrayList<>();
		this.hostname = hostname;
		this.port = port;
		connectToServer();
	}

	public void connectToServer() {
		try {
			System.out.println("Trying to find server...");
			clientSocket = new Socket(hostname, port);
			oos = new ObjectOutputStream(clientSocket.getOutputStream());
			ois = new ObjectInputStream(clientSocket.getInputStream());
			System.out.println("Connected.");
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + hostname);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: " + hostname);
		}

		if (clientSocket == null || oos == null) {
			System.err.println("Something is wrong. Socket or Object Output is null.");
		}
	}

	public void initialSyncWithServer() {
		try {
//		get data from server and synchronize client player
			if (clientSocket != null) {
				setInitialData((DataForSync) ois.readObject());
				System.out.println("Data came from server: " + getInitialData().toString());
			}
		} catch (IOException ex) {
			System.err.println(ex);
		} catch (ClassNotFoundException ex) {
			System.err.println(ex);
		}
	}

	public void sendObject(Serializable dataObj) throws IOException {
		oos.reset();
		oos.writeObject(dataObj);
		oos.flush();

	}

	public File[] getRemoteDirContents(String path) throws IOException {
//		send path to open remotly
		oos.reset();
		oos.writeObject(path);
		oos.flush();

		try {
			return (File[]) ois.readObject();
		} catch (ClassNotFoundException ex) {
			System.err.println("Could not read remote dir contents");
		}
		return null;
	}

	public void sendOpenRequest() throws IOException {
//		inform server about open file request
		System.out.println("Sending open request");
		DataForSync data = new DataForSync();
		data.setOpenrequest(true);
		oos.reset();
		oos.writeObject(data);
		oos.flush();
	}
	

	public void addIncomingDataListener(IncomingDataListener toadd) {
		newDataListener.add(toadd);
	}

	/**
	 * @return the initialData
	 */
	public DataForSync getInitialData() {
		return initialData;
	}

	/**
	 * @param initialData the initialData to set
	 */
	public void setInitialData(DataForSync initialData) {
		this.initialData = initialData;
		for (IncomingDataListener ping : newDataListener) {
			ping.SyncDataIncoming(initialData);
		}
	}

	public void stop() {
		try {
			clientSocket.close();
		} catch (IOException ex) {
			Logger.getLogger(MediaClient.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (NullPointerException e){
		}


	}

	public void receiveConfirmation() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}

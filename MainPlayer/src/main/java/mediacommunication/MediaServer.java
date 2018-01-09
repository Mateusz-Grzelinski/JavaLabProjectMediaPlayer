/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mediacommunication;

import MainPlayer.MainPlayerFXMLController;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mat
 */
public class MediaServer implements Runnable {

	// declare a server socket and a client socket for the server
	ServerSocket echoServer = null;
	Socket clientSocket = null;
	private DataForSync newData;
	private MainPlayerFXMLController controler;
	private final List<IncomingDataListener> newDataListener = new ArrayList<>();

	int port = 6789;
	boolean running;

	public static void main(String args[]) {
		int port = 6789;
		MediaServer server = new MediaServer(port);
		server.startServer();
	}

	public MediaServer(int port) {
		this.port = port;
		System.out.println("Server starting...");
	}

	public MediaServer(int port, MainPlayerFXMLController controler) {
		this.port = port;
		this.controler = controler;
		System.out.println("Server starting...");
	}

	public void stopServer() {
		System.out.println("Server cleaning up.");
		try {
			if (echoServer != null) {
				echoServer.close();
			}
			if (clientSocket != null) {
				clientSocket.close();
			}
		} catch (IOException ex) {
		}
		running = false;
	}

	public void startServer() {
		// Try to open a server socket on the given port
		try {
			echoServer = new ServerSocket(port);
			running = true;
		} catch (IOException e) {
			System.err.println("Unable to open port at port: " + port);
			return;
		}

		System.out.println("Waiting for connections. Only one connection is allowed.");

		// Create a socket object from the ServerSocket to listen and accept connections.
		// Use Server1Connection to process the connection.
		while (running) {
			try {
				clientSocket = echoServer.accept();
				Server1Connection newClient = new Server1Connection(clientSocket, this);
				newClient.runSingleClient();
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}

	public void addNewDataListener(IncomingDataListener toadd) {
		newDataListener.add(toadd);
	}

	@Override
	public void run() {
		startServer();
	}

	/**
	 * @return the newData
	 */
	public DataForSync getNewData() {
		return newData;
	}

	/**
	 * @param newData the newData to set can be triggered only by incoming data
	 * through socket
	 */
	void setNewData(DataForSync newData) {
		this.newData = newData;
		for (IncomingDataListener ping : newDataListener) {
			ping.SyncDataIncoming(newData);
		}
	}

	/**
	 * @return the controler
	 */
	public MainPlayerFXMLController getControler() {
		return controler;
	}

	/**
	 * @param controler the controler to set
	 */
	public void setControler(MainPlayerFXMLController controler) {
		this.controler = controler;
	}

}

class Server1Connection {

	ObjectOutputStream oos;
	ObjectInputStream ois;
	Socket clientSocket;
	MediaServer server;

	public Server1Connection(Socket clientSocket, MediaServer server) {
		this.clientSocket = clientSocket;
		this.server = server;
		System.out.println("Connection established with: " + clientSocket);
		try {
			oos = new ObjectOutputStream(clientSocket.getOutputStream());
			ois = new ObjectInputStream(clientSocket.getInputStream());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void runSingleClient() {
		try {
//		first, send initial server status to client
			DataForSync serverStatusData = server.getControler().inititialClientSync();
			oos.writeObject(serverStatusData);
			oos.flush();
			while (clientSocket.isConnected()) {
//				second, get responses
				System.out.println("Normal connection for synchong.");
				DataForSync newData = (DataForSync) ois.readObject();

				server.setNewData(newData);
				checkOpenRequest(newData);

				System.out.println("Received: ");
				System.out.println(server.getNewData().toString());
			}

			System.out.println("Connection closed.");
			ois.close();
			clientSocket.close();
		} catch (IOException e) {
			System.out.println(e);
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(Server1Connection.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private File[] listFiles(final File folder) {
		if (folder.isDirectory()) {
			return folder.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return !file.isHidden();
				}
			});
		}
		return null;
	}

	private void checkOpenRequest(DataForSync newData) throws IOException {
		String path;
		boolean continuewithloop;
		continuewithloop = newData.isOpenrequest();
		while (continuewithloop) {
			try {
				continuewithloop = newData.isOpenrequest();
				System.out.println("Waiting for path...");
				path = (String) ois.readObject();

				File folder;
				if (path.isEmpty()) {
					folder = new File("/home/mat/");
				} else {
					folder = new File(path);
				}

				System.out.println("Sending file list");
				oos.reset();
				oos.writeObject(listFiles(folder));
				oos.flush();

				System.out.println("Waiting for sync obj...");
				newData = (DataForSync) ois.readObject();
				server.setNewData(newData);
//				continuewithloop = false;

//				if (continuewithloop && !newData.getCurrentTitle().isEmpty()) {
//					File file = new File(newData.getCurrentTitle());
//					continuewithloop = file.isDirectory();
//				} else {
//					System.out.println("Can not play that file");
//				}
				System.out.println("Continue? " + continuewithloop);

			} catch (ClassNotFoundException ex) {
				System.err.println("Can not recognise reseived string. Open Request.");
			}
		}
	}

}

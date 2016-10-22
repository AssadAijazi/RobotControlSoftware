package mars;

import java.io.*;
import java.net.*;

public class NetworkDaemon {
	private Socket connection;
	private String serverName;
	private int port;

	public NetworkDaemon(String sN, int p) {
		connection = new Socket();
		serverName = sN;
		port = p;
	}

	// connects to robot
	public void connect() {
		try {
			connection.connect(new InetSocketAddress(serverName,port),1000);
		} catch (Exception e) {
			System.err.println("Robot connection failure: " + e);
		}
	}

	// sends byte array of joystick to robot
	public void send(byte[] byteArr) {
		if (connection.isConnected()) {
			try {
				OutputStream out = connection.getOutputStream();
				out.write(byteArr, 0, byteArr.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			System.err.println("Warning: data not sent! (no connection)");
		}
	}
	//determines if is connected (accessor method for socket)
	public boolean isConnected(){
		return connection.isConnected();
	}

}
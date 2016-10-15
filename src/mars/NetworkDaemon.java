package mars;

import java.io.*;
import java.net.*;

public class NetworkDaemon {
	private Socket connection;
	private String serverName;
	private int port;
	
	public NetworkDaemon(String sN, int p) {
		connection = null;
		serverName = sN;
		port = p;
	}
	
	//connects to robot
	public void connect() {
		try {
			connection = new Socket(serverName, port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//sends byte array of joystick to robot
	public void send(byte[] byteArr) {
		try {
			OutputStream out = connection.getOutputStream();
			out.write(byteArr, 0, byteArr.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
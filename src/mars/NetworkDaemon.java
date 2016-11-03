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
	public void connect() throws Exception {
		try {
			connection.connect(new InetSocketAddress(serverName,port),1000);
		} catch (Exception e) {
			throw e;
		}
	}

	// sends byte array of joystick to robot
	public void send(byte[] byteArr) throws Exception {
		if (connection.isConnected()) {
			try {
				OutputStream out = connection.getOutputStream();
				out.write(byteArr, 0, byteArr.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			throw new Exception("Connection interrupted. Restart");
		}
	}
	//determines if is connected (accessor method for socket)
	public boolean isConnected(){
		return connection.isConnected();
	}

}
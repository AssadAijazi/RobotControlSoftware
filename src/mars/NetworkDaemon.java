package mars;

import java.io.*;
import java.net.*;

public class NetworkDaemon {
	private Socket connection;
	private String serverName;
	private String debugServerName;
	private int port;

	public NetworkDaemon(String sN, int p) {
		serverName = sN;
		debugServerName = "localhost";
		port = p;
	}

	// connects to robot
	public void connect(boolean isDebug) throws Exception {
		String connectAddress;
		if(isDebug) {
			connectAddress = debugServerName;
		} else {
			connectAddress = serverName;
		}
		try {
			connection = new Socket();
			connection.connect(new InetSocketAddress(connectAddress,port),1000);
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
		return !connection.isClosed();
	}
	
	public void disconnect() {
		try {
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
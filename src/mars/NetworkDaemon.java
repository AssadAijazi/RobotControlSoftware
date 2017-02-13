package mars;

import java.io.*;
import java.net.*;

public class NetworkDaemon {
	private Socket connection;
	//used for connecting to robot
	private String robotAddress;
	//used for connecting to test server, if in debug mode
	private String debugServerName;
	//default port number; 5565
	private int port;

	public NetworkDaemon(String robotAddress, int port) {
		this.robotAddress = robotAddress;
		debugServerName = "localhost";
		this.port = port;
	}

	// connects to robot by setting up a socket connection
	public void connect(boolean isDebug) throws Exception {
		String connectAddress;
		//if in debug mode, connect to test server, otherwise attempt to connect to robot
		if (isDebug) {
			connectAddress = debugServerName;
		} else {
			connectAddress = robotAddress;
		}
		//attempt to create a socket connection with the specified connect address, 
		//times out after one second if no connection is found
		try {
			connection = new Socket();
			connection.connect(new InetSocketAddress(connectAddress, port), 1000);
		} catch (Exception e) {
			throw e;
		}
	}

	// sends byte array of joystick to robot
	public void send(byte[] byteArr) throws Exception {
		//makes sure socket is connected
		if (connection.isConnected()) {
			try {
				OutputStream out = connection.getOutputStream();
				out.write(byteArr, 0, byteArr.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new Exception("No connection found. Connect and try again");
		}
	}

	// receives byte array from test server/robot
	public byte[] receive() throws Exception {
		//expanding byte array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] received = new byte[20];
		InputStream in = connection.getInputStream();
		if(!connection.isClosed()) {
			int numBytes = in.read(received);
			int i = 0;
			while(baos.size() < numBytes) {
				baos.write(received, i, 1);
				i++;
			}
		}
		return baos.toByteArray();
	}

	// determines if is connnected
	public boolean isConnected() {
		return !connection.isClosed();
	}
	
	//attempts to close the socket connection
	public void disconnect() {
		try {
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
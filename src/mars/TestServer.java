package mars;

import java.io.*;
import java.net.*;

//test server to simulate robot; for debugging purposes
public class TestServer extends Thread {
	private ServerSocket serverSocket;
	private Socket server;

	public TestServer(int portNum) {
		try {
			serverSocket = new ServerSocket(portNum);
			serverSocket.setSoTimeout(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			//establishes connection
			server = serverSocket.accept();
			
			//test server receives byte array and prints it console
			while (!Thread.currentThread().isInterrupted()) {
				InputStream in = server.getInputStream();
				byte[] input = new byte[14];
				in.read(input, 0, input.length);
				System.out.print("Test Server received: ");
				for (byte b : input) {
					System.out.print(String.format("%8s", Integer.toBinaryString((b + 256) % 256)).replace(' ', '0'));
					System.out.print(" ");
				}
				System.out.println();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}

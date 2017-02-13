package mars;

import java.io.*;
import java.net.*;

//test server to simulate robot; for debugging purposes
public class TestServer {
	private ServerSocket serverSocket;
	private Socket server;
	private Thread rt;
	private static Thread st;

	public TestServer(int portNum) {
		try {
			serverSocket = new ServerSocket(portNum);
			serverSocket.setSoTimeout(0);
			rt = new ReceivingThread(serverSocket, server);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		System.out.println("Starting Test Server...");
		rt.start();
		System.out.println("Test Server Started");
	}

	public void stop() {
		rt.interrupt();
		st.interrupt();
	}

	public static class ReceivingThread extends Thread {
		ServerSocket serverSocket;
		Socket server;

		public ReceivingThread(ServerSocket serverSocket, Socket server) {
			this.serverSocket = serverSocket;
			this.server = server;
		}

		public void run() {
			try {
				// establishes connection
				System.out.println("Waiting for client...");
				server = serverSocket.accept();
				System.out.println("Test Server connected to client");
				st = new SendingThread(server);
				st.start();

				// test server receives byte array and prints it console
				while (!Thread.currentThread().isInterrupted()) {
					InputStream in = server.getInputStream();
					byte[] input = new byte[14];
					int bytesRead = in.read(input, 0, input.length);

					if (bytesRead > 0) {
						System.out.println("Test Server received: " + byteArrToString(input, bytesRead));
					}
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

	public static class SendingThread extends Thread {
		Socket server;

		public SendingThread(Socket server) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Thread.currentThread().interrupt();
			}
			this.server = server;
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(5500);
					OutputStream out = server.getOutputStream();
					int length = (int) (Math.random() * 10) + 1;
					byte[] output = new byte[length];
					for (int i = 0; i < output.length; i++) {
						output[i] = (byte) (Math.random() * 255 - 127);
					}
					System.out.println("Test Server sent: " + byteArrToString(output, length));

					out.write(output, 0, output.length);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
	
	//formats byte array to String for easier reading
	public static String byteArrToString(byte[] byteOutput, int length) {
		String formattedByteArr = "";
		for (int i = 0; i < length; i++) {
			formattedByteArr += String.format("%8s", Integer.toBinaryString((byteOutput[i] + 256) % 256)).replace(' ',
					'0');
			formattedByteArr += " ";
		}
		return formattedByteArr;
	}
	
	public static void main(String[] args) {
		TestServer testServer = new TestServer(5565);
		testServer.start();
		
	}

}

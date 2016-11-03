package mars;

import java.io.IOException;

public class Launcher {
	public static final boolean DEBUG_MODE = false;
	public static final String ROBOT_DEFAULT_ADDRESS = "192.168.1.102";
	public static boolean connectionActivated = false;

	public static void main(String[] args) {
		UserInterface ui = new UserInterface();

		Joystick j = null;
		try {
			j = new Joystick();
		} catch (IOException e1) {
			ui.addError(e1.toString());
			System.err.println(e1);
		}
		// class for converting raw poll data to byte array
		PollDataConverter pdc = new PollDataConverter();
		// handing network
		int port = 5565;
		String hostname = DEBUG_MODE ? "localhost" : "192.168.1.102";
		NetworkDaemon nd = new NetworkDaemon(hostname, port);
		if (DEBUG_MODE) {
			TestServer server = new TestServer(port);
			server.start();
		}
		
		// initial poll of the controller
		float[] output = new float[17];

		// main loop
		while (true) {

			// checks if threads sending signals have been instantiated yet
			if (!connectionActivated) {
				if (ui.getAttemptConnection()) {
					try {
						nd.connect();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						ui.addError("Robot Connection failure. Restart.");
					}
					// starts thread for periodic update
					new Thread(new PeriodicUpdate(nd, pdc, ui)).start();
					// starts thread for update on change
					if (j != null) {
						new Thread(new UpdateOnChange(j, nd, pdc, ui)).start();
					}
					connectionActivated = true;
				}
			}
			output = new float[17];
			if (j != null) {

				// updates joystick, receives raw data, then converts to byte
				// array
				try {
					j.update();
				} catch (Exception e) {
					ui.addError(e.toString());
				}
				
				output = j.getRawPollData();
			}
			pdc.convert(output);
		}

	}

	// sets up thread to send robot state of controller every 1 second
	public static class PeriodicUpdate implements Runnable {
		NetworkDaemon nd;
		PollDataConverter pdc;
		UserInterface ui;

		public PeriodicUpdate(NetworkDaemon net, PollDataConverter poll, UserInterface u) {
			nd = net;
			pdc = poll;
			ui = u;
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (nd.isConnected()) {
					printByteArr(pdc.getByteArr());
					try {
						nd.send(pdc.getByteArr());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						ui.addError(e.toString());
					}
				}
			}
		}
	}

	// Sets up thread to send robot state of controller if there is a change
	public static class UpdateOnChange implements Runnable {
		Joystick j;
		NetworkDaemon nd;
		PollDataConverter pdc;
		UserInterface ui;

		public UpdateOnChange(Joystick joy, NetworkDaemon net, PollDataConverter poll, UserInterface u) {
			j = joy;
			nd = net;
			pdc = poll;
			ui = u;
		}

		@Override
		public void run() {
			while (true) {
				if (nd.isConnected() && j.isChanged()) {
					printByteArr(pdc.getByteArr());
					try {
						nd.send(pdc.getByteArr());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						ui.addError(e.toString());
					}
				}
			}

		}

	}

	// fancy method from stack overflow to properly print bytes
	// to console

	public static void printByteArr(byte[] byteOutput) {
		if (!DEBUG_MODE) {
			System.out.print("Sent: ");
			for (int i = 0; i < byteOutput.length; i++) {
				String formattedByteArr = String.format("%8s", Integer.toBinaryString((byteOutput[i] + 256) % 256))
						.replace(' ', '0');
				System.out.print(formattedByteArr);
				System.out.print(" ");
			}
			System.out.println();
		}
	}

}

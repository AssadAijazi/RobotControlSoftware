package mars;

import java.awt.Color;
import java.io.IOException;

public class Launcher {
	public static final String ROBOT_DEFAULT_ADDRESS = "192.168.1.102";
	public static final int PORT = 5565;
	public static boolean isDebugMode;
	public static boolean connectionActivated = false;
	public static boolean connectedToStick;
	public static TestServer testServer;
	public static PeriodicUpdate pu;
	public static UpdateOnChange uoc;

	public static void main(String[] args) {
		// handing network
		String hostname = ROBOT_DEFAULT_ADDRESS;
		NetworkDaemon nd = new NetworkDaemon(hostname, PORT);
		UserInterface ui = new UserInterface(nd);
		Joystick j = null;
		try {
			j = new Joystick();
			connectedToStick = true;
			ui.addMessage("Successfully connected to joystick");
		} catch (IOException e1) {
			ui.addError(e1.toString());
			System.err.println(e1);
			connectedToStick = false;
		}

		// class for converting raw poll data to byte array
		PollDataConverter pdc = new PollDataConverter();

		// class for managing keyboard input
		KeyboardHandler kbHandler = ui.getKeyboardhandler();

		float[] output = new float[17];
		// main loop
		while (true) {

			// checks if threads sending signals have been instantiated yet
			if (!connectionActivated) {
				// checks if connect button has been pushed
				if (ui.getAttemptConnection()) {
					attemptInitializeConnection(j, nd, pdc, ui);
				}
			} else {
				// checks if disconnect button has been pushed
				if (ui.getAttemptDisconnection()) {
					pu.interrupt();
					if (uoc != null) {
						uoc.interrupt();
					}
					if (testServer != null) {
						testServer.interrupt();
					}
					nd.disconnect();
					if(isDebugMode) {
						ui.addMessage("Successfully disconnected from Test Server");
					} else {
						ui.addMessage("Successfully disconnected from Robot");
					}
					connectionActivated = false;
					ui.updateConnectionStatus(false);
					ui.setAttemptDisconnection(false);
					ui.setByteOutputColor(Color.BLACK);
				}
			}
			output = new float[17];

			if (j != null) {

				// updates joystick, receives raw data, then converts to byte
				// array
				try {
					j.update();
				} catch (Exception e) {
					connectedToStick = false;
					ui.addError(e.toString());
				}

				output = j.getRawPollData();

			}
			// uses keyboard as input source if in keyboard mode
			if (ui.getIsKB()) {
				kbHandler.update();
				output = kbHandler.getRawPollData();
			}
			pdc.convert(output);
			ui.updateJoystickPanel(output, connectedToStick);
		}

	}

	// code that tries to set up a connection to robot/test server if none
	// already exists
	public static void attemptInitializeConnection(Joystick j, NetworkDaemon nd, PollDataConverter pdc,
			UserInterface ui) {
		isDebugMode = ui.getDebugMode();

		connectionActivated = true;
		if (isDebugMode) {
			testServer = new TestServer(PORT);
			testServer.start();
		}
		try {
			nd.connect(isDebugMode);

			// starts thread for periodic update
			pu = new PeriodicUpdate(nd, pdc, ui);
			pu.start();
			// starts thread for update on change
			// if (j != null) {
			uoc = new UpdateOnChange(j, nd, pdc, ui);
			uoc.start();
			// }
			ui.updateConnectionStatus(true);
			if(isDebugMode) {
				ui.addMessage("Successfully connected to Test Server");
			} else {
				ui.addMessage("Successfully connected to Robot");
			}

		} catch (Exception e) {
			e.printStackTrace();
			ui.addError("Robot Connection failure. Try again.");
			connectionActivated = false;

		}
		

		ui.setAttemptConnection(false);

	}

	// sets up thread to send robot state of controller every 1 second
	public static class PeriodicUpdate extends Thread {
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
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(100);
					ui.setByteOutputColor(Color.BLACK);
					Thread.sleep(900);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Thread.currentThread().interrupt();
				}
				if (nd.isConnected()) {
					printByteArr(pdc.getByteArr());
					try {
						ui.setByteOutputColor(Color.GREEN);
						nd.send(pdc.getByteArr());
						ui.setByteOutput(pdc.getByteArrAsStr());
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
	public static class UpdateOnChange extends Thread {
		Joystick j;
		NetworkDaemon nd;
		PollDataConverter pdc;
		UserInterface ui;
		KeyboardHandler kbHandler;

		public UpdateOnChange(Joystick joy, NetworkDaemon net, PollDataConverter poll, UserInterface u) {
			j = joy;
			nd = net;
			pdc = poll;
			ui = u;
			kbHandler = ui.getKeyboardhandler();
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				// should probably make this statement clearer...
				if ((nd.isConnected() && ui.getIsKB() && kbHandler.isChanged())
						|| (nd.isConnected() && (j != null) && (!ui.getIsKB()) && j.isChanged())) {
					try {
						ui.setByteOutputColor(Color.GREEN);
						nd.send(pdc.getByteArr());
						ui.setByteOutput(pdc.getByteArrAsStr());
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
		if (!isDebugMode) {
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

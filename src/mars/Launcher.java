package mars;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;

public class Launcher {
	public static final String ROBOT_DEFAULT_ADDRESS = "192.168.1.102";
	public static final int PORT = 5565;
	public static boolean isDebugMode;
	public static boolean connectionActivated = false;
	public static boolean connectedToLeftStick;
	public static boolean connectedToRightStick;
	public static TestServer testServer;
	public static PeriodicUpdate pu;
	public static UpdateOnChange uoc;
	public static ReceiveThread rt;

	public static void main(String[] args) {
		// handing network
		String hostname = ROBOT_DEFAULT_ADDRESS;
		NetworkDaemon nd = new NetworkDaemon(hostname, PORT);
		UserInterface ui = new UserInterface(nd);
		Joystick rightJoy = null;
		Joystick leftJoy = null;

		try {
			rightJoy = new Joystick(1);
			connectedToRightStick = true;
			ui.addMessage("Successfully connected to right joystick");
		} catch (IOException e1) {
			ui.addError("Right Joystick: " + e1.toString());
			System.err.println(e1);
			connectedToRightStick = false;
		}

		try {
			leftJoy = new Joystick(2);
			connectedToLeftStick = true;
			ui.addMessage("Successfully connected to left joystick");
		} catch (IOException e1) {
			ui.addError("Left Joystick: " + e1.toString());
			System.err.println(e1);
			connectedToLeftStick = false;
		}

		JButton switchButton = ui.getSwitchButton();
		switchButton.addActionListener(new SwitchButtonListener(rightJoy, leftJoy, ui));

		// class for converting raw poll data to byte array
		PollDataConverter pdc = new PollDataConverter();

		// class for managing keyboard input
		KeyboardHandler kbHandler = ui.getKeyboardhandler();

		float[] rightOutput = new float[17];
		float[] leftOutput = new float[17];

		// main loop
		while (true) {

			// checks if threads sending signals have been instantiated yet
			if (!connectionActivated) {
				// checks if connect button has been pushed
				if (ui.getAttemptConnection()) {
					attemptInitializeConnection(rightJoy, leftJoy, nd, pdc, ui);
				}
			} else {
				// checks if disconnect button has been pushed
				if (ui.getAttemptDisconnection()) {
					nd.disconnect();
					pu.interrupt();
					if (uoc != null) {
						uoc.interrupt();
					}
					if (testServer != null) {
						testServer.stop();
					}
					rt.interrupt();
					if (isDebugMode) {
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
			rightOutput = new float[17];
			leftOutput = new float[17];

			if (rightJoy != null) {

				// updates joystick, receives raw data, then converts to byte
				// array
				try {
					rightJoy.update();
				} catch (Exception e) {
					connectedToRightStick = false;
					ui.addError("Right Joystick: " + e.toString());
				}

				rightOutput = rightJoy.getRawPollData();

			}

			if (leftJoy != null) {

				// updates joystick, receives raw data, then converts to byte
				// array
				try {
					leftJoy.update();
				} catch (Exception e) {
					connectedToLeftStick = false;
					ui.addError("Left Joystick: " + e.toString());
				}

				leftOutput = leftJoy.getRawPollData();

			}
			// uses keyboard as input source if in keyboard mode
			if (ui.getIsKB()) {
				kbHandler.update();
				rightOutput = kbHandler.getRawPollData();
			}
			pdc.convert(rightOutput, leftOutput);
			ui.updateJoystickPanel(rightOutput, connectedToRightStick, leftOutput, connectedToLeftStick);
		}

	}

	// used for switching which joystick is which
	public static class SwitchButtonListener implements ActionListener {

		private Joystick rightJoy, leftJoy;
		private UserInterface ui;

		public SwitchButtonListener(Joystick rightJoy, Joystick leftJoy, UserInterface ui) {
			this.rightJoy = rightJoy;
			this.leftJoy = leftJoy;
			this.ui = ui;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean rightSwitched = false, leftSwitched = false;
			if ((rightJoy != null && rightJoy.findDevicesConnected() == 2)
					|| (leftJoy != null && leftJoy.findDevicesConnected() == 2)) {
				if (rightJoy.getIndex() == 1) {
					try {
						rightJoy.switchIndex(2);
						rightSwitched = true;
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						ui.addError("Right Joystick: " + e1);
					}
					try {
						leftJoy.switchIndex(1);
						leftSwitched = true;
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						ui.addError("Left Joystick: " + e1);
					}
				} else {
					try {
						rightJoy.switchIndex(1);
						rightSwitched = true;
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						ui.addError("Right Joystick: " + e1);
					}
					try {
						leftJoy.switchIndex(2);
						leftSwitched = true;
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						ui.addError("Left Joystick: " + e1);
					}
				}
				if (rightSwitched && leftSwitched) {
					ui.addMessage("Successfully switched joysticks");
				}
			} else {
				ui.addError("Not enough joysticks to switch");
			}
		}
	}

	// code that tries to set up a connection to robot/test server if none
	// already exists
	public static void attemptInitializeConnection(Joystick rightJoy, Joystick leftJoy, NetworkDaemon nd,
			PollDataConverter pdc, UserInterface ui) {
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
			uoc = new UpdateOnChange(rightJoy, leftJoy, nd, pdc, ui);
			uoc.start();
			// }
			rt = new ReceiveThread(nd, ui);
			rt.start();
			ui.updateConnectionStatus(true);
			if (isDebugMode) {
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
		Joystick rightJoy, leftJoy;
		NetworkDaemon nd;
		PollDataConverter pdc;
		UserInterface ui;
		KeyboardHandler kbHandler;

		public UpdateOnChange(Joystick rightJoy, Joystick leftJoy, NetworkDaemon nd, PollDataConverter pdc,
				UserInterface ui) {
			this.rightJoy = rightJoy;
			this.leftJoy = leftJoy;
			this.nd = nd;
			this.pdc = pdc;
			this.ui = ui;
			this.kbHandler = this.ui.getKeyboardhandler();
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				// should probably make this statement clearer...
				if (nd.isConnected() && ((ui.getIsKB() && kbHandler.isChanged())
						|| (rightJoy != null && !ui.getIsKB() && rightJoy.isChanged())
						|| (leftJoy != null && !ui.getIsKB() && leftJoy.isChanged()))) {
					try {
						ui.setByteOutputColor(Color.GREEN);
						nd.send(pdc.getByteArr());
						ui.setByteOutput(pdc.getByteArrAsStr());
					} catch (Exception e) {
						e.printStackTrace();
						ui.addError(e.toString());
					}
				}
			}

		}

	}

	public static class ReceiveThread extends Thread {
		NetworkDaemon nd;
		UserInterface ui;

		public ReceiveThread(NetworkDaemon nd, UserInterface ui) {
			this.nd = nd;
			this.ui = ui;
		}

		@Override
		public void run() {
			byte[] input = new byte[0];
			while (true) {
				try {
					if (nd.isConnected()) {
						input = nd.receive();
					}
					if (input.length > 0) {
						ui.addMessage("Received: " + byteArrToString(input));
					}
				} catch (Exception e) {
					ui.addError(e.toString());
				}
			}
		}
	}

	// fancy method from stack overflow to properly print bytes
	// to console

	public static String byteArrToString(byte[] byteOutput) {
		String formattedByteArr = "";
		for (int i = 0; i < byteOutput.length; i++) {
			formattedByteArr += String.format("%8s", Integer.toBinaryString((byteOutput[i] + 256) % 256)).replace(' ',
					'0');
			formattedByteArr += " ";
		}
		return formattedByteArr;
	}

}

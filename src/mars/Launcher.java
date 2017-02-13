package mars;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.SocketException;

import javax.swing.JButton;

public class Launcher {
	// default configuration for connection to robot mode
	public static final String ROBOT_DEFAULT_ADDRESS = "192.168.1.102";
	public static final int PORT = 5565;
	// is the program currently in debug mode?
	public static boolean isDebugMode;
	// checks whether a socket connection is active
	public static boolean connectionActivated = false;
	// checks if connected to left joystick
	public static boolean connectedToLeftStick;
	// checks if connected to right joystick
	public static boolean connectedToRightStick;
	// used for debugging; hosts a server that the program can connect and send
	// signals to
	public static TestServer testServer;
	// thread that sends a byte representing the joystick state once every
	// second
	public static PeriodicUpdate pu;
	// thread that sends the joystick state if there is a change
	public static UpdateOnChange uoc;
	// thread that receives any signals from the robot/test server over the socket connection
	public static ReceiveThread rt;

	public static void main(String[] args) {
		// handing network
		String hostname = ROBOT_DEFAULT_ADDRESS;

		// Class that controls sending bytes to the robot
		NetworkDaemon nd = new NetworkDaemon(hostname, PORT);
		// Class that controls the Graphical User Interface
		UserInterface ui = new UserInterface(nd);

		Joystick rightJoy = null;
		Joystick leftJoy = null;
		// connects to right joystick, if it is connected
		try {
			rightJoy = new Joystick(1);
			connectedToRightStick = true;
			ui.addMessage("Successfully connected to right joystick");
		} catch (IOException e1) {
			ui.addError("Right Joystick: " + e1.toString());
			System.err.println(e1);
			connectedToRightStick = false;
		}
		// connects to left joystick, if it is connected
		try {
			leftJoy = new Joystick(2);
			connectedToLeftStick = true;
			ui.addMessage("Successfully connected to left joystick");
		} catch (IOException e1) {
			ui.addError("Left Joystick: " + e1.toString());
			System.err.println(e1);
			connectedToLeftStick = false;
		}

		// class for converting raw poll data to byte array
		PollDataConverter pdc = new PollDataConverter();

		// used for switching joysticks
		JButton switchButton = ui.getSwitchButton();
		switchButton.addActionListener(new SwitchButtonListener(rightJoy, leftJoy, ui));

		// class for managing keyboard input
		KeyboardHandler kbHandler = ui.getKeyboardhandler();

		// raw float output from the left and right joystick, respectively
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

				// checks if disconnect button has been pushed
			} else {
				// closes socket connection, stops all of the sending and
				// receiving threads
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

					// checks if pause button has been pushed; if so, closes
					// the threads that send the joystick state to the robot
					// but keeps everything else, i.e. socket connection and
					// receiving thread
				} else if (ui.getAttemptPause()) {
					pu.interrupt();
					uoc.interrupt();
					ui.addMessage("Successfully paused Joystick Stream");
					ui.setAttemptPause(false);

					// resumes the joystick state stream if it has been paused
				} else if (ui.getAttemptPlay()) {
					pu = new PeriodicUpdate(nd, pdc, ui);
					pu.start();
					uoc = new UpdateOnChange(rightJoy, leftJoy, nd, pdc, ui);
					uoc.start();
					ui.addMessage("Successfully resumed Joystick Stream");
					ui.setAttemptPlay(false);
				}
			}
			rightOutput = new float[17];
			leftOutput = new float[17];

			if (rightJoy != null) {

				// updates right joystick, receives raw data, then converts to
				// byte
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

				// updates right joystick, receives raw data, then converts to
				// byte
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
			// converts to the state of the two joysticks as specified in the
			// PollDataConverter class
			pdc.convert(rightOutput, leftOutput);
			// updates user interface with the newly polled joystick data
			ui.updateJoystickPanel(rightOutput, connectedToRightStick, leftOutput, connectedToLeftStick);
		}

	}

	// used for switching which joysticks
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
			// occurs if one joystick is actively connected and two total
			// joyticks were found
			if ((rightJoy != null && rightJoy.findDevicesConnected() == 2)
					|| (leftJoy != null && leftJoy.findDevicesConnected() == 2)) {
				if (rightJoy.getIndex() == 1) {
					try {
						rightJoy.switchIndex(2);
						rightSwitched = true;
					} catch (IOException e1) {
						ui.addError("Right Joystick: " + e1);
					}
					try {
						leftJoy.switchIndex(1);
						leftSwitched = true;
					} catch (IOException e1) {
						ui.addError("Left Joystick: " + e1);
					}
				} else {
					try {
						rightJoy.switchIndex(1);
						rightSwitched = true;
					} catch (IOException e1) {
						ui.addError("Right Joystick: " + e1);
					}
					try {
						leftJoy.switchIndex(2);
						leftSwitched = true;
					} catch (IOException e1) {
						ui.addError("Left Joystick: " + e1);
					}
				}
				// adds message to ui if joysticks were successfully switched
				if (rightSwitched && leftSwitched) {
					ui.addMessage("Successfully switched joysticks");
				}

				// if there is only one or no joy sticks connected, switching
				// fails
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
			uoc = new UpdateOnChange(rightJoy, leftJoy, nd, pdc, ui);
			uoc.start();
			// starts thread for receiving signals
			rt = new ReceiveThread(nd, ui);
			rt.start();

			// updates user interface if successfully connected
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

		// used to make sure it only tries once per button click
		ui.setAttemptConnection(false);

	}

	// sets up thread to send robot state of controller every 1 second
	public static class PeriodicUpdate extends Thread {
		NetworkDaemon nd;
		PollDataConverter pdc;
		UserInterface ui;

		public PeriodicUpdate(NetworkDaemon nd, PollDataConverter pdc, UserInterface ui) {
			this.nd = nd;
			this.pdc = pdc;
			this.ui = ui;
		}

		@Override
		public void run() {

			// sends a new signal every second (1000 milisconds);
			// green flashing effect provided by turning the byte text
			// green when the signal is sent, and then turing it black 900
			// Milliseconds later
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(100);
					ui.setByteOutputColor(Color.BLACK);
					Thread.sleep(900);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				if (nd.isConnected()) {
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

			ui.setByteOutputColor(Color.BLACK);

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
				// Sends the controller state to the robot if the socket connection is active
				// and at least one of the following conditions is met:
				// 1. Keyboard Mode is activated and keyboard state is changed
				// 2. Keyboard Mode is NOT actived, right joy stick is connected, and its state is changed
				// 3. Keyboard Mode is NOT actived, left joy stick is connected and its state is changed
				if (nd.isConnected() && ((ui.getIsKB() && kbHandler.isChanged())
						|| (rightJoy != null && !ui.getIsKB() && rightJoy.isChanged())
						|| (leftJoy != null && !ui.getIsKB() && leftJoy.isChanged()))) {
					try {
						//flashes green when a new signal is sent
						ui.setByteOutputColor(Color.GREEN);
						nd.send(pdc.getByteArr());
						ui.setByteOutput(pdc.getByteArrAsStr());
					} catch (Exception e) {
						e.printStackTrace();
						ui.addError(e.toString());
					}
				}
			}
			
			//sets the text to black when the thread is interrupted
			ui.setByteOutputColor(Color.BLACK);
		}

	}

	//Thread for receiving signals from the socket, constantly running 
	//independent of the sending threads
	public static class ReceiveThread extends Thread {
		NetworkDaemon nd;
		UserInterface ui;

		public ReceiveThread(NetworkDaemon nd, UserInterface ui) {
			this.nd = nd;
			this.ui = ui;
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				byte[] input = new byte[0];
				while (true) {
					try {
						if (nd.isConnected()) {
							input = nd.receive();
						}
						//prints the received byte array to the console in the UI
						if (input.length > 0) {
							ui.addMessage("Received: " + byteArrToString(input));
						}
						
					//catches if socket is closed; thread is terminated
					} catch (SocketException e) {
						break;
					} catch (Exception e) {
						ui.addError(e.toString());
					}
				}
			}
		}
	}

	// method to convert byte array to easily readable string

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

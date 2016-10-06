package mars;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier;

public class Joystick {
	private Controller joystick;
	private Component[] components;
	// the poll data as received from the joystick controller
	private float[] rawPollData;

	/*
	 * 8 Byte array to send to the robot. Byte 1 is a byte of all 1's. Bytes 2
	 * and the first half of byte 3 represent the state of the buttons (1 = on,
	 * 0 = off). The second half of byte 3 represents the state of the pov. Byte
	 * 4 represents the state of x-axis of the joystick. Byte 5 represents the
	 * state of the y-axis of the joystick. Byte 6 represents the state of the
	 * rotational z-axis. Byte 7 represents the state of the throttle. Byte 8 is
	 * a byte of all 1's
	 */
	private byte[] convertedPollData;

	// indices for all of the buttons in the Component array + description of
	// output

	// main stick y-axis; returns float between -1 and 1;
	// -1 is all the way forward, 1 is all the way back
	private int y = 0;
	// main stick x-axis; returns float between -1 and 1;
	// 1 is all the way forward, -1 is all the way back
	private int x = 1;
	// small stick on main stick; returns float between 0 and 1;
	// 0 is inactive, 0.25 is forward, 0.5 is right, 0.75 is down, 1.0 is left
	private int pov = 2;
	// main stick rotational z-axis; returns float between -1 and 1;
	// 1 is rotated all the way to the right, -1 is rotated all the way to the
	// left
	private int rz = 3;
	// all 12 buttons; returns a float 1.0 (pushed) or 0.0 (not pushed)
	private int b1 = 4;
	private int b2 = 5;
	private int b3 = 6;
	private int b4 = 7;
	private int b5 = 8;
	private int b6 = 9;
	private int b7 = 10;
	private int b8 = 11;
	// slider; returns float between 1 and -1;
	// -1 is pushed all the way to the top, 1 is pushed all the way to the
	// bottom
	private int slider = 12;
	private int b9 = 13;
	private int b10 = 14;
	private int b11 = 15;
	private int b12 = 16;

	public Joystick() {
		connectToStick();
		rawPollData = new float[components.length];
		convertedPollData = new byte[8];

		// sets the first and last bytes of the data stream as all 1's, used for
		// checking byte accuracy.
		for (int i = 0; i < 8; i++) {
			convertedPollData[0] |= (1 << i);
			convertedPollData[7] |= (1 << i);
		}
	}

	/*
	 * Loops through all connected devices and finds the joystick
	 */
	private void connectToStick() {

		while (true) {
			Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
			for (int i = 0; i < ca.length; i++) {
				if (ca[i].getType().equals(Controller.Type.STICK)) {
					System.out.println("Connection found");
					joystick = ca[i];
					components = joystick.getComponents();
					return;
				}
			}

		}
	}

	/*
	 * Main update method to execute in launcher. Updates gets raw poll data and
	 * converts to to byte array to be sent.
	 */
	public void update() {
		updateRawPollData();
		updateConvertedPollData();
	}

	/*
	 * Polls the controller then updates the raw poll data array with the new
	 * values
	 */
	private void updateRawPollData() {
		joystick.poll();
		for (int i = 0; i < components.length; i++) {
			rawPollData[i] = components[i].getPollData();
		}
	}

	/*
	 * Converts the raw poll data to a byte array to be sent to the robot
	 */
	private void updateConvertedPollData() {
		// setting up the bytes for the buttons

		// first byte of buttons
		for (int i = b1; i < slider; i++) {
			if (rawPollData[i] == 1.0f) {
				convertedPollData[1] |= (1 << (11 - i));
			} else if (rawPollData[i] == 0.0f) {
				convertedPollData[1] &= ~(1 << (11 - i));
			}
		}

		// last half byte of buttons
		for (int i = b9; i < rawPollData.length; i++) {
			if (rawPollData[i] == 1.0f) {
				convertedPollData[2] |= (1 << (20 - i));
			} else if (rawPollData[i] == 0.0f) {
				convertedPollData[2] &= ~(1 << (20 - i));
			}
		}

		// half byte of pov; bit 1 is forward, bit 2 is backward,
		// bit 3 is right, bit 4 is left
		// forward bit
		if ((rawPollData[pov] > 0.0f) && (rawPollData[pov] < 0.5f)) {
			convertedPollData[2] |= (1 << 3);
		} else {
			convertedPollData[2] &= ~(1 << 3);
		}
		// backward bit
		if ((rawPollData[pov] > 0.5f) && (rawPollData[pov] < 1.0f)) {
			convertedPollData[2] |= (1 << 2);
		} else {
			convertedPollData[2] &= ~(1 << 2);
		}
		// right bit
		if ((rawPollData[pov] > 0.25f) && (rawPollData[pov] < 0.75f)) {
			convertedPollData[2] |= (1 << 1);
		} else {
			convertedPollData[2] &= ~(1 << 1);
		}
		// left bit
		if (((rawPollData[pov] > 0.75f) && (rawPollData[pov] <= 1.0f))
				|| ((rawPollData[pov] > 0.0f) && (rawPollData[pov] < 0.25f))) {
			convertedPollData[2] |= (1 << 0);
		} else {
			convertedPollData[2] &= ~(1 << 0);
		}
	}

	public float[] getRawPollData() {
		return rawPollData;
	}

	public byte[] getConvertedPollData() {
		return convertedPollData;
	}

	/*
	 * Polls joystick, then loops through each component, printing the
	 * components name and poll data to the console
	 */
	public void updateToConsole() {
		joystick.poll();
		System.out.println("OUTPUT");
		for (int i = 0; i < components.length; i++) {
			System.out.println(components[i].getIdentifier() + ": " + components[i].getPollData());
		}

	}

	/*
	 * For testing purposes. Connects to the controller and updates to console
	 * every 5000 milliseconds
	 */
	public static void main(String[] args) {

		Joystick j = new Joystick();
		while (true) {
			// j.updateToConsole();
			j.update();
			float[] output = j.getRawPollData();
			byte[] byteOutput = j.getConvertedPollData();
			for (int i = 0; i < byteOutput.length; i++) {

				// fancy method from stack overflow to properly print bytes to console

				System.out.print(
						String.format("%8s", Integer.toBinaryString((byteOutput[i] + 256) % 256)).replace(' ', '0'));
				System.out.print(" ");
			}
			System.out.println();

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				System.out.println("error");
			}
		}

	}
}

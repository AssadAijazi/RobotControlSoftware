package mars;

import java.io.IOException;

import net.java.games.input.*;

public class Joystick {
	private Controller joystick;
	private boolean connected = false;
	private Component[] components;
	// the poll data as received from the joystick controller
	private float[] rawPollData;
	// used to check for changes in state of controller
	private float[] previousRawPollData;
	//used to tell what number joystick in the components array; used for switching
	private int index;

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

	public Joystick(int index) throws IOException {
		this.index = index;
		previousRawPollData = new float[17];
		rawPollData = new float[17];
		connectToStick(index);

	}

	//finds the total number of sticks connected
	public int findDevicesConnected() {
		int connectedDevices = 0;
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
		for(int i = 0; i < ca.length; i++) {
			if(ca[i].getType().equals(Controller.Type.STICK)) {
				connectedDevices++;
			}
		}
		return connectedDevices;
	}
	/*
	 * Loops through all connected devices and finds the joystick
	 */
	private void connectToStick(int index) throws IOException {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() < start + 500) {
			Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
			int devicesFound = 0;
			for (int i = 0; i < ca.length; i++) {
				if (ca[i].getType().equals(Controller.Type.STICK)) {
					devicesFound++;
					if (devicesFound == index) {
						connected = true;
						joystick = ca[i];
						components = joystick.getComponents();
						return;
					} 
				}
			}
		}
		throw new IOException("Joystick not found. Reconnect and restart.");
	}

	/*
	 * Polls the controller then updates the raw poll data array with the new
	 * values
	 */
	
	public void switchIndex(int i) throws IOException {
		this.index = i;
			connectToStick(i);
	}
	public void update() throws Exception {
		for (int i = 0; i < components.length; i++) {
			previousRawPollData[i] = components[i].getPollData();
		}
		if (connected) {

			connected = joystick.poll();
			if (!connected) {
				throw new Exception("Joystick has been disconnected. Restart.");
			}
		}

		for (int i = 0; i < components.length; i++) {
			rawPollData[i] = components[i].getPollData();
		}
	}

	public boolean isChanged() {
		for (int i = 0; i < components.length; i++) {
			if (!(Float.compare(rawPollData[i], previousRawPollData[i]) == 0)) {
				return true;
			}
		}
		return false;
	}
	
	public int getIndex() {
		return index;
	}

	public float[] getRawPollData() {
		return rawPollData;
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

}

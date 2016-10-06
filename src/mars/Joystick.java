package mars;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier;

public class Joystick {
	private Controller joystick;
	private Component[] components;
	//the poll data as received from the joystick controller
	private float[] rawPollData;
	
	/* 8 Byte array to send to the robot
	 * Byte 1 is a byte of all 1's. 
	 * Bytes 2 and the first half of byte 3 represent the state
	 * of the buttons (1 = on, 0 = off). 
	 * The second half of byte 3 represents the state of the pov. 
	 * Byte 4 represents the state of x-axis of the joystick. 
	 * Byte 5 represents the state of the y-axis of the joystick.
	 * Byte 6 represents the state of the rotational z-axis.
	 * Byte 7 represents the state of the throttle.
	 * Byte 8 is a byte of all 1's
	 */
	private byte[] convertedPollData;

	
	//indices for all of the buttons in the Component array + description of output
	
	//main stick y-axis; returns float between -1 and 1; 
	//-1 is all the way forward, 1 is all the way back
	private int y = 0;
	//main stick x-axis; returns float between -1 and 1;
	//1 is all the way forward, -1 is all the way back
	private int x = 1; 
	//small stick on main stick; returns float between 0 and 1;
	//0 is inactive, 0.25 is forward, 0.5 is right, 0.75 is down, 1.0 is left
	private int pov = 2; 
	//main stick rotational z-axis; returns float between -1 and 1;
	//1 is rotated all the way to the right, -1 is rotated all the way to the left
	private int rz = 3; 
	//all 12 buttons; returns a float 1.0 (pushed) or 0.0 (not pushed)
	private int b1 = 4;
	private int b2 = 5;
	private int b3 = 6;
	private int b4 = 7;
	private int b5 = 8;
	private int b6 = 9;
	private int b7 = 10;
	private int b8 = 11;
	//slider; returns float between 1 and -1;
	//-1 is pushed all the way to the top, 1 is pushed all the way to the bottom
	private int slider = 12;
	private int b9 = 13;
	private int b10 = 14;
	private int b11 = 15;
	private int b12 = 16;
	

	public Joystick() {
		connectToStick();
		rawPollData = new float[components.length];
		convertedPollData = new byte[8];
		
		//sets the first and last bytes of the data stream as all 1's, used for checking byte accuracy.
		for(int i = 0; i < 8; i++) {
			convertedPollData[0] |= (1 << i);
			convertedPollData[7] |= (1 << i);
		}
	}
	
	/*
	 * Loops through all connected devices and finds the joystick
	 */
	private void connectToStick() {
		
		while(true) {
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
	 * Main update method to execute in launcher. Updates gets raw poll data
	 * and converts to to byte array to be sent.
	 */
	public void update() {
		updateRawPollData();
		updateConvertedPollData();
	}
	
	/*
	 * Polls the controller then updates the raw poll data array with the new values
	 */
	private void updateRawPollData() {
		joystick.poll();
		for(int i = 0; i < components.length; i++) {
			rawPollData[i] = components[i].getPollData();
		}
	}
	
	/*
	 * Converts the raw poll data to a byte array to be sent to the robot
	 */
	private void updateConvertedPollData() {
		//first byte of all 1's.
		
	}
	
	public float[] getRawPollData() {
		return rawPollData;
	}
	
	public byte[] getConvertedPollData() {
		return convertedPollData;
	}

	/*
	 * Polls joystick, then loops through each component, printing the components name
	 * and poll data to the console
	 */
	public void updateToConsole() {
		joystick.poll();
		System.out.println("OUTPUT");
		for(int i = 0; i < components.length; i++ ) {
			System.out.println(components[i].getIdentifier() + ": " + components[i].getPollData());
		}
	

	}

	/*
	 * For testing purposes. Connects to the controller and updates to console
	 * every 5000 milliseconds
	 */
	public static void main(String[] args) {
		
		Joystick j = new Joystick();
		while(true) {
			j.update();
			float[] output = j.getRawPollData();
			for(float pollData: output) {
				System.out.println(pollData);
			}
			try {
			Thread.sleep(5000);
			} catch (InterruptedException e) {
				System.out.println("error");
			}
		}

		
	}

}

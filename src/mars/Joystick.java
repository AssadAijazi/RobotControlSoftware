package mars;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier;

public class Joystick {
	private Controller joystick;
	private Component[] components;
	
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
	 * Updates the Component array and returns a float array of the raw outputs
	 */
	public float[] updateRawPollData() {
		float[] outputs = new float[components.length];
		joystick.poll();
		for(int i = 0; i < components.length; i++) {
			outputs[i] = components[i].getPollData();
		}
		
		return outputs;
	}

	/*
	 * Polls joystick, then loops through each component, printing the components name
	 * and poll data to the console
	 */
	public void UpdateToConsole() {
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
			float[] output = j.updateRawPollData();
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

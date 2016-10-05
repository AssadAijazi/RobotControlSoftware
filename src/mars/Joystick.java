package mars;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier;

public class Joystick {
	private Controller joystick;
	private Component[] components;

	public Joystick() {
		connectToStick();
		

	}
	
	/*
	 * Loops through all connected devices and finds the joystick
	 */
	public void connectToStick() {
		
		while(true) {
			Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
			for (int i = 0; i < ca.length; i++) {
				if (ca[i].getType().equals(Controller.Type.STICK)) {
					System.out.println("Found it");
					joystick = ca[i];
					components = joystick.getComponents();
					return;
				}
			}
			
		}
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
		System.out.println("Connection found");
		while(true) {
			j.UpdateToConsole();
			try {
			Thread.sleep(5000);
			} catch (InterruptedException e) {
				System.out.println("error");
			}
		}

		
	}

}

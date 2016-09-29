package mars;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier;

public class JoystickController {
	private Controller joystick;
	private Component[] components;

	public JoystickController() {
		PollForStick();
		

	}
	
	public void PollForStick() {
		
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

	public void UpdateToConsole() {
		joystick.poll();
		System.out.println("OUTPUT");
		for(int i = 0; i < components.length; i++ ) {
			System.out.println(components[i].getIdentifier() + ": " + components[i].getPollData());
		}
	

	}

	public static void main(String[] args) {
		
		JoystickController j = new JoystickController();
		System.out.println("Connection found");
		while(true) {
			j.UpdateToConsole();
			try {
			Thread.sleep(5000);
			} catch (InterruptedException e) {
				System.out.println("error");
			}
		}

		/* Get the name of the controller */
	}

}

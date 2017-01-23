package mars;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class KeyboardHandler {
	private static float[] rawPollData;
	private static float[] previousRawPollData;
	private static boolean[] buttonStates;
	private static boolean[] arrowKeyStates; 
	private static final int UP = 0;
	private static final int DOWN = 1;
	private static final int LEFT = 2;
	private static final int RIGHT = 3;
	private static final int Y = 0;
	private static final int X = 1;
	private static final float SPEED = 0.000025f; //controls how fast the joystick moves
	private static Map<String, Integer> pollDataArrIndexMap;

	public KeyboardHandler(JComponent parent) {
		rawPollData = new float[17];
		previousRawPollData = new float[17];
		// keeps track of whether the buttons are pushed
		buttonStates = new boolean[12];
		// keeps track of whether the arrow keys are pushed
		arrowKeyStates = new boolean[4];

		// used to map button index to correct index in the rawPollData array,
		// since the order in the poll data array is a bit weird
		pollDataArrIndexMap = new Hashtable<String, Integer>();
		// maps button indexes
		for (int i = 0; i < 8; i++) {
			pollDataArrIndexMap.put(i + "", i + 4);
		}
		for (int i = 8; i < 12; i++) {
			pollDataArrIndexMap.put(i + "", i + 5);
		}

		// sets up key bindings
		InputMap inputMap = parent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = parent.getActionMap();
		setButtonKeyBinding(inputMap, actionMap);
		setJoystickKeyBinding(inputMap, actionMap);
		setPOVKeyBinding(inputMap, actionMap);
	}

	public void update() {
		for (int i = 0; i < rawPollData.length; i++) {
			previousRawPollData[i] = rawPollData[i];
		}
		updateButtons();
		updateJoystick();

	}

	private void updateButtons() {
		int pollDataArrIndex;
		boolean currentButtonState;

		for (int i = 0; i < buttonStates.length; i++) {
			pollDataArrIndex = pollDataArrIndexMap.get(i + "");
			currentButtonState = buttonStates[i];
			if (currentButtonState) {
				rawPollData[pollDataArrIndex] = 1.0f;
			} else {
				rawPollData[pollDataArrIndex] = 0.0f;
			}
		}
	}

	private void updateJoystick() {
		boolean[] currentArrowKeyStates = new boolean[4];
		for(int i = 0; i < arrowKeyStates.length; i++) {
			currentArrowKeyStates[i] = arrowKeyStates[i];
		}
		
		if(currentArrowKeyStates[UP] && rawPollData[Y] > -1 ) {
			rawPollData[Y] -= SPEED;
		} else if (currentArrowKeyStates[DOWN] && rawPollData[Y] < 1) {
			rawPollData[Y] += SPEED;
		}


		if(currentArrowKeyStates[LEFT] && rawPollData[X] > -1 ) {
			rawPollData[X] -= SPEED;
		} else if (currentArrowKeyStates[RIGHT] && rawPollData[X] < 1) {
			rawPollData[X] += SPEED;
		}
		if (!(currentArrowKeyStates[UP] || currentArrowKeyStates[DOWN]|| currentArrowKeyStates[LEFT] || currentArrowKeyStates[RIGHT])) {
			rawPollData[X] = 0.0f;
			rawPollData[Y] = 0.0f;
		}
		
		

	}

	public float[] getRawPollData() {
		return rawPollData;
	}

	public boolean isChanged() {
		for (int i = 0; i < rawPollData.length; i++) {
			if (!(Float.compare(rawPollData[i], previousRawPollData[i]) == 0)) {
				return true;
			}
		}
		return false;
	}

	private void setButtonKeyBinding(InputMap inputMap, ActionMap actionMap) {
		// keycodes for the keys used to simulate the buttons
		String[] buttonKeyCodes = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "MINUS", "EQUALS" };
		for (int i = 0; i < 12; i++) {
			String pushedKey = "button" + (i + 1) + "Pushed";
			inputMap.put(KeyStroke.getKeyStroke(buttonKeyCodes[i]), pushedKey);
			actionMap.put(pushedKey, new ButtonPushedAction(i));

			String releasedKey = "button" + (i + 1) + "Released";
			inputMap.put(KeyStroke.getKeyStroke("released " + buttonKeyCodes[i]), releasedKey);
			actionMap.put(releasedKey, new ButtonReleasedAction(i));
		}

	}

	private void setJoystickKeyBinding(InputMap inputMap, ActionMap actionMap) {
		String[] arrowKeyCodes = { "UP", "DOWN", "LEFT", "RIGHT" };
		for (int i = 0; i < 4; i++) {
			String pushedKey = arrowKeyCodes[i] + "Pushed";
			inputMap.put(KeyStroke.getKeyStroke(arrowKeyCodes[i]), pushedKey);
			actionMap.put(pushedKey, new ArrowPushedAction(i));

			String releasedKey = arrowKeyCodes[i] + "Released";
			inputMap.put(KeyStroke.getKeyStroke("released " + arrowKeyCodes[i]), releasedKey);
			actionMap.put(releasedKey, new ArrowReleasedAction(i));

		}
	}

	private void setPOVKeyBinding(InputMap inputMap, ActionMap actionMap) {

	}

	private static class ButtonPushedAction extends AbstractAction {
		public int buttonIndex;

		public ButtonPushedAction(int i) {
			buttonIndex = i;

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			buttonStates[buttonIndex] = true;
		}

	}

	private static class ButtonReleasedAction extends AbstractAction {
		public int buttonIndex;

		public ButtonReleasedAction(int i) {
			buttonIndex = i;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			buttonStates[buttonIndex] = false;
		}

	}

	private static class ArrowPushedAction extends AbstractAction {
		private int arrowIndex;

		public ArrowPushedAction(int i) {
			arrowIndex = i;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			arrowKeyStates[arrowIndex] = true;
		}

	}

	private static class ArrowReleasedAction extends AbstractAction {
		private int arrowIndex;

		public ArrowReleasedAction(int i) {
			arrowIndex = i;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			arrowKeyStates[arrowIndex] = false;
		}

	}

	// for testing purposes
	/*
	 * public static void main(String[] args) { JFrame frame = new
	 * JFrame("Tester"); frame.setSize(100, 100); JPanel content = new JPanel();
	 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 * frame.setContentPane(content); KeyboardHandler kbHandler = new
	 * KeyboardHandler(content); frame.setVisible(true); while (true) { for (int
	 * i = 0; i < 17; i++) { System.out.print(kbHandler.getRawPollData()[i] +
	 * " "); } System.out.println();
	 * 
	 * } }
	 */

}

package mars;

public class Launcher {

	public static void main(String[] args) {

		Joystick j = new Joystick();
		PollDataConverter pdc = new PollDataConverter();
		while (true) {
			// j.updateToConsole();
			j.update();
			float[] output = j.getRawPollData();
			byte[] byteOutput = pdc.convert(output);
			for (int i = 0; i < byteOutput.length; i++) {

				// fancy method from stack overflow to properly print bytes to console

				System.out.print(
						String.format("%8s", Integer.toBinaryString((byteOutput[i] + 256) % 256)).replace(' ', '0'));
				System.out.print(" ");
			}
			System.out.println();

		}

	}

}

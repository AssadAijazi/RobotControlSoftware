package mars;

import java.io.IOException;

public class Launcher {

	public static void main(String[] args) {

		Joystick j = null;
		try {
			j = new Joystick();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//class for converting raw poll data to byte array
		PollDataConverter pdc = new PollDataConverter();
		//handing network
		int port = 5565;
		String hostname = "192.168.1.102";
		NetworkDaemon nd = new NetworkDaemon(hostname , port);
		//TestServer server = new TestServer(port);
		//server.start();
		nd.connect();
		
		//main loop
		while (true) {
			float[] output = new float[20];//replace with expected length of raw poll data
			if (j != null){
			//updates joystick, receives raw data, then converts to byte array
			j.update();
			output = j.getRawPollData();
			}
			
			byte[] byteOutput = pdc.convert(output);
			
			//sends to console
			System.out.print("Sent                : ");
			for (int i = 0; i < byteOutput.length; i++) {

				// fancy method from stack overflow to properly print bytes to console

				System.out.print(
						String.format("%8s", Integer.toBinaryString((byteOutput[i] + 256) % 256)).replace(' ', '0'));
				System.out.print(" ");
			}
			System.out.println();
			
			//sends to robot
			nd.send(byteOutput);
			
			//gives robot enough time to receive byte array before sending another one
			try {
				Thread.sleep(1000); 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	public static void pnt(String s){
		System.out.println(s);
	}

}

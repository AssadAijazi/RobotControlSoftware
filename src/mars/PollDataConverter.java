package mars;

//converts raw poll data to byte array
public class PollDataConverter {
	private float[] rawPollData;
	
	/*
	 * 8 Byte array to send to the robot. Byte 1 is a byte of all 1's. Bytes 2
	 * and the first half of byte 3 represent the state of the buttons (1 = on,
	 * 0 = off). The second half of byte 3 represents the state of the pov. Byte
	 * 4 represents the state of y-axis of the joystick. Byte 5 represents the
	 * state of the x-axis of the joystick. Byte 6 represents the state of the
	 * rotational z-axis. Byte 7 represents the state of the throttle. Byte 8 is
	 * a byte of all 1's
	 */
	private byte[] convertedPollData;
	
	//see Joystick class for documentation
		private int y = 0;
		private int x = 1;
		private int pov = 2;
		private int rz = 3;
		private int b1 = 4;
		private int b2 = 5;
		private int b3 = 6;
		private int b4 = 7;
		private int b5 = 8;
		private int b6 = 9;
		private int b7 = 10;
		private int b8 = 11;
		private int slider = 12;
		private int b9 = 13;
		private int b10 = 14;
		private int b11 = 15;
		private int b12 = 16;
		
	public PollDataConverter() {
		convertedPollData = new byte[8];
		
		//used to check number of bytes
		convertedPollData[0] = -1;
		convertedPollData[7] = -1;
	}
	
	public byte[] convert(float[] floatArr) {
		rawPollData = floatArr;
		
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
		
		// converting the y-axis floating-point value to byte
		convertedPollData[3] = convertFloatToByte(rawPollData[y]);
		
		//converting the x-axis floating-point value to byte
		convertedPollData[4] = convertFloatToByte(rawPollData[x]);
		
		//converting the rotational z-axis floating-point value to byte
		convertedPollData[5] = convertFloatToByte(rawPollData[rz]);
		
		//converting the throttle floating-point value to byte
		convertedPollData[6] = convertFloatToByte(rawPollData[slider]);
		
		return convertedPollData;
	}
	
	//used to convert float to byte
	private byte convertFloatToByte(float f) {
		return (byte)(Math.round((f+1)/2*255));
	}
}

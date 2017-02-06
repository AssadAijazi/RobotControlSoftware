package mars;

//converts raw poll data to byte array
public class PollDataConverter {
	private float[] leftRawPollData;

	/*
	 * 8 Byte array to send to the robot. Byte 1 is a byte of all 1's. The
	 * next six bytes represent the right joystick. Bytes 2and the first half of byte 3 
	 * represent the state of the buttons (1 = on, * 0 = off). 
	 * The second half of byte 3 represents the state of the pov. Byte
	 * 4 represents the state of y-axis of the joystick. Byte 5 represents the
	 * state of the x-axis of the joystick. Byte 6 represents the state of the
	 * rotational z-axis. Byte 7 represents the state of the throttle. Bytes 8 - 13
	 * represent the left joystick, in the same order as before. Byte 14 is
	 * a byte of all 1's
	 */
	private volatile byte[] convertedPollData;

	// see Joystick class for documentation
	
	private static final int X = 1, Y = 0, POV = 2, RZ = 3;
	private static final int B1 = 4, B2 = 5, B3 = 6, B4 = 7, B5 = 8, B6 = 9, B7 = 10, B8 = 11, SLIDER = 12, B9 = 13,
			B10 = 14, B11 = 15, B12 = 16;

	public PollDataConverter() {
		leftRawPollData = new float[17];
		convertedPollData = new byte[14];

		// used to check number of bytes
		convertedPollData[0] = -1;
		convertedPollData[convertedPollData.length - 1] = -1;
	}
	
	public byte[] getByteArr() {
		return convertedPollData;
	}
	
	public String getByteArrAsStr() {
		String byteArrStr = "";
		for(int i = 0; i < convertedPollData.length; i++) {
			byteArrStr += (String.format("%8s", Integer.toBinaryString((convertedPollData[i] + 256) % 256)).replace(' ', '0') + " ");
		}
		return byteArrStr;
	}
	
	public void convert(float[] rightFloatArr, float[] leftFloatArr) {
		convertIndividual(rightFloatArr, 0); //right stick
		convertIndividual(leftFloatArr, 6); //left stick
	}

	private void convertIndividual(float[] floatArr, int startingIndex) {

		// setting up the bytes for the buttons

		// first byte of buttons
		for (int i = B1; i < SLIDER; i++) {
			if (floatArr[i] == 1.0f) {
				convertedPollData[1 + startingIndex] |= (1 << (11 - i));
			} else if (floatArr[i] == 0.0f) {
				convertedPollData[1 + startingIndex] &= ~(1 << (11 - i));
			}
		}

		// last half byte of buttons
		for (int i = B9; i < floatArr.length; i++) {
			if (floatArr[i] == 1.0f) {
				convertedPollData[2 + startingIndex] |= (1 << (20 - i));
			} else if (floatArr[i] == 0.0f) {
				convertedPollData[2 + startingIndex] &= ~(1 << (20 - i));
			}
		}

		// half byte of pov; bit 1 is forward, bit 2 is backward,
		// bit 3 is right, bit 4 is left
		// forward bit
		if ((floatArr[POV] > 0.0f) && (floatArr[POV] < 0.5f)) {
			convertedPollData[2 + startingIndex] |= (1 << 3);
		} else {
			convertedPollData[2 + startingIndex] &= ~(1 << 3);
		}
		// backward bit
		if ((floatArr[POV] > 0.5f) && (floatArr[POV] < 1.0f)) {
			convertedPollData[2 + startingIndex] |= (1 << 2);
		} else {
			convertedPollData[2 + startingIndex] &= ~(1 << 2);
		}
		// right bit
		if ((floatArr[POV] > 0.25f) && (floatArr[POV] < 0.75f)) {
			convertedPollData[2 + startingIndex] |= (1 << 1);
		} else {
			convertedPollData[2 + startingIndex] &= ~(1 << 1);
		}
		// left bit
		if (((floatArr[POV] > 0.75f) && (floatArr[POV] <= 1.0f))
				|| ((floatArr[POV] > 0.0f) && (floatArr[POV] < 0.25f))) {
			convertedPollData[2 + startingIndex] |= (1 << 0);
		} else {
			convertedPollData[2 + startingIndex] &= ~(1 << 0);
		}

		// converting the y-axis floating-point value to byte
		convertedPollData[3 + startingIndex] = convertFloatToByte(floatArr[Y]);

		// converting the x-axis floating-point value to byte
		convertedPollData[4 + startingIndex] = convertFloatToByte(floatArr[X]);

		// converting the rotational z-axis floating-point value to byte
		convertedPollData[5 + startingIndex] = convertFloatToByte(floatArr[RZ]);

		// converting the throttle floating-point value to byte
		convertedPollData[6 + startingIndex] = convertFloatToByte(floatArr[SLIDER]);

	}

	// used to convert float to byte
	private byte convertFloatToByte(float f) {
		
		//fixing rounding issues so that natural state is at 0
		if (f < 0 && f > -0.0079) {
			f = (float)(Math.ceil(f));
		}
		return (byte) (Math.round((f + 1.0) / 2.0 * 255.0));
	}
}

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
	private volatile byte[] convertedPollData;

	// see Joystick class for documentation
	
	private static final int X = 1, Y = 0, POV = 2, RZ = 3;
	private static final int B1 = 4, B2 = 5, B3 = 6, B4 = 7, B5 = 8, B6 = 9, B7 = 10, B8 = 11, SLIDER = 12, B9 = 13,
			B10 = 14, B11 = 15, B12 = 16;

	public PollDataConverter() {
		convertedPollData = new byte[8];

		// used to check number of bytes
		convertedPollData[0] = -1;
		convertedPollData[7] = -1;
	}
	
	public byte[] getByteArr() {
		return convertedPollData;
	}

	public void convert(float[] floatArr) {
		rawPollData = floatArr;

		// setting up the bytes for the buttons

		// first byte of buttons
		for (int i = B1; i < SLIDER; i++) {
			if (rawPollData[i] == 1.0f) {
				convertedPollData[1] |= (1 << (11 - i));
			} else if (rawPollData[i] == 0.0f) {
				convertedPollData[1] &= ~(1 << (11 - i));
			}
		}

		// last half byte of buttons
		for (int i = B9; i < rawPollData.length; i++) {
			if (rawPollData[i] == 1.0f) {
				convertedPollData[2] |= (1 << (20 - i));
			} else if (rawPollData[i] == 0.0f) {
				convertedPollData[2] &= ~(1 << (20 - i));
			}
		}

		// half byte of pov; bit 1 is forward, bit 2 is backward,
		// bit 3 is right, bit 4 is left
		// forward bit
		if ((rawPollData[POV] > 0.0f) && (rawPollData[POV] < 0.5f)) {
			convertedPollData[2] |= (1 << 3);
		} else {
			convertedPollData[2] &= ~(1 << 3);
		}
		// backward bit
		if ((rawPollData[POV] > 0.5f) && (rawPollData[POV] < 1.0f)) {
			convertedPollData[2] |= (1 << 2);
		} else {
			convertedPollData[2] &= ~(1 << 2);
		}
		// right bit
		if ((rawPollData[POV] > 0.25f) && (rawPollData[POV] < 0.75f)) {
			convertedPollData[2] |= (1 << 1);
		} else {
			convertedPollData[2] &= ~(1 << 1);
		}
		// left bit
		if (((rawPollData[POV] > 0.75f) && (rawPollData[POV] <= 1.0f))
				|| ((rawPollData[POV] > 0.0f) && (rawPollData[POV] < 0.25f))) {
			convertedPollData[2] |= (1 << 0);
		} else {
			convertedPollData[2] &= ~(1 << 0);
		}

		// converting the y-axis floating-point value to byte
		convertedPollData[3] = convertFloatToByte(rawPollData[Y]);

		// converting the x-axis floating-point value to byte
		convertedPollData[4] = convertFloatToByte(rawPollData[X]);

		// converting the rotational z-axis floating-point value to byte
		convertedPollData[5] = convertFloatToByte(rawPollData[RZ]);

		// converting the throttle floating-point value to byte
		convertedPollData[6] = convertFloatToByte(rawPollData[SLIDER]);

	}

	// used to convert float to byte
	private byte convertFloatToByte(float f) {
		return (byte) (Math.round((f + 1) / 2 * 255));
	}
}

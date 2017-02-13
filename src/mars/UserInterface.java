package mars;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;

import javax.swing.*;
import javax.swing.border.LineBorder;

//Code that runs the user interface it consists of a JFrame, and JPanel MainPanel/
//Main panel is further divided up into 4 separate panels: ConnnectionPanel,k
//JoystickPanel, ErrorPanel, and CameraPanel.
public class UserInterface {
	private static final int XWIDTH = 1366;
	private static final int YHEIGHT = 725;
	private MainPanel mainPanel;
	private boolean attemptConnection = false;
	private boolean attemptDisconnection = false;
	private boolean attemptPause = false;
	private boolean attemptPlay = false;
	private boolean isDebug = false;
	private boolean isConnected = false;
	private boolean isKB = false;
	private float[] kbOutput;
	private KeyboardHandler kbHandler;
	private static final float TITLEFONTSIZE = 20.0f;
	private static final float FONTSIZE = 17.0f;
	private NetworkDaemon nd;

	// loads up the frame on to screen and sets the panel as MainPanel
	public UserInterface(NetworkDaemon n) {
		nd = n;
		// used as output for keyboard mode
		kbOutput = new float[17];
		JFrame frame = new JFrame("Robot Control Software");
		frame.setSize(XWIDTH, YHEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainPanel = new MainPanel();
		kbHandler = new KeyboardHandler(mainPanel.jsPanel);
		frame.setContentPane(mainPanel);
		frame.setVisible(true);
	}

	// updates section of panel relating to joystick panel

	public void updateJoystickPanel(float[] rightPollData, boolean connectedToRightStick, float[] leftPollData,
			boolean connectedToLeftStick) {
		if (connectedToLeftStick) {
			mainPanel.jsPanel.leftStickConnectionStatus.setForeground(Color.GREEN);
			mainPanel.jsPanel.leftStickConnectionStatus.setText("Connected");
			mainPanel.jsPanel.connected = true;
		} else {
			mainPanel.jsPanel.leftStickConnectionStatus.setForeground(Color.RED);
			mainPanel.jsPanel.leftStickConnectionStatus.setText("Disconnected");
			mainPanel.jsPanel.connected = false;
		}
		mainPanel.jsPanel.rightPollData = rightPollData;
		mainPanel.jsPanel.leftPollData = leftPollData;

		mainPanel.jsPanel.repaint();
	}

	// when there is a change in connection status, this method is called so
	// the UI can respond appropriately
	public void updateConnectionStatus(boolean b) {
		isConnected = b;
		if (isConnected) {
			mainPanel.connectionPanel.status.setText("Connected");
			mainPanel.connectionPanel.status.setForeground(Color.GREEN);
			mainPanel.connectionPanel.mode.setEnabled(false);
			mainPanel.connectionPanel.connect.setText("Disconnect");
			mainPanel.connectionPanel.pause.setVisible(true);
		} else {
			mainPanel.connectionPanel.mode.setEnabled(true);
			mainPanel.connectionPanel.connect.setText("Connect");
			mainPanel.connectionPanel.status.setText("Disconnected");
			mainPanel.connectionPanel.status.setForeground(Color.RED);
			mainPanel.connectionPanel.pause.setVisible(false);
			mainPanel.connectionPanel.pause.setText("Pause Joystick Stream");
		}
	}

	// updates the bytes displayed on the ConnectionPanel
	public void setByteOutput(String output) {
		// split up the byte output into to 3 rows
		String[] dividedOutput = new String[3];
		for (int i = 0; i < dividedOutput.length; i++) {
			if (i != (dividedOutput.length - 1)) {
				dividedOutput[i] = output.substring(45 * i, 45 * (i + 1));
			} else {
				dividedOutput[i] = output.substring(45 * i, output.length());
			}
			mainPanel.connectionPanel.byteOutput[i].setText(dividedOutput[i]);
		}
	}

	// updates the color of the text of the byte output on the connection panel.
	// Used for the flashing effect when a new byte array is sent.
	public void setByteOutputColor(Color c) {
		for (int i = 0; i < mainPanel.connectionPanel.byteOutput.length; i++) {
			mainPanel.connectionPanel.byteOutput[i].setForeground(c);
		}
	}

	// determines whether debug mode should be turned on based
	// on if the debug mode is "on" or "off" in the connection panel
	public boolean getDebugMode() {
		return isDebug;
	}
	// determines whether to attempt connection, based on whether the "connect"
	// button has been pushed in the connection panel

	public boolean getAttemptConnection() {
		return attemptConnection;
	}

	// switches whether to attempt connection to boolean b
	public void setAttemptConnection(boolean b) {
		attemptConnection = b;
	}

	// if the connection is live, determines whether to attempt to disconnect
	// based on if the "disconnect" button is pushed
	public boolean getAttemptDisconnection() {
		return attemptDisconnection;
	}

	// sets whether to attempt disconnection to boolean b
	public void setAttemptDisconnection(boolean b) {
		attemptDisconnection = b;
	}
	
	//checks whether whether to pause the joystick stream 
	public boolean getAttemptPause() {
		return attemptPause;
	}
	
	//sets whether to attempt pause
	public void setAttemptPause(boolean b) {
		attemptPause = b;
	}
	
	//checks whether whether to play the joystick stream 
		public boolean getAttemptPlay() {
			return attemptPlay;
		}
		
		//sets whether to attempt play
		public void setAttemptPlay(boolean b) {
			attemptPlay = b;
		}

	// gets whether in keyboard mode
	public boolean getIsKB() {
		return isKB;
	}

	// adds error messages to the error panel
	public void addError(String error) {
		mainPanel.errorPanel.addError(error);
	}

	// adds normal message to the console
	public void addMessage(String m) {
		mainPanel.errorPanel.addMessage(m);
	}

	// returns the keyboard handler for handling keyboard input
	public KeyboardHandler getKeyboardhandler() {
		return kbHandler;
	}

	// used for switching joysticks
	public JButton getSwitchButton() {
		return mainPanel.jsPanel.switchJS;
	}
	
	//used for pausing the threads that send the joystick data streams
	public JButton getPauseButton() {
		return mainPanel.connectionPanel.pause;
	}

	private class MainPanel extends JPanel {
		private ConnectionPanel connectionPanel;
		private JSPanel jsPanel;
		private ErrorPanel errorPanel;
		private CameraPanel cameraPanel;

		// sets up framework for Main Panel using the layout manager
		// GridBagLayout
		// The layout sets out the 4 panels based on the defined proportions
		private MainPanel() {
			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			int totalGridWidth = 3;
			int totalGridHeight = 6;

			// Top Left Panel
			connectionPanel = new ConnectionPanel();

			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 1;
			c.gridheight = 2;
			setFinalSize(connectionPanel, (XWIDTH * 1) / totalGridWidth, (YHEIGHT * 2) / totalGridHeight);
			this.add(connectionPanel, c);
			// Top Right Panel
			jsPanel = new JSPanel();
			c.gridx = 1;
			c.gridy = 0;
			c.gridwidth = 2;
			c.gridheight = 3;
			setFinalSize(jsPanel, (XWIDTH * 2) / totalGridWidth, (YHEIGHT * 3) / totalGridHeight);
			this.add(jsPanel, c);

			// Bottom Left Panel
			errorPanel = new ErrorPanel();
			c.gridx = 0;
			c.gridy = 2;
			c.gridwidth = 1;
			c.gridheight = 4;
			setFinalSize(errorPanel, (XWIDTH * 1) / totalGridWidth, (YHEIGHT * 4) / totalGridHeight);
			this.add(errorPanel, c);

			// Bottom Right Panel
			cameraPanel = new CameraPanel();
			c.gridx = 1;
			c.gridy = 3;
			c.gridwidth = 3;
			c.gridheight = 3;
			setFinalSize(cameraPanel, (XWIDTH * 2) / totalGridWidth, (YHEIGHT * 3) / totalGridHeight);
			this.add(cameraPanel, c);
		}

		private void setFinalSize(JComponent component, int width, int height) {
			component.setPreferredSize(new Dimension(width, height));
			component.setMinimumSize(new Dimension(width, height));
			component.setMaximumSize(new Dimension(width, height));
		}

		// panel used for connection aspects, including connecting
		// disconnecting,
		// connection status, and byte output.
		private class ConnectionPanel extends JPanel {
			private JLabel title, status;
			private JLabel[] byteOutput;
			private JButton connect, mode, pause;

			private ConnectionPanel() {
				this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				this.setBorder(new LineBorder(Color.BLACK));

				Dimension titleDim = new Dimension(80, 25);
				Dimension compDim = new Dimension(120, 25);
				Dimension panelDim = new Dimension(220, 40);

				title = new JLabel("Connection");
				title.setFont(title.getFont().deriveFont(TITLEFONTSIZE));
				title.setAlignmentX(CENTER_ALIGNMENT);
				this.add(title);

				JPanel statusPanel = new JPanel();
				JLabel statusTitle = new JLabel("Status: ");
				statusTitle.setFont(statusTitle.getFont().deriveFont(FONTSIZE));
				statusTitle.setHorizontalAlignment(SwingConstants.RIGHT);
				statusTitle.setPreferredSize(titleDim);
				statusTitle.setMaximumSize(titleDim);
				statusPanel.add(statusTitle);

				status = new JLabel("Disconnected");
				status.setFont(status.getFont().deriveFont(FONTSIZE));
				status.setPreferredSize(compDim);
				status.setMinimumSize(compDim);
				status.setForeground(Color.RED);

				statusPanel.add(status);
				statusPanel.setPreferredSize(panelDim);
				statusPanel.setMaximumSize(panelDim);
				this.add(statusPanel);

				JPanel debug = new JPanel();
				JLabel debugTitle = new JLabel("Debug Mode: ");
				debugTitle.setFont(debugTitle.getFont().deriveFont(FONTSIZE));
				debug.add(debugTitle);
				mode = new JButton("Off");
				Dimension buttonDim = new Dimension(80, 25);
				mode.setPreferredSize(buttonDim);
				mode.setMinimumSize(buttonDim);

				// for toggling debug mode via debug button
				mode.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JButton source = (JButton) (e.getSource());
						if (source.getText().equals("On")) {
							source.setText("Off");
							isDebug = false;
						} else {
							source.setText("On");
							isDebug = true;
						}

					}

				});
				debug.add(mode);
				Dimension debugDim = panelDim;
				debug.setPreferredSize(debugDim);
				debug.setMaximumSize(debugDim);

				this.add(debug);

				JPanel buttonPanel = new JPanel();
				
				pause = new JButton("Pause Joystick Stream");
				pause.setSize(pause.getSize());
				//used for pausing and playing joystick stream
				pause.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						JButton source = (JButton)e.getSource();
						if(source.getText().equals("Pause Joystick Stream")) {
							attemptPause = true;
							source.setText("Resume Joystick Stream");
						} else {
							attemptPlay = true;
							source.setText("Pause Joystick Stream");
						}
						
					}
					
				});
				pause.setVisible(false);
				
				connect = new JButton("Connect");
				connect.setAlignmentX(CENTER_ALIGNMENT);
				// action listener for the connect button; used for connecting
				// and disconnecting
				connect.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (isConnected) {
							attemptDisconnection = true;
						} else {
							attemptConnection = true;
						}
					}
				});
				
				buttonPanel.add(pause);
				buttonPanel.add(connect);
				this.add(buttonPanel);
				byteOutput = new JLabel[3];
				for (int i = 0; i < byteOutput.length; i++) {
					if (i != (byteOutput.length - 1)) {
						byteOutput[i] = new JLabel("00000000 00000000 00000000 00000000 00000000");
					} else {
						byteOutput[i] = new JLabel("00000000 00000000 00000000 00000000");
					}
					byteOutput[i].setFont(byteOutput[i].getFont().deriveFont(FONTSIZE));
					byteOutput[i].setAlignmentX(CENTER_ALIGNMENT);
					byteOutput[i].setAlignmentY(BOTTOM_ALIGNMENT);
					Dimension byteDim = new Dimension(400, 25);
					byteOutput[i].setPreferredSize(byteDim);
					byteOutput[i].setMinimumSize(byteDim);
					this.add(byteOutput[i]);
				}
			}
		}

		// displays all of the joystick aspects, including joystick connection
		// status
		// and visuals for all of the components
		private class JSPanel extends JPanel {
			private boolean connected;
			private float[] rightPollData;
			private float[] leftPollData;
			private JLabel leftStickConnectionStatus;
			private JButton switchJS;

			private JSPanel() {
				this.setFocusable(true);
				this.addMouseListener(new jsMouseListener(this));
				this.addFocusListener(new jsFocusListener());
				this.setLayout(new BorderLayout());
				rightPollData = new float[17];
				leftPollData = new float[17];
				connected = false;
				this.setBorder(new LineBorder(Color.BLACK));

				Dimension titleDim = new Dimension(60, 25);
				Dimension compDim = new Dimension(120, 25);
				Dimension panelDim = new Dimension(200, 30);
				JPanel titlePan = new JPanel();

				JLabel title = new JLabel("Joystick");
				titlePan.add(title);
				this.add(titlePan, BorderLayout.NORTH);
				titlePan.setPreferredSize(panelDim);
				titlePan.setMaximumSize(panelDim);
				// titlePan.setBackground(Color.RED);
				title.setFont(title.getFont().deriveFont(TITLEFONTSIZE));
				title.setBackground(Color.RED);

				JPanel statusPanel = new JPanel();
				statusPanel.setAlignmentX(CENTER_ALIGNMENT);
				statusPanel.setPreferredSize(panelDim);
				statusPanel.setMaximumSize(panelDim);
				JLabel statusTitle = new JLabel("Status: ");
				statusTitle.setFont(statusTitle.getFont().deriveFont(FONTSIZE));
				statusTitle.setHorizontalAlignment(SwingConstants.LEFT);
				statusTitle.setPreferredSize(titleDim);
				statusTitle.setMaximumSize(titleDim);
				statusPanel.add(statusTitle);

				leftStickConnectionStatus = new JLabel("Disconnected");
				leftStickConnectionStatus.setFont(leftStickConnectionStatus.getFont().deriveFont(FONTSIZE));
				leftStickConnectionStatus.setHorizontalAlignment(SwingConstants.LEFT);
				leftStickConnectionStatus.setPreferredSize(compDim);
				leftStickConnectionStatus.setMinimumSize(compDim);
				leftStickConnectionStatus.setForeground(Color.RED);

				statusPanel.add(leftStickConnectionStatus);
				statusPanel.setPreferredSize(panelDim);
				statusPanel.setMaximumSize(panelDim);
				// this.add(statusPanel);

				JPanel buttonPanel = new JPanel();
				JButton mode = new JButton("Keyboard Mode: Off");
				Dimension buttonDim = new Dimension(150, 25);
				mode.setPreferredSize(buttonDim);
				mode.setMinimumSize(buttonDim);
				// for toggling keyboard mode
				buttonPanel.add(mode);
				mode.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JButton source = (JButton) (e.getSource());
						if (source.getText().equals("Keyboard Mode: On")) {
							source.setText("Keyboard Mode: Off");
							isKB = false;
						} else {
							source.setText("Keyboard Mode: On");
							isKB = true;
						}

					}

				});

				mode.addFocusListener(new jsFocusListener());

				Dimension kbDim = new Dimension(100, 35);
				buttonPanel.setPreferredSize(kbDim);
				buttonPanel.setMaximumSize(kbDim);

				switchJS = new JButton("Switch Joysticks");
				switchJS.setPreferredSize(buttonDim);
				switchJS.setMaximumSize(buttonDim);
				buttonPanel.add(switchJS);

				this.add(buttonPanel, BorderLayout.SOUTH);

			}

			private class jsMouseListener implements MouseListener {
				JSPanel parent;

				public jsMouseListener(JSPanel panel) {
					parent = panel;
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					// TODO Auto-generated method stub
					parent.requestFocusInWindow();
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub

				}

			}

			private class jsFocusListener implements FocusListener {

				@Override
				public void focusGained(FocusEvent e) {
				}

				@Override
				public void focusLost(FocusEvent e) {
					kbHandler.clear();
				}

			}

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				// shifts everything on the panel left and right
				// shifts everything on the panel up or down
				int horizontalOffset = 50;
				int verticalOffset = 30;
				renderRightStick(g, rightPollData, horizontalOffset, verticalOffset);
				renderLeftStick(g, leftPollData, horizontalOffset, verticalOffset);
			}

			private void renderRightStick(Graphics g, float[] pollData, int horizontalOffset, int verticalOffset) {
				renderButtons(g, 545 - horizontalOffset, 0 + verticalOffset, pollData);
				renderPOVIndicator(g, 555 - horizontalOffset, 195 + verticalOffset, pollData[2], "POV");
				renderJoyStickIndicator(g, 765 - horizontalOffset, 10 + verticalOffset, pollData[1], pollData[0],
						"Main Stick");
				renderThrottleIndicator(g, 680 - horizontalOffset, 195 + verticalOffset, pollData[12], "Throttle");
				renderRZIndicator(g, 810 - horizontalOffset, 195 + verticalOffset, pollData[3], "Z Rotation");
			}

			private void renderLeftStick(Graphics g, float[] pollData, int horizontalOffset, int verticalOffset) {
				renderButtons(g, 10 + horizontalOffset, 0 + verticalOffset, pollData);
				renderPOVIndicator(g, 20 + horizontalOffset, 195 + verticalOffset, pollData[2], "POV");
				renderJoyStickIndicator(g, 230 + horizontalOffset, 10 + verticalOffset, pollData[1], pollData[0],
						"Main Stick");
				renderThrottleIndicator(g, 150 + horizontalOffset, 195 + verticalOffset, pollData[12], "Throttle");
				renderRZIndicator(g, 280 + horizontalOffset, 195 + verticalOffset, pollData[3], "Z Rotation");
			}

			private void renderButtons(Graphics g, int x, int y, float[] pollData) {
				g.setFont(new Font("Dialog", Font.BOLD, 15));

				for (int i = 1; i < 7; i++) {
					g.setColor(Color.BLACK);
					g.drawString("Button " + i, x, y + 30 * i);
					if (pollData[i + 3] == 1.0f) {
						g.setColor(Color.GREEN);
					} else {
						g.setColor(Color.BLACK);
					}
					g.fillOval(x + 70, y - 17 + 30 * i, 20, 20);
					g.setColor(Color.BLACK);
					g.drawString("Button " + (6 + i), x + 120, y + 30 * i);
					// to get around the index of the slider
					if (i + 3 + 6 >= 12) {
						if (pollData[i + 3 + 7] == 1.0f) {
							g.setColor(Color.GREEN);
						} else {
							g.setColor(Color.BLACK);
						}
					} else {
						if (pollData[i + 3 + 6] == 1.0f) {
							g.setColor(Color.GREEN);
						} else {
							g.setColor(Color.BLACK);
						}
					}

					g.fillOval(x + 190, y - 17 + 30 * i, 20, 20);
				}

			}
		}

		private void renderPOVIndicator(Graphics g, int x, int y, double pov, String label) {
			double jx, jy;
			if (pov == 0.0f) {
				jx = 0;
				jy = 0;
			} else {
				jx = Math.cos(2 * Math.PI * pov + Math.PI);
				jy = Math.sin(2 * Math.PI * pov + Math.PI);
			}
			int jlength = 20;
			int boxsize = 60;
			g.setColor(Color.BLACK);
			g.fillRect(x + 3, y + 3, boxsize, boxsize);
			g.setColor(new Color(127, 127, 127));
			g.fillRect(x, y, boxsize, boxsize);
			g.setColor(new Color(220, 220, 220));
			g.drawLine(x + (boxsize / 2), y + (boxsize / 2), (int) ((x + (boxsize / 2)) + jx * (jlength)),
					(int) ((y + (boxsize / 2)) + jy * (jlength)));
			g.fillOval((int) ((x + (boxsize / 2)) + jx * (jlength)) - 18,
					(int) ((y + (boxsize / 2)) + jy * (jlength)) - 18, 36, 36);
			g.setColor(Color.BLACK);
			g.drawString(label, x + boxsize / 4, y + (boxsize + 20));

		}

		private void renderJoyStickIndicator(Graphics g, int x, int y, float jx, float jy, String label) {
			int jlength = 40;
			int boxsize = 140;
			g.setColor(Color.BLACK);
			g.fillRect(x + 3, y + 3, boxsize, boxsize);
			g.setColor(new Color(127, 127, 127));
			g.fillRect(x, y, boxsize, boxsize);
			g.setColor(Color.WHITE);
			g.drawLine(x + (boxsize / 2), y + (boxsize / 2), (int) ((x + (boxsize / 2)) + jx * (jlength)),
					(int) ((y + (boxsize / 2)) + jy * (jlength)));
			g.fillOval((int) ((x + (boxsize / 2)) + jx * (jlength)) - 40,
					(int) ((y + (boxsize / 2)) + jy * (jlength)) - 40, 80, 80);
			g.setColor(Color.BLACK);
			g.drawString(label, x + boxsize / 4, y + (boxsize + 20));

		}

		private void renderThrottleIndicator(Graphics g, int x, int y, float slider, String label) {
			double jx = Math.cos(-1 * Math.PI / 4 * slider + Math.PI / 2);
			double jy = Math.sin(-1 * Math.PI / 4 * slider + Math.PI / 2);
			;
			int jlength = 35;
			int boxsize = 60;
			g.setColor(Color.BLACK);
			g.fillRect(x + 3, y + 3, boxsize, boxsize);
			g.setColor(new Color(127, 127, 127));
			g.fillRect(x, y, boxsize, boxsize);
			g.setColor(new Color(220, 220, 220));
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(5));
			g2.draw(new Line2D.Double(x + (boxsize / 2), y + boxsize - 4, (jx * jlength + x + boxsize / 2),
					(-jy * jlength + y + boxsize - 4)));
			g.fillOval(x + (boxsize / 2 - 8), y + boxsize - 16, 16, 16);
			g.setColor(Color.BLACK);
			g.drawString(label, x + boxsize / 12, y + (boxsize + 20));
		}

		private void renderRZIndicator(Graphics g, int x, int y, float rz, String label) {
			double jx = Math.cos(-1 * Math.PI / 4 * rz + Math.PI / 2);
			double jy = Math.sin(-1 * Math.PI / 4 * rz + Math.PI / 2);
			;
			int jlength = 35;
			int boxsize = 60;
			g.setColor(Color.BLACK);
			g.fillRect(x + 3, y + 3, boxsize, boxsize);
			g.setColor(new Color(127, 127, 127));
			g.fillRect(x, y, boxsize, boxsize);
			g.setColor(Color.WHITE);
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(5));
			g2.draw(new Line2D.Double(x + (boxsize / 2), y + boxsize - 4, (jx * jlength + x + boxsize / 2),
					(-jy * jlength + y + boxsize - 4)));
			g.setColor(Color.BLACK);
			g.drawString(label, x - 5 + boxsize / 12, y + (boxsize + 20));
		}

		// panel for displaying all error messages
		private class ErrorPanel extends JPanel {
			Console console;

			private ErrorPanel() {

				this.setLayout(new BorderLayout());
				this.setBorder(new LineBorder(Color.BLACK));

				// JLabel title = new JLabel("Error Log");
				// title.setFont(title.getFont().deriveFont(TITLEFONTSIZE));
				//
				// title.setAlignmentX(CENTER_ALIGNMENT);
				// this.add(title, BorderLayout.NORTH);

				console = new Console(nd);
				this.add(console, BorderLayout.CENTER);

			}

			// adds error to the panel
			private void addError(String error) {
				console.addError(error);
			}

			// adds normal message to console
			private void addMessage(String m) {
				console.addMessage(m);
			}
		}

		// displays all aspects of the Camera Panel
		private class CameraPanel extends JPanel {
			JLabel title;

			private CameraPanel() {
				title = new JLabel("Camera");
				title.setFont(title.getFont().deriveFont(TITLEFONTSIZE));
				this.add(title);
				this.setBorder(new LineBorder(Color.BLACK));
			}
		}
	}
}

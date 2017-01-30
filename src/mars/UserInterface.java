package mars;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;

import javax.swing.*;
import javax.swing.border.LineBorder;

//Code that runs the user interface it consists of a JFrame, and JPanel MainPanel/
//Main panel is further divided up into 4 seperate panels: ConnnectionPanel,k
//JoystickPanel, ErrorPanel, and CameraPanel.
public class UserInterface {
	private static final int XWIDTH = 1500;
	private static final int YHEIGHT = 700;
	private MainPanel mainPanel;
	private boolean attemptConnection = false;
	private boolean attemptDisconnection = false;
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

	public void updateJoystickPanel(float[] pollData, boolean connectedToStick) {
		if (connectedToStick) {
			mainPanel.jsPanel.connectionStatus.setForeground(Color.GREEN);
			mainPanel.jsPanel.connectionStatus.setText("Connected");
			mainPanel.jsPanel.connected = true;
		} else {
			mainPanel.jsPanel.connectionStatus.setForeground(Color.RED);
			mainPanel.jsPanel.connectionStatus.setText("Disconnected");
			mainPanel.jsPanel.connected = false;
		}
		mainPanel.jsPanel.pollData = pollData;

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
		} else {
			mainPanel.connectionPanel.mode.setEnabled(true);
			mainPanel.connectionPanel.connect.setText("Connect");
			mainPanel.connectionPanel.status.setText("Disconnected");
			mainPanel.connectionPanel.status.setForeground(Color.RED);
		}
	}

	// updates the bytes displayed on the ConnectionPanel
	public void setByteOutput(String output) {
		// split up the byte output into to two rows of 4 bytes each
		String topHalf = output.substring(0, output.length() / 2 - 1);
		String bottomHalf = output.substring(output.length() / 2, output.length() - 1);
		mainPanel.connectionPanel.byteOutput[0].setText(topHalf);
		mainPanel.connectionPanel.byteOutput[1].setText(bottomHalf);
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

	// gets whether in keyboard mode
	public boolean getIsKB() {
		return isKB;
	}

	// adds error messages to the error panel
	public void addError(String error) {
		mainPanel.errorPanel.addError(error);
	}
	
	//adds normal message to the console
	public void addMessage(String m) {
		mainPanel.errorPanel.addMessage(m);
	}

	// returns the keyboard handler for handling keyboard input
	public KeyboardHandler getKeyboardhandler() {
		return kbHandler;
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
		}

		// panel used for connection aspects, including connecting
		// disconnecting,
		// connection status, and byte output.
		private class ConnectionPanel extends JPanel {
			private JLabel title, status;
			private JLabel[] byteOutput;
			private JButton connect, mode;

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

				this.add(connect);

				byteOutput = new JLabel[2];
				for (int i = 0; i < byteOutput.length; i++) {
					byteOutput[i] = new JLabel("00000000 00000000 00000000 00000000");
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
			private float[] pollData;
			private JLabel connectionStatus;

			private JSPanel() {
				this.setFocusable(true);
				this.addMouseListener(new jsMouseListener(this));
				this.addFocusListener(new jsFocusListener());
				pollData = new float[17];
				connected = false;
				this.setBorder(new LineBorder(Color.BLACK));

				this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				Dimension titleDim = new Dimension(80, 25);
				Dimension compDim = new Dimension(120, 25);
				Dimension panelDim = new Dimension(220, 40);

				JLabel title = new JLabel("Joystick");
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

				connectionStatus = new JLabel("Disconnected");
				connectionStatus.setFont(connectionStatus.getFont().deriveFont(FONTSIZE));
				connectionStatus.setPreferredSize(compDim);
				connectionStatus.setMinimumSize(compDim);
				connectionStatus.setForeground(Color.RED);

				statusPanel.add(connectionStatus);
				statusPanel.setPreferredSize(panelDim);
				statusPanel.setMaximumSize(panelDim);
				this.add(statusPanel);

				JPanel kbMode = new JPanel();
				JLabel kbTitle = new JLabel("Keyboard Mode: ");
				kbTitle.setFont(kbTitle.getFont().deriveFont(FONTSIZE));
				kbMode.add(kbTitle);
				JButton mode = new JButton("Off");
				Dimension buttonDim = new Dimension(80, 25);
				mode.setPreferredSize(buttonDim);
				mode.setMinimumSize(buttonDim);
				// for toggling keyboard mode
				kbMode.add(mode);
				mode.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JButton source = (JButton) (e.getSource());
						if (source.getText().equals("On")) {
							source.setText("Off");
							isKB = false;
						} else {
							source.setText("On");
							isKB = true;
						}

					}

				});

				mode.addFocusListener(new jsFocusListener());

				Dimension kbDim = new Dimension(225, 40);
				kbMode.setPreferredSize(kbDim);
				kbMode.setMaximumSize(kbDim);

				this.add(kbMode);

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
				renderButtons(g, 10, 0);
				renderPOVIndicator(g, 50, 195, pollData[2], "POV");
				renderJoyStickIndicator(g, 230, 10, pollData[1], pollData[0], "Main Stick");
				renderThrottleIndicator(g, 170, 195, pollData[12], "Throttle");
				renderRZIndicator(g, 300, 195, pollData[3], "Z Rotation");
			}

			private void renderButtons(Graphics g, int x, int y) {
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
			g.drawString(label, x + boxsize / 12, y + (boxsize + 20));
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
			
			//adds normal message to console
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

package mars;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;

import javax.swing.*;
import javax.swing.border.LineBorder;

public class UserInterface {
	private static final int XWIDTH = 1500;
	private static final int YHEIGHT = 600;
	private MainPanel mainPanel;
	private boolean attemptConnection = false;
	private boolean attemptDisconnection = false;
	private boolean isDebug = false;
	private boolean isConnected = false;

	public UserInterface() {
		JFrame frame = new JFrame("Robot Control Software");
		frame.setSize(XWIDTH, YHEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainPanel = new MainPanel();
		frame.setContentPane(mainPanel);
		frame.setVisible(true);
	}

	// updates section of panel relating to joystick panel

	public void updateJoystickPanel(float[] pollData, boolean connectedToStick) {
		if (connectedToStick) {
			mainPanel.jsPanel.status = "Connected";
			mainPanel.jsPanel.connected = true;
		} else {
			mainPanel.jsPanel.status = "Disconnected";
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

	public void setByteOutput(String output) {
		String topHalf = output.substring(0, output.length() / 2 - 1);
		String bottomHalf = output.substring(output.length() / 2, output.length() - 1);
		mainPanel.connectionPanel.byteOutput[0].setText(topHalf);
		mainPanel.connectionPanel.byteOutput[1].setText(bottomHalf);
	}

	public void setByteOutputColor(Color c) {
		for (int i = 0; i < mainPanel.connectionPanel.byteOutput.length; i++) {
			mainPanel.connectionPanel.byteOutput[i].setForeground(c);
		}
	}

	public boolean getDebugMode() {
		return isDebug;
	}
	// determines whether to attempt connection, based on whether the "connect"
	// button has been pushed

	public boolean getAttemptConnection() {
		return attemptConnection;
	}

	public void setAttemptConnection(boolean b) {
		attemptConnection = b;
	}

	public boolean getAttemptDisconnection() {
		return attemptDisconnection;
	}

	public void setAttemptDisconnection(boolean b) {
		attemptDisconnection = b;
	}

	public void addError(String error) {
		mainPanel.errorPanel.addError(error);
	}

	private class MainPanel extends JPanel {
		private ConnectionPanel connectionPanel;
		private JSPanel jsPanel;
		private ErrorPanel errorPanel;
		private CameraPanel cameraPanel;

		// sets up framework for UI
		private MainPanel() {
			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			// Top Left Panel
			connectionPanel = new ConnectionPanel();
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 1;
			c.gridheight = 3;
			this.add(connectionPanel, c);

			// Top Right Panel
			jsPanel = new JSPanel();
			c.gridx = 1;
			c.gridy = 0;
			c.gridwidth = 3;
			c.gridheight = 2;
			this.add(jsPanel, c);

			// Bottom Left Panel
			errorPanel = new ErrorPanel();
			c.gridx = 0;
			c.gridy = 3;
			c.gridwidth = 1;
			c.gridheight = 1;
			this.add(errorPanel, c);

			// Bottom Right Panel
			cameraPanel = new CameraPanel();
			c.gridx = 1;
			c.gridy = 2;
			c.gridwidth = 3;
			c.gridheight = 3;
			this.add(cameraPanel, c);
		}

		private class ConnectionPanel extends JPanel {
			private JLabel title, status;
			private JLabel[] byteOutput;
			private JButton connect, mode;

			private ConnectionPanel() {
				this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				this.setBorder(new LineBorder(Color.BLACK));
				this.setPreferredSize(new Dimension((XWIDTH * 1) / 3, (YHEIGHT * 3) / 4));
				this.setMinimumSize(new Dimension((XWIDTH * 1) / 3, (YHEIGHT * 3) / 4));

				Dimension titleDim = new Dimension(80, 25);
				Dimension compDim = new Dimension(80, 25);
				Dimension panelDim = new Dimension(200, 25);

				title = new JLabel("Connection");
				title.setAlignmentX(CENTER_ALIGNMENT);
				this.add(title);

				JPanel statusPanel = new JPanel();
				JLabel statusTitle = new JLabel("Status: ");
				statusTitle.setHorizontalAlignment(SwingConstants.RIGHT);
				statusTitle.setPreferredSize(titleDim);
				statusTitle.setMaximumSize(titleDim);
				statusPanel.add(statusTitle);

				status = new JLabel("Disconnected");
				status.setPreferredSize(compDim);
				status.setMinimumSize(compDim);
				status.setForeground(Color.RED);

				statusPanel.add(status);
				statusPanel.setPreferredSize(panelDim);
				statusPanel.setMaximumSize(panelDim);
				this.add(statusPanel);

				JPanel debug = new JPanel();
				JLabel debugTitle = new JLabel("Debug Mode: ");
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
				Dimension debugDim = new Dimension(200, 40);
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
					byteOutput[i].setFont(new Font(this.getFont().getFontName(), Font.BOLD, 20));
					byteOutput[i].setAlignmentX(CENTER_ALIGNMENT);
					byteOutput[i].setAlignmentY(BOTTOM_ALIGNMENT);
					Dimension byteDim = new Dimension(400, 25);
					byteOutput[i].setPreferredSize(byteDim);
					byteOutput[i].setMinimumSize(byteDim);
					this.add(byteOutput[i]);
				}
			}
		}

		private class JSPanel extends JPanel {
			private boolean connected;
			private String status;
			private float[] pollData;

			private JSPanel() {
				pollData = new float[17];
				connected = false;
				status = "Disconnected";
				this.setBorder(new LineBorder(Color.BLACK));
				this.setPreferredSize(new Dimension((XWIDTH * 2) / 3, (YHEIGHT * 2) / 4));
				this.setMinimumSize(new Dimension((XWIDTH * 2) / 3, (YHEIGHT * 2) / 4));

			}

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setFont(new Font("Dialog", Font.BOLD, 12));
				g.drawString("Joystick", this.getWidth() / 2 - 20, 15);
				g.drawString("Status: ", this.getWidth() / 2 - 55, 40);
				if (connected) {
					g.setColor(Color.GREEN);
				} else {
					g.setColor(Color.RED);
				}
				g.drawString(status, this.getWidth() / 2 - 5, 40);
				renderButtons(g, 10, 0);
				renderPOVIndicator(g, 50, 195, pollData[2], "POV");
				renderJoyStickIndicator(g, 250, 10, pollData[1], pollData[0], "Main Stick");
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
		
		private void renderJoyStickIndicator(Graphics g, int x, int y, float jx, float jy, String label){
			int jlength = 40;
			int boxsize=150;
			g.setColor(Color.BLACK);
			g.fillRect(x+3, y+3, boxsize, boxsize);
			g.setColor(new Color(127,127,127));
			g.fillRect(x, y, boxsize, boxsize);
			g.setColor(Color.WHITE);
			g.drawLine(x+(boxsize/2), y+(boxsize/2), (int)((x+(boxsize/2))+jx*(jlength)), (int)((y+(boxsize/2))+jy*(jlength)));
			g.fillOval((int)((x+(boxsize/2))+jx*(jlength))-40, (int)((y+(boxsize/2))+jy*(jlength))-40, 80, 80);
			g.setColor(Color.BLACK);
			g.drawString(label,x + boxsize / 4,y+(boxsize+20));	
			
		}
		
		private void renderThrottleIndicator(Graphics g, int x, int y, float slider, String label) {
			double jx = Math.cos(-1 * Math.PI/4 *slider + Math.PI/2);
			double jy = Math.sin(-1 * Math.PI/4 *slider + Math.PI/2);;
			int jlength = 35;
			int boxsize = 60;
			g.setColor(Color.BLACK);
			g.fillRect(x + 3, y + 3, boxsize, boxsize);
			g.setColor(new Color(127, 127, 127));
			g.fillRect(x, y, boxsize, boxsize);
			g.setColor(new Color(220, 220, 220));
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(5));
			g2.draw(new Line2D.Double(x + (boxsize / 2), y + boxsize - 4, (jx * jlength + x + boxsize/2),
					(-jy *jlength + y + boxsize -4 )));
			g.fillOval(x + (boxsize/2 - 8), y + boxsize - 16, 16, 16);
			g.setColor(Color.BLACK);
			g.drawString(label, x + boxsize / 12, y + (boxsize + 20));
		}
		
		private void renderRZIndicator(Graphics g, int x, int y, float rz, String label) {
			double jx = Math.cos(-1 * Math.PI/4 *rz + Math.PI/2);
			double jy = Math.sin(-1 * Math.PI/4 *rz + Math.PI/2);;
			int jlength = 35;
			int boxsize = 60;
			g.setColor(Color.BLACK);
			g.fillRect(x + 3, y + 3, boxsize, boxsize);
			g.setColor(new Color(127, 127, 127));
			g.fillRect(x, y, boxsize, boxsize);
			g.setColor(Color.WHITE);
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(5));
			g2.draw(new Line2D.Double(x + (boxsize / 2), y + boxsize - 4, (jx * jlength + x + boxsize/2),
					(-jy *jlength + y + boxsize -4 )));
			g.setColor(Color.BLACK);
			g.drawString(label, x + boxsize / 12, y + (boxsize + 20));
		}

		private class ErrorPanel extends JPanel {
			JLabel title;

			private ErrorPanel() {
				this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				this.setBorder(new LineBorder(Color.BLACK));
				this.setPreferredSize(new Dimension((XWIDTH * 1) / 3, (YHEIGHT * 1) / 4));
				this.setMinimumSize(new Dimension((XWIDTH * 1) / 3, (YHEIGHT * 1) / 4));

				title = new JLabel("Error Log");
				title.setAlignmentX(CENTER_ALIGNMENT);
				this.add(title);
			}

			private void addError(String error) {
				if (this.getComponentCount() > 7) {
					Component t = this.getComponent(0);
					this.removeAll();
					this.add(t);
				}
				JLabel errorMsg = new JLabel(error);
				errorMsg.setAlignmentX(CENTER_ALIGNMENT);
				errorMsg.setForeground(Color.RED);
				this.add(errorMsg);
				this.revalidate();
			}
		}

		private class CameraPanel extends JPanel {
			private CameraPanel() {
				this.add(new JLabel("Camera"));
				this.setBorder(new LineBorder(Color.BLACK));
				this.setPreferredSize(new Dimension((XWIDTH * 2) / 3, (YHEIGHT * 2) / 4));
				this.setMinimumSize(new Dimension((XWIDTH * 2) / 3, (YHEIGHT * 2) / 4));
			}
		}
	}
}

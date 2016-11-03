package mars;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.LineBorder;

public class UserInterface {
	private static final int XWIDTH = 1500;
	private static final int YHEIGHT = 600;
	private MainPanel mainPanel;
	private boolean attemptConnection = false;

	public UserInterface() {
		JFrame frame = new JFrame("Robot Control Software");
		frame.setSize(XWIDTH, YHEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainPanel = new MainPanel();
		frame.setContentPane(mainPanel);
		frame.setVisible(true);
	}

	// determines whether to attempt connection, based on whether the "connect"
	// button has been pushed
	public boolean getAttemptConnection() {
		return attemptConnection;
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
			private JLabel title;
			private JButton connect, mode;

			private ConnectionPanel() {
				this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				this.setBorder(new LineBorder(Color.BLACK));
				this.setPreferredSize(new Dimension((XWIDTH * 1) / 3, (YHEIGHT * 3) / 4));
				this.setMinimumSize(new Dimension((XWIDTH * 1) / 3, (YHEIGHT * 3) / 4));

				title = new JLabel("Connection");
				title.setAlignmentX(CENTER_ALIGNMENT);
				this.add(title);

				JPanel debug = new JPanel();
				debug.add(new JLabel("Debug Mode: "));
				mode = new JButton("Off");
				Dimension buttonDim = new Dimension(80, 25);
				mode.setPreferredSize(buttonDim);
				mode.setMinimumSize(buttonDim);
				mode.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JButton source = (JButton)(e.getSource());
						if (source.getText().equals("On")) {
							source.setText("Off");
						} else {
							source.setText("On");
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
				connect.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						attemptConnection = true;
					}
				});

				this.add(connect);
			}
		}

		private class JSPanel extends JPanel {
			private JSPanel() {
				this.setBorder(new LineBorder(Color.BLACK));
				this.add(new JLabel("Joystick"));
				this.setPreferredSize(new Dimension((XWIDTH * 2) / 3, (YHEIGHT * 2) / 4));
				this.setMinimumSize(new Dimension((XWIDTH * 2) / 3, (YHEIGHT * 2) / 4));
			}
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
				JLabel errorMsg = new JLabel(error);
				errorMsg.setAlignmentX(CENTER_ALIGNMENT);
				errorMsg.setForeground(Color.RED);
				this.add(errorMsg);
				System.out.println(this.getComponentCount());
				if (this.getComponentCount() > 2 ) {
					this.remove(this.getComponent(1));
				}
				this.repaint();
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

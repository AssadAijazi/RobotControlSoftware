package mars;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class UserInterface {
	private static final int WIDTH = 900;
	private static final int HEIGHT = 600;
	private ContentPanel contentPanel;
	
	public UserInterface() {
		JFrame frame = new JFrame("Robot Control Software");
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPanel = new ContentPanel(WIDTH, HEIGHT);
		frame.setContentPane(contentPanel);
		frame.setVisible(true);
	}
	public void update(String byteArr) {
		contentPanel.update(byteArr);
		contentPanel.repaint();
	}

	public static class ContentPanel extends JPanel {
		private static int WIDTH, HEIGHT;
		private JPanel connectionPanel, jsPanel, errorPanel, cameraPanel;
		
		//sets up framework for UI
		public ContentPanel(int w, int h) {
			WIDTH = w;
			HEIGHT = h;
			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			
			connectionPanel = new JPanel();
			connectionPanel.add(new JLabel("Connection"));
			connectionPanel.setBorder(new LineBorder(Color.BLACK));
			connectionPanel.setPreferredSize(new Dimension((WIDTH * 1)/3, (HEIGHT * 3)/4));
			connectionPanel.setMinimumSize(new Dimension((WIDTH * 1)/3, (HEIGHT * 3)/4));
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 1;
			c.gridheight = 3;
			this.add(connectionPanel, c);
			
			jsPanel = new JPanel();
			jsPanel.setBorder(new LineBorder(Color.BLACK));
			jsPanel.add(new JLabel("Joystick"));
			jsPanel.setPreferredSize(new Dimension((WIDTH * 2)/3, (HEIGHT * 2)/4));
			jsPanel.setMinimumSize(new Dimension((WIDTH * 2)/3, (HEIGHT * 2)/4));
			c.gridx = 1;
			c.gridy = 0;
			c.gridwidth = 3;
			c.gridheight = 2;
			this.add(jsPanel, c);
			
			errorPanel = new JPanel();
			errorPanel.add(new JLabel("Error Log"));
			errorPanel.setBorder(new LineBorder(Color.BLACK));
			errorPanel.setPreferredSize(new Dimension((WIDTH * 1)/3, (HEIGHT * 1)/4));
			errorPanel.setMinimumSize(new Dimension((WIDTH * 1)/3, (HEIGHT * 1)/4));
			c.gridx = 0;
			c.gridy = 3;
			c.gridwidth = 1;
			c.gridheight = 1;
			this.add(errorPanel, c);
			
			cameraPanel = new JPanel();
			cameraPanel.add(new JLabel("Camera"));
			cameraPanel.setBorder(new LineBorder(Color.BLACK));
			cameraPanel.setPreferredSize(new Dimension((WIDTH * 2)/3, (HEIGHT * 2)/4));
			cameraPanel.setMinimumSize(new Dimension((WIDTH * 2)/3, (HEIGHT * 2)/4));
			c.gridx = 1;
			c.gridy = 2;
			c.gridwidth = 3;
			c.gridheight = 3;
			this.add(cameraPanel, c);
		}
		
		public void update(String byteArr) {
			
			revalidate();
		}
	}
}

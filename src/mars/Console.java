package mars;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Console extends JPanel {
	public JFrame frame;
	public JPanel content;
	public JTextPane console;
	public JTextField input;
	public JScrollPane scrollPane;
	public StyledDocument document;
	boolean trace = false;
	private NetworkDaemon nd;

	public Console(NetworkDaemon n) {
		nd = n;
		this.setLayout(new BorderLayout());

		console = new JTextPane();
		console.setEditable(false);
		console.setFont(new Font("Monospaced", Font.BOLD, 18));
		console.setOpaque(false);
		console.setFocusable(false);
		document = console.getStyledDocument();
		scrollPane = new JScrollPane(console);
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		input = new JTextField();

		input.setBorder(BorderFactory.createMatteBorder(1, 0, 13, 0, Color.GREEN));
		input.setFont(new Font("Monospaced", Font.BOLD, 20));
		input.setEditable(true);
		input.setCaretColor(Color.GREEN);
		input.setForeground(Color.GREEN);
		input.setOpaque(false);
		input.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String text = input.getText();
				addMessage(text);
				doCommand(text);
				input.selectAll();
			}
		});

		input.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {

			}

			@Override
			public void keyReleased(KeyEvent arg0) {

			}

			@Override
			public void keyTyped(KeyEvent arg0) {

			}

		});

		this.add(scrollPane, BorderLayout.CENTER);
		this.add(input, BorderLayout.SOUTH);
		this.setBackground(new Color(50, 50, 50));
	}

	public void addMessage(String text) {
		addText(text, Color.GREEN);
	}

	public void addError(String text) {
		addText(text, Color.RED);
	}

	private void addText(String text, Color c) {
		Style style = console.addStyle("Style", null);
		StyleConstants.setForeground(style, c);
		try {
			document.insertString(document.getLength(), text + "\n", style);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scrollBottom();
	}

	private void doCommand(String text) {
		String[] commands = text.split(" ");
		if (commands[0].equals("clear")) {
			clear();
		} else if (commands[0].equals("send")) {
			send(commands);
		} else {
			addError("Command \"" + commands[0] + "\" not recognized");
		}
	}

	private void clear() {
		try {
			document.remove(0, document.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	private void send(String[] commands) {
		if (commands.length <= 1) {
			addError("Specify argument bytes to send");
		} else {
			int i = 1;
			byte[] output = new byte[commands.length - 1];
			try {
				while (i < commands.length) {
					output[i - 1] = Byte.parseByte(commands[i]);
					i++;
				}
				if (!nd.isConnected())
					throw new Exception("No connection Found. Connect and try again");
				nd.send(output);
			} catch (Exception e) {
				addError(e.toString());
			}

		}
	}

	private void scrollBottom() {
		console.setCaretPosition(document.getLength());
	}

}

//
// Panel for displaying game status messages at the botton of the screen
//

package tyrant.mikera.tyrant;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javelin.controller.old.Game;

public class MessagePanel extends TPanel implements IMessageHandler {
	private static final long serialVersionUID = 3258416114332807730L;
	public final TextZone textzone = new TextZone();

	public MessagePanel(final QuestApp q) {
		super(q);
		setLayout(new BorderLayout());
		textzone.setBackground(QuestApp.PANELCOLOUR);
		textzone.setForeground(QuestApp.INFOTEXTCOLOUR);

		add("Center", textzone);
		Game.messagepanel = this;
	}

	private void setText(final String s) {
		textzone.setText(s);
	}

	private String getText() {
		return textzone.getText();
	}

	@Override
	public void clear() {
		textzone.setText("");
	}

	@Override
	public void add(final String s) {
		add(s, Color.lightGray);
	}

	@Override
	public void add(final String s, final Color c) {
		/*
		 * textzone.setForeground(Color.BLACK); repaint();
		 */
		final String t = getText();
		if (t.length() > 2000) {
			setText(t.substring(t.length() - 2000, t.length()));
		}

		final String newtext = getText() + s;
		setText(newtext);

		// try {
		// setCaretPosition(newtext.length()-1);
		// } catch (Exception e) {}
	}

	// public void append(String s){
	// text=text+s;
	// repaint();
	// }

	// public void setText(String s) {
	// text=s;
	// repaint();
	// }

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(500, 120);
	}

	@Override
	public TPanel getPanel() {
		return this;
	}
}
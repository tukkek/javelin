//
// Panel for displaying game status messages at the botton of the screen
//

package javelin.old;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

public class MessagePanel extends TPanel {
	public final TextZone textzone = new TextZone();

	public MessagePanel() {
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

	public void clear() {
		textzone.setText("");
	}

	public void add(final String s) {
		add(s, Color.lightGray);
	}

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
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(500, 120);
	}

	public TPanel getPanel() {
		return this;
	}
}
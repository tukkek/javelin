package javelin.old.messagepanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javelin.old.QuestApp;
import javelin.old.TPanel;

public class MessagePanel extends TPanel {
	public final TextZone textzone = new TextZone();
	public static MessagePanel active;

	public MessagePanel() {
		setLayout(new BorderLayout());
		textzone.setBackground(QuestApp.PANELCOLOUR);
		textzone.setForeground(QuestApp.INFOTEXTCOLOUR);
		add("Center", textzone);
		MessagePanel.active = this;
	}

	public void clear() {
		textzone.setText("");
	}

	public void add(final String s) {
		add(s, Color.lightGray);
	}

	public void add(final String s, final Color c) {
		final String t = textzone.getText();
		if (t.length() > 2000) {
			textzone.setText(t.substring(t.length() - 2000, t.length()));
		}
		textzone.setText(textzone.getText() + s);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(500, 120);
	}
}
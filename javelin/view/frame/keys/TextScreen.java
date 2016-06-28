package javelin.view.frame.keys;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import javelin.view.frame.Frame;

public abstract class TextScreen extends Frame {

	public TextScreen(String title) {
		super(title);
	}

	protected abstract void savetext(final String string);

	protected abstract String loadtext();

	@Override
	protected Container generate() {
		JPanel panel = new JPanel(new BorderLayout());
		final TextArea text = new TextArea(loadtext(), 30, 80);
		panel.add(text, BorderLayout.CENTER);
		java.awt.Button save = new java.awt.Button();
		save.setLabel("Save");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				savetext(text.getText());
				frame.dispose();
			}
		});
		panel.add(save, BorderLayout.SOUTH);
		return panel;
	}

}
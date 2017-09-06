package javelin.view;

import java.awt.Button;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;

import javelin.controller.scenario.Campaign;
import javelin.controller.scenario.Scenario;
import javelin.model.world.World;

public class ModeSelectionScreen extends Frame {
	private static final String[] MESSAGES = new String[] {
			"Javelin offers two game modes:", "",
			"* Campaign: a long, epic quest to retrieve the seven Relics from deep inside their ancient Temples. It presents all of the game features, including mini-games.",
			"",
			"* Scenario: a shorter mode, offering a challenging experience in which you have to capture all enemy towns. It has a reduced number of game features but is more suitable to play in a single session.",
			"",
			"You can read more about each mode by pressing F1 after the game has started.",
			"", "What mode do you want to start (or continue) today?" };

	class Close extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}

	void draw() {
		setTitle("Welcome to Javelin!");
		setSize(400, 400);
		addWindowListener(new Close());
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		for (String s : preparetext()) {
			addline(" " + s);
		}
		Container buttonarea = new Container();
		buttonarea.setLayout(new BoxLayout(buttonarea, BoxLayout.X_AXIS));
		add(buttonarea);
		addbutton("Launch campaign mode", new Campaign(), buttonarea);
		addbutton("Launch scenario mode", new Scenario(), buttonarea);
	}

	void addline(String s) {
		Label l = new Label();
		l.setText(s);
		add(l);
	}

	ArrayList<String> preparetext() {
		ArrayList<String> lines = new ArrayList<String>();
		for (String s : MESSAGES) {
			String line = "";
			for (String word : s.split(" ")) {
				line += word + " ";
				if (line.length() > 80) {
					lines.add(line);
					line = "";
				}
			}
			if (line != "") {
				lines.add(line);
			}
		}
		return lines;
	}

	void addbutton(String text, final Scenario s, Container buttonarea) {
		Button b = new Button(text);
		buttonarea.add(b);
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				World.scenario = s;
				dispose();
			}
		});
	}

	public static void choose() {
		ModeSelectionScreen s = new ModeSelectionScreen();
		s.draw();
		s.pack();
		s.setLocationRelativeTo(null);
		s.setVisible(true);
		while (s.isVisible()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}

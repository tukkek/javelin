package javelin.view;

import java.awt.Button;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BoxLayout;

import javelin.controller.TextReader;
import javelin.controller.scenario.Campaign;
import javelin.controller.scenario.Scenario;
import javelin.model.world.World;

public class ModeSelectionDialog extends Frame {
	static final Map<String, Scenario> MODES = new TreeMap<String, Scenario>();

	static {
		MODES.put("campaign", new Campaign());
		MODES.put("scenario", new Scenario());
	}

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
		for (String mode : MODES.keySet()) {
			addbutton("Launch " + mode + " mode", MODES.get(mode), buttonarea);
		}
	}

	void addline(String s) {
		Label l = new Label();
		l.setText(s);
		add(l);
	}

	ArrayList<String> preparetext() {
		ArrayList<String> lines = new ArrayList<String>();
		String file = TextReader.read(new File("doc", "introduction.txt"));
		for (String s : file.trim().split("\n")) {
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

	public static void choose(String[] args) {
		if (choosefromcommandline(args)) {
			return;
		}
		ModeSelectionDialog s = new ModeSelectionDialog();
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

	static boolean choosefromcommandline(String[] args) {
		if (args.length == 0) {
			return false;
		}
		World.scenario = MODES.get(args[0].toLowerCase());
		return World.scenario != null;
	}
}

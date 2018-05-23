package javelin.controller;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javelin.Javelin;
import javelin.old.Game;
import javelin.view.screen.InfoScreen;

/**
 * Outputs the content of a text file to the screen.
 * 
 * @author alex
 */
public class TextReader {
	static public String read(File text) {
		try {
			String out = "";
			BufferedReader reader = new BufferedReader(new FileReader(text));
			String line = reader.readLine();
			while (line != null) {
				out += line + "\n";
				line = reader.readLine();
			}
			return out;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static KeyEvent show(File f, String footer) {
		Javelin.app.switchScreen(new InfoScreen(TextReader.read(f) + footer));
		return Game.input();
	}
}

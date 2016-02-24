package javelin.controller.db.reader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;

/**
 * Logs special attacks and special qualities for each monster.
 * 
 * @se {@link MonsterReader}
 * 
 * @author alex
 */
public class SpecialtiesLog {
	private static FileWriter log;

	static {
		if (Javelin.DEBUG) {
			try {
				log = new FileWriter(new File("specialties.log"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	static List<String> lines = new ArrayList<String>();

	public static void log() {
		if (Javelin.DEBUG) {
			try {
				for (String line : lines) {
					log.write(line + "\n");
				}
				log.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void log(String string) {
		if (Javelin.DEBUG) {
			lines.add(string);
		}
	}

	public static void clear() {
		lines.clear();
	}

}

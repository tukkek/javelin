package javelin.controller.generator.dungeon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javelin.controller.db.Preferences;
import javelin.controller.generator.dungeon.template.Irregular;
import javelin.controller.generator.dungeon.template.Template;

public class DungeonGenerator {
	/** Procedurally generated templates only. */
	static final Template[] TEMPLATES = new Template[] { new Irregular() };
	/**
	 * How many times to generate each procedurally-generated {@link Template}.
	 */
	static final int PERMUTATIONS = 10;

	ArrayList<Template> pool = new ArrayList<Template>();
	String log = "";

	public DungeonGenerator() {
		for (Template t : TEMPLATES) {
			for (int i = 0; i < PERMUTATIONS; i++) {
				pool.add(t.create());
			}
		}
		Collections.shuffle(pool);
		for (Template t : pool) {
			log += t + "\n";
		}
		write();
	}

	void write() { // debug
		try {
			Preferences.write(log, "/tmp/dungeon.txt");
		} catch (IOException e) {
			throw new RuntimeException();
		}
		log = "";
	}

	public static void main(String[] args) {
		new DungeonGenerator();
	}
}

package javelin.controller.generator.dungeon;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import javelin.controller.db.Preferences;
import javelin.controller.generator.dungeon.tables.ConnectionTable;
import javelin.controller.generator.dungeon.template.Irregular;
import javelin.controller.generator.dungeon.template.Template;

public class DungeonGenerator {
	/** Procedurally generated templates only. */
	static final Template[] TEMPLATES = new Template[] { new Irregular() };
	/**
	 * How many times to generate each procedurally-generated {@link Template}.
	 */
	static final int PERMUTATIONS = 10;

	LinkedList<Template> pool = new LinkedList<Template>();
	ConnectionTable connections = new ConnectionTable();
	VirtualMap map = new VirtualMap();
	String log = "";

	public DungeonGenerator() {
		generatepool();
		draw(pool.pop());
		// log = pool.pop().toString();
		write();
	}

	void draw(Template root) {
		map.draw(root, 0, 0);
		log += root.toString() + "\n";
		log += map.toString() + "\n";
	}

	void generatepool() {
		for (Template t : TEMPLATES) {
			for (int i = 0; i < PERMUTATIONS; i++) {
				pool.add(t.create());
			}
		}
		Collections.shuffle(pool);
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

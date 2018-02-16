package javelin.controller.generator.dungeon.tables;

import java.util.HashMap;

public class LevelTables {
	HashMap<Class<? extends DungeonTable>, DungeonTable> tables = new HashMap<Class<? extends DungeonTable>, DungeonTable>();

	public <K extends DungeonTable> K get(Class<K> table) {
		DungeonTable t = tables.get(table);
		if (t == null) {
			try {
				t = table.newInstance();
				t.modify();
				tables.put(table, t);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
		return (K) t;
	}
}

package javelin.controller.generator.dungeon.tables;

import java.util.HashMap;

public class LevelTables {
	HashMap<Class<? extends DungeonTable>, DungeonTable> tables = new HashMap<Class<? extends DungeonTable>, DungeonTable>();

	public DungeonTable get(Class<? extends DungeonTable> table) {
		DungeonTable t = tables.get(table);
		if (t == null) {
			try {
				t = table.newInstance();
				tables.put(table, t);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
		return t;
	}
}

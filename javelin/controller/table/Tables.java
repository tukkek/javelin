package javelin.controller.table;

import java.io.Serializable;
import java.util.HashMap;

public class Tables implements Serializable, Cloneable {
	HashMap<Class<? extends Table>, Table> tables = new HashMap<Class<? extends Table>, Table>();

	public <K extends Table> K get(Class<K> table) {
		try {
			Table t = tables.get(table);
			if (t == null) {
				t = table.newInstance();
				t.modify();
				tables.put(table, t);
			}
			return (K) t;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Tables clone() {
		try {
			Tables clone;
			clone = (Tables) super.clone();
			clone.tables = new HashMap<Class<? extends Table>, Table>(tables);
			for (Class<? extends Table> table : clone.tables.keySet()) {
				clone.tables.put(table, tables.get(table).clone());
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}

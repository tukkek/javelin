package javelin.controller.table;

import java.io.Serializable;
import java.util.HashMap;

public class Tables implements Serializable, Cloneable {
	HashMap<Class<? extends Table>, Table> tables = new HashMap<>();

	public <K extends Table> K get(Class<K> table) {
		try {
			Table t = tables.get(table);
			if (t == null) {
				t = table.getDeclaredConstructor().newInstance();
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
			clone.tables = new HashMap<>(tables);
			for (Class<? extends Table> table : clone.tables.keySet()) {
				clone.tables.put(table, tables.get(table).clone());
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		String tables = "";
		for (Class<? extends Table> table : this.tables.keySet()) {
			tables += this.tables.get(table) + "\n\n";
		}
		return tables;
	}
}

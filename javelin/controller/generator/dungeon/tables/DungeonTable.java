package javelin.controller.generator.dungeon.tables;

import java.util.ArrayList;

import tyrant.mikera.engine.RPG;

/**
 * TODO this implementation needs more work in order for cloning to work as
 * intended - {@link #modify()} is not acting on the {@link #table}, for
 * example.
 *
 * @author alex
 */
public class DungeonTable implements Cloneable {
	ArrayList<Row> table = new ArrayList<Row>();

	public void add(Row r) {
		int n = r.getamount();
		for (int i = 0; i < n; i++) {
			table.add(r);
		}
	}

	public Row roll() {
		return RPG.pick(table);
	}

	@Override
	protected DungeonTable clone() {
		try {
			DungeonTable clone = (DungeonTable) super.clone();
			for (int i = 0; i < table.size(); i++) {
				clone.table.set(i, clone.table.get(i).clone());
			}
			clone.modify();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	protected void modify() {
		for (Row r : table) {
			r.modify();
		}
	}
}

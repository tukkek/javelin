package javelin.controller.generator.dungeon.tables;

import java.util.ArrayList;

import tyrant.mikera.engine.RPG;

public class DungeonTable implements Cloneable {
	ArrayList<Row> table = new ArrayList<Row>();
	ArrayList<Row> rows = new ArrayList<Row>();

	void add(Row r) {
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
			for (int i = 0; i < rows.size(); i++) {
				clone.rows.set(i, clone.rows.get(i).clone());
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}

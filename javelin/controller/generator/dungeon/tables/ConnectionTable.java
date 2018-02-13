package javelin.controller.generator.dungeon.tables;

public class ConnectionTable extends DungeonTable {
	public static final Row CORRIDOR = new Row(2, 2, 2, true);
	public static final Row ROOM = new Row(2, 2, 2, true);

	public ConnectionTable() {
		add(CORRIDOR);
		add(ROOM);
	}
}

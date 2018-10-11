package javelin.controller.table.dungeon;

import javelin.controller.table.Table;

public class RoomSizeTable extends Table{
	public RoomSizeTable(){
		for(int size=3;size<=7;size++)
			add(size,20);
	}
}

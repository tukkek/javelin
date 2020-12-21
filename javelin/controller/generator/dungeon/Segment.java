package javelin.controller.generator.dungeon;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.FloorTile;

public class Segment{
	public FloorTile room;
	public Point cursor;

	public Segment(FloorTile room,Point cursor){
		this.room=room;
		this.cursor=cursor;
	}
}

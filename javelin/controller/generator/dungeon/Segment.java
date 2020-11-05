package javelin.controller.generator.dungeon;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.MapTemplate;

public class Segment{
	public MapTemplate room;
	public Point cursor;

	public Segment(MapTemplate room,Point cursor){
		this.room=room;
		this.cursor=cursor;
	}
}

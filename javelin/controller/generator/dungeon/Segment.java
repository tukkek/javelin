package javelin.controller.generator.dungeon;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Template;

public class Segment {
	public Template room;
	public Point cursor;

	public Segment(Template room, Point cursor) {
		this.room = room;
		this.cursor = cursor;
	}
}

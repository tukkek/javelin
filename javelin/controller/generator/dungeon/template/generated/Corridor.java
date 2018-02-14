package javelin.controller.generator.dungeon.template.generated;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.VirtualMap;
import javelin.controller.generator.dungeon.template.Template;
import tyrant.mikera.engine.RPG;

public class Corridor extends Template {
	@Override
	public void generate() {
		corridor = true;
		init(RPG.chancein(4) ? 2 : 1, RPG.r(3, 7));
	}

	public static void clear(Template t, Point cursor, Point door,
			Template next, Point doorb, VirtualMap map) {
		if (t instanceof Corridor && next instanceof Corridor) {
			map.set(FLOOR, cursor.x + door.x, cursor.y + door.y);
			next.tiles[doorb.x][doorb.y] = FLOOR;
		}
	}
}

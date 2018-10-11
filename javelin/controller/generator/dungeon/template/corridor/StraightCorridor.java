package javelin.controller.generator.dungeon.template.corridor;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.VirtualMap;
import javelin.controller.generator.dungeon.template.Template;
import javelin.old.RPG;

public class StraightCorridor extends Template{
	public StraightCorridor(){
		corridor=true;
	}

	@Override
	public void generate(){
		init(RPG.chancein(4)?2:1,RPG.r(3,7));
	}

	public static void clear(Template t,Point cursor,Point door,Template next,
			Point doorb,VirtualMap map){
		if(t instanceof StraightCorridor&&next instanceof StraightCorridor){
			map.set(FLOOR,cursor.x+door.x,cursor.y+door.y);
			next.tiles[doorb.x][doorb.y]=FLOOR;
		}
	}
}

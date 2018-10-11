package javelin.controller.generator.dungeon.template.mutator;

import java.util.ArrayList;
import java.util.Collections;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Template;
import javelin.old.RPG;

public class Noise extends Mutator{
	public Noise(){
		// chance = 1.0;
	}

	@Override
	public void apply(Template t){
		final ArrayList<Point> free=new ArrayList<>();
		t.iterate(t1->{
			if(t1.c==Template.FLOOR) free.add(new Point(t1.x,t1.y));
		});
		int walls=Math.round(free.size()*RPG.r(10,25)/100f);
		Collections.shuffle(free);
		Character glyph=t.count(Template.FLOOR)>=t.count(Template.WALL)
				?Template.WALL
				:Template.FLOOR;
		for(int i=0;i<walls;i++){
			Point wall=free.get(i);
			t.tiles[wall.x][wall.y]=glyph;
		}
	}
}

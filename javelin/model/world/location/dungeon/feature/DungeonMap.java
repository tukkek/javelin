package javelin.model.world.location.dungeon.feature;

import java.util.ArrayList;
import java.util.HashSet;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Template;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.RPG;
import javelin.view.screen.BattleScreen;

/**
 * Shows an area of the {@link Dungeon} map, with a cumulative 50% chance of
 * doubling that area (50% chance of 2*area, 25% chance of 3*area, etc).
 *
 * TODO allow other Dungeon floors to be shown.
 *
 * @author alex
 */
public class DungeonMap extends Feature{
	final double AREA=RPG.r(10,25)/100.0;

	/** Constructor. */
	public DungeonMap(){
		super("map");
	}

	@Override
	public boolean activate(){
		var d=Dungeon.active;
		var floors=new HashSet<Point>();
		for(var x=0;x<d.size;x++)
			for(var y=0;y<d.size;y++)
				if(d.map[x][y]!=Template.WALL) floors.add(new Point(x,y));
		for(var f:new ArrayList<>(floors))
			floors.addAll(f.getadjacent());
		var area=AREA;
		while(area<1&&RPG.chancein(2))
			area+=AREA;
		if(area>1) area=1;
		var p=RPG.pick(floors);
		floors.stream().sorted((a,b)->Double.compare(a.distance(p),b.distance(p)))
				.limit((int)Math.round(floors.size()*area))
				.forEach(tile->Brazier.reveal(tile));
		BattleScreen.active.center(p.x,p.y);
		Javelin.redraw();
		Javelin.message("A map reveals a portion of the level!",false);
		return true;
	}
}

package javelin.model.world.location.dungeon.feature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Template;
import javelin.controller.walker.Walker;
import javelin.controller.walker.pathing.DirectPath;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.FireTemple;
import javelin.view.mappanel.dungeon.DungeonTile;

/**
 * Lights up any square on this feature's field of vision, revealing any hidden
 * {@link Feature}s along the way too. Actually cheats a little bit by revealing
 * any square adjacent to a quare in the line of sight, to give it a bit more of
 * a "wow" radiance effect.
 *
 * @see FireTemple
 * @see Feature#discover(javelin.model.unit.Combatant, int)
 * @author alex
 */
public class Brazier extends Feature{
	//	static final int RADIUS=13;

	class VisionWalker extends Walker{
		public VisionWalker(Point from,Point to){
			super(from,to);
			pathing=new DirectPath();
		}

		@Override
		public boolean validate(Point p,LinkedList<Point> previous){
			return Dungeon.active.map[p.x][p.y]!=Template.WALL;
		}
	}

	/** Constructor. */
	public Brazier(){
		super("dungeonbrazier");
	}

	HashSet<Point> crawl(){
		var revealed=new HashSet<Point>();
		var hits=-1;
		for(var range=1;range==1||hits>0;range++){
			hits=0;
			for(var x=-range;x<=+range;x++)
				for(var y=-range;y<=+range;y++)
					if(x==range||x==-+range||y==-range||y==+range){
						var p=new Point(this.x+x,this.y+y);
						if(!p.validate(0,0,Dungeon.active.size,Dungeon.active.size))
							continue;
						var path=new VisionWalker(getlocation(),p).walk();
						if(path!=null){
							hits+=1;
							revealed.add(p);
							revealed.addAll(path);
						}
					}
		}
		return revealed;
	}

	@Override
	public boolean activate(){
		var revealed=crawl();
		for(var r:new ArrayList<>(revealed))
			if(Dungeon.active.map[r.x][r.y]!=Template.WALL)
				revealed.addAll(r.getadjacent());
		for(var r:revealed)
			reveal(r);
		Javelin.redraw();
		var p=JavelinApp.context.getsquadlocation();
		JavelinApp.context.view(p.x,p.y);
		Javelin.message("You light up the brazier!",false);
		return true;
	}

	/**
	 * @param r Shows {@link DungeonTile} and
	 *          {@link Feature#discover(javelin.model.unit.Combatant, int)} any
	 *          feature in that tile.i
	 */
	static public void reveal(Point r){
		Dungeon.active.setvisible(r.x,r.y);
		var f=Dungeon.active.features.get(r.x,r.y);
		if(f!=null) f.discover(null,9000);
	}
}

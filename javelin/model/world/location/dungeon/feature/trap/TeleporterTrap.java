package javelin.model.world.location.dungeon.feature.trap;

import java.util.ArrayList;
import java.util.HashSet;

import javelin.Javelin;
import javelin.controller.DungeonCrawler;
import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Template;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.old.RPG;

/**
 * Teleports the player to a random spot on the floor - making sure not to cross
 * any doors, preventing the player from becoming stuck.
 *
 * TODO magic traps in theory have some rules of their own
 *
 * @author alex
 */
public class TeleporterTrap extends Trap{
	class TeleportCrawl extends DungeonCrawler{
		public TeleportCrawl(){
			super(new Point(x,y),Integer.MAX_VALUE,Dungeon.active);
		}

		@Override
		protected boolean validate(Point p){
			if(dungeon.map[p.x][p.y]==Template.WALL) return false;
			return super.validate(p);
		}

		@Override
		protected boolean validate(Feature f){
			return f==null||f==TeleporterTrap.this;
		}
	}

	public TeleporterTrap(int cr,Point p){
		super(cr,p.x,p.y,"dungeontrap");
	}

	@Override
	protected void spring(){
		Combatant victim=null;
		for(var m:RPG.shuffle(new ArrayList<>(Squad.active.members)))
			if(RPG.r(1,20)+m.source.ref<savedc){
				victim=m;
				break;
			}
		if(victim==null) return;
		Javelin.message(victim+" activates the teleportation trap!",false);
		HashSet<Point> targets=new TeleportCrawl().crawl();
		targets.remove(new Point(x,y));
		targets.remove(Dungeon.active.herolocation);
		if(targets.isEmpty()) return;
		Point to=RPG.pick(new ArrayList<>(targets));
		Dungeon.active.teleport(to);
	}
}

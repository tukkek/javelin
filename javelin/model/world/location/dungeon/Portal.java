package javelin.model.world.location.dungeon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.table.dungeon.BranchTable;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Tier;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.World;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/** {@link World} portal to a {@link Wilderness} plane. */
public class Portal extends DungeonEntrance{
	static final int SPAWN=7;

	/**
	 * TODO would be interesting if portals dropped you in a random accessible
	 * spot rather than the entrance, making it more dangerous and so the player
	 * has to find the exit. This could, however, prove to be much more deadly.
	 *
	 * A sort of "road" could be created between the exit and other branches for
	 * the player to stumble upon as he explores, rather than just walk around
	 * aimlessly huggning the edge hoping for an exit.
	 */
	static class Plane extends Wilderness{
		public Plane(Terrain t){
			super(Tier.EPIC.getrandomel(false));
			name="Plane";
			branches.addAll(new BranchTable(List.of(t)).rollaffixes());
		}
	}

	boolean discovered=false;

	/** Constructor. */
	public Portal(Point p){
		super(new Plane(Terrain.get(p.x,p.y)));
		setlocation(p);
	}

	@Override
	public String getimagename(){
		return "portal";
	}

	/** @return <code>true</code> if succesfully placed a portal. */
	public static Portal create(){
		var size=World.getseed().map.length;
		var free=Point.getrange(0,0,size,size);
		var actors=World.getactors().stream().map(a->a.getlocation())
				.collect(Collectors.toList());
		free.removeAll(actors);
		var terrains=new HashSet<Terrain>(6);
		var locations=new ArrayList<Point>(6);
		for(var f:RPG.shuffle(new ArrayList<>(free))){
			var t=Terrain.get(f.x,f.y);
			if(!t.equals(Terrain.WATER)&&terrains.add(t)) locations.add(f);
		}
		if(locations.isEmpty()) return null;
		var p=new Portal(RPG.pick(locations));
		p.place();
		return p;
	}

	/** Called once a day to generate new Portals. */
	public static void turn(){
		if(!RPG.chancein(SPAWN)) return;
		var p=create();
		if(p!=null) p.dungeon.generate();
	}

	@Override
	public void turn(long time,WorldScreen world){
		super.turn(time,world);
		if(RPG.chancein(SPAWN)) remove();
	}

	@Override
	public boolean interact(){
		if(!discovered) discovered=Squad.active.getbest(Skill.KNOWLEDGE)
				.taketen(Skill.KNOWLEDGE)>=10+dungeon.level;
		return super.interact();
	}

	@Override
	public String toString(){
		return discovered?super.toString():"Portal";
	}
}

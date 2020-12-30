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
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * {@link World} portal to a {@link Branch}ed {@link Wilderness} plane. Players
 * are dropped on a random position and can find one of (usually) multiple exits
 * to leave - making exploration not only a mechanic but a pressing concern.
 */
public class Portal extends DungeonEntrance{
	static final int SPAWN=7;

	static class Plane extends Wilderness{
		public Plane(Terrain t){
			super(Tier.EPIC.getrandomel(false));
			name="Plane";
			branches.addAll(new BranchTable(List.of(t)).rollaffixes());
			entrances=RPG.randomize(2,1,Integer.MAX_VALUE);
		}

		@Override
		protected DungeonFloor chooseentrance(){
			var f=super.chooseentrance();
			var area=new DungeonZoner(f,f.squadlocation).zones.get(0).area;
			f.squadlocation=RPG.pick(area);
			return f;
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

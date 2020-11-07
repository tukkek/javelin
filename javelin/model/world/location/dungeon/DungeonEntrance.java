package javelin.model.world.location.dungeon;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.feature.inhabitant.Inhabitant;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.model.world.location.dungeon.temple.TempleFloor;

/**
 * {@link World} gate to a {@link Dungeon}.
 *
 * @author alex
 */
public class DungeonEntrance extends Location{
	/** Dungeon instance to enter. */
	public Dungeon dungeon;

	/** Constructor. */
	public DungeonEntrance(Dungeon d){
		super(null);
		dungeon=d;
		link=false;
		discard=false;
		impermeable=true;
		allowedinscenario=false;
		allowentry=false;
		unique=true;
		description=d.name;
		d.entrance=this;
	}

	@Override
	public boolean interact(){
		if(dungeon.fluff!=null) Javelin.prompt(dungeon.fluff);
		if(Javelin.prompt("You are about to enter: "+describe()+"\n"
				+"Press ENTER to continue or any other key to cancel...")!='\n')
			return false;
		dungeon.enter();
		return true;
	}

	@Override
	public Integer getel(Integer attackel){
		return attackel-3;
	}

	/**
	 * TODO this should not even be a thing and if it has to, should it include
	 * {@link Inhabitant}s and {@link DungeonFloor#encounters}? Probably not as those are
	 * cloned into battle and thus won't have {@link Combatant#id} clashes?
	 */
	@Override
	public List<Combatant> getcombatants(){
		return null;
	}

	@Override
	public String describe(){
		int squadel=ChallengeCalculator.calculateel(Squad.active.members);
		String difficulty=Difficulty.describe(dungeon.floors.get(0).level-squadel);
		return description+" ("+difficulty+").";
	}

	@Override
	public String getimagename(){
		return dungeon.getimagename();
	}

	static public List<DungeonEntrance> getdungeons(){
		var actors=World.getall(DungeonEntrance.class);
		var dungeons=new ArrayList<DungeonEntrance>(actors.size());
		for(Actor a:actors){
			var e=(DungeonEntrance)a;
			if(e.dungeon.getClass()==Dungeon.class) dungeons.add(e);
		}
		return dungeons;
	}

	/**
	 * @return All {@link DungeonFloor}s and {@link TempleFloor}s (first
	 *         {@link #floors} only).
	 */
	static public List<DungeonEntrance> getdungeonsandtemples(){
		var dungeons=getdungeons();
		for(var t:Temple.gettemples())
			dungeons.add(t);
		return dungeons;
	}
}

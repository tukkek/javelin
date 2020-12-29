package javelin.model.world.location.dungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.branch.temple.Temple;
import javelin.model.world.location.dungeon.feature.inhabitant.Inhabitant;

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
		link=false;
		discard=false;
		impermeable=true;
		allowedinscenario=false;
		allowentry=false;
		unique=true;
		if(d!=null) set(d);
	}

	/** Attaches dungeon and vice-versa. */
	public void set(Dungeon d){
		dungeon=d;
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
	 * {@link Inhabitant}s and {@link DungeonFloor#encounters}? Probably not as
	 * those are cloned into battle and thus won't have {@link Combatant#id}
	 * clashes?
	 */
	@Override
	public List<Combatant> getcombatants(){
		return null;
	}

	@Override
	public String describe(){
		var squadel=ChallengeCalculator.calculateel(Squad.active.members);
		var difficulty=Difficulty.describe(dungeon.level-squadel);
		return this+" ("+difficulty+").";
	}

	@Override
	public String getimagename(){
		return dungeon.getimagename();
	}

	/** @return All {@link Dungeon} entrances (no subclasses). */
	static public List<DungeonEntrance> getdungeons(){
		var actors=World.getall(DungeonEntrance.class);
		var dungeons=new ArrayList<DungeonEntrance>(20);
		for(Actor a:actors){
			var e=(DungeonEntrance)a;
			if(e.dungeon.getClass()==Dungeon.class) dungeons.add(e);
		}
		return dungeons;
	}

	/** @return {@link Dungeon} and {@link Temple} entrances.. */
	static public List<DungeonEntrance> getdungeonsandtemples(){
		var dungeons=getdungeons();
		for(var t:Temple.gettemples())
			dungeons.add(t);
		return dungeons;
	}

	/**
	 * @return {@link Dungeon}s, {@link Temple}s, {@link Wilderness}es,
	 *         {@link Portal}s....
	 */
	static public List<DungeonEntrance> getall(){
		return World.getactors().stream().filter(a->a instanceof DungeonEntrance)
				.map(a->(DungeonEntrance)a).collect(Collectors.toList());
	}
}

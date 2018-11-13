package javelin.controller.scenario.dungeondelve;

import java.util.Map;

import javelin.Javelin;
import javelin.controller.fight.Fight;
import javelin.controller.scenario.Campaign;
import javelin.model.item.Item;
import javelin.model.item.Ruby;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.inhabitant.Inhabitant;

/**
 * TODO
 *
 * - an {@link Inhabitant} that sells {@link Item}s so players can spend gold
 * earned in {@link Fight}s
 *
 * - Learning Stones so players can spend XP
 *
 * @author alex
 */
public class DungeonDelve extends Campaign{
	public final static int LEVELS=20;

	public DungeonDelve(){
		featuregenerator=DungeonDelveGenerator.class;
		worldgenerator=DungeonDelveWorld.class;
		allowallactors=true;
		worldencounters=false;
		worldhazards=false;
		fogofwar=false;
	}

	@Override
	public void ready(World w){
		for(Dungeon d:getdungeons().values())
			d.map();
	}

	@Override
	public boolean win(){
		if(!hasmcguffin()) return false;
		String congrats="You have returned the McGuffin to safety, congratulations!";
		Javelin.message(congrats,true);
		return true;
	}

	public static boolean hasmcguffin(){
		for(Squad s:Squad.getsquads())
			if(s.equipment.get(McGuffin.class)!=null) return true;
		return false;
	}

	/**
	 * @return {@link DungeonDelveGenerator#dungeons}.
	 */
	public static Map<Integer,Dungeon> getdungeons(){
		DungeonDelveGenerator fg=(DungeonDelveGenerator)World.seed.featuregenerator;
		return fg.dungeons;
	}

	@Override
	public Item openspecialchest(Dungeon d){
		return new Ruby();
	}
}

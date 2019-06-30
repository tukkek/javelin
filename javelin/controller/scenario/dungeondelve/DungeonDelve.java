package javelin.controller.scenario.dungeondelve;

import java.util.List;

import javelin.Javelin;
import javelin.controller.db.StateManager;
import javelin.controller.fight.Fight;
import javelin.controller.scenario.Campaign;
import javelin.controller.scenario.Scenario;
import javelin.model.item.Item;
import javelin.model.item.consumable.Ruby;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.inhabitant.Inhabitant;
import javelin.view.screen.WorldScreen;

/**
 * TODO
 *
 * - an {@link Inhabitant} that sells {@link Item}s so players can spend gold
 * earned in {@link Fight}s
 *
 * - Learning Stones so players can spend XP
 *
 * TODO make depend on {@link Scenario}
 *
 * @author alex
 */
public class DungeonDelve extends Campaign{
	/** Amount of physical levels the {@link Megadungeon} should have. */
	public final static int FLOORS=20;
	/** Becomes true when the player takes hold of the {@link McGuffin}. */
	public boolean climbmode=false;

	/** Constructor. */
	public DungeonDelve(){
		featuregenerator=DungeonDelveGenerator.class;
		worldgenerator=DungeonDelveWorld.class;
		allowallactors=true;
		worldencounters=false;
		worldhazards=false;
		fogofwar=false;
		quests=false;
		diplomacy=false;
		helpfile="Dungeon delve";
		urbanevents=false;
	}

	@Override
	public void ready(){
		var floors=getdungeons();
		for(Dungeon d:floors)
			d.define();
		Squad.active.setlocation(floors.get(0).getlocation());
		Squad.active.displace();
	}

	@Override
	public boolean win(){
		if(!climbmode||Dungeon.active!=null) return false;
		Javelin.app.switchScreen(WorldScreen.current);
		String congrats="You have returned the McGuffin to safety! Everyone starts clapping as you walk into the sunset...\n";
		congrats+="Congratulations, you've won! \\o/";
		Javelin.message(congrats,true);
		return true;
	}

	/**
	 * @return {@link DungeonDelveGenerator#dungeons}.
	 */
	public static List<Dungeon> getdungeons(){
		var dungeonDelveGenerator=(DungeonDelveGenerator)World
				.getseed().featuregenerator;
		return dungeonDelveGenerator.dungeons;
	}

	@Override
	public Item openspecialchest(){
		return new Ruby();
	}

	@Override
	public void endturn(){
		super.endturn();
		if(Dungeon.active==null) return;
		var mcguffin=Squad.active.equipment.get(McGuffin.class);
		if(!climbmode)
			climbmode=mcguffin!=null;
		else if(mcguffin==null){
			Javelin.app.switchScreen(WorldScreen.current);
			String fail="You have lost the McGuffin? Oh my...\n";
			fail+="Better luck next time!";
			Javelin.message(fail,true);
			StateManager.clear();
			System.exit(0);
		}
	}

	/** @return The active Dungeon Delve {@link Scenario}. */
	static public DungeonDelve get(){
		return (DungeonDelve)World.scenario;
	}

	@Override
	public void endday(){
		//nothing
	}
}

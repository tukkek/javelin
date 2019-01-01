package javelin.controller.scenario.dungeonworld;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.scenario.Campaign;
import javelin.controller.scenario.Scenario;
import javelin.model.item.Item;
import javelin.model.item.Ruby;
import javelin.model.item.key.TempleKey;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.old.RPG;

/**
 * Game mode which provides a JRPG like experience.
 *
 * TODO make depend on {@link Scenario}
 *
 * @author alex
 */
public class DungeonWorld extends Campaign{
	/** Constructor. */
	public DungeonWorld(){
		size=size*2;
		lockedtemples=false;
		minigames=false;
		record=false;
		respawnlocations=false;
		expiredungeons=true;
		worldencounters=false;
		worldhazards=false;
		helpfile="Dungeon world";
		spawn=false;
		labormodifier=0;
		featuregenerator=ZoneGenerator.class;
		worldgenerator=DungeonWorldGenerator.class;
		districtmodifier=1;
		crossrivers=false;
		quests=false;
		diplomacy=false;
		urbanevents=false;
	}

	@Override
	public boolean win(){
		for(Dungeon d:Dungeon.getdungeons())
			if(d.gettier()==DungeonTier.HIGHEST) return false;
		String success="You have cleared all major dungeons! Congratulations!";
		Javelin.message(success,true);
		return true;
	}

	@Override
	public Item openspecialchest(){
		return new Ruby();
	}

	@Override
	public Item openaltar(Temple t){
		return new TempleKey(t.realm);
	}

	@Override
	public void endday(){
		if(Debug.disablecombat) return;
		for(Squad s:Squad.getsquads())
			if(s.getdistrict()==null&&RPG.chancein(7)){
				Squad.active=s;
				throw new StartBattle(new DungeonWorldFight());
			}
	}
}

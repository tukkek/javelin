package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.controller.action.world.WorldMove;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.labor.basic.Lodge;
import javelin.old.RPG;

/**
 * Representes a semi-permanent safe place to rest inside a {@link Dungeon}. May
 * not last forever and total safety isn't 100% guaranteed!
 *
 * @author alex
 */
public class Campfire extends Feature{
	static final String PROMPT="This room seems safe to rest in. Do you want to set up camp?\n"
			+"Press ENTER to camp, any other key to cancel...";

	/** Constructor. */
	public Campfire(){
		super("dungeoncampfire");
		remove=false;
	}

	@Override
	public boolean activate(){
		if(Javelin.prompt(PROMPT)!='\n') return false;
		WorldMove.abort=true;
		if(RPG.chancein(20)){
			remove();
			Javelin.message("This safe resting spot has been compromised!",true);
			throw new StartBattle(Dungeon.active.fight());
		}
		Lodge.rest(1,8,true,Lodge.LODGE);
		return true;
	}
}

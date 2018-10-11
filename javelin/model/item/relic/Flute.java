package javelin.model.item.relic;

import javelin.Javelin;
import javelin.model.transport.FlyingNimbus;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.screen.BattleScreen;

/**
 * Summons a {@link FlyingNimbus}.
 *
 * @author alex
 */
public class Flute extends Relic{
	/** Constructor. */
	public Flute(Integer level){
		super("Flute of Wind",level);
		usedinbattle=false;
		usedoutofbattle=true;
	}

	@Override
	protected boolean activate(Combatant user){
		if(Dungeon.active!=null){
			Javelin.app.switchScreen(BattleScreen.active);
			Javelin.message(
					"You play the flute but nothing happens. Try it outside next time!",
					false);
			return true;
		}
		Squad.active.transport=new FlyingNimbus(Squad.active.transport);
		Squad.active.updateavatar();
		Javelin.app.switchScreen(BattleScreen.active);
		Javelin.message("You are taken by a "+Squad.active.transport+"!",false);
		return true;
	}

}

package javelin.controller.fight.minigame.arena.building;

import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.fight.minigame.arena.Arena;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;

/**
 * Provides {@link Arena#gold}.
 *
 * @author alex
 */
public class ArenaMine extends ArenaFountain{
	/** Constructor. */
	public ArenaMine(){
		super("Mine","locationmine","locationmineempty","Click to gather gold.");
	}

	@Override
	protected String activate(Combatant current,List<Combatant> nearby){
		Arena f=Arena.get();
		float cr=1;
		Combatants crs=new Combatants(f.getallies());
		crs.addAll(f.getopponents());
		for(Combatant c:crs)
			if(c.source.cr>cr) cr=c.source.cr;
		int gold=Javelin.round(RewardCalculator.getgold(cr));
		f.gold+=gold;
		return "You receive $"+Javelin.format(gold)+"! You now have $"
				+Javelin.format(f.gold)+"!";
	}
}

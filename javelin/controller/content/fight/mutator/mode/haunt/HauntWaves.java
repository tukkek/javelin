package javelin.controller.content.fight.mutator.mode.haunt;

import javelin.Javelin;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.mutator.mode.Waves;
import javelin.controller.exception.GaveUp;
import javelin.model.unit.Combatants;
import javelin.model.world.location.haunt.Haunt;

public class HauntWaves extends Waves{
	/**
	 *
	 */
	private final Haunt haunt;

	public HauntWaves(Haunt haunt){
		super(haunt.targetel,null);
		this.haunt=haunt;
	}

	@Override
	public Combatants generate(Fight f){
		try{
			return haunt.generatemonsters(waveel);
		}catch(GaveUp e){
			if(Javelin.DEBUG) throw new RuntimeException(e);
			return new Combatants();
		}
	}
}
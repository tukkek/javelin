package javelin.model.world.location.town.governor;

import java.util.ArrayList;

import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Growth;
import javelin.model.world.location.town.labor.Labor;
import tyrant.mikera.engine.RPG;

/**
 * Governor for hostile towns.
 * 
 * TODO here should go all the code for generating monster garissons and squads
 * 
 * @see Town#ishostile()
 * @author alex
 */
public class MonsterGovernor extends Governor {

	/** Constructor. */
	public MonsterGovernor(Town t) {
		super(t);
	}

	@Override
	public void manage() {
		System.out.println("implement mosnter manager!"); // TODO
		ArrayList<Labor> hand = new ArrayList<Labor>();
		for (Labor l : gethand()) {
			if (l.automatic) {
				hand.add(l);
			}
		}
		if (hand.isEmpty()) {
			hand.add(new Growth().generate(town));
		}
		for (Labor l : hand) {
			if (Growth.class.isInstance(l)) {
				l.start();
				return;
			}
		}
		RPG.pick(hand).start();
	}
}

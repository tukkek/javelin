package javelin.model.world.location.town.governor;

import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Growth;
import javelin.model.world.location.town.labor.Labor;

/**
 * Governor for hostile towns. Auto-manage is always on.
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
		// hand.get(0).start();
		for (Labor l : gethand()) {
			if (Growth.class.isInstance(l)) {
				l.start();
				return;
			}
		}
		// TODO
		System.out.println("implement mosnter manager!");
	}
}

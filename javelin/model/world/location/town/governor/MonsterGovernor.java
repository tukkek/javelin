package javelin.model.world.location.town.governor;

import javelin.model.world.location.town.Town;

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
		// TODO
		System.out.println("implement mosnter manager!");
	}
}

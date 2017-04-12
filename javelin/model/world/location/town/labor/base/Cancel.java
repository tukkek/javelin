package javelin.model.world.location.town.labor.base;

import java.util.ArrayList;

import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Labor;

public class Cancel extends Labor {

	public Cancel() {
		super("Cancel projects", 0, Rank.HAMLET);
		automatic = false;
	}

	@Override
	protected void define() {
		// nothing
	}

	@Override
	public void done() {
		for (Labor l : new ArrayList<Labor>(town.governor.getprojects())) {
			l.cancel();
		}
	}
}

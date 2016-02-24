package javelin.model.feat;

import javelin.model.unit.Monster;

/**
 * See the d20 SRD for more info.
 */
public class RapidShot extends Feat {

	public static final Feat SINGLETON = new RapidShot();

	public RapidShot() {
		super("Rapid shot");
	}

	@Override
	public void update(Monster m) {
		javelin.controller.upgrade.feat.RapidShot.update(m);
	}
}

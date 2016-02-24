package javelin.controller.upgrade.feat;

import javelin.model.unit.Monster;

/**
 * @see javelin.model.feat.LightningReflexes
 * @author alex
 */
public class LightningReflexes extends IronWill {
	public LightningReflexes() {
		super("Lightning reflexes",
				javelin.model.feat.LightningReflexes.singleton);
	}

	@Override
	public Integer getBonus(Monster m) {
		return m.ref;
	}

	@Override
	public void setBonus(Monster m, int value) {
		m.ref = value;
	}

	@Override
	protected String getname() {
		return "reflex";
	}
}

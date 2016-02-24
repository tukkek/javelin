package javelin.controller.upgrade.feat;

import javelin.model.unit.Monster;

/**
 * @se {@link javelin.model.feat.GreatFortitude}
 * @author alex
 */
public class GreatFortitude extends IronWill {
	public GreatFortitude() {
		super("Great fortitude", javelin.model.feat.GreatFortitude.singleton);
	}

	@Override
	public Integer getBonus(Monster m) {
		return m.fort;
	}

	@Override
	public void setBonus(Monster m, int value) {
		m.fort = value;
	}

	@Override
	protected String getname() {
		return "fortitude";
	}
}

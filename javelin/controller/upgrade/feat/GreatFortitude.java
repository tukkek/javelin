package javelin.controller.upgrade.feat;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @se {@link javelin.model.feat.save.GreatFortitude}
 * @author alex
 */
public class GreatFortitude extends IronWill {
	/** Constructor. */
	public GreatFortitude() {
		super("Great fortitude", javelin.model.feat.save.GreatFortitude.singleton);
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

	@Override
	public boolean apply(Combatant m) {
		return m.source.constitution > 0 && super.apply(m);
	}
}

package javelin.controller.upgrade.feat;

import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

public class IronWill extends FeatUpgrade {
	public IronWill() {
		super("Iron will", javelin.model.feat.IronWill.singleton);
	}

	public IronWill(String string, Feat singleton) {
		super(string, singleton);
	}

	@Override
	public String info(final Combatant m) {
		return "Current " + getname() + ": " + getBonus(m.source);
	}

	protected String getname() {
		return "will";
	}

	@Override
	public boolean apply(final Combatant m) {
		if (m.source.hasfeat(feat) > 0) {
			return false;
		}
		super.apply(m);
		setBonus(m.source, getBonus(m.source) + 2);
		return true;
	}

	public void setBonus(final Monster m, int value) {
		m.will = value;
	}

	public Integer getBonus(final Monster m) {
		return m.will;
	}

	@Override
	public boolean isstackable() {
		return false;
	}
}
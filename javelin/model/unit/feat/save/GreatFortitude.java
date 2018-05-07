package javelin.model.unit.feat.save;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.Feat;

/**
 * See the d20 SRD for more info.
 */
@SuppressWarnings("deprecation")
public class GreatFortitude extends SaveFeat {
	/** Unique instance of this {@link Feat}. */
	public static final Feat SINGLETON = new GreatFortitude();

	private GreatFortitude() {
		super("great fortitude");
	}

	@Override
	public Integer getbonus(Monster m) {
		return m.fort;
	}

	@Override
	public void setbonus(Monster m, int value) {
		m.fort = value;
	}

	@Override
	protected String getname() {
		return "fortitude";
	}

	@Override
	public boolean upgrade(Combatant m) {
		return m.source.constitution > 0 && super.upgrade(m);
	}
}

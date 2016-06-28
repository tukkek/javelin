package javelin.model.item;

import javelin.Javelin;
import javelin.controller.action.CastSpell;
import javelin.controller.upgrade.Spell;
import javelin.model.unit.Combatant;
import tyrant.mikera.engine.RPG;

/**
 * A wand is ideally a item that fires a ray. It has up to 50 charges and is
 * spent when empty.
 * 
 * @author alex
 */
public class Wand extends Item {
	int charges;
	int baseprice;
	Spell spell;
	boolean shop = false;

	/**
	 * Constructor.
	 * 
	 * @param s
	 *            Spell to be cast.
	 * @param upgradeset
	 */
	public Wand(Spell s) {
		super("Wand of " + s.name.toLowerCase(),
				s.level * s.casterlevel * 15 + s.components,
				s.realm.getitems());
		if (Javelin.DEBUG && s.level > 4) {
			throw new RuntimeException(
					"Cannot be a wand (level too high): " + s);
		}
		if (name.contains("ray of ")) {
			name = name.replace("ray of ", "");
		}
		spell = s;
		baseprice = price;
		recharge();
		usedinbattle = s.castinbattle;
		usedoutofbattle = s.castoutofbattle;
		apcost = 0;
	}

	/**
	 * Offer a new charge/price for this item instead of always having a fixed
	 * price and full charges.
	 */
	void recharge() {
		charges = shop ? 50 : RPG.r(1, 50);
		price = baseprice * charges;
	}

	@Override
	public Item clone() {
		Wand clone = (Wand) super.clone();
		recharge();
		return clone;
	}

	@Override
	public boolean use(Combatant user) {
		if (!decipher(user)) {
			return false;
		}
		CastSpell.singleton.cast(spell, user);
		return discharge();
	}

	boolean discharge() {
		charges -= 1;
		price = baseprice * charges;
		return charges == 0;
	}

	boolean decipher(Combatant user) {
		failure = null;
		if (user.source.usemagicdevice() >= 20) {
			return true;
		}
		if (user.source.decipher(spell)) {
			return true;
		}
		failure = "Cannot currently decipher this spell.";
		return false;
	}

	@Override
	public boolean usepeacefully(Combatant user) {
		if (!decipher(user)) {
			return false;
		}
		spell.castpeacefully(user);
		return discharge();
	}

	@Override
	public String toString() {
		return name + " [" + charges + "]";
	}

	@Override
	public String canuse(Combatant c) {
		if (c.source.decipher(spell)) {
			return null;
		}
		return "can't decipher";
	}

	@Override
	public void shop() {
		shop = true;
		recharge();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Wand) {
			return name.equals(((Wand) obj).name);
		}
		return false;
	}
}

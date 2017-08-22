package javelin.model.item;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.action.CastSpell;
import javelin.controller.upgrade.Spell;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import tyrant.mikera.engine.RPG;

/**
 * A wand is ideally a item that fires a ray. It has up to 50 charges and is
 * spent when empty.
 * 
 * @author alex
 */
public class Wand extends Item {
	public int charges;
	int baseprice;
	Spell spell;
	boolean shop = false;
	int maxcharges;

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
		apcost = s.apcost;
	}

	/**
	 * Offer a new charge/price for this item instead of always having a fixed
	 * price and full charges.
	 */
	void recharge() {
		maxcharges = shop ? 50 : RPG.r(1, 50);
		charges = maxcharges;
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
		Monster m = user.source;
		if (m.skills.usemagicdevice(m) >= 20) {
			return true;
		}
		if (m.skills.decipher(spell, m)) {
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
		return c.source.skills.decipher(spell, c.source) ? null
				: "can't decipher";
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

	@Override
	public String waste(float resourcesused, Combatant c, ArrayList<Item> bag) {
		if (canuse(c) != null) {
			return null;
		}
		int used = Math.round(maxcharges * resourcesused);
		if (used == 0) {
			return null;
		}
		used = Math.min(used, Math.max(1, maxcharges / 5));
		charges -= used;
		if (charges <= 0) {
			bag.remove(this);
			return "exhausted " + name.toLowerCase();
		}
		return name.toLowerCase() + " (" + used + " times)";
	}
}

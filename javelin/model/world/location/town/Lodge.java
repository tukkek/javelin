package javelin.model.world.location.town;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.upgrade.Spell;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.view.Images;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;

/***
 * Allows a {@link Squad} to rest outside of a {@link Town}.
 *
 * @author alex
 */
public class Lodge extends Fortification {
	public static final Lodging LODGE = new Lodging("lodge", 1, 0);
	public static final Lodging HOTEL = new Lodging("hotel", 2, .5f);
	public static final Lodging HOSPITAL = new Lodging("hospital", 4, 2);
	public static final Lodging[] LODGING = new Lodging[] { LODGE, HOTEL,
			HOSPITAL };

	public static class Lodging {
		String name;
		private float fee;
		int quality;

		public Lodging(String name, int quality, float fee) {
			super();
			this.name = name;
			this.fee = fee;
			this.quality = quality;
		}

		public int getfee() {
			return fee == 0 ? 0
					: Math.round(Math.max(1, Squad.active.eat() * fee));
		}
	}

	public static final String[] LEVELS = new String[] { "Traveller's lodge",
			"Hotel", "Hospital" };
	public static final String[] IMAGES = new String[] { "locationinn",
			"locationinnhotel", "locationinnhospital" };
	public static final int[] LABOR = new int[] { 5, 10, 15 };

	static final int RESTPERIOD = 8;
	static final int WEEKLONGREST = 24 * 7 / RESTPERIOD;
	static final int MAXLEVEL = LEVELS.length - 1;

	class UpgradeInn extends BuildingUpgrade {
		public UpgradeInn(Lodge i) {
			super(LEVELS[i.level + 1], LABOR[i.level + 1], 5, i,
					Rank.RANKS[i.level + 1]);
		}

		@Override
		public Location getgoal() {
			return previous;
		}

		@Override
		public void done(Location l) {
			Lodge i = (Lodge) l;
			if (i.level < MAXLEVEL) {
				i.level += 1;
				i.rename(LEVELS[i.level]);
			}
			super.done(l);
		}

		@Override
		public boolean validate(District d) {
			Lodge i = (Lodge) previous;
			return d != null && i.level < MAXLEVEL && super.validate(d);
		}
	}

	int level = 0;

	public Lodge() {
		super(LEVELS[0], LEVELS[0], 1, 5);
		gossip = true;
		neutral = true;
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		int price = LODGING[level].getfee();
		int weekprice = WEEKLONGREST * price;
		String s = "Do you want to rest at the " + LEVELS[level].toLowerCase()
				+ "?\n";
		s += "\nENTER or d to stay ($" + price + "), w to stay for a week ($"
				+ weekprice + ")";
		s += "\np to pillage ($" + SelectScreen.formatcost(getspoils()) + ")";
		s += "\nany other key to leave";
		Character input = Javelin.prompt(s);
		if (input == '\n' || input == 'd') {
			return rest(price, level + 1);
		}
		if (input == 'w') {
			return rest(weekprice, WEEKLONGREST * (level + 1));
		}
		if (input == 'p') {
			pillage();
			return true;
		}
		return false;
	}

	boolean rest(long price, int periods) {
		if (Squad.active.gold < price) {
			Javelin.message("You can't pay the $" + price + " fee!", false);
			return false;
		}
		Squad.active.gold -= price;
		Lodge.rest(periods, RESTPERIOD * periods, LODGING[level]);
		return true;
	}

	@Override
	protected void generate() {
		x = -1;
		while (x == -1 || getdistrict() != null || findnearest(Lodge.class)
				.distance(x, y) <= District.RADIUSMAX) {
			generateawayfromtown();
		}
	}

	@Override
	public List<Combatant> getcombatants() {
		return garrison;
	}

	@Override
	public ArrayList<Labor> getupgrades(District d) {
		ArrayList<Labor> upgrades = super.getupgrades(d);
		if (level < MAXLEVEL) {
			upgrades.add(new UpgradeInn(this));
		}
		return upgrades;
	}

	@Override
	public Image getimage() {
		return Images.getImage(IMAGES[level]);
	}

	/**
	 * @param restperiods
	 *            Normally 1 rest period equals to 8 hours of rest in normal
	 *            conditions.
	 * @param hours
	 *            Number of hours elapsed.
	 * @param accomodation
	 *            Level of the resting environment.
	 */
	public static void rest(int restperiods, long hours, Lodging a) {
		for (final Combatant c : Squad.active.members) {
			int heal = c.source.hd.count() * restperiods;
			if (!a.equals(HOSPITAL) && c.heal() >= 15) {
				heal *= 2;
			}
			if (heal < 1) {
				heal = 1;
			}
			c.hp += heal;
			if (c.hp > c.maxhp) {
				c.hp = c.maxhp;
			}
			for (Spell p : c.spells) {
				p.used = 0;
			}
			if (c.source.poison > 0) {
				int detox = restperiods == 1 ? RPG.r(0, 1) : restperiods / 2;
				c.detox(Math.min(c.source.poison, detox));
			}
			c.terminateconditions((int) hours);
		}
		Squad.active.hourselapsed += hours;
	}
}

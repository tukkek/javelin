package javelin.model.world.location.town;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.view.Images;
import javelin.view.screen.town.PurchaseScreen;

/***
 * Allows a {@link Squad} to rest outside of a {@link Town}.
 *
 * @author alex
 */
public class Inn extends Fortification {
	public static final String[] LEVELS = new String[] { "Traveller's lodge",
			"Hotel", "Hospital" };
	public static final String[] IMAGES = new String[] { "locationinn",
			"locationinnhotel", "locationinnhospital" };
	public static final int[] LABOR = new int[] { 5, 10, 15 };

	static final int RESTPERIOD = 8;
	static final int WEEKLONGREST = 24 * 7 / RESTPERIOD;
	static final int MAXLEVEL = LEVELS.length - 1;

	class UpgradeInn extends BuildingUpgrade {
		public UpgradeInn(Inn i) {
			super(LEVELS[i.level + 1], LABOR[i.level + 1], 5, i);
		}

		@Override
		public Location getgoal() {
			return previous;
		}

		@Override
		public void done(WorldActor l) {
			Inn i = (Inn) l;
			i.level += 1;
			i.rename(LEVELS[i.level]);
			super.done(l);
		}

		@Override
		public boolean validate(District d) {
			Inn i = (Inn) previous;
			return d != null && i.level < MAXLEVEL
					&& d.town.getrank() - 1 > i.level && super.validate(d);
		}
	}

	int level = 0;

	public Inn() {
		super(LEVELS[0], LEVELS[0], 1, 5);
		gossip = true;
		neutral = true;
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		int price = Math.round(Math.round(Math.ceil(Squad.active.eat())));
		int weekprice = WEEKLONGREST * price;
		// UpgradeInn upgrade = new UpgradeInn(this);
		// District d = getdistrict();
		// if (!upgrade.validate(d)) {
		// upgrade = null;
		// }
		String prompt = "Do you want to rest at the "
				+ LEVELS[level].toLowerCase() + "?\n";
		prompt += "\nENTER to stay ($" + price + "), w to stay for a week ($"
				+ weekprice + ")";
		prompt += "\np to pillage ($" + PurchaseScreen.formatcost(getspoils())
				+ ")";
		// if (upgrade != null) {
		// int upgradeto = level + 1;
		// prompt += " or u to upgrade to a " + LEVELS[upgradeto].toLowerCase()
		// + " (" + LABOR[upgradeto] + " labor)";
		// }
		prompt += "\nany other key to leave";
		Character input = Javelin.prompt(prompt);
		if (input == '\n') {
			return rest(price, level + 1);
		}
		if (input == 'w') {
			return rest(weekprice, WEEKLONGREST * (level + 1));
		}
		if (input == 'p') {
			pillage();
			return true;
		}
		// if (upgrade != null && input == 'u') {
		// upgrade.start(d.town);
		// return true;
		// }
		return false;
	}

	boolean rest(long price, int periods) {
		if (Squad.active.gold < price) {
			Javelin.message("You can't pay the $" + price + " fee!", false);
			return false;
		}
		Squad.active.gold -= price;
		Town.rest(periods, RESTPERIOD * periods, Accommodations.LODGE);
		return true;
	}

	@Override
	protected void generate() {
		x = -1;
		while (x == -1 || getdistrict() != null) {
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
}

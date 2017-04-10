package javelin.model.world.location.town.labor.religious;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javelin.controller.kit.Kit;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.model.world.location.unique.TrainingHall;
import javelin.view.Images;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.hiringacademy.HiringAcademy;
import javelin.view.screen.hiringacademy.HiringAcademyScreen;
import javelin.view.screen.upgrading.AcademyScreen;
import tyrant.mikera.engine.RPG;

public class Sanctuary extends Academy implements HiringAcademy {
	static final String DESCRIPTION = "Sanctuary";
	static final int UPGRADECOST = Kit.PALADIN.upgrades.size();
	static final ArrayList<Monster> CANDIDATES = TrainingHall.CANDIDATES;

	static {
		for (Monster m : new ArrayList<Monster>(CANDIDATES)) {
			if (Boolean.FALSE.equals(m.good)) {
				CANDIDATES.remove(m);
			}
		}
	}

	public static class BuildSanctuary extends BuildAcademy {
		public BuildSanctuary() {
			super(Rank.VILLAGE);
		}

		@Override
		protected Academy getacademy() {
			return new Sanctuary();
		}

	}

	public class UpǵradeSanctuary extends BuildingUpgrade {
		public UpǵradeSanctuary(Location previous) {
			super("Cathedral", UPGRADECOST, UPGRADECOST, previous, Rank.TOWN);
		}

		@Override
		public Location getgoal() {
			return previous;
		}

		@Override
		protected void done(Location goal) {
			super.done(goal);
			Sanctuary s = (Sanctuary) goal;
			s.upgrades.addAll(Kit.PALADIN.upgrades);
			s.upgraded = true;
			s.descriptionknown = s.descriptionunknown = "Cathedral";
			boolean forcepriest = false;
			boolean forcepaladin = false;
			while (!forcepriest && !forcepaladin) {
				forcepriest = RPG.chancein(2);
				forcepaladin = RPG.chancein(2);
			}
			s.generatehires(true, forcepriest, forcepaladin);
		}
	}

	boolean upgraded = false;
	Combatant acolyte = null;
	Combatant priest = null;
	Combatant paladin = null;

	public Sanctuary() {
		super(DESCRIPTION, DESCRIPTION, 1, 1, Kit.CLERIC.upgrades, null, null);
		minlevel = Math.max(1, upgrades.size() - 1);
		maxlevel = upgrades.size() + 1;
		generatehires(true, false, false);
	}

	@Override
	public ArrayList<Labor> getupgrades(District d) {
		ArrayList<Labor> upgrades = super.getupgrades(d);
		if (!upgraded) {
			upgrades.add(new UpǵradeSanctuary(this));
		}
		return upgrades;
	}

	@Override
	protected AcademyScreen getscreen() {
		return new HiringAcademyScreen(this);
	}

	@Override
	public Image getimage() {
		return upgraded ? Images.getImage("locationcathedral")
				: super.getimage();
	}

	void generatehires(boolean forceacolyte, boolean forcepriest,
			boolean forcepaladin) {
		if (acolyte == null && (forceacolyte || RPG.chancein(7))) {
			acolyte = generatehire("Acolyte", 1, 5, Kit.CLERIC);
		}
		if (upgraded && priest == null && (forcepriest || RPG.chancein(30))) {
			priest = generatehire("Priest", 6, 10, Kit.CLERIC);
		}
		if (upgraded && paladin == null && (forcepaladin || RPG.chancein(30))) {
			paladin = generatehire("Paladin", 6, 15, Kit.PALADIN);
		}
	}

	Combatant generatehire(String string, int i, int j, Kit cleric) {
		return generatehire(string, i, j, cleric, RPG.pick(CANDIDATES));
	}

	@Override
	public void turn(long time, WorldScreen world) {
		super.turn(time, world);
		generatehires(false, false, false);
	}

	public static Combatant generatehire(String title, int minlevel,
			int maxlevel, Kit k, Monster m) {
		Combatant c = new Combatant(m.clone(), true);
		int target = RPG.r(minlevel, maxlevel);
		int tries = target * 100;
		while (c.source.challengerating < target) {
			c.upgrade(k.upgrades);
			tries -= 1;
			if (tries == 0) {
				break;
			}
		}
		c.source.customName = title;
		return c;
	}

	@Override
	public List<Combatant> getcombatants() {
		List<Combatant> combatants = super.getcombatants();
		for (Combatant c : new Combatant[] { acolyte, priest, paladin }) {
			if (c != null) {
				combatants.add(c);
			}
		}
		return combatants;
	}

	@Override
	public boolean isworking() {
		if (super.isworking()) {
			return true;
		}
		if (acolyte == null) {
			return true;
		}
		return upgraded && priest == null && paladin == null;
	}

	@Override
	public Combatant[] gethires() {
		return new Combatant[] { acolyte, priest, paladin };
	}

	@Override
	public void clearhire(Combatant hire) {
		if (hire == acolyte) {
			acolyte = null;
		} else if (hire == priest) {
			priest = null;
		} else {
			paladin = null;
		}
	}
}

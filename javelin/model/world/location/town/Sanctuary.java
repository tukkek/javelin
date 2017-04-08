package javelin.model.world.location.town;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.controller.kit.Kit;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.model.world.location.unique.TrainingHall;
import javelin.view.Images;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;
import javelin.view.screen.upgrading.AcademyScreen;
import tyrant.mikera.engine.RPG;

public class Sanctuary extends Academy {
	public static class BuildSacntuary extends BuildAcademy {
		public BuildSacntuary() {
			super(Rank.VILLAGE);
		}

		@Override
		protected Academy getacademy() {
			return new Sanctuary();
		}

	}

	static final int UPGRADECOST = Kit.PALADIN.upgrades.size();
	static final Character[] HIREKEYS = new Character[] { 'x', 'y', 'z' };

	class Hire extends Option {
		Combatant hire;

		public Hire(Combatant hire) {
			super("Hire " + hire.toString().toLowerCase(), 0);
			this.hire = hire;
			name += " ($"
					+ SelectScreen.formatcost(MercenariesGuild.getfee(hire))
					+ "/day)";
		}
	}

	class SanctuaryScreen extends AcademyScreen {
		SanctuaryScreen(Sanctuary s, Town t) {
			super(s, t);
			showmoneyinfo = false;
		}

		@Override
		protected void sort(List<Option> options) {
			options.sort(new Comparator<Option>() {
				@Override
				public int compare(Option o1, Option o2) {
					boolean a = o1 instanceof Hire;
					boolean b = o2 instanceof Hire;
					if (a && !b) {
						return +1;
					}
					if (!a && b) {
						return -1;
					}
					return o1.name.compareTo(o2.name);
				}
			});
		}

		@Override
		public List<Option> getoptions() {
			List<Option> options = new ArrayList<Option>();
			if (acolyte != null) {
				options.add(new Hire(acolyte));
			}
			if (paladin != null) {
				options.add(new Hire(paladin));
			}
			if (priest != null) {
				options.add(new Hire(priest));
			}
			for (int i = 0; i < options.size(); i++) {
				options.get(i).key = HIREKEYS[i];

			}
			options.addAll(super.getoptions());
			return options;
		}

		@Override
		public boolean select(Option o) {
			if (o instanceof Hire) {
				Combatant hire = ((Hire) o).hire;
				if (!MercenariesGuild.recruit(hire, false)) {
					print(text
							+ "\nYou don't have enough money to pay today's advancement...\n");
					return false;
				}
				if (hire == acolyte) {
					acolyte = null;
				} else if (hire == priest) {
					priest = null;
				} else {
					paladin = null;
				}
				return true;
			}
			return super.select(o);
		}

		@Override
		public String printinfo() {
			return "Your squad has $"
					+ SelectScreen.formatcost(Squad.active.gold);
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

	static final String DESCRIPTION = "Sanctuary";
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
		return new SanctuaryScreen(this, null);
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

	@Override
	public void turn(long time, WorldScreen world) {
		super.turn(time, world);
		generatehires(false, false, false);
	}

	private Combatant generatehire(String title, int min, int max, Kit k) {
		Combatant c = new Combatant(RPG.pick(generatecandidates()).clone(),
				true);
		int target = RPG.r(min, max);
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

	public static ArrayList<Monster> generatecandidates() {
		ArrayList<Monster> candidates = TrainingHall.getcandidates();
		for (Monster m : new ArrayList<Monster>(candidates)) {
			if (Boolean.FALSE.equals(m.good)) {
				candidates.remove(m);
			}
		}
		return candidates;
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
}

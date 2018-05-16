package javelin.model.world.location.town.labor.basic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.old.Game;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

/**
 * Allows a player to recruit one type of unit.
 *
 * @author alex
 */
public class Dwelling extends Fortification {
	public static class BuildDwelling extends Build {
		public static final int CRMULTIPLIER = 1;

		Dwelling goal = null;

		public BuildDwelling() {
			super("Build dwelling", 0, null, Rank.HAMLET);
		}

		@Override
		public Location getgoal() {
			return goal;
		}

		@Override
		protected void define() {
			ArrayList<Monster> candidates = Dwelling.getcandidates(town.x,
					town.y);
			Collections.shuffle(candidates);
			for (Monster m : candidates) {
				if (m.cr <= town.population + 2) {
					goal = new Dwelling(m);
					name += ": " + m.toString().toLowerCase();
					cost = getcost(m);
					return;
				}
			}
		}

		public static int getcost(Monster m) {
			return Math.max(1, Math.round(m.cr * CRMULTIPLIER));
		}

		@Override
		public boolean validate(District d) {
			ArrayList<Location> dwellings = d.getlocationtype(Dwelling.class);
			for (Location l : dwellings) {
				if (((Dwelling) l).descriptionknown
						.equalsIgnoreCase(goal.descriptionknown)) {
					return false;
				}
			}
			double max = Math.floor(d.town.getrank().rank * 1.5f);
			return super.validate(d) && goal != null && dwellings.size() < max;
		}
	}

	public Combatant dweller;
	int volunteers = 1;

	/** Constructor. */
	public Dwelling() {
		this(null);
	}

	public Dwelling(Monster m) {
		super(null, null, 0, 0);
		if (m != null) {
			setdweller(m);
		}
		generate();
		generategarrison(0, 0);
		descriptionknown = dweller.toString() + " dwelling";
		descriptionunknown = "A dwelling";
		clear = false;
	}

	@Override
	protected void generategarrison(int minel, int maxel) {
		if (dweller == null) {
			setdweller(RPG.pick(getcandidates(x, y)));
		}
		gossip = dweller.source.intelligence > 8;
		garrison.addAll(RPG.pick(Organization.ENCOUNTERSBYMONSTER
				.get(dweller.source.name)).group);
		targetel = ChallengeCalculator.calculateel(garrison);
		generategarrison = false;
	}

	static public ArrayList<Monster> getcandidates(int x, int y) {
		ArrayList<Monster> candidates = new ArrayList<Monster>();
		monsters: for (Monster m : Javelin.ALLMONSTERS) {
			String terrain = Terrain.get(x, y).toString();
			if (m.getterrains().contains(terrain)) {
				candidates.add(m);
				continue monsters;
			}
		}
		return candidates;
	}

	void setdweller(Monster m) {
		dweller = new Combatant(m.clone(), true);
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		String monstertype = dweller.toString().toLowerCase();
		InfoScreen screen = new InfoScreen("");
		char choice = ' ';
		while (choice != 'q') {
			screen.print(prompt(monstertype));
			choice = InfoScreen.feedback();
			if (choice == 'p') {
				pillage();
				break;
			}
			if (volunteers == 0) {
				continue;
			}
			if (choice == 'h') {
				hire();
			} else if (choice == 'd') {
				draft(screen, monstertype);
			}
		}
		return true;
	}

	void draft(InfoScreen s, String monstertype) {
		int xp = Math.round(dweller.source.cr * 100);
		if (!canrecruit(xp)) {
			s.print("Cannot afford a " + monstertype + " (" + xp + "XP)...");
			Game.getInput();
			return;
		}
		spend(dweller.source.cr);
		Javelin.recruit(dweller.source.clone());
		volunteers -= 1;
	}

	void hire() {
		Combatant mercenary = new Combatant(dweller.source.clone(), true);
		if (MercenariesGuild.recruit(mercenary, true)) {
			volunteers -= 1;
		}
	}

	String prompt(String monstertype) {
		String text = "You enter a " + monstertype
				+ " dwelling. What do you want to do?\n\n";
		if (volunteers > 0) {
			text += "There are " + volunteers + " available units here.\n\n";
			text += "d - draft as volunteer ("
					+ Math.round(100 * dweller.source.cr)
					+ "XP)\n";
			text += "h - hire as " + monstertype + " mercenary ($"
					+ Javelin.format(MercenariesGuild.getfee(dweller))
					+ "/day)\n";
		} else {
			text += "There are currently no available units here.\n\n";
		}
		text += "p - pillage this dwelling ($"
				+ Javelin.format(getspoils()) + ")\n";
		text += "q - quit\n";
		text += "\nCurrent gold: $" + Javelin.format(Squad.active.gold)
				+ "\n";
		if (volunteers > 0) {
			text += "Current XP: " + Squad.active.sumxp() + "XP\n";
		}
		return text;
	}

	@Override
	public void turn(long time, WorldScreen world) {
		int cr = Math.max(1, Math.round(dweller.source.cr));
		int max = Math.max(1, 21 - cr);
		if (volunteers == max) {
			return;
		}
		if (RPG.r(getspawnrate(cr)) == 0) {
			volunteers += 1;
		}
	}

	/**
	 * @param cr
	 *            Given this challenge rating...
	 * @return a monster should spawn once every x days.
	 */
	public static int getspawnrate(float cr) {
		return Math.max(1, Math.round(cr * 100 / 20));
	}

	@Override
	public boolean isworking() {
		return volunteers == 0 && !ishostile();
	}

	@Override
	public List<Combatant> getcombatants() {
		return garrison;
	}

	/**
	 * @param price
	 *            Price in XP (100XP = 1CR).
	 * @return <code>true</code> if currently active {@link Squad} can afford
	 *         this much.
	 */
	static public boolean canrecruit(double price) {
		return price <= Squad.active.sumxp();
	}

	/**
	 * @param cr
	 *            Spend this much CR in recruiting a rookie (1CR = 100XP).
	 */
	static public void spend(double cr) {
		double percapita = cr / new Float(Squad.active.members.size());
		boolean buyfromall = true;
		for (Combatant c : Squad.active.members) {
			if (percapita > c.xp.doubleValue()) {
				buyfromall = false;
				break;
			}
		}
		if (buyfromall) {
			for (Combatant c : Squad.active.members) {
				c.xp = c.xp.subtract(new BigDecimal(percapita));
			}
		} else {
			ArrayList<Combatant> squad = new ArrayList<Combatant>(
					Squad.active.members);
			ChallengeCalculator.calculateel(squad);
			Collections.sort(squad, new Comparator<Combatant>() {
				@Override
				public int compare(Combatant o1, Combatant o2) {
					final float cr1 = o2.xp.floatValue()
							+ o2.source.cr;
					final float cr2 = o1.xp.floatValue()
							+ o1.source.cr;
					return new Float(cr1).compareTo(cr2);
				}
			});
			for (Combatant c : squad) {
				if (c.xp.doubleValue() >= cr) {
					c.xp = c.xp.subtract(new BigDecimal(cr));
					return;
				}
				cr -= c.xp.doubleValue();
				c.xp = new BigDecimal(0);
			}
		}
	}

	/**
	 * @param m
	 *            Given a monster...
	 * @return recruits into {@link Squad#active} and {@link #spend(double)} XP
	 *         if {@link #canrecruit(double)}.
	 */
	public static boolean recruit(Monster m) {
		if (!canrecruit(m.cr * 100)) {
			return false;
		}
		spend(m.cr);
		Javelin.recruit(m);
		return true;
	}
}

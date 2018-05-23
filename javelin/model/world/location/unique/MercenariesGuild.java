package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.CombatantByCr;
import javelin.controller.generator.NpcGenerator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Build;
import javelin.old.RPG;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;

/**
 * The **Mercenaries guild** allows a player to hire mercenaries, which are paid
 * a certain amount in gold per day.
 *
 * @see Combatant#mercenary
 * @author alex
 */
public class MercenariesGuild extends Fortification {
	private static final int STARTINGMERCENARIES = 9;
	static final boolean DEBUG = false;

	public static class BuildMercenariesGuild extends Build {
		public BuildMercenariesGuild() {
			super("Build mercenaries guild", 15, null, Rank.TOWN);
		}

		@Override
		public Location getgoal() {
			return new MercenariesGuild();
		}
	}

	/** Available mercenaries. */
	public ArrayList<Combatant> mercenaries = new ArrayList<>();
	/** All mercenaries. */
	public ArrayList<Combatant> all = new ArrayList<>();

	/** Constructor. */
	public MercenariesGuild() {
		super("Mercenaries' Guild", "Mercenaries' Guild", 11, 15);
		gossip = true;
		vision = 3;
		while (mercenaries.size() < STARTINGMERCENARIES) {
			generatemercenary();
		}
		if (DEBUG) {
			garrison.clear();
		}
	}

	void generatemercenary() {
		int cr = RPG.r(11, 20);
		List<Monster> candidates = new ArrayList<>();
		for (Float tier : Javelin.MONSTERSBYCR.keySet()) {
			if (cr / 2 <= tier && tier < cr) {
				for (Monster m : Javelin.MONSTERSBYCR.get(tier)) {
					if (m.think(-1) && m.humanoid) {
						candidates.add(m);
					}
				}
			}
		}
		Monster m = RPG.pick(candidates);
		Combatant c = NpcGenerator.generatenpc(m, cr);
		c.setmercenary(true);
		mercenaries.add(c);
		all.add(c);
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		ChallengeCalculator.updatecr(mercenaries);
		mercenaries.sort(Collections.reverseOrder(CombatantByCr.SINGLETON));
		ArrayList<String> prices = new ArrayList<>(mercenaries.size());
		for (Combatant c : mercenaries) {
			prices.add(c + " ($" + Javelin.format(getfee(c)) + ")");
		}
		int index = Javelin.choose(
				"\"Welcome to the guild! Do you want to hire one of our mercenaries for a modest daily fee?\"\n\nYou have $"
						+ Javelin.format(Squad.active.gold),
				prices, true, false);
		if (index == -1) {
			return true;
		}
		if (!recruit(mercenaries.get(index), true)) {
			return false;
		}
		mercenaries.remove(index);
		return true;
	}

	/**
	 * Pays for the rest of the day and adds to active {@link Squad}. If cannot
	 * pay warn the user.
	 *
	 * @param message
	 *            If <code>true</code> and doesn't have enough money will open
	 *            up a {@link InfoScreen} to let the player know. Use
	 *            <code>false</code> to warn in another manner.
	 * @return <code>false</code> if doesn't have enough money to pay in
	 *         advance.
	 */
	static public boolean recruit(Combatant combatant, boolean message) {
		long advance = Math.max(1, Javelin.getHour() * getfee(combatant) / 24);
		if (Squad.active.gold < advance) {
			if (message) {
				Javelin.app.switchScreen(new InfoScreen(
						"You don't have the money to pay today's advancement ($"
								+ Javelin.format(advance) + ")!"));
				Javelin.input();
			}
			return false;
		}
		combatant.setmercenary(true);
		Squad.active.gold -= advance;
		Squad.active.add(combatant);
		return true;
	}

	/** See {@link #getfee(Monster)}. */
	public static int getfee(Combatant c) {
		return getfee(c.source);
	}

	/**
	 * @return Daily fee for a mercenary, based on it's CR (single treasure
	 *         value).
	 */
	public static int getfee(Monster m) {
		float value = RewardCalculator
				.getgold(ChallengeCalculator.calculatecr(m));
		int roundto;
		if (value > 1000) {
			roundto = 1000;
		} else if (value > 100) {
			roundto = 100;
		} else if (value > 10) {
			roundto = 10;
		} else {
			roundto = 1;
		}
		int fee = Math.round(value / roundto);
		return fee * roundto;
	}

	/**
	 * @param returning
	 *            Returns a mercenary to {@link #mercenaries}.
	 */
	public void receive(Combatant returning) {
		if (all.contains(returning)) {
			mercenaries.add(returning);
		}
	}

	@Override
	public List<Combatant> getcombatants() {
		ArrayList<Combatant> combatants = new ArrayList<>(garrison);
		combatants.addAll(all);
		return combatants;
	}

	public static List<MercenariesGuild> getguilds() {
		ArrayList<Actor> all = World.getall(MercenariesGuild.class);
		ArrayList<MercenariesGuild> guilds = new ArrayList<>(all.size());
		for (Actor a : all) {
			guilds.add((MercenariesGuild) a);
		}
		return guilds;
	}

	public static void die(Combatant c) {
		for (MercenariesGuild g : getguilds()) {
			if (g.all.contains(c)) {
				g.all.remove(c);
			}
		}
	}

	@Override
	public void turn(long time, WorldScreen world) {
		super.turn(time, world);
		if (all.size() < STARTINGMERCENARIES && RPG.chancein(100)) {
			generatemercenary();
		}
	}

	/**
	 * @param c
	 *            Given a combatant
	 * @return its daily fee as in "$40/day".
	 */
	public static String getformattedfee(Combatant c) {
		return "$" + Javelin.format(getfee(c)) + "/day";
	}
}

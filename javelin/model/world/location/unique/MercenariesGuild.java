package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.old.Game;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.BuildUnique;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;

/**
 * The **Mercenaries guild** allows a player to hire mercenaries, which are paid
 * a certain amount in gold per day.
 *
 * @see Combatant#mercenary
 * @author alex
 */
public class MercenariesGuild extends UniqueLocation {
	private static final int STARTINGMERCENARIES = 9;
	static final boolean DEBUG = false;

	public static class BuildMercenariesGuild extends BuildUnique {
		public BuildMercenariesGuild() {
			super(15, new MercenariesGuild(), Rank.TOWN);
		}
	}

	/** Available mercenaries. */
	public ArrayList<Combatant> mercenaries = new ArrayList<Combatant>();
	/** All mercenaries. */
	public ArrayList<Combatant> all = new ArrayList<Combatant>();

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
		Combatant c = null;
		Realm r = Realm.random();
		while (c == null) {
			List<Monster> tier = Javelin.MONSTERSBYCR.get((float) RPG.r(1, cr));
			if (tier != null) {
				Monster m = RPG.pick(tier);
				if (!m.humanoid) {
					return;
				}
				c = new Combatant(m.clone(), true);
				c.mercenary = true;
				r.baptize(c);
				for (Combatant c2 : mercenaries) {
					if (c.toString().equals(c2.toString())) {
						return;
					}
				}
			}
		}
		int tries = 0;
		while (c.source.challengerating < cr) {
			c.upgrade(r);
			tries += 1;
			if (tries >= 100) {
				return;
			}
		}
		mercenaries.add(c);
		all.add(c);
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		Collections.sort(mercenaries, new Comparator<Combatant>() {
			@Override
			public int compare(Combatant o1, Combatant o2) {
				return new Float(
						ChallengeRatingCalculator.calculatecr(o2.source))
								.compareTo(ChallengeRatingCalculator
										.calculatecr(o1.source));
			}
		});
		ArrayList<String> prices = new ArrayList<String>(mercenaries.size());
		for (Combatant c : mercenaries) {
			prices.add(c + " ($" + SelectScreen.formatcost(getfee(c)) + ")");
		}
		int index = Javelin.choose(
				"\"Welcome to the guild! Do you want to hire one of our mercenaries for a modest daily fee?\"\n\nYou have $"
						+ SelectScreen.formatcost(Squad.active.gold),
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
								+ SelectScreen.formatcost(advance) + ")!"));
				Game.getInput();
			}
			return false;
		}
		combatant.setmercenary(true);
		Squad.active.gold -= advance;
		Squad.active.members.add(combatant);
		return true;
	}

	/**
	 * @return Daily fee for a mercenary, based on it's CR (single treasure
	 *         value).
	 */
	public static int getfee(Combatant c) {
		float value = RewardCalculator
				.getgold(ChallengeRatingCalculator.calculatecr(c.source));
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
		ArrayList<Combatant> combatants = new ArrayList<Combatant>(garrison);
		combatants.addAll(all);
		return combatants;
	}

	public static List<MercenariesGuild> getguilds() {
		ArrayList<Actor> all = World.getall(MercenariesGuild.class);
		ArrayList<MercenariesGuild> guilds = new ArrayList<MercenariesGuild>(
				all.size());
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
}

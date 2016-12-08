package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Attack;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Skills;
import javelin.model.unit.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.unique.AssassinsGuild;
import javelin.view.screen.Option;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;

/**
 * Lets a unit go into an enemy {@link Town} unnoticed and perform sabotage or
 * assassination. A failed attempt means death while successive attempts incur a
 * penalty.
 * 
 * TODO after {@link Town} redesign SABOTAGE was removed. Does this make this
 * entire action less interesting?
 * 
 * @author alex
 */
public class Infiltrate extends WorldAction {
	static final boolean DEBUG = false;
	// static final Option SABOTAGE = new Option("Sabotage", 0, 's');
	static final Option ASSASSINATION = new Option("Assassination", 0, 'a');

	class SpyReport extends SelectScreen {
		boolean die = false;
		boolean exit = false;
		Combatant spy;

		public SpyReport(Combatant spy, Town target) {
			super("Report for " + target + ":", target);
			this.spy = spy;
			town.garrison.sort(new Comparator<Combatant>() {
				@Override
				public int compare(Combatant o1, Combatant o2) {
					return o1.toString().compareTo(o2.toString());
				}
			});
			stayopen = false;
		}

		@Override
		public String getCurrency() {
			return "";
		}

		@Override
		public String printpriceinfo(Option o) {
			return "";
		}

		@Override
		public String printinfo() {
			String info = "";

			// info += "Accomodation: " + town.lodging + "\n";
			// info += "Transportation: "
			// + (town.transport == null ? "none" : town.transport)
			// + "\n\n";

			info += "Troops: ";
			for (Combatant c : town.garrison) {
				info += c.toString().toLowerCase() + ", ";
			}
			info = info.substring(0, info.length() - 2) + "\n\n";

			// info += "Lairs: ";
			// for (RecruitOption c : town.lairs) {
			// info += c.m.toString().toLowerCase() + ", ";
			// }
			// info = info.substring(0, info.length() - 2) + "\n\n";

			// if (!town.items.isEmpty()) {
			// info += "Shop: ";
			// for (Item i : town.items) {
			// info += i.toString().toLowerCase() + ", ";
			// }
			// info = info.substring(0, info.length() - 2) + "\n\n";
			// }

			// if (!town.upgrades.isEmpty()) {
			// info += "Upgrades: ";
			// for (Upgrade u : town.upgrades) {
			// info += u.toString().toLowerCase() + ", ";
			// }
			// info = info.substring(0, info.length() - 2) + "\n\n";
			// }
			return info;
		}

		@Override
		public boolean select(Option o) {
			if (o == ASSASSINATION) {
				return assassinate();
			}
			// if (o == SABOTAGE) {
			// return sabotage();
			// }
			return false;
		}

		@Override
		public List<Option> getoptions() {
			ArrayList<Option> list = new ArrayList<Option>();
			if (!getassassinationtargets().isEmpty()) {
				list.add(ASSASSINATION);
			}
			// if (town.lairs.size() > 1) {
			// list.add(SABOTAGE);
			// }
			return list;
		}

		// public boolean sabotage() {
		// ArrayList<String> choices =
		// new ArrayList<String>(town.lairs.size());
		// for (RecruitOption m : town.lairs) {
		// choices.add(m.m + " (level " + Math.round(m.m.challengeRating)
		// + ")");
		// }
		// town.lairs.remove(Javelin.choose("Sabotage which lair?", choices,
		// true, true));
		// AssassinsGuild.notify(.5f);
		// print("Sabotage succesful!");
		// getInput();
		// return true;
		// }

		public boolean assassinate() {
			ArrayList<Combatant> targets = getassassinationtargets();
			Combatant target = targets.get(
					Javelin.choose("Assassinate who?", targets, true, true));
			if (RPG.r(1, 20) + target.source.skills.perceive(false, false,
					target.source) >= Skills.take10(spy.source.skills.stealth,
							spy.source.dexterity)) {
				die = true;
				return true;
			}
			Attack blow = spy.source.melee.get(0).get(0);
			if (RPG.r(1, 20) + target.source.fortitude() >= 10
					+ blow.getaveragedamage() * blow.multiplier) {
				die = true;
				return true;
			}
			town.garrison.remove(target);
			AssassinsGuild.notify(1f);
			print("Assassination succesful!");
			getInput();
			if (town.garrison.isEmpty()) {
				town.captureforhuman(true);
			}
			return true;
		}

		ArrayList<Combatant> getassassinationtargets() {
			ArrayList<Monster> preventdouble = new ArrayList<Monster>();
			ArrayList<Combatant> targets = new ArrayList<Combatant>();
			for (Combatant c : town.garrison) {
				if (!preventdouble.contains(c.source)
						&& !c.source.immunitytocritical
						&& c.source.fortitude() != Integer.MAX_VALUE) {
					targets.add(c);
					preventdouble.add(c.source);
				}
			}
			return targets;
		}

		@Override
		protected void proceed() {
			exit = true;
		}
	}

	/** Constructor. */
	public Infiltrate() {
		super("Infiltrate enemy town", new int[] { 'I' }, new String[] { "I" });
	}

	@Override
	public void perform(WorldScreen screen) {
		Town target = null;
		for (WorldActor a : Town.getall(Town.class)) {
			Town t = (Town) a;
			if (t.ishostile() && t.isadjacent(Squad.active)) {
				target = t;
				break;
			}
		}
		if (target == null) {
			Javelin.message("No hostile town nearby!", false);
			return;
		}
		Combatant spy = selectspy(target);
		if (spy != null) {
			while (infiltrate(spy, target)) {
				if (!target.ishostile()) {
					return;
				}
			}
		}
	}

	boolean infiltrate(Combatant spy, Town target) {
		if (detect(spy, target)) {
			die(spy);
			return false;
		}
		SpyReport screen = new SpyReport(spy, target);
		screen.show();
		if (screen.die) {
			die(spy);
			return false;
		}
		return !screen.exit;
	}

	void die(Combatant spy) {
		Javelin.app.switchScreen(WorldScreen.active);
		Javelin.message(spy + " was discovered and executed!", true);
		Squad.active.remove(spy);
	}

	boolean detect(Combatant spy, Town target) {
		if (DEBUG) {
			return false;
		}
		for (Combatant c : target.garrison) {
			if (RPG.r(1, 20) + c.source.skills.perceive(false, false,
					c.source) >= spy.source.skills.disguise(spy.source)) {
				return true;
			}
		}
		return false;
	}

	Combatant selectspy(Town target) {
		ArrayList<Combatant> spies =
				new ArrayList<Combatant>(Squad.active.members);
		spies.sort(new Comparator<Combatant>() {
			@Override
			public int compare(Combatant o1, Combatant o2) {
				return o2.source.skills.disguise(o2.source)
						- o1.source.skills.disguise(o1.source);
			}
		});
		ArrayList<String> choices = new ArrayList<String>(spies.size());
		for (Combatant c : spies) {
			Monster m = c.source;
			choices.add(c + " (" + Skills.signed(m.skills.disable(m) - 10)
					+ " disguise, "
					+ Skills.signed(
							Skills.take10(m.skills.stealth, m.dexterity) - 10)
					+ " stealth)");
		}
		int choice =
				Javelin.choose("Who will infiltrate " + target.toString() + "?",
						choices, true, false);
		return choice >= 0 ? spies.get(choice) : null;
	}
}

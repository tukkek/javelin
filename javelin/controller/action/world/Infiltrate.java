package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * Lets a unit go into an enemy {@link Location} unnoticed and perform sabotage
 * or assassination. A failed attempt means death while successive attempts
 * incur a penalty.
 *
 * @author alex
 */
public class Infiltrate extends WorldAction {
	static final boolean DEBUG = false;

	/** Constructor. */
	public Infiltrate() {
		super("Infiltrate guarded location'", new int[] { 'I' },
				new String[] { "I" });
	}

	@Override
	public void perform(WorldScreen screen) {
		Location target = selecttarget();
		if (target != null) {
			infiltrate(selectspy(target), target);
		}
	}

	Location selecttarget() {
		ArrayList<Location> targets = new ArrayList<Location>();
		ArrayList<Actor> actors = World.getactors();
		for (int x = Squad.active.x - 1; x <= Squad.active.x + 1; x++) {
			for (int y = Squad.active.y - 1; y <= Squad.active.y + 1; y++) {
				Actor a = World.get(x, y, actors);
				Location l = a instanceof Location ? (Location) a : null;
				if (l != null && l.ishostile()) {
					targets.add(l);
				}
			}
		}
		if (targets.isEmpty()) {
			Javelin.message("No hostile locations nearby...", false);
			return null;
		}
		if (targets.size() == 1) {
			return targets.get(0);
		}
		int choice = Javelin.choose("Which location to infiltrate?", targets,
				true, false);
		if (choice == -1) {
			return null;
		}
		return targets.get(choice);
	}

	void infiltrate(Combatant spy, Location l) {
		if (detect(spy, l)) {
			die(spy);
			return;
		}
		boolean killed = false;
		while (true) {
			List<Combatant> targets = new ArrayList<Combatant>();
			String immune = getassassinationtargets(spy, l, targets);
			ArrayList<String> description = new ArrayList<String>(
					targets.size());
			for (Combatant c : targets) {
				description.add(c + " (difficulty: "
						+ Javelin.describedifficulty(calculatestealthdc(spy, c))
						+ ")");
			}
			int choice = Javelin.choose(
					(killed ? "Success!" : "Assassinate who?") + immune,
					description, true, false);
			if (choice < 0) {
				break;
			}
			Combatant target = targets.get(choice);
			if (!assassinate(spy, target, l)) {
				die(spy);
				break;
			}
			killed = true;
			if (l.garrison.isEmpty()) {
				break;
			}
		}
	}

	void die(Combatant spy) {
		Javelin.show(spy
				+ " was discovered and executed!\n\nPress ENTER to continue...");
		Squad.active.remove(spy);
	}

	boolean detect(Combatant spy, Location target) {
		return !roll(calculatedisguiseroll(spy, target)) && !DEBUG;
	}

	int calculatedisguiseroll(Combatant spy, Location target) {
		int dc = Integer.MIN_VALUE;
		for (Combatant c : target.garrison) {
			dc = Math.max(dc, RPG.r(1, 20) + perceive(c));
		}
		return dc - Skill.DISGUISE.getbonus(spy);
	}

	boolean roll(int dc) {
		int r = RPG.r(1, 20);
		if (r == 1) {
			return false;
		}
		if (r == 20) {
			return true;
		}
		return r >= dc;
	}

	int perceive(Combatant c) {
		return c.perceive(false, true, true);
	}

	Combatant selectspy(Location target) {
		ArrayList<Combatant> spies = new ArrayList<Combatant>(
				Squad.active.members);
		spies.sort(new Comparator<Combatant>() {
			@Override
			public int compare(Combatant a, Combatant b) {
				return Skill.DISGUISE.getbonus(a) - Skill.DISGUISE.getbonus(b);
			}
		});
		ArrayList<String> choices = new ArrayList<String>(spies.size());
		for (Combatant c : spies) {
			Monster m = c.source;
			choices.add(c + " (difficulty: " + Javelin.describedifficulty(
					calculatedisguiseroll(c, target)) + ")");
		}
		int choice = Javelin.choose(
				"Who will infiltrate the target: " + target.toString() + "?",
				choices, true, false);
		return choice >= 0 ? spies.get(choice) : null;
	}

	public boolean assassinate(Combatant spy, Combatant target, Location l) {
		int dc = calculatestealthdc(spy, target);
		if (!roll(dc)) {
			return false;
		}
		l.garrison.remove(target);
		if (l.garrison.isEmpty()) {
			l.capture();
		}
		return true;
	}

	int calculatestealthdc(Combatant spy, Combatant target) {
		return 10 + perceive(target) - Skill.STEALTH.getbonus(spy);
	}

	String getassassinationtargets(Combatant spy, Location l,
			List<Combatant> targets) {
		Attack blow = spy.source.melee.get(0).get(0);
		int damage = blow.getaveragedamage() * blow.multiplier;
		String s = "";
		for (Combatant c : l.garrison) {
			if (kill(damage, c) && !c.source.immunitytocritical) {
				targets.add(c);
			} else {
				s += c + ", ";
			}
		}
		if (s.isEmpty()) {
			return "";
		}
		return "\n\n"
				+ "The following targets are either immune to assassinations "
				+ "or too buff for your assassin to kill them in one blow:\n"
				+ s.substring(0, s.length() - 2) + "\n";
	}

	private boolean kill(int damage, Combatant target) {
		if (damage >= target.hp) {
			return true;
		}
		int fort = target.source.fortitude();
		return fort != Integer.MAX_VALUE && damage > fort;
	}
}

package javelin.model.world.location.fortification;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.transport.Transport;
import javelin.model.world.WorldActor;
import javelin.model.world.location.town.TrainingOrder;
import javelin.view.screen.upgrading.AcademyScreen;
import javelin.view.screen.upgrading.UpgradingScreen;

/**
 * A place where units can go to learn about a general topic - be it physical
 * feats or intellectual or magical prowess.
 * 
 * @author alex
 */
public abstract class Academy extends Fortification {
	/** Currently training unit. */
	public TrainingOrder training = null;
	/** Money {@link #training} unit had before entering here (if alone). */
	public int stash;
	/** Upgrades that can be learned here. */
	public ArrayList<Upgrade> upgrades;
	public boolean pillage = true;
	/** If a single unit parks with a vehicle here it is parked. */
	public Transport parking = null;

	/**
	 * See {@link Fortification#Fortification(String, String, int, int)}.
	 * 
	 * @param upgradesp
	 */
	public Academy(String descriptionknown, String descriptionunknown,
			int minlevel, int maxlevel, HashSet<Upgrade> upgradesp) {
		super(descriptionknown, descriptionunknown, minlevel, maxlevel);
		upgrades = new ArrayList<Upgrade>(upgradesp);
		sort(upgrades);
	}

	/**
	 * @param upgrades
	 *            {@link #upgrades}, to be sorted.
	 */
	protected void sort(ArrayList<Upgrade> upgrades) {
		upgrades.sort(new Comparator<Upgrade>() {
			@Override
			public int compare(Upgrade o1, Upgrade o2) {
				return o1.name.compareTo(o2.name);
			}
		});
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		if (training != null) {
			if (Squad.active.hourselapsed < training.completionat) {
				long daysleft = Math.round(Math.ceil(
						(training.completionat - Squad.active.hourselapsed)
								/ 24f));
				Game.messagepanel.clear();
				Game.message(training.trained + "will complete training in "
						+ daysleft + " day(s)...", null, Delay.NONE);
				Game.getInput();
			} else {
				completetraining();
			}
			return true;
		}
		new AcademyScreen(this, null).show();
		return true;
	}

	Squad completetraining() {
		Squad s = UpgradingScreen.completetraining(training, this,
				training.trained);
		s.gold += stash;
		stash = 0;
		if (parking != null) {
			if (s.transport == null || s.transport.price < parking.price) {
				s.transport = parking;
				s.updateavatar();
			}
			parking = null;
		}
		training = null;
		return s;
	}

	@Override
	public boolean hasupgraded() {
		return training != null
				&& Squad.active.hourselapsed >= training.completionat;
	}

	/**
	 * Normally {@link #training} units don't get out of the academy by
	 * themselves since this would mean being alone in the wild but if the game
	 * is about to be lost due to the absence of {@link Squad}s then the unit
	 * gets out to offer a fighting chance.
	 * 
	 * @return <code>false</code> if there was no unit in {@link #training}.
	 */
	public static boolean train() {
		boolean trained = false;
		for (WorldActor a : WorldActor.getall()) {
			if (a instanceof Academy) {
				Academy academy = (Academy) a;
				if (academy.training == null) {
					continue;
				}
				/* don't inline */
				long time = Math.max(academy.training.completionat,
						Squad.active.hourselapsed);
				academy.completetraining().hourselapsed = time;
				trained = true;
			}
		}
		return trained;
	}

	@Override
	public List<Combatant> getcombatants() {
		ArrayList<Combatant> combatants = new ArrayList<Combatant>(garrison);
		if (training != null) {
			combatants.add(training.untrained);
		}
		return combatants;
	}
}
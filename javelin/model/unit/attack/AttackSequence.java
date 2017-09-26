package javelin.model.unit.attack;

import javelin.model.unit.CloneableList;
import javelin.model.unit.feat.attack.PowerAttack;
import javelin.model.unit.feat.attack.shot.RapidShot;
import javelin.view.screen.StatisticsScreen;

/**
 * One of the possible mêlée or ranged full-attack options of a
 * {@link Combatant}.
 * 
 * @author alex
 */
public class AttackSequence extends CloneableList<Attack> {
	/** @see PowerAttack */
	public boolean powerful = false;
	/** @see RapidShot */
	public boolean rapid = false;

	@Override
	public String toString() {
		return toString(null);
	}

	@Override
	public AttackSequence clone() {
		return (AttackSequence) super.clone();
	}

	public String toString(Combatant target) {
		String line = "";
		for (Attack a : this) {
			line += StatisticsScreen.capitalize(a.toString(target)) + ", ";
		}
		return line.substring(0, line.length() - 2);
	}
}

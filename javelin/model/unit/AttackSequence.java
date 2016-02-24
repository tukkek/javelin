package javelin.model.unit;

import java.util.ArrayList;

import javelin.controller.upgrade.feat.RapidShot;
import javelin.view.screen.StatisticsScreen;

/**
 * One of the possible mêlée or ranged full-attack options of a
 * {@link Combatant}.
 * 
 * @author alex
 */
public class AttackSequence extends ArrayList<Attack> {
	public boolean powerful = false;
	/**
	 * See {@link RapidShot}
	 */
	public boolean rapid = false;

	@Override
	public String toString() {
		return toString(null);
	};

	@Override
	public AttackSequence clone() {
		final AttackSequence clone = (AttackSequence) super.clone();
		final int size = size();
		for (int i = 0; i < size; i++) {
			clone.set(i, get(i).clone());
		}
		return clone;
	}

	public String toString(Combatant target) {
		String line = "";
		for (Attack a : this) {
			line += StatisticsScreen.capitalize(a.toString(target)) + ", ";
		}
		return line.substring(0, line.length() - 2);
	}
}

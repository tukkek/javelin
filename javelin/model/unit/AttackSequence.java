package javelin.model.unit;

import java.util.ArrayList;

import javelin.view.screen.StatisticsScreen;

public class AttackSequence extends ArrayList<Attack> {
	@Override
	public String toString() {
		String line = "";
		for (Attack a : this) {
			line += StatisticsScreen.capitalize(a.toString()) + ", ";
		}
		return line.substring(0, line.length() - 2);
	};

	// @Override
	// public boolean equals(Object arg0) {
	// // if (!(arg0 instanceof AttackSequence)) {
	// // return false;
	// // }
	// final AttackSequence arg = (AttackSequence) arg0;
	// final int size = size();
	// if (size != arg.size()) {
	// return false;
	// }
	// for (int i = 0; i < size; i++) {
	// if (!get(i).equals(arg.get(i))) {
	// return false;
	// }
	// }
	// return true;
	// }

	@Override
	public AttackSequence clone() {
		final AttackSequence clone = (AttackSequence) super.clone();
		final int size = size();
		for (int i = 0; i < size; i++) {
			clone.set(i, get(i).clone());
		}
		return clone;
	}
}

package javelin.model.item.artifact;

import java.io.Serializable;
import java.util.ArrayList;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Complete adaptation from
 * http://www.d20srd.org/srd/magicItems/creatingMagicItems.htm#
 * bodySlotAffinities .
 *
 * Rings were added here as {@link Slot#FINGER}.
 *
 * @see Monster#humanoid
 *
 * @author alex
 */
public class Slot implements Serializable {
	/**
	 * Assumes heads and eyes are independent instead of worrying about complex
	 * scenarios.
	 *
	 * Valid example: a hat and lenses.
	 *
	 * Invalid example: A full-head helmet and googles.
	 */
	public static final Slot HEAD = new Slot(0);
	/** @see #HEAD */
	public static final Slot EYES = new Slot(1);

	/**
	 * Assuming that whatever covers your whole hands will force you to remove
	 * anything that is on your fingers. It's easy to imagine cloth gloves and
	 * rings being worn together for example but less so when it comes to full
	 * battle gloves so to simplify we supposed the "magical hand energies"
	 * would eliminate each other..
	 */
	public static final Slot HAND = new Slot(2) {
		@Override
		protected boolean conflicts(Slot slot) {
			return slot.equals(HAND) || slot.equals(FINGER);
		};
	};
	/**
	 * Only one finger on each hand is allowed at a time. Hand items also need
	 * to be removed.
	 */
	public static final Slot FINGER = new Slot(3) {
		@Override
		protected boolean conflicts(Slot slot) {
			return slot.equals(HAND);
		}

		@Override
		public void clear(Combatant c) {
			super.clear(c);
			int ringcount = 0;
			for (Artifact a : new ArrayList<Artifact>(c.equipped)) {
				if (a.slot.equals(FINGER)) {
					ringcount += 1;
					if (ringcount == 2) {
						a.remove(c);
					}
				}
			}
		};
	};

	public static final Slot COLLAR = new Slot(4);
	public static final Slot TORSO = new Slot(5);
	public static final Slot ARM = new Slot(6);
	public static final Slot WAIST = new Slot(7);
	public static final Slot FEET = new Slot(8);
	public static final Slot BACK = new Slot(9);

	final int id;

	public Slot(int id) {
		this.id = id;
	}

	/**
	 * To be called before equipping an {@link Artifact}.
	 *
	 * @see Artifact#usepeacefully(javelin.model.unit.Combatant)
	 * @param c
	 *            Removes all incompatible items from this unit.
	 */
	public void clear(Combatant c) {
		for (Artifact a : new ArrayList<Artifact>(c.equipped)) {
			if (conflicts(a.slot)) {
				c.equipped.remove(a);
				a.remove(c);
			}
		}
	}

	protected boolean conflicts(Slot slot) {
		return id == slot.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Slot)) {
			return false;
		}
		Slot s = (Slot) obj;
		return id == s.id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		if (equals(COLLAR)) {
			return "Collar";
		}
		if (equals(TORSO)) {
			return "Torso";
		}
		if (equals(ARM)) {
			return "Arm";
		}
		if (equals(WAIST)) {
			return "Waist";
		}
		if (equals(FEET)) {
			return "Feet";
		}
		if (equals(BACK)) {
			return "Back";
		}
		if (equals(HAND)) {
			return "Hand";
		}
		if (equals(FINGER)) {
			return "Finger";
		}
		if (equals(HEAD)) {
			return "Head";
		}
		if (equals(EYES)) {
			return "Eyes";
		}
		throw new RuntimeException("unknown slot #slot");
	}
}
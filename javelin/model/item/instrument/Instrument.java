package javelin.model.item.instrument;

import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.view.screen.town.PurchaseScreen;

/**
 * Currently just a helper class to help with designing costs.
 * 
 * Your average bard (assuming a charisma of 16) can take 10 on a perform roll
 * with a result of 16+level.
 * 
 * Easy DC: 10+level. Medium DC: 16+level. Hard DC: 22+level.
 * 
 * @author alex
 */
public class Instrument {
	static final int[] BARDCASTERLEVEL = new int[] { 1, 2, 4, 7, 10, 13, 16, 20,
			20, 20 };
	static final boolean ENABLEBARDLEVELS = false;

	int performdc;
	int cost;

	public Instrument(Spell s, int extraperformdc) {
		this(s.level, s.casterlevel, extraperformdc);
	}

	public Instrument(int level, int casterlevel, int extraperformdc) {
		performdc = 10 + getbardcasterlevel(level, casterlevel)
				+ extraperformdc;
		cost = calculatecost(level, casterlevel, extraperformdc);
	}

	/** TODO needs to be perform, not spellcraft. */
	public boolean checkifcanuse(Combatant c) {
		return c.source.skills.spellcraft + 20 > performdc;
	}

	static int calculatecost(Spell s, int extraperformdc) {
		return calculatecost(s.level,
				getbardcasterlevel(s.level, s.casterlevel), extraperformdc);
	}

	public static int getbardcasterlevel(int level, int casterlevel) {
		return ENABLEBARDLEVELS ? Math.max(casterlevel, BARDCASTERLEVEL[level])
				: level;
	}

	static int calculatecost(int level, int casterlevel, int extraperformdc) {
		return Math.round(
				level * casterlevel * 2000 * (.9f - extraperformdc * .01f));
	}

	public static void main(String[] args) {
		Instrument i = new Instrument(7, 13, 3);
		System.out.println(PurchaseScreen.formatcost(i.cost));
		System.out.println("DC" + i.performdc);
		// System.out.println("Caster level " + new Doom().casterlevel);
	}
}

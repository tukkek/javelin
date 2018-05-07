package javelin.view.screen.wish;

import java.math.BigDecimal;
import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Return an unit to it's original {@link Monster} form. Keep all the XP though
 * so it can be spent again.
 * 
 * @author alex
 */
public class Rebirth extends Wish {
	/**
	 * Constructor.
	 * 
	 * @param haxorScreen
	 */
	public Rebirth(String name, Character keyp, double price,
			boolean requirestargetp, WishScreen haxorScreen) {
		super(name, keyp, price, requirestargetp, haxorScreen);
	}

	@Override
	protected boolean wish(Combatant target) {
		ArrayList<Artifact> equipment = new ArrayList<Artifact>(
				target.equipped);
		for (Artifact a : equipment) {
			target.unequip(a);
		}
		ChallengeCalculator.calculatecr(target.source);
		Float originalcr = target.source.cr;
		target.spells.clear();
		String customname = target.source.customName;
		target.source = Javelin.getmonster(target.source.name);
		target.source.customName = customname;
		ChallengeCalculator.calculatecr(target.source);
		target.learn(originalcr - target.source.cr);
		if (target.xp.intValue() < 0) {
			target.xp = new BigDecimal(0);
		}
		for (Artifact a : equipment) {
			target.equip(a);
		}
		return true;
	}
}
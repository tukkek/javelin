package javelin.view.screen.haxor;

import java.math.BigDecimal;
import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Return an unit to it's original {@link Monster} form. Keep all the XP though
 * so it can be spent again.
 * 
 * @author alex
 */
public class Rebirth extends Hax {
	/** Constructor. */
	public Rebirth(String name, Character keyp, double price,
			boolean requirestargetp) {
		super(name, keyp, price, requirestargetp);
	}

	@Override
	protected boolean hack(Combatant target, HaxorScreen s) {
		ArrayList<Artifact> equipment = new ArrayList<Artifact>(
				target.equipped);
		for (Artifact a : equipment) {
			target.unequip(a);
		}
		ChallengeRatingCalculator.calculatecr(target.source);
		Float originalcr = target.source.challengerating;
		target.spells.clear();
		String customname = target.source.customName;
		target.source = Javelin.getmonster(target.source.name);
		target.source.customName = customname;
		ChallengeRatingCalculator.calculatecr(target.source);
		target.learn(originalcr - target.source.challengerating);
		if (target.xp.intValue() < 0) {
			target.xp = new BigDecimal(0);
		}
		for (Artifact a : equipment) {
			target.equip(a);
		}
		return true;
	}
}
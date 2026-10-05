/**
 *
 */
package javelin.controller.challenge.factor;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.content.upgrade.classes.ClassLevelUpgrade;
import javelin.model.unit.Monster;

/**
 * Not actually used by {@link ChallengeCalculator} but an utility class to be
 * used elsewhere.
 *
 * This class always uses {@link Monster#cr} over {@link ClassLevelUpgrade}s
 * when determining an unit's "level".
 *
 * @author alex
 */
public class EquipmentFactor{
	static final float PC=.2f;
	static final float NPC=.125f;

	/** @return Portion of {@link Monster#cr} that should represent equipment. */
	public static final float getpcfactor(Monster m){
		return m.cr*PC;
	}

	/** @return Portion of {@link Monster#cr} that should represent equipment. */
	public static final float getnpcfactor(Monster m){
		return m.cr*NPC;
	}

	/** @return Ideal wealth for this unit. */
	public static final int getpcwealth(Monster m){
		return Math.round(m.cr*m.cr*m.cr*100);
	}

	/** @return Ideal wealth for this unit. */
	public static final int getnpcwealth(Monster m){
		return Math.round(m.cr*m.cr*m.cr*25);
	}
}

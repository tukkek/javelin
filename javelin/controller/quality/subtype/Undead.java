package javelin.controller.quality.subtype;

import javelin.controller.challenge.factor.AbilitiesFactor;
import javelin.controller.quality.NoHealing;
import javelin.controller.quality.Quality;
import javelin.controller.quality.resistance.CriticalImmunity;
import javelin.controller.quality.resistance.MindImmunity;
import javelin.controller.quality.resistance.ParalysisImmunity;
import javelin.controller.quality.resistance.PoisonImmunity;
import javelin.model.unit.Monster;

/**
 * Counts as a +0cr because it delegates to other qualities instead (some which
 * are set by {@link AbilitiesFactor}.
 *
 * One of the things that is missing from Javelin is turning undead, which would
 * be a -1.5cr disadvantage if it were implemented.
 *
 * @see PoisonImmunity
 * @see CriticalImmunity
 * @see ParalysisImmunity
 * @see MindImmunity
 * @see NoHealing
 *
 * @author alex
 */
public class Undead extends Quality{
	/** Constructor. */
	public Undead(){
		super("undead");
	}

	@Override
	public void add(String declaration,Monster m){
		m.vision=Monster.VISION_DARK;
		m.heal=false;
	}

	@Override
	public boolean has(Monster m){
		return m.type.equals("undead");
	}

	@Override
	public float rate(Monster m){
		/* see respective HD and Quality factors */
		return 0;
	}

	@Override
	public boolean apply(String attack,Monster m){
		return super.apply(attack,m)||has(m);
	}

}

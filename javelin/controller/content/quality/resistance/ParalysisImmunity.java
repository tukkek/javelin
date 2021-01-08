package javelin.controller.content.quality.resistance;

import javelin.controller.content.quality.Quality;
import javelin.controller.content.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Reader and {@link Upgrade} for paralysis immunity.
 *
 * @see Monster#getwill()
 */
public class ParalysisImmunity extends Quality{
	/** {@link Monster#cr} factor. */
	public static final float CR=0.1f;
	/** Applies {@link Monster#immunitytoparalysis}. */
	public static final Upgrade UPGRADE=new ParalysisImmunityUpgrade();

	static class ParalysisImmunityUpgrade extends Upgrade{
		ParalysisImmunityUpgrade(){
			super("Paralysis immunity");
		}

		@Override
		public String inform(Combatant c){
			return "";
		}

		@Override
		protected boolean apply(Combatant c){
			if(c.source.immunitytoparalysis) return false;
			c.source.immunitytoparalysis=true;
			return true;
		}

	}

	/** Constructor. */
	public ParalysisImmunity(){
		super("paralysis immunity");
	}

	@Override
	public void add(String declaration,Monster m){
		m.immunitytoparalysis=true;
	}

	@Override
	public boolean has(Monster monster){
		return monster.immunitytoparalysis;
	}

	@Override
	public float rate(Monster monster){
		return CR;
	}
}

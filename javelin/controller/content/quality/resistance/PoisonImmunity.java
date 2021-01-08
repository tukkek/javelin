package javelin.controller.content.quality.resistance;

import javelin.controller.content.quality.Quality;
import javelin.controller.content.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Reader and {@link Upgrade} for poison immunity.
 *
 * @see Monster#getwill()
 */
public class PoisonImmunity extends Quality{
	/** {@link Monster#cr} value. */
	public static final float CR=0.2f;
	/** Applies {@link Monster#immunitytopoison}. */
	public static final Upgrade UPGRADE=new PoisonImmunityUpgrade();

	static class PoisonImmunityUpgrade extends Upgrade{
		PoisonImmunityUpgrade(){
			super("Poison immunity");
		}

		@Override
		public String inform(Combatant c){
			return "";
		}

		@Override
		protected boolean apply(Combatant c){
			if(c.source.immunitytopoison) return false;
			c.source.immunitytopoison=true;
			return true;
		}

	}

	/** Constructor. */
	public PoisonImmunity(){
		super("poison immunity");
	}

	@Override
	public void add(String declaration,Monster m){
		m.immunitytopoison=true;
	}

	@Override
	public boolean has(Monster monster){
		return monster.immunitytopoison;
	}

	@Override
	public float rate(Monster monster){
		return CR;
	}
}

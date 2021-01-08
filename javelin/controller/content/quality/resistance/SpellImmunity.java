package javelin.controller.content.quality.resistance;

import javelin.controller.content.quality.Quality;
import javelin.controller.content.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * See more info on the d20 SRD.
 */
public class SpellImmunity extends Quality{
	/** Applies 100% {@link Monster#sr}. */
	public static final Upgrade UPGRADE=new SpellImmunityUpgrade();

	/**
	 * See the d20 SRD for more info.
	 */
	static class SpellImmunityUpgrade extends Upgrade{
		SpellImmunityUpgrade(){
			super("Spell immunity");
		}

		@Override
		public String inform(Combatant m){
			return "";
		}

		@Override
		public boolean apply(Combatant m){
			if(m.source.sr==Integer.MAX_VALUE) return false;
			m.source.sr=Integer.MAX_VALUE;
			return true;
		}

	}

	/** Constructor. */
	public SpellImmunity(){
		super("spell immunity");
	}

	@Override
	public void add(String declaration,Monster m){
		m.sr=Integer.MAX_VALUE;
	}

	@Override
	public boolean has(Monster m){
		return m.sr==Integer.MAX_VALUE;
	}

	@Override
	public float rate(Monster m){
		return 5;
	}

	@Override
	public boolean apply(String attack,Monster m){
		return super.apply(attack,m)||attack.equals("magic immunity");
	}
}

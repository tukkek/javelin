package javelin.controller.content.quality.resistance;

import javelin.controller.content.quality.Quality;
import javelin.controller.content.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * See more info on the d20 SRD.
 */
public class SpellResistance extends Quality{
	/** Applies {@link Monster#sr}. */
	public static final Upgrade UPGRADE=new SpellResistanceUpgrade();

	/**
	 * See the d20 SRD for more info.
	 */
	static class SpellResistanceUpgrade extends Upgrade{
		SpellResistanceUpgrade(){
			super("Spell resistance");
		}

		@Override
		public String inform(Combatant m){
			return "Current spell resistance is "+m.source.sr;
		}

		@Override
		public boolean apply(Combatant m){
			if(m.source.sr==Integer.MAX_VALUE) return false;
			// design parameter
			m.source.sr=m.source.hd.count()+12;
			if(m.source.sr<11) m.source.sr=11;
			return true;
		}

	}

	public SpellResistance(){
		super("sr ");
	}

	@Override
	public void add(String declaration,Monster m){
		if(m.sr!=Integer.MAX_VALUE){
			m.sr=Integer.parseInt(declaration.substring(declaration.indexOf(' ')+1));
			if(m.sr<=10) /* CR is given only for SR11+ */
				m.sr=11;
		}
	}

	@Override
	public boolean has(Monster monster){
		return 11<=monster.sr&&monster.sr<Integer.MAX_VALUE;
	}

	@Override
	public float rate(Monster monster){
		return (monster.sr-10)*.1f;
	}

	@Override
	public String describe(Monster m){
		return "spell resistance "+m.sr;
	}
}

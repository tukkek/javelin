package javelin.controller.content.quality.resistance;

import javelin.controller.content.quality.Quality;
import javelin.controller.content.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * This is not an official d20 SRD quality but represents the mind-effect
 * resistance many {@link Monster}s have.
 *
 * @see Monster#getwill()
 */
public class MindImmunity extends Quality{
	/** {@link Monster#cr} value. */
	public static final float CR=0.5f;
	/** Applies {@link Monster#immunitytomind}. */
	public static final Upgrade UPGRADE=new MindImmunityUpgrade();

	static class MindImmunityUpgrade extends Upgrade{
		MindImmunityUpgrade(){
			super("Mind immunity");
		}

		@Override
		public String inform(Combatant c){
			return "Current will: "+c.source.getwill();
		}

		@Override
		protected boolean apply(Combatant c){
			if(c.source.immunitytomind) return false;
			c.source.immunitytomind=true;
			return true;
		}

	}

	/** Constructor. */
	public MindImmunity(){
		super("mind immunity");
	}

	@Override
	public void add(String declaration,Monster m){
		m.immunitytomind=true;
	}

	@Override
	public boolean has(Monster monster){
		return monster.immunitytomind;
	}

	@Override
	public float rate(Monster monster){
		return CR;
	}
}

package javelin.controller.quality.resistance;

import javelin.controller.kit.Kit;
import javelin.controller.quality.Quality;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Applied to {@link Kit}s in the sense that some monsters should be able to
 * dodge mortal strikes, Matrix-style, not as intended in the SRD where it would
 * presume a monster without discernible or sensitive critical parts.
 *
 * @see Monster#immunitytocritical
 * @author alex
 */
public class CriticalImmunity extends Quality{
	/** CR factor for this quality. */
	public static final float CR=0.5f;
	/** Upgrade for this quality. */
	public static final CriticalImmunityUpgrade UPGRADE=new CriticalImmunityUpgrade();

	static class CriticalImmunityUpgrade extends Upgrade{
		CriticalImmunityUpgrade(){
			super("Critical immunity");
		}

		@Override
		public String inform(Combatant c){
			return "Not immune.";
		}

		@Override
		protected boolean apply(Combatant c){
			if(c.source.immunitytocritical) return false;
			c.source.immunitytocritical=true;
			return true;
		}

	}

	/** Constructor. */
	public CriticalImmunity(){
		super("Critical immunity");
	}

	@Override
	public void add(String declaration,Monster m){
		m.immunitytocritical=true;
	}

	@Override
	public boolean has(Monster m){
		return m.immunitytocritical;
	}

	@Override
	public float rate(Monster m){
		return CR;
	}
}

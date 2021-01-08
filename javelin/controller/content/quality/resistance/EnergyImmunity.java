package javelin.controller.content.quality.resistance;

import javelin.controller.content.quality.Quality;
import javelin.controller.content.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * This is a tricky one because since we're treating all energy types as the
 * same one, and this is a high-rated special quality at 1cr per energy type, it
 * means that any monster with a immunity immediately is at least level 5 -
 * including, for example, a Small Skeleton.
 *
 * Initially this was meant to flag {@link Monster#energyresistance} as
 * {@link Integer#MAX_VALUE} but to counter the problem above we're simply
 * adding the equivalent of 1CR as {@link EnergyResistance} instead.
 *
 * Units can still have true immunity to all energy types through the
 * {@link EnergyImmunityUpgrade}.
 *
 * @see EnergyResistance
 */
public class EnergyImmunity extends Quality{
	/** Applies {@link Monster#energyresistance}. */
	public static final Upgrade UPGRADE=new EnergyImmunityUpgrade();

	/**
	 * See the d20 SRD for more info.
	 */
	static class EnergyImmunityUpgrade extends Upgrade{

		public EnergyImmunityUpgrade(){
			super("Energy immunity");
		}

		@Override
		public String inform(Combatant m){
			return "";
		}

		@Override
		public boolean apply(Combatant m){
			if(m.source.energyresistance==Integer.MAX_VALUE) return false;
			m.source.energyresistance=Integer.MAX_VALUE;
			return true;
		}
	}

	public EnergyImmunity(){
		super("Energy immunity");
	}

	@Override
	public void add(String declaration,Monster m){
		for(String type:EnergyResistance.ENERGYTYPES)
			if(declaration.contains(type)){
				m.energyresistance+=Math.round(1/EnergyResistance.CR);
				return;
			}
	}

	@Override
	public boolean has(Monster monster){
		return monster.energyresistance==Integer.MAX_VALUE;
	}

	@Override
	public float rate(Monster monster){
		return 1*5;
	}

	@Override
	public boolean apply(String attack,Monster m){
		if(!attack.contains("immunity")) return false;
		return attack.contains("cold")||attack.contains("electricity")
				||attack.contains("fire")||attack.contains("acid");
	}
}

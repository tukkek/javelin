package javelin.controller.quality.resistance;

import javelin.controller.quality.Quality;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Tries to treat all energies as a single resistance.
 *
 * They should be .02CR but currently the game assume 1 point of energy
 * resistance is actually 5 (one for each type of energy resistance).
 *
 * @author alex
 * @see javelin.controller.quality.resistance.EnergyResistance#ENERGYTYPES
 * @see Monster#energyresistance
 */
public class EnergyResistance extends Quality{
	/**
	 * How much CR to add per point of energy resistance. Currently .02 (from CCR
	 * document) times 5 (since it applies to any of the five energy types).
	 */
	public static final float CR=.02f*5;
	/** All types of energy (currently being consolidated as "energy damage"). */
	public static final String[] ENERGYTYPES=new String[]{"cold","fire","acid",
			"electricity","sonic"};
	/** Applies {@link Monster#energyresistance}. */
	public static final Upgrade UPGRADE=new EnergyResistanceUpgrade();

	static final String[] BLACKLIST=new String[]{"turn resistance",
			"resistance to ranged attacks","charm resistance"};

	/**
	 * See the d20 SRD for more info.
	 */
	static class EnergyResistanceUpgrade extends Upgrade{
		EnergyResistanceUpgrade(){
			super("Energy resistance");
		}

		@Override
		public String inform(Combatant m){
			return "Currently resists "+m.source.energyresistance
					+" points of energy damage";
		}

		@Override
		public boolean apply(Combatant c){
			var m=c.source;
			if(m.energyresistance==Integer.MAX_VALUE
					||m.energyresistance+1>m.hd.count())
				return false;
			m.energyresistance+=1;
			return true;
		}

	}

	/** Constructor. */
	public EnergyResistance(){
		super("resistance");
	}

	@Override
	public void add(String declaration,Monster m){
		if(m.energyresistance==Integer.MAX_VALUE) return;
		declaration=declaration.toLowerCase();
		for(String ignore:BLACKLIST)
			if(declaration.contains(ignore)) return;
		float amount=Integer
				.parseInt(declaration.substring(declaration.lastIndexOf(' ')+1));
		float types=0;
		for(String type:ENERGYTYPES)
			if(declaration.contains(type)) types+=1f;
		m.energyresistance+=Math.round(amount*types/5f);
	}

	@Override
	public boolean has(Monster monster){
		return 0<monster.energyresistance
				&&monster.energyresistance<Integer.MAX_VALUE;
	}

	@Override
	public float rate(Monster monster){
		return monster.energyresistance*CR;
	}

	@Override
	public void listupgrades(UpgradeHandler handler){
	}

	@Override
	public String describe(Monster m){
		return "energy resistance "+m.energyresistance;
	}
}

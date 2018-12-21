package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureModerateWounds;
import javelin.model.unit.skill.Skill;

public class Cleric extends Kit{
	/**
	 * TODO might be intesteing to separate into Good and Evil clerc. Evil Cleric
	 * academies could only be buitl on criminal + religious cities.
	 */
	public static final Kit INSTANCE=new Cleric();

	private Cleric(){
		super("cleric",Aristocrat.SINGLETON,RaiseWisdom.SINGLETON,"Initiate",
				"Priest","Cleric","Pathiarch");
	}

	@Override
	protected void define(){
		basic.add(new CureModerateWounds());
		basic.add(Skill.KNOWLEDGE.getupgrade());
		basic.add(Skill.HEAL.getupgrade());
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.addAll(h.good);
		extension.addAll(h.schoolhealwounds);
		extension.addAll(h.schoolrestoration);
		extension.addAll(h.schooldivination);
		extension.addAll(h.schoolcompulsion);
	}

	@Override
	public boolean allow(int bestability,int secondbest,Monster m){
		if(!super.allow(bestability,secondbest,m)) return false;
		return !m.alignment.ischaotic()&&!m.alignment.isevil();
	}
}
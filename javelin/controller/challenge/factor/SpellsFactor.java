package javelin.controller.challenge.factor;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;

/**
 * TODO {@link Monster#spellcr}
 *
 * @see CrFactor
 */
public class SpellsFactor extends CrFactor{
	@Override
	public float calculate(Monster monster){
		return monster.spellcr;
	}

	@Override
	public void registerupgrades(UpgradeHandler handler){

	}

	@Override
	public String log(Monster m){
		String log="";
		if(m.spellcr!=0) log+="#spellbook "+m.spellcr+" ";
		if(!m.spells.isEmpty()) log+="#"+m.spells+" ";
		return log;
	}
}

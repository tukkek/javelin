package javelin.controller.kit;

import javelin.controller.kit.wizard.Conjurer;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.classes.Warrior;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureLightWounds;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bless;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.labor.religious.Sanctuary;

public class Paladin extends Kit{
	public static final Kit INSTANCE=new Paladin();

	Paladin(){
		super("paladin",Warrior.SINGLETON,RaiseCharisma.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(new CureLightWounds());
		basic.add(new Bless());
		basic.add(Skill.SENSEMOTIVE.getupgrade());
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.addAll(h.good);
		extension.addAll(h.magic);
		extension.addAll(Conjurer.HEALING);
	}

	@Override
	public boolean allow(int bestability,int secondbest,Monster m){
		return m.alignment.isgood()
				&&Cleric.INSTANCE.allow(bestability,secondbest,m);
	}

	@Override
	public Academy createguild(){
		var s=new Sanctuary();
		s.upgrade();
		return s;
	}
}
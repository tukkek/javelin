package javelin.controller.kit;

import javelin.controller.kit.wizard.Conjurer;
import javelin.controller.upgrade.NaturalArmor;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseConstitution;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.damage.MeleeDamage;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureLightWounds;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bless;
import javelin.model.unit.feat.CombatCasting;
import javelin.model.unit.feat.Toughness;
import javelin.model.unit.feat.attack.focus.MeleeFocus;
import javelin.model.unit.feat.save.GreatFortitude;
import javelin.model.unit.feat.save.IronWill;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.labor.religious.Sanctuary;

public class Paladin extends Kit{
	public static final Kit INSTANCE=new Paladin();

	Paladin(){
		super("paladin",Warrior.SINGLETON,RaiseCharisma.SINGLETON,
				RaiseConstitution.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(new CureLightWounds());
		basic.add(new Bless());
		basic.add(Skill.SENSEMOTIVE.getupgrade());
		basic.add(CombatCasting.SINGLETON.toupgrade());
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.addAll(h.good);
		extension.addAll(h.magic);
		extension.addAll(Conjurer.HEALING);
		extension.add(NaturalArmor.PLATES);
		extension.add(IronWill.SINGLETON.toupgrade());
		extension.add(MeleeFocus.UPGRADE.toupgrade());
		extension.add(MeleeDamage.INSTANCE);
		extension.add(Toughness.SINGLETON.toupgrade());
		extension.add(GreatFortitude.SINGLETON.toupgrade());
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
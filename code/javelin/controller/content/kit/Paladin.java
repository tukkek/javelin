package javelin.controller.content.kit;

import javelin.controller.content.kit.wizard.Conjurer;
import javelin.controller.content.quality.resistance.MindImmunity;
import javelin.controller.content.quality.resistance.PoisonImmunity;
import javelin.controller.content.quality.resistance.SpellResistance;
import javelin.controller.content.upgrade.NaturalArmor;
import javelin.controller.content.upgrade.ability.RaiseCharisma;
import javelin.controller.content.upgrade.ability.RaiseConstitution;
import javelin.controller.content.upgrade.classes.Warrior;
import javelin.controller.content.upgrade.damage.MeleeDamage;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureLightWounds;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bless;
import javelin.model.unit.feat.CombatCasting;
import javelin.model.unit.feat.Toughness;
import javelin.model.unit.feat.attack.focus.MeleeFocus;
import javelin.model.unit.feat.save.GreatFortitude;
import javelin.model.unit.feat.save.IronWill;
import javelin.model.unit.feat.skill.Alertness;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.academy.Academy;
import javelin.model.world.location.town.labor.religious.Sanctuary;

public class Paladin extends Kit{
	public static final Kit INSTANCE=new Paladin();

	Paladin(){
		super("Paladin",Warrior.SINGLETON,RaiseCharisma.SINGLETON,
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
	protected void extend(){
		extension.addAll(Conjurer.HEALING);
		extension.add(NaturalArmor.PLATES);
		extension.add(IronWill.SINGLETON.toupgrade());
		extension.add(MeleeFocus.UPGRADE.toupgrade());
		extension.add(MeleeDamage.INSTANCE);
		extension.add(Toughness.SINGLETON.toupgrade());
		extension.add(GreatFortitude.SINGLETON.toupgrade());
		extension.add(Alertness.SINGLETON.toupgrade());
		extension.add(MindImmunity.UPGRADE);
		extension.add(PoisonImmunity.UPGRADE);
		extension.add(SpellResistance.UPGRADE);
		extension.add(Skill.HEAL.getupgrade());
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
package javelin.controller.kit;

import javelin.controller.kit.wizard.Abjurer;
import javelin.controller.kit.wizard.Enchanter;
import javelin.controller.kit.wizard.Transmuter;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.feat.skill.Alertness;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.unique.AssassinsGuild;

public class Bard extends Kit{
	public static final Kit INSTANCE=new Bard();

	private Bard(){
		super("bard",Aristocrat.SINGLETON,RaiseCharisma.SINGLETON,
				RaiseIntelligence.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(Skill.DIPLOMACY.getupgrade());
		basic.add(Skill.KNOWLEDGE.getupgrade());
		basic.add(Skill.USEMAGICDEVICE.getupgrade());
		basic.add(Skill.BLUFF.getupgrade());
		basic.add(Skill.SENSEMOTIVE.getupgrade());
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.addAll(Transmuter.INSTANCE.filter(Spell.class));
		extension.addAll(Enchanter.INSTANCE.filter(Spell.class));
		extension.addAll(Abjurer.INSTANCE.filter(Spell.class));
		extension.add(Alertness.SINGLETON.toupgrade());
	}

	@Override
	public Academy createguild(){
		return new AssassinsGuild();
	}
}
package javelin.controller.kit;

import javelin.controller.kit.wizard.Abjurer;
import javelin.controller.kit.wizard.Enchanter;
import javelin.controller.kit.wizard.Transmuter;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.classes.Expert;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.unique.AssassinsGuild;

public class Bard extends Kit{
	public static final Kit INSTANCE=new Bard();

	private Bard(){
		super("bard",Expert.SINGLETON,RaiseCharisma.SINGLETON);
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
		extension.addAll(h.wind);
		extension.addAll(h.magic);
		extension.addAll(Transmuter.INSTANCE.getspells());
		extension.addAll(Enchanter.INSTANCE.getspells());
		extension.addAll(Abjurer.INSTANCE.getspells());
	}

	@Override
	public Academy createguild(){
		return new AssassinsGuild();
	}
}
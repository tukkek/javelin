package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.classes.Expert;
import javelin.model.unit.skill.Skill;

public class Bard extends Kit{
	public static final Kit INSTANCE=new Bard();

	private Bard(){
		super("bard",Expert.SINGLETON,RaiseCharisma.SINGLETON,"Joker","Minstrel",
				"Bard","Maestro");
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
		extension.addAll(h.schoolabjuration);
		extension.addAll(h.schooltotem);
		extension.addAll(h.schoolcompulsion);
	}
}
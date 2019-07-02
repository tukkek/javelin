package javelin.controller.kit;

import javelin.controller.kit.wizard.Enchanter;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.feat.skill.Alertness;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.Academy;
import javelin.model.world.location.town.labor.cultural.BardsGuild;

/**
 * a social expert, performer and often found in supportive and leadership
 * roles, well suited as a front-man and magic item user.
 *
 * TODO should be an instrument-based spell caster in the future. Update docs.
 *
 * @author alex
 *
 */
public class Bard extends Kit{
	/** Singleton. */
	public static final Kit INSTANCE=new Bard();

	private Bard(){
		super("Bard",Aristocrat.SINGLETON,RaiseCharisma.SINGLETON,
				RaiseIntelligence.SINGLETON);
	}

	@Override
	protected void define(){
		//let finish() transfer spells
	}

	@Override
	protected void extend(){
		extension.add(Skill.DIPLOMACY.getupgrade());
		extension.add(Skill.KNOWLEDGE.getupgrade());
		extension.add(Skill.USEMAGICDEVICE.getupgrade());
		extension.add(Skill.BLUFF.getupgrade());
		extension.add(Skill.SENSEMOTIVE.getupgrade());
		extension.addAll(Enchanter.INSTANCE.filter(Spell.class));
		extension.add(Alertness.SINGLETON.toupgrade());
		extension.add(Skill.PERCEPTION.getupgrade());
	}

	@Override
	public Academy createguild(){
		return new BardsGuild();
	}
}
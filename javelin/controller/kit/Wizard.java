package javelin.controller.kit;

import javelin.controller.upgrade.SkillUpgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.model.unit.abilities.spell.evocation.MagicMissile;
import javelin.model.unit.skill.Skill;

public class Wizard extends Kit {
	public static final Kit INSTANCE = new Wizard();

	private Wizard() {
		super("wizard", Aristocrat.SINGLETON, RaiseIntelligence.SINGLETON,
				"Trickster", "Adept", "Wizard", "Warlock");
	}

	@Override
	protected void define() {
		basic.add(new MagicMissile());
		basic.add(new SkillUpgrade(Skill.CONCENTRATION));
		basic.add(new SkillUpgrade(Skill.SPELLCRAFT));
	}

	@Override
	protected void extend(UpgradeHandler h) {
		extension.addAll(h.magic);
		extension.addAll(h.schoolabjuration);
		extension.addAll(h.schoolconjuration);
		extension.addAll(h.schoolevocation);
		extension.addAll(h.schooltransmutation);
		extension.addAll(h.schoolcompulsion);
		extension.addAll(h.schooltotem);
	}
}
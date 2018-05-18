package javelin.controller.kit;

import java.util.LinkedList;

import javelin.Javelin;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.upgrade.classes.Commoner;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.skill.Skill;
import tyrant.mikera.engine.RPG;

public class Druid extends Kit {
	public static final Kit INSTANCE = new Druid();

	private Druid() {
		super("druid", Commoner.SINGLETON, RaiseWisdom.SINGLETON, "Herder",
				"Elder", "Druid", "Archdruid");
	}

	@Override
	protected void define() {
		basic.add(new Summon("Small monstrous centipede", 1));
		basic.add(new Summon("Dire rat", 1));
		basic.add(new Summon("Eagle", 1));
		basic.add(Skill.SURVIVAL.getupgrade());
	}

	@Override
	protected void extend(UpgradeHandler h) {
		extension.addAll(h.earth);
		extension.addAll(h.fire);
		extension.addAll(h.water);
		extension.addAll(h.wind);
		extension.addAll(h.schoolhealwounds);
		extension.addAll(h.schooltotem);
		extension.addAll(h.schooltransmutation);
		extension.addAll(h.schooldivination);
		addsummons(extension.size());
	}

	void addsummons(int nsummons) {
		LinkedList<Monster> summons = new LinkedList<Monster>();
		summons.addAll(Javelin.getmonsterbytype("vermin"));
		summons.addAll(Javelin.getmonsterbytype("animal"));
		summons.addAll(Javelin.getmonsterbytype("fey"));
		summons.addAll(Javelin.getmonsterbytype("elemental"));
		for (int i = 0; i < nsummons && !summons.isEmpty(); i++) {
			Monster m = RPG.pick(summons);
			extension.add(new Summon(m.name));
		}
	}
}
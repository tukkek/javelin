package javelin.controller.fight;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.CrCalculator;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.model.Realm;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.Temple;

/**
 * {@link Temple} fights are different from normal {@link Dungeon} encounters
 * because the creatures are upgraded with {@link Upgrade}s from the respective
 * {@link Realm}.
 *
 * @author alex
 */
public class TempleEncounter extends RandomDungeonEncounter {
	Temple temple;

	/** Constructor. */
	public TempleEncounter(Temple temple) {
		super();
		this.temple = temple;
	}

	@Override
	public Integer getel(int teamel) {
		return temple.el + EncounterGenerator.getdifficulty();
	}

	@Override
	public void enhance(List<Combatant> foes) {
		while (CrCalculator.calculateel(foes) < temple.el) {
			Combatant.upgradeweakest(foes, temple.realm);
		}
	}

	@Override
	public boolean onend() {
		super.onend();
		Temple.leavingfight = true;
		return true;
	}

	@Override
	public boolean validate(ArrayList<Combatant> foes) {
		return temple.validate(foes);
	}

	@Override
	public ArrayList<Terrain> getterrains() {
		return temple.getterrains();
	}
}

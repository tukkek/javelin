package javelin.model.world.location.town.labor.military;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import javelin.controller.Calendar;
import javelin.controller.upgrade.skill.Knowledge;
import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.unique.TrainingHall;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.option.ScreenOption;
import tyrant.mikera.engine.RPG;

/**
 * TODO How to offer a defined upgrade path so that it doesn't become to
 * burdersome for players to learn? Also for upgrading NPCs? Here it should be
 * easy enough to have a {@link ScreenOption} that takes care of that but what
 * for NPCs?
 * 
 * @author alex
 */
public class DisciplineAcademy extends Academy {
	/** CR 5 mercenary. */
	Combatant initiate = null;
	/** CR 10 mercenary. */
	Combatant disciple = null;
	/** CR 15 mercenary. */
	Combatant master = null;

	public DisciplineAcademy(Discipline d) {
		super(d.name + " academy", null, 5, 15, Collections.EMPTY_SET,
				d.abilityupgrade, d.classupgrade);
		descriptionunknown = descriptionknown;
		upgrades.add(d.skillupgrade);
		upgrades.add(Knowledge.SINGLETON);
	}

	@Override
	public List<Combatant> getcombatants() {
		List<Combatant> combatants = super.getcombatants();
		for (Combatant c : new Combatant[] { initiate, disciple, master }) {
			if (c != null) {
				combatants.add(c);
			}
		}
		return combatants;
	}

	@Override
	public void turn(long time, WorldScreen world) {
		super.turn(time, world);
		initiate = train(initiate, Calendar.MONTH, 5);
		disciple = train(disciple, Calendar.SEASON, 10);
		master = train(master, Calendar.YEAR, 15);
	}

	Combatant train(Combatant rank, int period, int level) {
		if (rank != null || !RPG.chancein(period)) {
			return rank;
		}
		Combatant c = new Combatant(RPG.pick(TrainingHall.CANDIDATES), true);
		c.xp = new BigDecimal(level * 100);
		return train(c);
	}

	private Combatant train(Combatant c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getlabor() {
		return 10;
	}
}

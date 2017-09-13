package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.skill.Heal;
import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.unit.abilities.discipline.Maneuver;

public class SteelSerpent extends Discipline {
	public static final Discipline INSTANCE = new SteelSerpent();

	static final Maneuver[] MANEUVERS = new Maneuver[] {
			new DizzyingVenomPrana(), new IronFang(),
			new SickeningVenomStrike(), new StingOfTheAdder(),
			new StingOfTheAsp(), new StingOfTheRattler(), new TearingFang(),
			new WeakeningVenomPrana() };

	public SteelSerpent() {
		super("Steel serpent", Warrior.SINGLETON, RaiseWisdom.SINGLETON,
				Heal.SINGLETON);
	}

	@Override
	protected Maneuver[] getmaneuvers() {
		return MANEUVERS;
	}
}

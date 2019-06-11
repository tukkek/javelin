package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.unit.abilities.discipline.Maneuver;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.town.labor.military.DisciplineAcademy.BuildDisciplineAcademy;

public class SteelSerpent extends Discipline{
	public static final Maneuver[] MANEUVERS=new Maneuver[]{
			new DizzyingVenomPrana(),new IronFang(),new SickeningVenomStrike(),
			new StingOfTheAdder(),new StingOfTheAsp(),new StingOfTheRattler(),
			new TearingFang(),new WeakeningVenomPrana()};
	public static final Discipline INSTANCE=new SteelSerpent();
	public static final BuildDisciplineAcademy LABOR=INSTANCE.buildacademy();

	public SteelSerpent(){
		super("Steel serpent","Steel serpent",RaiseWisdom.SINGLETON,Skill.HEAL);
	}

	@Override
	protected Maneuver[] getmaneuvers(){
		return MANEUVERS;
	}
}

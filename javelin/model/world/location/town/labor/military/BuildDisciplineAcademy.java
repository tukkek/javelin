package javelin.model.world.location.town.labor.military;

import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.Trait;
import javelin.model.world.location.town.labor.military.Academy.BuildAcademies;

/**
 * TODO use one per {@link Discipline} so the {@link Labor}s don't have to
 * be all at one {@link Trait}.
 */
public class BuildDisciplineAcademy extends BuildAcademies {
	Discipline d;

	public BuildDisciplineAcademy(Discipline d) {
		super(Rank.VILLAGE);
		this.d = d;
	}

	@Override
	protected Academy generateacademy() {
		return d.generateacademy();
	}
}
package javelin.model;

import java.util.List;

import javelin.model.unit.Combatant;

/**
 * Anything that has blue and red opposing teams.
 * 
 * @author alex
 */
public interface TeamContainer {
	List<Combatant> getblueTeam();

	List<Combatant> getredTeam();

	List<Combatant> getcombatants();
}

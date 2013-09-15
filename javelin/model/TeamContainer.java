package javelin.model;

import java.util.List;

import javelin.model.unit.Combatant;

public interface TeamContainer {
	TeamContainer DEFAULT = new TeamContainer() {
		@Override
		public List<Combatant> getRedTeam() {
			return BattleMap.redTeam;
		}

		@Override
		public List<Combatant> getBlueTeam() {
			return BattleMap.blueTeam;
		}

		@Override
		public List<Combatant> getCombatants() {
			return BattleMap.combatants;
		}
	};

	List<Combatant> getBlueTeam();

	List<Combatant> getRedTeam();

	List<Combatant> getCombatants();
}

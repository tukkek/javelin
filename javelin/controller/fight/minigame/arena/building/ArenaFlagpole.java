package javelin.controller.fight.minigame.arena.building;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.fight.Fight;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.battle.action.BattleMouseAction;

public class ArenaFlagpole extends ArenaBuilding{
	public ArenaFlagpole(){
		super("Flagpole","flagpolered","Flagpole.");
	}

	@Override
	protected void upgradebuilding(){
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean click(Combatant current){
		throw new UnsupportedOperationException();
	}

	static public List<ArenaFlagpole> getflags(){
		return Fight.state.redTeam.stream().filter(c->c instanceof ArenaFlagpole)
				.map(c->(ArenaFlagpole)c).sorted((a,b)->a.level-b.level)
				.collect(Collectors.toList());
	}

	@Override
	public BattleMouseAction getmouseaction(){
		return null;
	}
}

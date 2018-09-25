package javelin.controller.fight.minigame.arena.building;

import java.util.ArrayList;

import javelin.controller.fight.Fight;
import javelin.model.unit.Combatant;

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

	static public ArrayList<ArenaFlagpole> getflags(){
		ArrayList<ArenaFlagpole> flags=new ArrayList<>(4);
		for(Combatant c:Fight.state.redTeam)
			if(c instanceof ArenaFlagpole) flags.add((ArenaFlagpole)c);
		flags.sort((a,b)->a.level-b.level);
		return flags;
	}
}

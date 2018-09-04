package javelin.controller.fight.minigame.arena.building;

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
}

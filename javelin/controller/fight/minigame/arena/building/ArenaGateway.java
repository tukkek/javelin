package javelin.controller.fight.minigame.arena.building;

import javelin.model.unit.Combatant;

public class ArenaGateway extends ArenaBuilding{

	public ArenaGateway(){
		super("Flagpole","flagpolered","Flagpole.");
	}

	@Override
	protected void upgradebuilding(){
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean click(Combatant current){
		// TODO Auto-generated method stub
		return false;
	}
}

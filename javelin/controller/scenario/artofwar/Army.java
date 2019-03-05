package javelin.controller.scenario.artofwar;

import java.util.List;

import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.world.Incursion;
import javelin.view.screen.WorldScreen;

public class Army extends Incursion{
	public Army(int x,int y,List<Combatant> squadp,Realm r){
		super(x,y,squadp,r);
	}

	@Override
	public void turn(long time,WorldScreen world){
		//see ArtOfWar
	}
}

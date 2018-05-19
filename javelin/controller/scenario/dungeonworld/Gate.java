package javelin.controller.scenario.dungeonworld;

import java.awt.Image;
import java.util.List;

import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.world.location.Location;
import javelin.view.Images;

public class Gate extends Location {
	public Gate(Realm r) {
		super(r + " gate");
	}

	@Override
	public List<Combatant> getcombatants() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getel(int attackerel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Image getimage() {
		return Images.getImage("locationportal");
	}
}

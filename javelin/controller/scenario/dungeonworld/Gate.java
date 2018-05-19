package javelin.controller.scenario.dungeonworld;

import java.awt.Image;
import java.util.List;

import javelin.controller.scenario.dungeonworld.ZoneGenerator.Zone;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.world.location.Location;
import javelin.view.Images;

public class Gate extends Location {
	public Realm key;
	public transient Zone to;

	public Gate(Realm r, Zone to) {
		super(null);
		this.to = to;
		setkey(r);
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

	public void setkey(Realm r) {
		key = r;
		description = r + " gate";
	}
}

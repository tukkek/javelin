package javelin.controller.scenario.dungeonworld;

import java.util.List;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.scenario.dungeonworld.ZoneGenerator.Zone;
import javelin.model.Realm;
import javelin.model.item.key.TempleKey;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;

public class Gate extends Location{
	public Realm key;
	public transient Zone to;

	public Gate(Realm r,Zone to){
		super(null);
		this.to=to;
		setkey(r);
		allowentry=false;
		sacrificeable=false;
	}

	@Override
	public List<Combatant> getcombatants(){
		return null;
	}

	@Override
	public Integer getel(Integer attackerel){
		return null;
	}

	public void setkey(Realm r){
		key=r;
		description=r+" gate";
		realm=r;
	}

	@Override
	public boolean interact(){
		if(Debug.bypassdoors){
			remove();
			return true;
		}
		TempleKey key=new TempleKey(this.key);
		if(Squad.active.equipment.get(key)==null){
			String fail="Only the "+key+" will unlock this gate...";
			Javelin.message(fail,false);
		}else{
			Javelin.message("You unlock the "+description+"!",false);
			remove();
		}
		return true;
	}
}

package javelin.controller.event.wild.negative;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.event.wild.WildEvent;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.unit.Squad;
import javelin.model.world.Incursion;
import javelin.model.world.location.PointOfInterest;
import javelin.old.RPG;

public class FindIncursion extends WildEvent{
	static final int ATTEMPTS=10000;

	ArrayList<Incursion> incursions=new ArrayList<>();

	public FindIncursion(PointOfInterest l){
		super("Find incursion",l);
	}

	@Override
	public void happen(Squad s){
		for(var incursion:incursions){
			incursion.displace();
			incursion.place();
		}
		Javelin.redraw();
		Javelin.message("You uncover a band of enemies!",true);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		var nincursions=1;
		while(RPG.chancein(2))
			nincursions+=1;
		var realm=Realm.random();
		var tries=ATTEMPTS;
		while(incursions.size()<nincursions){
			var el=squadel+Difficulty.get();
			var terrain=Terrain.get(location.x,location.y);
			var group=EncounterGenerator.generate(el,terrain);
			incursions.add(new Incursion(location.x,location.y,group,realm));
		}
		return true;
	}
}

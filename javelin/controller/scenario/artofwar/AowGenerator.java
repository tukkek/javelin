package javelin.controller.scenario.artofwar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javelin.controller.InfiniteList;
import javelin.controller.Point;
import javelin.controller.challenge.Difficulty;
import javelin.controller.fight.minigame.battlefield.Reinforcement;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.Underground;
import javelin.controller.terrain.Water;
import javelin.model.Realm;
import javelin.model.item.Tier;
import javelin.model.unit.Combatant;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.haunt.Haunt;
import javelin.model.world.location.haunt.WitchesHideout;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.basic.Dwelling;
import javelin.model.world.location.town.labor.expansive.Hub;
import javelin.old.RPG;

/** {@link FeatureGenerator} for {@link ArtOfWar}. */
public class AowGenerator extends FeatureGenerator{
	static final Set<Class<? extends Location>> BANNED=Set
			.of(WitchesHideout.class);

	@Override
	public Location generate(LinkedList<Realm> realms,
			ArrayList<HashSet<Point>> regions,World w){
		InfiniteList<Terrain> terrains=new InfiniteList<>(
				Arrays.asList(Terrain.ALL),true);
		for(int el=ArtOfWar.INITIALEL+Difficulty.MODERATE
				+1;el<=ArtOfWar.ENDGAME;el++)
			generatedwellings(el,terrains.pop());
		for(Haunt h:generatehaunts())
			if(!BANNED.contains(h.getClass())) h.place();
		generatetowns(terrains);
		ArrayList<Actor> actors=World.getactors();
		Actor start=null;
		while(!(start instanceof Location))
			start=RPG.pick(actors);
		return (Location)start;
	}

	void generatetowns(InfiniteList<Terrain> terrains){
		int el=ArtOfWar.INITIALEL;
		while(el<ArtOfWar.ENDGAME){
			Terrain t=terrains.pop();
			if(t.equals(Terrain.WATER)||t.equals(Terrain.UNDERGROUND)) continue;
			el=Math.min(el+RPG.r(1,10),ArtOfWar.ENDGAME);
			Town town=new Town(findterrain(t),null);
			town.population=el;
			town.place();
			town.garrison.addAll(generatearmy(el,t));
		}
	}

	void generatedwellings(int el,Terrain target){
		Dwelling d=new Dwelling(); //TODO change to simple fight icon
		generate(d,findterrain(target));
		d.garrison.addAll(generatearmy(el,target));
	}

	List<Combatant> generatearmy(float el,Terrain t){
		List<Float> squads=new ArrayList<>();
		squads.add(el);
		int tier=Tier.get(Math.round(el)).ordinal()+1;
		while(squads.size()<tier*1.5||RPG.r(1,tier)!=1){
			Float squad=RPG.pick(squads);
			if(squad<=2) break;
			squads.remove(squad);
			squad-=1.6f;
			squads.add(squad);
			squads.add(squad);
		}
		InfiniteList<Integer> ranksi=new InfiniteList<>(List.of(0,1,2),true);
		List<Combatant> army=new ArrayList<>();
		for(float squad:squads){
			Reinforcement r=new Reinforcement(squad,List.of(t));
			List<List<Combatant>> ranks=List.of(r.commander,r.elites,r.footsoldiers);
			army.addAll(ranks.get(ranksi.pop()));
		}
		return army;
	}

	void generatedock(){
		Point p=null;
		while(p==null){
			p=findterrain(Terrain.WATER);
			p=checkshore(p,Terrain.WATER);
		}
		Hub h=new Hub();
		h.level=2;
		h.setlocation(p);
		h.place();
	}

	public Dwelling generate(Dwelling d,Point p){
		d.setlocation(p);
		d.place();
		d.maximize();
		d.garrison.clear();
		return d;
	}

	boolean validate(Point p){
		return p!=null&&p.validate(0,0,World.scenario.size,World.scenario.size)
				&&World.get(p.x,p.y,World.getactors())==null
				&&!Terrain.get(p.x,p.y).equals(Terrain.WATER);
	}

	Point findterrain(Terrain t){
		int max=World.scenario.size-1;
		if(t instanceof Underground)
			return findterrain(RPG.pick(List.of(Terrain.NONWATER)));
		Point p=null;
		while(p==null||!Terrain.get(p.x,p.y).equals(t)
				||World.get(p.x,p.y,World.getactors())!=null||checkshore(p,t)==null)
			p=new Point(RPG.r(0,max),RPG.r(0,max));
		return p;
	}

	Point checkshore(Point p,Terrain t){
		if(!(t instanceof Water)) return p;
		for(Point a:Point.getadjacent()){
			a.x+=p.x;
			a.y+=p.y;
			if(a.validate(0,0,World.scenario.size,World.scenario.size)
					&&!(Terrain.get(a.x,a.y) instanceof Water))
				return a;
		}
		return null;
	}
}
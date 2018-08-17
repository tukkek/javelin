package javelin.controller.scenario.artofwar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.InfiniteList;
import javelin.controller.Point;
import javelin.controller.fight.minigame.battlefield.Reinforcement;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.Underground;
import javelin.controller.terrain.Water;
import javelin.model.Realm;
import javelin.model.item.Tier;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.labor.basic.Dwelling;
import javelin.model.world.location.town.labor.basic.Lodge;
import javelin.model.world.location.town.labor.expansive.Hub;
import javelin.old.RPG;

/** {@link FeatureGenerator} for {@link ArtOfWar}. */
public class AowGenerator extends FeatureGenerator{
	@Override
	public Location generate(LinkedList<Realm> realms,
			ArrayList<HashSet<Point>> regions,World w){
		List<Monster> units=ArtOfWar.singleton.getunits(ArtOfWar.singleton.region);
		List<Monster> fodder=units.stream().filter(m->m.cr<=5)
				.collect(Collectors.toList());
		Location start=generatefriendly(fodder);
		units=units.stream().filter(m->m.cr>=ArtOfWar.COMMANDERCRMIN)
				.collect(Collectors.toList());
		InfiniteList<Terrain> terrains=new InfiniteList<>(
				Arrays.asList(Terrain.ALL),true);
		for(int el=ArtOfWar.COMMANDERCRMIN;!units.isEmpty();el++)
			generatehostile(units,el,terrains.pop());
		return start;
	}

	void generatehostile(List<Monster> units,int el,Terrain target){
		List<Monster> tier=units.stream().filter(m->m.cr==el)
				.collect(Collectors.toList());
		if(tier.isEmpty()) return;
		units.removeAll(tier);
		Monster m=RPG.pick(tier);
		Dwelling d=new Dwelling(m);
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

	Location generatefriendly(List<Monster> fodder){
		generatedock();
		generatelodges(RPG.r(1,4));
		Collections.shuffle(fodder);
		Point spawn=findterrain(ArtOfWar.singleton.region);
		Dwelling d=null;
		for(int i=0;i<fodder.size()&&i<5;i++){
			Monster m=fodder.get(i);
			Point p=null;
			while(!validate(p))
				p=new Point(spawn.x+RPG.r(-5,+5),spawn.y+RPG.r(-5,+5));
			d=generate(new Dwelling(m),p);
		}
		return d;
	}

	void generatelodges(int lodges){
		for(int i=0;i<lodges;i++)
			new Lodge().place();
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
		return p!=null
				&&p.validate(0,0,ArtOfWar.singleton.size,ArtOfWar.singleton.size)
				&&World.get(p.x,p.y,World.getactors())==null
				&&!Terrain.get(p.x,p.y).equals(Terrain.WATER);
	}

	Point findterrain(Terrain t){
		int max=ArtOfWar.singleton.size-1;
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
package javelin.controller.scenario.artofwar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.challenge.Difficulty;
import javelin.controller.generator.feature.LocationGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.haunt.Haunt;
import javelin.model.world.location.haunt.SunkenShip;
import javelin.old.RPG;

/** {@link LocationGenerator} for {@link ArtOfWar}. */
public class AowGenerator extends LocationGenerator{
	static final Set<Class<? extends Location>> BANNED=Set.of(SunkenShip.class);

	class Territory{
		Set<Point> area=new HashSet<>();
		Point center=null;
		WarLocation location=null;

		Territory(){
			var size=World.scenario.size;
			while(center==null||!validate())
				center=new Point(RPG.r(0,size-1),RPG.r(0,size-1));
			area.add(center);
		}

		boolean validate(){
			World w=World.getseed();
			if(Terrain.search(center,Terrain.WATER,1,w)>0) return false;
			for(var t:territories)
				if(t.center.distanceinsteps(center)<=2) return false;
			return true;
		}

		void expand(){
			var p=new Point(RPG.pick(area));
			while(area.contains(p))
				p.displace();
			if(!World.validatecoordinate(p.x,p.y)||territories.stream()
					.filter(t->t!=this&&t.center.distanceinsteps(p)<=1).limit(1)
					.count()==1)
				return;
			for(var t:territories)
				if(t!=this) t.area.remove(p);
			area.add(p);
		}

		void fill(){
			var terrains=new ArrayList<>(Arrays.asList(Terrain.STANDARD));
			var neighbors=territories.stream().sorted((a,
					b)->a.center.distanceinsteps(center)-b.center.distanceinsteps(center))
					.limit(terrains.size()-1);
			terrains.removeAll(neighbors.map(t->Terrain.get(t.center.x,t.center.y))
					.collect(Collectors.toList()));
			var t=RPG.pick(terrains);
			for(var point:area)
				if(!Terrain.get(point.x,point.y).equals(Terrain.WATER))
					World.getseed().map[point.x][point.y]=t;
		}

		public Location place(WarLocation l){
			location=l;
			l.x=center.x;
			l.y=center.y;
			l.place();
			if(l.hires.isEmpty()) l.generategarrison();
			return l;
		}
	}

	transient final List<Territory> territories=new ArrayList<>();

	@Override
	public Location generate(LinkedList<Realm> realmsp,
			ArrayList<HashSet<Point>> regions,World w){
		var realms=realmsp.subList(0,RPG.r(2,realmsp.size()));
		var haunts=makehaunts();
		var nlocations=ArtOfWar.ENDGAME-ArtOfWar.INITIALEL+realms.size()
				+haunts.size()-BANNED.size();
		var dungeons=nlocations/10;
		nlocations+=dungeons;
		buildterritories(nlocations);
		var start=generatelocations(realms,haunts,dungeons);
		maprealms();
		return start;
	}

	void maprealms(){
		var towns=territories.stream().filter(t->t.location.town)
				.collect(Collectors.toList());
		var territories=this.territories.stream().filter(t->!t.location.town)
				.collect(Collectors.toList());
		for(var t:territories){
			var p=t.location.getlocation();
			towns.sort((a,b)->a.location.getlocation().distanceinsteps(p)
					-b.location.getlocation().distanceinsteps(p));
			t.location.realm=towns.get(0).location.realm;
		}
	}

	Location generatelocations(List<Realm> realms,List<Haunt> haunts,
			int dungeons){
		var territories=RPG.shuffle(new LinkedList<>(this.territories));
		realms.forEach(r->{
			var town=new WarLocation(ArtOfWar.ENDGAME,"locationtowncity");
			town.town=true;
			territories.pop().place(town).realm=r;
		});
		for(var h:haunts)
			if(BANNED.contains(h.getClass())){
				h.generategarrison();
				territories.pop().place(new WarLocation(h));
			}
		for(var i=0;i<dungeons;i++){
			var el=RPG.r(ArtOfWar.INITIALEL,ArtOfWar.ENDGAME);
			var dungeon=new WarLocation(el,DungeonTier.get(el).getimagename());
			dungeon.setdungeon();
			territories.pop().place(dungeon);
		}
		var el=ArtOfWar.INITIALEL;
		var high=(ArtOfWar.INITIALEL+ArtOfWar.ENDGAME)/2;
		Location start=null;
		while(!territories.isEmpty()){
			var l=new WarLocation(el,el>=high?"flagpolered":"flagpoleblue");
			territories.pop().place(l);
			el+=1;
			if(start==null) start=l;
		}
		for(var minor=RPG.rolldice(4,4);minor>0;minor-=1){
			var minorel=RPG.r(ArtOfWar.INITIALEL+Difficulty.EASY,
					ArtOfWar.INITIALEL+Difficulty.MODERATE);
			new WarLocation(minorel,"locationdwelling").place();
		}
		return start;
	}

	void buildterritories(int nlocations){
		int area=World.scenario.size*World.scenario.size;
		for(;nlocations>0;nlocations--)
			territories.add(new Territory());
		while(territories.stream()
				.collect(Collectors.summingInt(t->t.area.size()))<area)
			for(var t:RPG.shuffle(territories))
				t.expand();
		for(var t:territories)
			t.fill();
	}
}
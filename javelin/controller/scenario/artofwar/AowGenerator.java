package javelin.controller.scenario.artofwar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.haunt.SunkenShip;
import javelin.model.world.location.haunt.WitchesHideout;
import javelin.old.RPG;

/** {@link FeatureGenerator} for {@link ArtOfWar}. */
public class AowGenerator extends FeatureGenerator{
	static final Set<Class<? extends Location>> BANNED=Set
			.of(WitchesHideout.class,SunkenShip.class);

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

		public void place(WarLocation l){
			location=l;
			l.x=center.x;
			l.y=center.y;
			l.place();
		}
	}

	transient final List<Territory> territories=new ArrayList<>();

	@Override
	public Location generate(LinkedList<Realm> realms,
			ArrayList<HashSet<Point>> regions,World w){
		int area=World.scenario.size*World.scenario.size;
		var locations=ArtOfWar.ENDGAME-ArtOfWar.INITIALEL;
		for(;locations>0;locations--)
			territories.add(new Territory());
		while(territories.stream()
				.collect(Collectors.summingInt(t->t.area.size()))<area)
			for(var t:RPG.shuffle(territories))
				t.expand();
		for(var t:territories)
			t.fill();
		var start=RPG.pick(territories);
		var sorted=territories.stream()
				.sorted((a,b)->start.center.distanceinsteps(a.center)
						-start.center.distanceinsteps(b.center))
				.collect(Collectors.toList());
		var el=ArtOfWar.INITIALEL;
		for(var t:sorted){
			t.place(new WarLocation(el));
			el+=1;
		}
		sorted.get(sorted.size()-1).location.win=true;
		return start.location;
	}
}
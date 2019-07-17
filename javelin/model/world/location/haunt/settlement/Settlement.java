package javelin.model.world.location.haunt.settlement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.map.location.LocationMap;
import javelin.controller.map.location.TownMap;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Tier;
import javelin.model.unit.Alignment;
import javelin.model.unit.Alignment.Ethics;
import javelin.model.unit.Alignment.Morals;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.world.location.haunt.Haunt;
import javelin.old.RPG;

/**
 * A haunt for {@link MonsterType#HUMANOID}s of a certain {@link Alignment}.
 *
 * @author alex
 */
public abstract class Settlement extends Haunt{
	static final List<MonsterType> TYPES=List.of(MonsterType.MONSTROUSHUMANOID,
			MonsterType.HUMANOID);
	static final List<Monster> POOL=Monster.MONSTERS.stream()
			.filter(m->TYPES.contains(m.type)).collect(Collectors.toList());

	class SettlementMap extends LocationMap{
		SettlementMap(){
			super(null);
		}

		void expand(Point start,ArrayList<Point> area){
			area.add(start);
			var target=RPG.r(1,6)+4;
			while(area.size()<target){
				var p=getempty();
				if(p.distanceinsteps(start)<=9&&!startingareablue.contains(p)
						&&!startingareared.contains(p))
					area.add(p);
			}
		}

		@Override
		public void generate(){
			var map=new TownMap(Tier.get(targetel).getordinal()+1);
			floor=map.floor;
			obstacle=map.obstacle;
			wall=map.wall;
			map.generate();
			this.map=map.map;
			Point red=null;
			Point blue=null;
			while(red==null||red.distanceinsteps(blue)>9){
				red=getempty();
				blue=getempty();
			}
			expand(blue,startingareablue);
			expand(red,startingareared);
		}
	}

	/** Constructor. */
	protected Settlement(Alignment a,List<Terrain> terrains){
		super("Settlement",null,POOL,terrains);
		pool=pool.stream().filter(
				m->(a.ethics==Ethics.NEUTRAL||a.ethics.equals(m.alignment.ethics))
						&&(a.morals==Morals.NEUTRAL||a.morals.equals(m.alignment.morals)))
				.collect(Collectors.toList());
	}

	@Override
	public LocationMap getmap(){
		return new SettlementMap();
	}

	@Override
	public String getimagename(){
		return "locationsettlement";
	}
}

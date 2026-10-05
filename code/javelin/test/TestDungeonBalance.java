package javelin.test;

import java.util.ArrayList;
import java.util.stream.Collectors;

import javelin.model.world.World;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.dungeon.Portal;
import javelin.model.world.location.dungeon.branch.temple.WaterTemple.WaterTempleEntrance;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.common.Campfire;
import javelin.model.world.location.dungeon.feature.common.Passage;
import javelin.model.world.location.dungeon.feature.rare.Fountain;

/**
 * Tests balance-critical {@link Feature} generation in {@link DungeonFloor}s.
 */
public class TestDungeonBalance{
	/**
	 * TODO make "include Portals" an option (off by default since they have they
	 * own balance scheme with encounters levels and rates)
	 */
	public void test(){
		for(var t:DungeonTier.TIERS){
			var floors=World.getactors().stream()
					.filter(a->a instanceof DungeonEntrance&&!(a instanceof Portal))
					.map(e->((DungeonEntrance)e).dungeon)
					.filter(d->t.tier.minlevel<=d.level&&d.level<=t.tier.maxlevel)
					.flatMap(d->d.floors.stream()).collect(Collectors.toList());
			var fountains=new ArrayList<Integer>();
			var camps=new ArrayList<Integer>();
			var exits=new ArrayList<Long>();
			var totalfountains=0;
			var totalcamps=0;
			var totalexits=0;
			for(var f:floors){
				if(f.dungeon.entrance instanceof WaterTempleEntrance) continue;
				int fs=f.features.getall(Fountain.class).size();
				int c=f.features.getall(Campfire.class).size();
				var e=f.features.getall(Passage.class).stream()
						.filter(p->p.destination==null).count();
				fountains.add(fs);
				camps.add(c);
				exits.add(e);
				totalfountains+=fs;
				totalcamps+=c;
				totalexits+=e;
			}
			fountains.sort(null);
			camps.sort(null);
			exits.sort(null);
			System.out.println(String.format(
					"%s floors: %s floors, %s campsites, %s fountains, %s exits",t,
					floors.size(),totalcamps,totalfountains,totalexits));
			System.out.println(String.format(
					"Typical %s floor: %s campsites, %s fountains, %s exits",t,
					camps.get(camps.size()/2),fountains.get(fountains.size()/2),
					exits.get(exits.size()/2)));
			System.out.println();
		}
	}
}

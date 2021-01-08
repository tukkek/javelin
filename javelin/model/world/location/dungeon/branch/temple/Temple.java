package javelin.model.world.location.dungeon.branch.temple;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.content.template.KitTemplate;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.content.wish.Win;
import javelin.controller.generator.feature.LocationGenerator;
import javelin.model.Realm;
import javelin.model.item.Tier;
import javelin.model.item.artifact.Artifact;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.dungeon.feature.Decoration;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.chest.ArtifactChest;
import javelin.old.RPG;

/**
 * Temples are {@link Tier#EPIC} {@link Dungeon}s with a single {@link Branch},
 * Each Temple is focused on a particular {@link Realm}.
 *
 * Deep in each Temple there will be an {@link Artifact} and once all of those
 * are collected, the player can {@link Win}.
 *
 * TODO once all Temples have {@link Branch#templates}, remove
 * {@link KitTemplate} (or keep)?
 *
 * @author alex
 */
public abstract class Temple extends Dungeon{
	/** @see Location */
	public static class TempleEntrance extends DungeonEntrance{
		/** Constructor. */
		public TempleEntrance(Temple t){
			super(t);
		}

		@Override
		protected boolean validateplacement(boolean water,World w,
				List<Actor> actors){
			var t=(Temple)dungeon;
			return t.terrains.contains(Terrain.get(x,y))
					&&super.validateplacement(water,w,actors);
		}
	}

	class TempleFloor extends DungeonFloor{
		TempleFloor(Integer level,Dungeon d){
			super(level,d);
		}

		@Override
		protected LinkedList<Decoration> generatedecoration(int minimum){
			return null;
		}
	}

	/** @see LocationGenerator */
	public static void generatetemples(){
		var temples=List.of(new AirTemple(),new EarthTemple(),new FireTemple(),
				new EvilTemple(),new GoodTemple(),new MagicTemple(),new WaterTemple());
		for(var t:RPG.shuffle(new ArrayList<>(temples)))
			t.place();
	}

	/** @return All temple {@link Location}s. */
	public static List<TempleEntrance> gettemples(){
		var temples=new ArrayList<TempleEntrance>(7);
		for(var a:World.getactors())
			if(a instanceof TempleEntrance) temples.add((TempleEntrance)a);
		return temples;
	}

	Realm realm;

	/** Constructor. */
	public Temple(Realm r,Branch b,String f){
		super("The Temple of "+Javelin.capitalize(r.name),
				Tier.EPIC.getrandomel(false),RPG.randomize(2,1,Integer.MAX_VALUE));
		realm=r;
		fluff=f;
		b.templates.add(KitTemplate.SINGLETON);
		branches.add(b);
		terrains.clear();
		terrains.addAll(b.terrains);
	}

	@Override
	public String getimagename(){
		return "temple"+realm;
	}

	@Override
	protected void generateappearance(){
		var b=branches.get(0);
		doorbackground=b.doorbackground;
		images.put(DungeonImages.FLOOR,b.floor);
		images.put(DungeonImages.WALL,b.wall);
	}

	@Override
	protected DungeonFloor createfloor(int level){
		return new TempleFloor(level,this);
	}

	/** @see TempleEntrance#place() */
	protected void place(){
		new TempleEntrance(this).place();
	}

	@Override
	public Feature generatespecialchest(DungeonFloor f){
		return realm!=null&&f==f.dungeon.floors.getLast()
				?new ArtifactChest(RPG.pick(realm.artifacts))
				:super.generatespecialchest(f);
	}

	@Override
	protected synchronized String baptize(String suffix){
		return name;
	}
}

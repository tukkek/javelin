package javelin.model.world.location.dungeon.branch.temple;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.fight.RandomDungeonEncounter;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.generator.feature.LocationGenerator;
import javelin.controller.template.KitTemplate;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.wish.Win;
import javelin.controller.wish.Wish;
import javelin.model.Realm;
import javelin.model.item.Tier;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.branch.temple.WaterTemple.WaterTempleEntrance;
import javelin.old.RPG;

/**
 * Temples are Javelin's {@link Tier#EPIC} {@link Dungeon}s, with features that
 * distinguish them from ordinary {@link Dungeon}s like {@link Hazard}s and
 * {@link NpcGenerator}-based {@link Combatant}s from
 * non-{@link Terrain#UNDERGROUND} pools.
 *
 * Deep in the Temple there will be an {@link Artifact} and once all of those
 * are collected the player can make the {@link Win} {@link Wish}.
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
		protected void generate(){
			var t=(Temple)dungeon;
			while(x==-1||!t.terrains.contains(Terrain.get(x,y)))
				super.generate();
		}
	}

	/** @see LocationGenerator */
	public static void generatetemples(){
		var w=new WaterTemple(Tier.EPIC.getrandomel(false));
		new WaterTempleEntrance(w).place();
		for(var type:List.of(AirTemple.class,EarthTemple.class,FireTemple.class,
				EvilTemple.class,GoodTemple.class,MagicTemple.class))
			try{
				var el=Tier.EPIC.getrandomel(false);
				var t=type.getConstructor(Integer.class).newInstance(el);
				new TempleEntrance(t).place();
			}catch(ReflectiveOperationException e){
				throw new RuntimeException(e);
			}
	}

	/** @return All temple {@link Location}s. */
	public static List<TempleEntrance> gettemples(){
		var temples=new ArrayList<TempleEntrance>(7);
		for(var a:World.getactors())
			if(a instanceof TempleEntrance) temples.add((TempleEntrance)a);
		return temples;
	}

	Realm realm;
	Artifact artifact;

	/** Constructor. */
	public Temple(Realm r,List<Terrain> t,int level,String f,TempleBranch b,
			Artifact a){
		super("The Temple of "+r.getname(),level,
				RPG.randomize(2,1,Integer.MAX_VALUE));
		fluff=f;
		realm=r;
		artifact=a;
		terrains.clear();
		terrains.addAll(t);
		b.temple=this;
		b.templates.add(new KitTemplate(level/2));
		branches.add(b);
	}

	public Temple(Realm r,Terrain t,int level,String f,TempleBranch b,Artifact a){
		this(r,List.of(t),level,f,b,a);
	}

	@Override
	public String getimagename(){
		return "temple"+realm.getname().toLowerCase();
	}

	@Override
	public RandomDungeonEncounter fight(){
		var f=super.fight();
		f.setterrain(Terrain.UNDERGROUND);
		return f;
	}

	@Override
	protected void generateappearance(){
		var b=branches.get(0);
		doorbackground=b.doorbackground;
		images.put(DungeonImages.FLOOR,b.floor);
		images.put(DungeonImages.WALL,b.wall);
	}
}

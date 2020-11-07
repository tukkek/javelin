package javelin.model.world.location.dungeon.temple;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.generator.NpcGenerator;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.wish.Win;
import javelin.model.Realm;
import javelin.model.item.Tier;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.chest.ArtifactChest;
import javelin.model.world.location.dungeon.temple.WaterTemple.WaterTempleEntrance;
import javelin.old.RPG;

/**
 * Temples are Javelin's {@link Tier#EPIC} {@link Dungeon}s, with features that
 * distinguish them from ordinary {@link Dungeon}s like {@link Hazard}s and
 * {@link NpcGenerator}-based {@link Combatant}s from
 * non-{@link Terrain#UNDERGROUND} pools.
 *
 * Inside the Temple there will be a {@link Artifact}, and once all of those are
 * collected the player can make the {@link Win} wish to finish the game.
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

	/**
	 * Create the temples during world generation.
	 */
	public static void generatetemples(){
		new WaterTempleEntrance(new WaterTemple(Tier.EPIC.getrandomel(false)))
				.place();
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

	/**
	 * Reward found on the deepest of the {@link #floors}.
	 *
	 * @see ArtifactChest
	 * @see TempleFloor#deepest
	 */
	public Artifact artifact;
	/** {@link DungeonFloor} {@link Feature} most likely to be found here. */
	public Class<? extends Feature> feature=null;

	Realm realm;

	/** Constructor. */
	public Temple(Realm r,List<Terrain> t,int level,Artifact a,String fluffp){
		super("The Temple of "+r.getname(),level,
				RPG.randomize(4,1,Integer.MAX_VALUE));
		artifact=a;
		fluff=fluffp;
		realm=r;
		terrains.addAll(t);
		for(var f:floors){
			var d=(TempleFloor)f;
			d.temple=this;
		}
	}

	/** Constructor with a single {@link Terrain}. */
	public Temple(Realm r,Terrain t,int level,Artifact a,String fluff){
		this(r,List.of(t),level,a,fluff);
	}

	@Override
	protected DungeonFloor createfloor(int level){
		return new TempleFloor(level,this);
	}

	@Override
	public String getimagename(){
		return "temple"+realm.getname().toLowerCase();
	}

	@Override
	public boolean validate(List<Monster> foes){
		return true;
	}

	/**
	 * See {@link DungeonFloor#hazard()}.
	 *
	 * @return <code>true</code> if a hazard happens.
	 */
	public boolean hazard(DungeonFloor f){
		return false;
	}

	/** @return All temple {@link Location}s. */
	public static List<TempleEntrance> gettemples(){
		var temples=new ArrayList<TempleEntrance>(7);
		for(var a:World.getactors())
			if(a instanceof TempleEntrance) temples.add((TempleEntrance)a);
		return temples;
	}
}

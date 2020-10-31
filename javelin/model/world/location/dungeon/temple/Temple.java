package javelin.model.world.location.dungeon.temple;

import java.awt.Image;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.Difficulty;
import javelin.controller.fight.Fight;
import javelin.controller.fight.TempleEncounter;
import javelin.controller.scenario.Campaign;
import javelin.controller.terrain.Terrain;
import javelin.controller.wish.Win;
import javelin.model.Realm;
import javelin.model.item.Tier;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.DisableDevice;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.ArtifactChest;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.unique.UniqueLocation;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * Temples are the key to winning Javelin's {@link Campaign} mode. Each temple
 * is locked and needs to be unlocked by a {@link TempleKey}, brute
 * {@link Monster#strength} or {@link DisableDevice}. Inside the Temple there
 * will be a {@link Artifact}, and once all of those are collected the player can
 * make the {@link Win} wish to finish the game.
 *
 * Each temple is a multi-level {@link TempleDungeon}, where on each floor can
 * be found a special {@link Chest}. The Relic sits on the deepest floor.
 *
 * @see Win
 * @see TempleEncounter
 * @see TempleDungeon#createspecialchest(Point)
 * @author alex
 */
public abstract class Temple extends UniqueLocation{
	//	/**
	//	 * TODO there's gotta be a better way to do this
	//	 */
	//	public static boolean climbing=false;
	/** TODO same as {@link #climbing} */
	public static boolean leavingfight=false;

	/**
	 * Create the temples during world generation.
	 */
	public static void generatetemples(){
		var els=new LinkedList<Integer>();
		for(var i=0;i<7;i++)
			els.add(Tier.EPIC.getrandomel(false));
		new AirTemple(els.pop()).place();
		new EarthTemple(els.pop()).place();
		new FireTemple(els.pop()).place();
		new WaterTemple(els.pop()).place();
		new EvilTemple(els.pop()).place();
		new GoodTemple(els.pop()).place();
		new MagicTemple(els.pop()).place();
		if(Javelin.DEBUG&&!els.isEmpty())
			throw new RuntimeException("Didn't generate all temples.");
	}

	/**
	 * Reward found on the deepest of the {@link #floors}.
	 *
	 * @see ArtifactChest
	 * @see TempleDungeon#deepest
	 */
	public Artifact relic;
	/**
	 * Each floor has a {@link Chest} with a ruby in it and there is also an
	 * {@link ArtifactChest} on the deepest level.
	 */
	public List<Dungeon> floors=new ArrayList<>();
	/** Encounter level equivalent for {@link #level}. */
	public int el;
	String fluff;
	/** If not <code>null</code> will override {@link Dungeon#tilefloor}. */
	public String floor=null;
	/** If not <code>null</code> will override {@link Dungeon#tilewall}. */
	public String wall=null;
	/** If <code>false</code>, draw doors without a wall behind them. */
	public boolean doorbackground=true;
	/** Module level. */
	public int level;
	/** {@link Dungeon} {@link Feature} most likely to be found here. */
	public Class<? extends Feature> feature=null;

	/**
	 * @param r Temple's defining characteristic.
	 * @param fluffp Text description of temple and surrounding area.
	 */
	public Temple(Realm r,int level,Artifact relicp,String fluffp){
		super("The temple of "+r.getname(),"Temple of "+r.getname(),level,level);
		allowedinscenario=false;
		realm=r;
		this.level=level;
		el=level;
		relic=relicp;
		fluff=fluffp;
		link=true;
		generatefloors(level);
	}

	void generatefloors(int level){
		TempleDungeon parent=null;
		for(int i=0;i<RPG.randomize(4,1,Integer.MAX_VALUE);i++){
			parent=new TempleDungeon(el+i,parent,this);
			floors.add(parent);
		}
	}

	@Override
	protected void generategarrison(int minlevel,int maxlevel){
		// no outside garrison
	}

	@Override
	public void place(){
		Realm r=realm;
		super.place();
		realm=r;
	}

	@Override
	public Image getimage(){
		final String name="locationtemple"+realm.getname().toLowerCase();
		return Images.get(name);
	}

	@Override
	public boolean interact(){
		Javelin.message(fluff,true);
		floors.get(0).activate(false);
		return true;
	}

	Combatant force(){
		Combatant best=null;
		for(Combatant c:Squad.active.members){
			int roll=Monster.getbonus(c.source.strength);
			if(roll<level) continue;
			if(best==null||roll>Monster.getbonus(best.source.strength)) best=c;
		}
		return best;
	}

	@Override
	public Realm getrealmoverlay(){
		return null;
	}

	/**
	 * @return Starts a {@link TempleEncounter}.
	 */
	public Fight encounter(Dungeon d){
		return new TempleEncounter(this,d);
	}

	/** See {@link Fight#validate(ArrayList)}. */
	public boolean validate(List<Monster> foes){
		return true;
	}

	/** See {@link Fight#getterrains(Terrain)}; */
	public List<Terrain> getterrains(){
		/**
		 * TODO Terrain.UNDERGROUND left out for now so that each temple will be
		 * more unique instead, may need to reintroduce or find an alternative if
		 * there aren't enough encounters for every possible challenge level.
		 */
		return List.of(terrain);
	}

	@Override
	protected void generate(){
		if(terrain==null||terrain.equals(Terrain.WATER))
			super.generate();
		else
			while(x==-1||!Terrain.get(x,y).equals(terrain))
				super.generate();
	}

	/**
	 * See {@link Dungeon#hazard()}.
	 *
	 * @return <code>true</code> if a hazard happens.
	 */
	public boolean hazard(Dungeon templeDungeon){
		return false;
	}

	@Override
	public List<Combatant> getcombatants(){
		return null;
	}

	public static ArrayList<Temple> gettemples(){
		ArrayList<Temple> temples=new ArrayList<>(7);
		for(Actor a:World.getactors())
			if(a instanceof Temple) temples.add((Temple)a);
		return temples;
	}

	@Override
	public String describe(){
		String difficulty=Difficulty.describe(level-Squad.active.getel());
		return descriptionknown+" ("+difficulty+").";
	}
}

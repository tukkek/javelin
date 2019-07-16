package javelin.model.world;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.comparator.CombatantByCr;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.IncursionFight;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.Fortification;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.feature.Portal;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.screen.WorldScreen;

/**
 * An attacking {@link Squad}, trying to destroy a {@link Town} or other
 * {@link Actor}. Each one that appears grows stronger, which should eventually
 * end the game.
 *
 * An {@link Incursion} is similar to an invading {@link Squad} while an
 * Invasion refers to the ongoing process of trying to destroy the player's
 * region.
 *
 * @author alex
 */
public class Incursion extends Actor{
	/** Only taken into account if running {@link Javelin#DEBUG}. */
	static final boolean SPAWN=true;
	static final int PREFERREDVICTORYCHANCE=5+2;
	/** Move even if {@link Debug#disablecombat} is enabled. */
	static final boolean FORCEMOVEMENT=false;
	static final VictoryChance VICTORYCHANCES=new VictoryChance();
	static final int[] DELTAS=new int[]{-1,0,+1};

	static class VictoryChance{
		HashMap<Integer,Integer> chances=new HashMap<>();

		VictoryChance(){
			chances.put(-4,9);
			chances.put(-3,8);
			chances.put(-2,7);
			chances.put(-1,6);
			chances.put(0,5);
			chances.put(1,4);
			chances.put(2,3);
			chances.put(3,2);
			chances.put(4,1);
		}

		/**
		 * @param attackerel Encounter level.
		 * @param defenderel Encounter level.
		 * @return A chance from 1 to 9 that the attacker will win (meant to be used
		 *         with a d10 roll).
		 */
		Integer get(int attackerel,int defenderel){
			int gap=defenderel-attackerel;
			if(gap<-4)
				gap=-4;
			else if(gap>4) gap=4;
			return chances.get(gap);
		}
	}

	/** Should probably move this to {@link Portal}? */
	public static Integer currentel=1;

	/** @see #getel() */
	public List<Combatant> squad=new ArrayList<>();

	/**
	 * Current target to move towards and attack.
	 *
	 * @see #choosetarget()
	 * @see Location#destroy(Incursion)
	 */
	public Actor target=null;

	/** @see #describe() */
	protected String description="Enemy incursion";

	/**
	 * @param x {@link World} coordinate.
	 * @param y {@link World} coordinate.
	 * @param squadp See {@link #currentel}.
	 * @param r See {@link Actor#realm}.
	 */
	public Incursion(final int x,final int y,List<Combatant> squadp,Realm r){
		this.x=x;
		this.y=y;
		if(squadp==null){
			ArrayList<Terrain> terrains=new ArrayList<>(1);
			terrains.add(Terrain.get(x,y));
			squad.addAll(Fight.generate(Incursion.currentel,terrains));
			currentel+=1;
		}else
			squad.addAll(squadp);
		realm=r;
	}

	/**
	 * Incursions only move once a day but this is balanced by several implicit
	 * benefits like canonical {@link #squad} HP not decreasing after a battle
	 * that has been won, jumping over certain {@link Location}s, not having to
	 * deal with {@link RandomEncounter}...
	 */
	void move(){
		choosetarget();
		if(target==null){
			displace();
			return;
		}
		final int targetx=target.x;
		final int targety=target.y;
		int newx=x+determinemove(x,targetx);
		int newy=y+determinemove(y,targety);
		if(Terrain.get(newx,newy).equals(Terrain.WATER)){
			displace();
			return;
		}
		Actor arrived=World.get(newx,newy,World.getactors());
		x=newx;
		y=newy;
		place();
		if(arrived!=null) attack(arrived);
	}

	/** Can be used from {@link Debug} to simulate captures. */
	public void attack(Actor target){
		fightdefenders(target);
		Boolean status=target.destroy(this);
		if(status==null) return;
		if(status)
			target.remove();
		else
			remove();
	}

	void fightdefenders(Actor target){
		if(target instanceof Squad) return;
		if(target instanceof Location&&((Location)target).ishostile()) return;
		District d=target.getdistrict();
		if(d==null||d.town.ishostile()) return;
		ArrayList<Squad> defenders=d.getsquads();
		if(defenders.isEmpty()) return;
		RPG.pick(defenders).destroy(this);
		throw new RuntimeException("Squad#destroy should throw StartBattle!");
	}

	/**
	 * Updates {@link #target} when first called or if it's been destroyed. Will
	 * not consider {@link Actor#impermeable} targets, those from teh same
	 * {@link Actor#realm} or targets that would require the Incursion to cross
	 * water to get there. If this results in no potential target, will assgign
	 * <code>null</code> to {@link #target}.
	 *
	 * Once tha filtering is done, will select the closest target that allows for
	 * the {@link #PREFERREDVICTORYCHANCE} of winning. If none is available, will
	 * use the nearest valid target.
	 */
	protected void choosetarget(){
		final ArrayList<Actor> actors=World.getactors();
		List<Actor> targets=new ArrayList<>();
		int vision=Math.max(1,
				(Squad.perceive(true,true,true,squad)+Terrain.get(x,y).visionbonus)/5);
		for(final Actor a:actors)
			if(!a.impermeable&&a.realm!=realm&&a.distanceinsteps(x,y)<=vision
					&&!crosseswater(this,a.x,a.y))
				targets.add(a);
		if(targets.isEmpty()){
			target=null;
			return;
		}
		final int incursionel=getel();
		targets.sort((o1,o2)->o1.getel(incursionel)-o2.getel(incursionel));
		for(Actor a:targets)
			if(a.getel(incursionel)<incursionel){
				target=a;
				return;
			}
		target=null;
	}

	/**
	 * @return Checks if there is any body of water between these two actors.
	 */
	public static boolean crosseswater(Actor from,int tox,int toy){
		if(Terrain.get(tox,toy).equals(Terrain.WATER)) return true;
		int x=from.x;
		int y=from.y;
		while(x!=tox||y!=toy){
			x+=determinemove(x,tox);
			y+=determinemove(y,toy);
			if(Terrain.get(x,y).equals(Terrain.WATER)) return true;
		}
		return false;
	}

	static int determinemove(final int me,final int target){
		if(target==me) return 0;
		return target>me?+1:-1;
	}

	@Override
	public void turn(long time,WorldScreen world){
		move();
	}

	/**
	 * Creates and places a new incursion. Finds an empty spot close to the given
	 * coordinates.
	 *
	 * @param r See {@link Actor#realm}.
	 * @param xp Starting {@link World} coordinate.
	 * @param yp Starting {@link World} coordinate.
	 * @param squadp See {@link Incursion#squad}.
	 * @see Actor#place()
	 */
	@SuppressWarnings("unused")
	static Incursion place(Realm r,int xp,int yp,List<Combatant> squadp){
		if(Javelin.DEBUG&&!SPAWN) return null;
		int size=World.scenario.size;
		ArrayList<Actor> actors=World.getactors();
		int x=xp;
		int y=yp;
		while(World.get(x,y,actors)!=null||Terrain.get(x,y).equals(Terrain.WATER)){
			int delta=DELTAS[RPG.r(DELTAS.length)];
			if(RPG.chancein(2))
				x+=delta;
			else
				y+=delta;
			if(x<0||x>=size) x=xp;
			if(y<0||y>=size) y=yp;
		}
		Incursion i=new Incursion(x,y,squadp,r);
		i.place();
		return i;
	}

	@Override
	public Boolean destroy(Incursion attacker){
		/* don't inline the return statement: null bug */
		if(attacker.realm==realm) return ignore(attacker);
		return fight(attacker.getel(),getel());
	}

	/**
	 * Helper method for {@link #destroy(Incursion)}
	 *
	 * @return <code>null</code>.
	 */
	static public Boolean ignore(Actor attacker){
		attacker.displace();
		return null;
	}

	@Override
	public Integer getel(Integer attackerel){
		return getel();
	}

	/**
	 * @return Encounter level for {@link #squad}.
	 * @see ChallengeCalculator#calculateel(List)
	 */
	public int getel(){
		return ChallengeCalculator.calculateel(squad);
	}

	/**
	 * Helper method for {@link #destroy(Incursion)}. Uses a percentage to decide
	 * which combatant wins (adapted from CCR).
	 *
	 * @param attacker Encounter level.
	 * @param defender Encounter level. {@link Integer#MIN_VALUE} means an
	 *          automatic victory for the attacker.
	 * @return <code>true</code> if attacker wins.
	 */
	public static boolean fight(int attacker,int defender){
		return defender==Integer.MIN_VALUE
				||RPG.r(1,10)<=VICTORYCHANCES.get(attacker,defender);
	}

	@Override
	public String toString(){
		return description;
	}

	@Override
	public boolean interact(){
		if(Debug.disablecombat){
			remove();
			return true;
		}
		var description=Location.describe(squad,toString(),true,this);
		if(!Location.headsup(description)) return false;
		throw new StartBattle(new IncursionFight(this));
	}

	@Override
	public List<Combatant> getcombatants(){
		return squad;
	}

	@Override
	public Image getimage(){
		if(squad==null) return null;
		Combatant leader=null;
		for(Combatant c:squad)
			if(leader==null||c.source.cr>leader.source.cr) leader=c;
		return Images.get(leader.source.avatarfile);
	}

	@Override
	public String describe(){
		var enemies=Squad.active.spotenemies(squad,this);
		String description=this.description+" ("+Difficulty.describe(squad)
				+" fight)";
		if(enemies.isEmpty())
			description+=".";
		else
			description+=":\n\n"+enemies;
		return description;
	}

	/**
	 * TODO why isn't this being used?
	 *
	 * @return Like {@link #fight(int, int)} but also damage the survivor, killing
	 *         off creatures according to the gravity of battle.
	 */
	public boolean fight(List<Combatant> defenders){
		int me=getel();
		int them=ChallengeCalculator.calculateel(defenders);
		boolean win=fight(me,them);
		if(win)
			damage(squad,VICTORYCHANCES.get(me,them));
		else
			damage(defenders,VICTORYCHANCES.get(them,me));
		return win;
	}

	/** TODO this hasn't been properly tested */
	void damage(List<Combatant> survivors,int chance){
		int totalcr=0;
		for(Combatant c:survivors)
			totalcr+=c.source.cr;
		float damage=totalcr*(1-chance/10f);
		LinkedList<Combatant> wounded=new LinkedList<>(survivors);
		Collections.sort(wounded,CombatantByCr.SINGLETON);
		while(damage>0&&survivors.size()>1){
			Combatant dead=wounded.pop();
			survivors.remove(dead);
			damage-=dead.source.cr;
		}
	}

	/**
	 * @return All Incursions in the game {@link World}.
	 */
	public static ArrayList<Incursion> getall(){
		ArrayList<Incursion> all=new ArrayList<>();
		for(Actor a:World.getall(Incursion.class))
			if(a instanceof Incursion) all.add((Incursion)a);
		return all;
	}

	/**
	 * @param l Possibly spawns an Incursion here.
	 */
	public static void raid(Location l){
		if(l.garrison.size()<2) return;
		Fortification f=l instanceof Fortification?(Fortification)l:null;
		int target;
		if(l instanceof Town)
			target=((Town)l).population;
		else if(f!=null&&f.targetel!=null)
			target=f.targetel;
		else{
			int day=Math.round(Math.round(WorldScreen.lastday));
			target=Math.min(20,20*day/400);
		}
		if(ChallengeCalculator.calculateel(l.garrison)<=target+2) return;
		l.garrison.sort(CombatantByCr.SINGLETON);
		List<Combatant> incursion=new ArrayList<>(
				l.garrison.subList(0,l.garrison.size()/2));
		l.garrison.removeAll(incursion);
		Incursion.place(l.realm,l.x,l.y,incursion);
		if(f!=null&&f.targetel!=null) f.targetel+=1;
	}
}

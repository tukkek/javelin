package javelin.model.world;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.comparator.CombatantByCr;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.IncursionFight;
import javelin.controller.content.fight.RandomEncounter;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
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
		int newx=x+determinemove(x,target.x);
		int newy=y+determinemove(y,target.y);
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
		//		fightdefenders(target);
		Boolean status=target.destroy(this);
		if(status==null) return;
		if(status)
			target.remove();
		else
			remove();
	}

	/** TODO disabled */
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
		var vision=Math.max(1,
				Squad.perceive(true,true,true,squad)+Terrain.get(x,y).visionbonus/5);
		var targets=World.getactors().stream()
				.filter(a->!a.impermeable&&a.realm!=realm&&!(a instanceof Incursion)
						&&a.distanceinsteps(x,y)<=vision&&!crosseswater(this,a.x,a.y))
				.collect(Collectors.toList());
		if(targets.isEmpty()){
			target=null;
			return;
		}
		var el=getel();
		targets.sort((o1,o2)->Integer.compare(o1.getel(el),o2.getel(el)));
		for(var a:targets)
			if(a instanceof Incursion&&realm==a.realm){
				target=a;
				return;
			}
		for(var a:targets)
			if(a.getel(el)<el){
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

	/** Finds an suitable spot and places the incursion. */
	@SuppressWarnings("unused")
	public static void spawn(Incursion i){
		if(Javelin.DEBUG&&!SPAWN) return;
		int size=World.scenario.size;
		var actors=World.getactors();
		var p=new Point(i.x,i.y);
		var tries=0;
		while(!p.validate(0,0,size,size)||World.get(p.x,p.y,actors)!=null
				||Terrain.get(p.x,p.y).equals(Terrain.WATER)){
			p.displace();
			tries+=1;
			if(tries==20){
				p=new Point(i.x,i.y);
				tries=0;
			}
		}
		i.setlocation(p);
		i.place();
	}

	@Override
	public Boolean destroy(Incursion attacker){
		if(attacker.realm!=realm) return fight(attacker.getel(),getel());
		squad.addAll(attacker.squad);
		return false;
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
		return Images.get(List.of("monster",leader.source.avatarfile));
	}

	@Override
	public String describe(){
		var enemies=Squad.active.scout(squad,this);
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
}

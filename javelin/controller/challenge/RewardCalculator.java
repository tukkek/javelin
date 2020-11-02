package javelin.controller.challenge;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.world.World;
import javelin.old.RPG;

/**
 * Determines experience points and treasure to be awarded after winning a
 * battle. Rules for this are found in the core d20 rules and also on Upper
 * Krust's work which is repackaged with permition on the 'doc' directory.
 *
 * Gold- and experience- wise, a guideline is that values double every 2 levels
 * - that doesn't hold up mathematically on a level-by-level basis but it's a
 * decent-enough simplification and overview. This is also the baseline of how
 * Encounter Levels work as per the official rules.
 *
 * @author alex
 */
public class RewardCalculator{
	static class TableLine{
		final double a,b,c,d,e,f,g,h;

		public TableLine(final double a,final double b,final double c,
				final double d,final double e,final double f,final double g,
				final double h){
			super();
			this.a=a;
			this.b=b;
			this.c=c;
			this.d=d;
			this.e=e;
			this.f=f;
			this.g=g;
			this.h=h;
		}

		public double getValue(final int partysize){
			if(partysize==1) return a;
			if(partysize==2) return b;
			if(partysize==3) return c;
			if(partysize<=5) return d;
			if(partysize<=7) return e;
			if(partysize<=11) return f;
			if(partysize<=15) return g;
			if(partysize<=23) return h;
			throw new RuntimeException("Can't calculate party of more than 23");
		}
	}

	static Map<Integer,TableLine> table=new TreeMap<>();

	static{
		table.put(-12,
				new TableLine(18.75,9.375,6.25,4.6875,3.125,2.34375,1.5625,1.171875));
		table.put(-11,
				new TableLine(25,12.5,9.375,6.25,4.6875,3.125,2.34375,1.5625));
		table.put(-10,
				new TableLine(37.5,18.75,12.5,9.375,6.250,4.6875,3.125,2.34375));
		table.put(-9,new TableLine(50,25,18.75,12.5,9.375,6.250,4.6875,3.125));
		table.put(-8,new TableLine(75,37.5,25,18.75,12.5,9.375,6.250,4.6875));
		table.put(-7,new TableLine(100,50,37.5,25,18.75,12.5,9.3750,6.25));
		table.put(-6,new TableLine(150,75,50,37.5,25,18.75,12.50,9.375));
		table.put(-5,new TableLine(200,100,75,50,37.5,25,18.75,12.5));
		table.put(-4,new TableLine(300,150,100,75,50,37.5,25,18.75));
		table.put(-3,new TableLine(400,200,150,100,75,50,37.5,25));
		table.put(-2,new TableLine(600,300,200,150,100,75,50,37.5));
		table.put(-1,new TableLine(800,400,300,200,150,100,75,50));
		table.put(0,new TableLine(1200,600,400,300,200,150,100,75));
		table.put(1,new TableLine(1600,800,600,400,300,200,150,100));
		table.put(2,new TableLine(2400,1200,800,600,400,300,200,150));
		table.put(3,new TableLine(3200,1600,1200,800,600,400,300,200));
		table.put(4,new TableLine(4800,2400,1600,1200,800,600,400,300));
	}

	static double getcharacterxp(int eldifference,final int nsurvivors){
		if(eldifference>4)
			eldifference=4;
		else if(eldifference<-12) eldifference=-12;
		return table.get(eldifference).getValue(nsurvivors);
	}

	static double getpartyxp(int eldifference,int nsurvivors,float bonus){
		return nsurvivors*.8*getcharacterxp(eldifference,nsurvivors)*bonus/1000.0;
	}

	/**
	 * @param team Opponent force defeated.
	 * @return sum of gold this battle should reward.
	 * @see #getgold(float)
	 */
	public static int receivegold(final List<Combatant> team){
		int sum=0;
		for(final Combatant m:team)
			sum+=getgold(ChallengeCalculator.calculatecr(m.source));
		return sum;
	}

	/**
	 * @param cr Given a challenge rating...
	 * @return gold treasure reward for such an opponent.
	 */
	public static int getgold(final float cr){
		if(cr<=0) return 0;
		float gold=cr*cr*cr*7.5f;
		if(World.scenario!=null) gold*=World.scenario.boost;
		return Math.round(gold);
	}

	/**
	 * Calculates proper experience reward for a given battle and distributes in a
	 * non-uniform manner. d20 level caps are exponential but since Javelin uses
	 * challenge rating as XP it becomes more linear - to circumvent that this
	 * method distributes 1 xp part to the strongest unit (including it's xp
	 * bank), 2 to the second strongest, etc. This may look random at first but in
	 * the long run ensures {@link Combatant}s will level up in a balanced manner,
	 * with lower level units gaining levels faster than already strong units -
	 * which is what the d20 system is designed to do.
	 *
	 * @param winners {@link Combatant}s to award XP to.
	 * @param team Allied team that started the battle.
	 * @param el Encounter Level overcome.
	 * @param bonus Multiplier bonus.
	 * @return A string representing how much XP was gained by the party.
	 * @see Combatant#xp
	 */
	public static String rewardxp(List<Combatant> team,int el,float bonus){
		int elblue=ChallengeCalculator.calculateelfromcrs(
				team.stream().map((c)->c.source.cr+Math.max(0,c.xp.floatValue()))
						.collect(Collectors.toList()));
		int eldifference=Math.round(el-elblue);
		if(World.scenario!=null) bonus*=World.scenario.boost;
		double partycr=getpartyxp(eldifference,team.size(),bonus);
		distributexp(team,partycr);
		BigDecimal xp=new BigDecimal(100*partycr).setScale(0,RoundingMode.UP);
		return "Party earns "+xp+"XP!";
	}

	/**
	 * @param enemies Converts to EL and passes to
	 *          {@link #rewardxp(List, int, float)}.
	 * @see ChallengeCalculator#calculateel(List)
	 */
	public static String rewardxp(List<Combatant> team,List<Combatant> enemies,
			float bonus){
		var crs=enemies.stream().map(c->c.source.cr).collect(Collectors.toList());
		return rewardxp(team,ChallengeCalculator.calculateelfromcrs(crs),bonus);
	}

	/**
	 * This discounts a linear parcel for any mercenaries involved and then
	 * distributes the remainder in a parcel according to unit power, with weaker
	 * units receiving more XP, as to emulate offical d20 XP progressions. This is
	 * necessary because Javelin actually uses a CR value (100XP = 1CR) instead of
	 * the official exponential XP tables.
	 *
	 * @param units {@link Combatant}s to receive experience. Summons and
	 *          mercenaries don't receive XP.
	 * @param xp Total amount of experience to be distributed.
	 * @see #rewardxp(ArrayList, List, List, int)
	 */
	public static void distributexp(List<Combatant> units,double xp){
		units=units.stream().filter(u->!u.summoned).collect(Collectors.toList());
		var members=units.stream().filter(u->!u.mercenary)
				.collect(Collectors.toList());
		if(members.isEmpty()) return;
		xp=xp*members.size()/units.size();
		var power=members.stream().sequential()
				.map(m->m.xp.doubleValue()+m.source.cr).collect(Collectors.toList());
		var leader=power.stream().max((a,b)->Double.compare(a,b)).orElse(null);
		var reversepower=power.stream().sequential().map(m->leader-m+1)
				.collect(Collectors.toList());
		var total=reversepower.stream().collect(Collectors.summingDouble(p->p));
		for(var i=0;i<members.size();i++){
			var m=members.get(i);
			m.xp=m.xp.add(new BigDecimal(xp*reversepower.get(i)/total));
		}
	}

	public static int calculatepcequipment(int level){
		return level*level*level*100;
	}

	public static int calculatenpcequipment(int level){
		return level*level*level*25;
	}

	/**
	 * @param pool Given an amount of gold, will try return a list of items equal
	 *          of similar value.
	 * @param selection All items that can be generated. Will be passed to
	 *          {@link Item#randomize(Collection)}.
	 * @param nitems Target number of items. Not guaranteed to be an exact match.
	 * @return Empty list if could not generate any items.
	 */
	static public ArrayList<Item> generateloot(int pool,int nitems,
			Collection<Item> selection){
		var items=new ArrayList<Item>(nitems);
		var floor=nitems==1?pool*.8:pool/nitems;
		for(Item i:Item.randomize(selection)){
			if(!(floor<=i.price&&i.price<pool)) continue;
			pool-=i.price;
			items.add(i.clone());
		}
		return items;
	}

	/**
	 * {@link #getgold(float)} is "correct", however it always returns the exact
	 * value in gold for a given input. To mitigate this, this method will return
	 * a range, usually between <code>gold(cr-1)</code> and
	 * <code>gold(cr+1)</code>.
	 *
	 * Since d20 progresson rules are exponential, this method goes through an
	 * extra layer to provide fairness since a naive approach between cr-1 and
	 * cr+1 is more likely to provide results higher than lower. This is decided
	 * by a prior 50% chance of giving either a "lower" or "higher" result and
	 * then proceeds from there to determine it (for example: either
	 * <code>[cr-1,cr]</code> or <code>[cr,cr+1]</code>.
	 *
	 * @param cr As in {@link #getgold(float)}.
	 * @param variance How much leeway to grant the given CR, up or down (use more
	 *          than 1 with caution).
	 * @return A random, rounded amount of gold.
	 * @see Javelin#round(int)
	 */
	public static int getgold(int cr,int variance){
		if(Javelin.DEBUG&&variance<=0)
			throw new RuntimeException("Negative variance!");
		int min;
		int max;
		if(RPG.chancein(2)){
			min=cr-RPG.r(1,variance);
			max=cr;
		}else{
			min=cr;
			max=cr+RPG.r(1,variance);
		}
		return Javelin.round(RPG.r(getgold(min),getgold(max)));
	}
}

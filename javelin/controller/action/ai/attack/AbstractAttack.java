package javelin.controller.action.ai.attack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.action.Action;
import javelin.controller.action.ActionCost;
import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.BattleAi;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.Node;
import javelin.controller.audio.Audio;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.Maneuver;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.feat.attack.Cleave;
import javelin.model.unit.skill.Bluff;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * In Javelin 1.7 a new approach of rolling one d20 to resolve attack sequences
 * will be attempted. This is a major win for the game because latter iterative
 * attacks are unlikely to hit, especially on higher levels and represent a
 * major pain for players - even more than inthe tabletop game where it's a
 * constant source of house rules and criticism. It will also help eliminate the
 * number of {@link BattleState} during {@link BattleAi} machinations, which
 * could represent something between a small to huge performance improvement. It
 * also overall helps with attack feeling more powerful and less
 * "choppy"/confusing gameplay overall and removes some implementation
 * complexity of keeping track of {@link AttackSequence} state.
 *
 * The first attack of the sequence will take a {@link ActionCost#STANDARD}
 * action, which is equivalent to both a single-attack sequence and a standard
 * (non-full) attack action. The rest of the attacks take a total of
 * {@link ActionCost#SWIFT} action, which each attempted attack taking an equal
 * share of that total. This makes a full-attack equivalent to a full turn of a
 * Standard, a Swift and a {@link ActionCost#FIVEFOOTSTEP} (1AP total).
 *
 * If an attack fails, the remainder of the sequence is halted, adding an amount
 * of dynamism and unpredactibility to attacking and improving performance since
 * later attacks would fail anyway, assuming they are ordered by attack bonus.
 *
 * Implementation details: for each 5% chance of a d20, a cache is created
 * holding the result of each attack. This cache will determine as equal any
 * rolls with the same results, so that outcome chances can be calculated
 * appropriately instead of generating {@link BattleState}s for all 20
 * possibilities. A second pass will then resolve criticals and a third pass
 * will calculate damage.
 *
 * @author alex
 */
public abstract class AbstractAttack extends Action implements AiAction{
	/**
	 * Inspired by (but deals minimum damage instead of half)
	 * https://dnd-wiki.org/wiki/Graze_Damage_(3.5e_Variant_Rule)#dynamic_user_navbox
	 *
	 * Found the link when looking for a less miss-prone variant combat rules.
	 */
	static final boolean GRAZE=true;
	/**
	 * If <code>true</code>, always applies average damage. Disabled because there
	 * doesn't seem to be any drastic performance improvement from it.
	 */
	static final boolean FLATDAMAGE=false;

	class DamageNode extends ChanceNode{
		public DamageChance damage;

		DamageNode(Node n,DamageChance damage,String action,Javelin.Delay delay,
				Combatant active,Combatant target,String audio){
			super(n,damage.chance,action,delay);
			this.damage=damage;
			overlay=new AiOverlay(target.location[0],target.location[1]);
			this.audio=target.hp<=0?new Audio("die",target):new Audio(audio,active);
		}
	}

	/** Manuever to be applied. */
	protected Strike maneuver=null;
	/** @see Bluff#feign(Combatant) */
	protected boolean feign=false;
	/** @see Cleave */
	protected boolean cleave=false;

	String soundhit;
	String soundmiss;

	/** Constructor. */
	public AbstractAttack(String name,Strike s,String hitsound,String misssound){
		super(name);
		maneuver=s;
		soundhit=hitsound;
		soundmiss=misssound;
	}

	/** @return A bonus or penalty to damage. */
	@SuppressWarnings("unused")
	protected int getdamagebonus(Combatant attacker,Combatant target){
		return 0;
	}

	/**
	 * Always a full attack (1AP) but divided among the {@link AttackSequence}.
	 * This would penalize creatures with only one attack so max AP cost is .5 per
	 * attack.
	 *
	 * If a {@link #CURRENTMANEUVER} is being used, returns {@link Maneuver}
	 * instead.
	 */
	float calculateattackap(AttackSequence attacks){
		if(maneuver!=null) return maneuver.ap;
		int nattacks=attacks.size();
		if(nattacks==1) return .5f;
		/* if we let ap=.5 in this case it means that a combatant with a 2-attack
		 * sequence is identical to one with 1 attack */
		if(nattacks==2) return .4f;
		return 1f/nattacks;
	}

	abstract List<AttackSequence> getattacks(Combatant active);

	static void validate(final List<DamageChance> chances){
		var sum=chances.stream().collect(Collectors.summingDouble(a->a.chance));
		if(!(0.999<sum&&sum<=1.001))
			throw new RuntimeException("Attack sum not whole: "+sum);
	}

	String posthit(Combatant c,Combatant target,Attack a,float ap,DamageChance dc,
			BattleState s){
		if(target.hp>0){
			if(dc.save!=null){
				target.source=target.source.clone();
				c.source=c.source.clone();
				return a.geteffect().cast(c,target,dc.save,s,null);
			}
		}else if(cleave) c.cleave(ap);
		return null;
	}

	/**
	 * @param c Checks if swimmer.
	 * @return The penalty for attacking while standing on water (same as the
	 *         bonus for being attacked while staning on water).
	 */
	static int waterpenalty(BattleState s,Combatant c){
		return c.source.swim()>0&&s.map[c.location[0]][c.location[1]].flooded?2:0;
	}

	/**
	 * @param target Target of the attack
	 * @return Positive integer describing a penalty.
	 */
	protected int getpenalty(Combatant c,Combatant target,BattleState s){
		var penalty=waterpenalty(s,c)-waterpenalty(s,target)+target.surprise();
		if(target.burrowed) penalty+=4;
		return penalty;
	}

	/**
	 * @return the chance of at least 1 out of 2 independent events happening,
	 *         given two percentage odds (1 = 100%).
	 */
	static public float or(float a,float b){
		return a+b-a*b;
	}

	/**
	 * @param attackbonus Bonus of the given any extraordinary bonuses (such as +2
	 *          from charge). Most common chances are calculated here or by the
	 *          concrete class.
	 * @return A bound % chance of an attack completely missing it's target.
	 * @see #bind(float)
	 */
	public float misschance(BattleState s,Combatant c,Combatant target,
			int attackbonus){
		var misschance=(target.getac()+getpenalty(c,target,s)-attackbonus)/20f;
		return Action.bind(or(misschance,target.source.misschance));
	}

	/** @return An estimate of the chance of hitting an attack ("easy to hit"). */
	public String getchance(Combatant c,Combatant target,Attack a,BattleState s){
		var misschance=misschance(s,c,target,a.bonus);
		return Javelin.translatetochance(Math.round(20*misschance))+" to hit";
	}

	DamageNode miss(Combatant c,Attack a,Combatant target,Strike m,
			DamageChance dc,BattleState s){
		if(feign&&target.source.dexterity>=12){
			s=s.clone();
			target=s.clone(target);
			Bluff.feign(c,target);
		}
		String name;
		Delay wait;
		if(m==null){
			name=target.toString();
			wait=Delay.WAIT;
		}else{
			name=m.name.toLowerCase();
			wait=Delay.BLOCK;
		}
		var output=c+" misses "+name+" ("+getchance(c,target,a,s)+")...";
		return new DamageNode(s,dc,output,wait,c,target,soundmiss);
	}

	DamageNode createnode(Combatant attacker,Combatant target,Attack a,float ap,
			Strike m,DamageChance dc,BattleState s){
		if(dc.damage==0) return miss(attacker,a,target,m,dc,s);
		s=s.clone();
		attacker=s.clone(attacker);
		target=s.clone(target);
		if(m!=null) m.prehit(attacker,target,a,dc,s);
		var name=m==null?a.name:m.name.toLowerCase();
		var lines=new ArrayList<String>(5);
		var tohit=" ("+getchance(attacker,target,a,s)+")...";
		lines.add(attacker+" "+dc.message+" "+target+" with "+name+tohit);
		if(dc.critical) lines.add("Critical hit!");
		var monster=target.source;
		if(dc.damage==0)
			lines.add("Damage absorbed!");
		else{
			var resistance=a.energy?monster.energyresistance:monster.dr;
			target.damage(dc.damage,s,resistance);
			lines.add(target+" is "+target.getstatus()+".");
			var posthit=posthit(attacker,target,a,ap,dc,s);
			if(posthit!=null) lines.add(posthit);
		}
		if(m!=null) m.posthit(attacker,target,a,dc,s);
		var wait=monster.passive
				&&target.getnumericstatus()>Combatant.STATUSUNCONSCIOUS;
		var delay=wait?Delay.WAIT:Delay.BLOCK;
		var output=String.join("\n",lines);
		return new DamageNode(s,dc,output,delay,attacker,target,soundhit);
	}

	static List<DamageChance> hit(Attack a,float hitchance,int multiplier,
			Boolean savep){
		if(hitchance==0) return List.of();
		if(FLATDAMAGE) return List.of(
				new DamageChance(hitchance,a.getaveragedamage(),multiplier!=1,savep));
		var effetc=a.geteffect();
		var damagerolls=Action.distributeroll(a.damage[0],a.damage[1]).entrySet();
		return damagerolls.stream().map(roll->{
			var damage=Math.max(1,(roll.getKey()+a.damage[2])*multiplier);
			var chance=hitchance*roll.getValue();
			var save=effetc==null?null:savep;
			return new DamageChance(chance,damage,multiplier!=1,save);
		}).collect(Collectors.toList());
	}

	List<DamageChance> dealattack(Combatant active,Combatant target,Attack a,
			int bonus,BattleState s){
		bonus+=a.bonus;
		if(a.touch) bonus+=target.source.armor;
		var chances=new ArrayList<DamageChance>();
		var miss=misschance(s,active,target,bonus);
		chances.add(new DamageChance(miss,0,false,null));
		var graze=GRAZE?(target.getac()-target.gettouchac())/20f:0;
		if(graze>0){
			var dc=new DamageChance(graze,a.getminimumdamage(),false,null);
			dc.message="grazes";
			chances.add(dc);
		}
		var effect=target.source.passive?null:a.geteffect();
		var save=effect==null?1:effect.getsavechance(active,target);
		var nosave=1-save;
		var hit=1-miss-graze;
		var threat=(21-a.threat)/20f;
		var confirm=target.source.immunitytocritical?0:threat*hit;
		chances.addAll(hit(a,(hit-confirm)*save,1,true));
		chances.addAll(hit(a,(hit-confirm)*nosave,1,false));
		chances.addAll(hit(a,confirm*save,a.multiplier,true));
		chances.addAll(hit(a,confirm*nosave,a.multiplier,false));
		if(Javelin.DEBUG) validate(chances);
		return chances;
	}

	public List<ChanceNode> attack(Combatant attacker,Combatant target,Attack a,
			int attackbonus,int damagebonus,float ap,BattleState s){
		s=s.clone();
		attacker=s.clone(attacker);
		attacker.ap+=ap;
		if(maneuver!=null) maneuver.preattacks(attacker,target,a,s);
		var nodes=new ArrayList<ChanceNode>();
		for(DamageChance dc:dealattack(attacker,target,a,attackbonus,s)){
			if(dc.damage>0) dc.damage+=damagebonus;
			if(dc.damage<0) dc.damage=0;
			nodes.add(createnode(attacker,target,a,ap,maneuver,dc,s));
		}
		if(maneuver!=null) maneuver.postattacks(attacker,target,a,s);
		return nodes;
	}

	public List<ChanceNode> attack(final BattleState s,final Combatant current,
			final Combatant target,AttackSequence attacks,int bonus){
		final Attack a=attacks.getnext();
		final int damagebonus=getdamagebonus(current,target);
		final float ap=calculateattackap(
				getattacks(current).get(attacks.sequenceindex));
		return attack(current,target,a,bonus,damagebonus,ap,s);
	}

	@Override
	public boolean perform(Combatant active){
		throw new UnsupportedOperationException();
	}
}
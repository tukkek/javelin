package javelin.controller.action.ai.attack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.action.Action;
import javelin.controller.action.ActionCost;
import javelin.controller.ai.ChanceNode;
import javelin.controller.audio.Audio;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.skill.Bluff;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * Despite not being very large, {@link AbstractAttack} is extremely complex.
 * This new utility class is being created in the hopes of simplifying it after
 * a number of incarnations and revisions.
 *
 * @author alex
 */
public class AttackResolver{
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

	public class DamageNode extends ChanceNode{
		public DamageChance damage;

		DamageNode(Combatant attacker,Combatant target,BattleState state,
				DamageChance damage,String action,Delay delay,String audio){
			super(state,damage.chance,action,delay);
			this.damage=damage;
			overlay=new AiOverlay(target.location[0],target.location[1]);
			this.audio=target.hp<=0?new Audio("die",target):new Audio(audio,attacker);
		}
	}

	AbstractAttack action;
	public int attackbonus=0;
	public int damagebonus=0;
	public float misschance;
	float hitchance;
	public String chance;
	public float ap;
	Attack attack;
	AttackSequence sequence;
	Strike maneuver;

	/** Constructor. */
	public AttackResolver(AbstractAttack action,Combatant attacker,
			Combatant target,Attack a,AttackSequence sequence,BattleState state){
		this.action=action;
		attack=a;
		this.sequence=sequence;
		attackbonus+=a.bonus;
		if(a.touch) attackbonus+=target.source.armor;
		attackbonus-=action.getpenalty(attacker,target,state);
		damagebonus+=action.getdamagebonus(attacker,target);
		misschance=(target.getac()-attackbonus)/20f;
		misschance=Action.bind(Action.or(misschance,target.source.misschance));
		hitchance=1-misschance;
		chance=Javelin.translatetochance(Math.round(20*misschance))+" to hit";
		ap=sequence.indexOf(a)==0?ActionCost.STANDARD
				:ActionCost.SWIFT/(sequence.size()-1);
		maneuver=action.maneuver;
	}

	static void validate(final List<DamageChance> chances){
		var sum=chances.stream().collect(Collectors.summingDouble(a->a.chance));
		if(!(0.999<sum&&sum<=1.001))
			throw new RuntimeException("Attack sum not whole: "+sum);
	}

	DamageNode miss(Combatant c,Combatant target,BattleState s,DamageChance dc){
		if(action.feign&&target.source.dexterity>=12) Bluff.feign(c,target);
		String name;
		Delay wait;
		if(maneuver==null){
			name=target.toString();
			wait=Delay.WAIT;
		}else{
			name=maneuver.name.toLowerCase();
			wait=Delay.BLOCK;
		}
		var output=c+" misses "+name+" ("+chance+")...";
		return new DamageNode(c,target,s,dc,output,wait,action.soundmiss);
	}

	List<DamageChance> hit(float hitchance,int multiplier,Boolean savep){
		if(hitchance==0) return List.of();
		if(FLATDAMAGE) return List.of(new DamageChance(hitchance,
				attack.getaveragedamage(),multiplier!=1,savep));
		var effetc=attack.geteffect();
		var damagerolls=Action.distributeroll(attack.damage[0],attack.damage[1])
				.entrySet();
		return damagerolls.stream().map(roll->{
			var damage=Math.max(1,(roll.getKey()+attack.damage[2])*multiplier);
			var chance=hitchance*roll.getValue();
			var save=effetc==null?null:savep;
			return new DamageChance(chance,damage,multiplier!=1,save);
		}).collect(Collectors.toList());
	}

	List<DamageChance> dealattack(Combatant c,Combatant target){
		var chances=new ArrayList<DamageChance>();
		chances.add(new DamageChance(misschance,0,false,null));
		var graze=GRAZE?(target.getac()-target.gettouchac())/20f:0;
		if(graze>0){
			var dc=new DamageChance(graze,attack.getminimumdamage(),false,null);
			dc.message="grazes";
			chances.add(dc);
		}
		var effect=target.source.passive?null:attack.geteffect();
		var save=effect==null?1:effect.getsavechance(c,target);
		var nosave=1-save;
		var hit=1-misschance-graze;
		var threat=(21-attack.threat)/20f;
		var confirm=target.source.immunitytocritical?0:threat*hit;
		chances.addAll(hit((hit-confirm)*save,1,true));
		chances.addAll(hit((hit-confirm)*nosave,1,false));
		chances.addAll(hit(confirm*save,attack.multiplier,true));
		chances.addAll(hit(confirm*nosave,attack.multiplier,false));
		if(Javelin.DEBUG) AttackResolver.validate(chances);
		return chances;
	}

	String posthit(Combatant c,Combatant target,BattleState s,DamageChance dc){
		if(target.hp>0){
			if(dc.save!=null) return attack.geteffect().cast(c,target,dc.save,s,null);
		}else if(action.cleave) c.cleave(ap);
		return null;
	}

	DamageNode createnode(Combatant c,Combatant target,BattleState s,
			DamageChance dc){
		s=s.clone();
		c=s.clone(c).clonesource();
		target=s.clone(target).clonesource();
		if(dc.damage>0) dc.damage+=damagebonus;
		if(dc.damage<0) dc.damage=0;
		if(dc.damage==0) return miss(c,target,s,dc);
		if(maneuver!=null) maneuver.prehit(c,target,attack,dc,s);
		var name=maneuver==null?attack.name:maneuver.name.toLowerCase();
		var lines=new ArrayList<String>(5);
		var tohit=" ("+chance+")...";
		lines.add(c+" "+dc.message+" "+target+" with "+name+tohit);
		if(dc.critical) lines.add("Critical hit!");
		if(dc.damage==0)
			lines.add("Damage absorbed!");
		else{
			var resistance=attack.energy?target.source.energyresistance
					:target.source.dr;
			target.damage(dc.damage,s,resistance);
			lines.add(target+" is "+target.getstatus()+".");
			var posthit=posthit(c,target,s,dc);
			if(posthit!=null) lines.add(posthit);
		}
		if(maneuver!=null){
			maneuver.posthit(c,target,attack,dc,s);
			maneuver.postattacks(c,target,attack,s);
		}
		var wait=target.source.passive
				&&target.getnumericstatus()>Combatant.STATUSUNCONSCIOUS;
		var delay=wait?Delay.WAIT:Delay.BLOCK;
		var output=String.join("\n",lines);
		return new DamageNode(c,target,s,dc,output,delay,action.soundhit);
	}

	public List<ChanceNode> attack(Combatant c,Combatant target,BattleState s){
		s=s.clone();
		c=s.clone(c).clonesource();
		target=s.clone(target).clonesource();
		c.ap+=ap;
		if(maneuver!=null) maneuver.preattacks(c,target,attack,s);
		var nodes=new ArrayList<ChanceNode>();
		for(var dc:dealattack(c,target))
			nodes.add(createnode(c,target,s,dc));
		return nodes;
	}
}

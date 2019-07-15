package javelin.controller.action.ai.attack;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.view.mappanel.battle.overlay.AiOverlay;
import javelin.view.screen.StatisticsScreen;

/**
 * Despite not being very large, {@link AbstractAttack} is extremely complex.
 * This new utility class is being created in the hopes of simplifying it after
 * a number of incarnations and revisions.
 *
 * @author alex
 */
public class AttackResolver{
	class DamageNode extends ChanceNode{
		DamageNode(Combatant attacker,Combatant target,BattleState state,
				float chance,String action,Delay delay,String audio){
			super(state,chance,action,delay);
			overlay=new AiOverlay(target);
			this.audio=target.hp<=0?new Audio("die",target):new Audio(audio,attacker);
		}

		@Override
		public boolean equals(Object o){
			var r=o instanceof DamageNode?(DamageNode)o:null;
			return r!=null&&action.equals(r.action);
		}

		@Override
		public int hashCode(){
			return action.hashCode();
		}
	}

	enum Outcome{
		MISS,
		/**
		 * Inspired by (but deals minimum damage instead of half)
		 * https://dnd-wiki.org/wiki/Graze_Damage_(3.5e_Variant_Rule)#dynamic_user_navbox
		 *
		 * Found the link when looking for a less miss-prone variant combat rules.
		 */
		GRAZE,HIT,CRITICAL_UNCONFIRMED,CRITICAL
	}

	class SequenceResult implements Cloneable{
		ArrayList<Outcome> outcomes;
		ArrayList<String> chances;

		public SequenceResult(){
			var size=sequence.size();
			outcomes=new ArrayList<>(size);
			chances=new ArrayList<>(size);
		}

		@Override
		public boolean equals(Object o){
			var r=o instanceof SequenceResult?(SequenceResult)o:null;
			return r!=null&&outcomes.equals(r.outcomes);
		}

		@Override
		public int hashCode(){
			var exponent=Outcome.values().length;
			var hash=0;
			for(int i=0;i<outcomes.size();i++)
				hash+=(outcomes.get(i).ordinal()+1)*Math.pow(exponent,i);
			return hash;
		}

		@Override
		public String toString(){
			return outcomes.toString();
		}

		@Override
		public SequenceResult clone(){
			try{
				var clone=(SequenceResult)super.clone();
				clone.outcomes=new ArrayList<>(outcomes);
				clone.chances=new ArrayList<>(chances);
				return clone;
			}catch(CloneNotSupportedException e){
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Bonus to applied to all {@link Attack}s. Not a preview, previews are
	 * calculated by also adding the first {@link Attack#bonus} of the
	 * {@link #sequence}.
	 */
	public int attackbonus=0;
	/** @see Attack#damage */
	public int damagebonus=0;
	/** @see #preview(Combatant, AttackSequence) */
	public Float misschance=null;
	/** @see #preview(Combatant, AttackSequence) */
	public Float hitchance=null;
	/** Human-text preview, see {@link #attackbonus}. */
	public String chance=null;
	/** Can be overriden to force a particular {@link ActionCost}. */
	public Float ap=null;

	List<Float> critical;
	AttackSequence sequence;
	AbstractAttack action;
	Strike maneuver;

	/** {@link AttackSequence} Constructor. */
	public AttackResolver(AbstractAttack action,Combatant attacker,
			Combatant target,AttackSequence sequence,BattleState state){
		this.action=action;
		this.sequence=sequence;
		sequence.sort();
		critical=new ArrayList<>(sequence.size());
		maneuver=action.maneuver;
		attackbonus-=action.getpenalty(attacker,target,state);
		damagebonus+=action.getdamagebonus(attacker,target);
	}

	/** Single-{@link Attack} constructor. */
	public AttackResolver(AbstractAttack action,Combatant attacker,
			Combatant target,Attack attack,BattleState state){
		this(action,attacker,target,new AttackSequence(List.of(attack)),state);
	}

	/** Calculates some fields to expose attack information and statistics. */
	public void preview(Combatant target){
		var preview=sequence.get(0);
		misschance=(target.getac()-attackbonus-preview.getbonus(target))/20f;
		misschance=Action.bind(Action.or(misschance,target.source.misschance));
		hitchance=1-misschance;
		chance=Javelin.getchance(Math.round(20*misschance))+" to hit";
	}

	static void validate(final Collection<Float> chances){
		var sum=chances.stream().collect(Collectors.summingDouble(c->c));
		if(!(0.999<sum&&sum<=1.001))
			throw new RuntimeException("Attack sum not whole: "+sum);
	}

	/*DamageNode miss(Combatant c,Combatant target,BattleState s,DamageChance dc){
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

	List<DamageChance> hit(Attack a,float hitchance,int multiplier,Boolean savep){
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

	String posthit(Attack a,Combatant c,Combatant target,BattleState s,
			DamageChance dc){
		if(target.hp>0){
			if(dc.save!=null) return a.geteffect().cast(c,target,dc.save,s,null);
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
		if(maneuver!=null) maneuver.hit(c,target,attack,dc,s);
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
		var wait=target.source.passive
				&&target.getnumericstatus()>Combatant.STATUSUNCONSCIOUS;
		var delay=wait?Delay.WAIT:Delay.BLOCK;
		var output=String.join("\n",lines);
		return new DamageNode(c,target,s,dc,output,delay,action.soundhit);
	}

	List<DamageChance> dealattack(Combatant c,Combatant target){
		var chances=new ArrayList<DamageChance>();
		chances.add(new DamageChance(misschance,0,false,null));
		var graze=(target.getac()-target.gettouchac())/20f;
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
	}*/

	/** @return Attack roll penalty equivalent to {@link Monster#misschance}. */
	int getmisspenalty(int bonus,Combatant target){
		var attackmisschance=Action.bind((target.getac()-bonus)/20f);
		var totalmisschance=Action.or(attackmisschance,target.source.misschance);
		return Math.round(Action.bind(totalmisschance-attackmisschance)*20);
	}

	SequenceResult dealattacks(int roll,Combatant target){
		var r=new SequenceResult();
		for(var a:sequence){
			var bonus=a.getbonus(target)+attackbonus;
			if(target.source.misschance>0) bonus-=getmisspenalty(bonus,target);
			var ac=target.getac();
			r.chances.add(Javelin.getchance(ac-bonus));
			final Outcome o;
			if(roll==1)
				o=Outcome.MISS;
			else if(roll>=a.threat){
				o=Outcome.CRITICAL_UNCONFIRMED;
				if(roll==20) critical.add(Action.bind((20+ac-bonus)/20f));
			}else if(roll+bonus>=ac)
				o=Outcome.HIT;
			else if(roll+bonus>=ac-target.source.armor)
				o=Outcome.GRAZE;
			else
				o=Outcome.MISS;
			r.outcomes.add(o);
			if(o==Outcome.MISS) break;
		}
		return r;
	}

	void confirm(HashMap<SequenceResult,Float> results){
		while(true){
			SequenceResult result=null;
			int i=0;
			for(var r:results.keySet()){
				i=r.outcomes.indexOf(Outcome.CRITICAL_UNCONFIRMED);
				if(i>=0){
					result=r;
					break;
				}
			}
			if(result==null) break;
			var chance=results.remove(result);
			var criticalchance=critical.get(i);
			result.outcomes.set(i,Outcome.CRITICAL);
			var previous=results.getOrDefault(result,0f);
			results.put(result,chance*criticalchance+previous);
			result=result.clone();
			result.outcomes.set(i,Outcome.HIT);
			previous=results.getOrDefault(result,0f);
		}
	}

	String damage(Combatant target,Outcome o,Attack a,BattleState s){
		final int damage;
		final String description;
		if(o==Outcome.GRAZE){
			damage=a.getminimumdamage()+damagebonus;
			description="graze";
		}else if(o==Outcome.HIT){
			damage=a.getaveragedamage()+damagebonus;
			description="hit";
		}else if(o==Outcome.CRITICAL){
			//TODO critical sound would be nice
			damage=(a.getaveragedamage()+damagebonus)*a.multiplier;
			description="CRITICAL";
		}else
			throw new InvalidParameterException(o.toString());
		target.damage(Math.max(1,damage),s,0);//TODO reduction
		return description;
	}

	ChanceNode apply(SequenceResult r,Float chance,BattleState s,Combatant c,
			Combatant target){
		s=s.clone();
		c=s.clone(c).clonesource();
		target=s.clone(target).clonesource();
		var descriptions=new ArrayList<String>(sequence.size());
		var hit=false;
		var ap=0f;
		for(int i=0;i<sequence.size();i++){
			var a=sequence.get(i);
			ap+=sequence.indexOf(a)==0?ActionCost.STANDARD
					:ActionCost.SWIFT/(sequence.size()-1);
			var chancetohit=" ("+r.chances.get(i)+" to hit)";
			var name=i==0?StatisticsScreen.capitalize(a.name):a.name;
			var o=r.outcomes.get(i);
			if(o==Outcome.MISS){
				descriptions.add(name+": miss"+chancetohit);
				break;
			}
			hit=true;
			var apply=maneuver!=null&&o!=Outcome.GRAZE;
			if(apply) maneuver.prehit(c,target,a,s);
			descriptions.add(name+": "+damage(target,o,a,s)+chancetohit);
			if(apply) maneuver.posthit(c,target,a,s);
			if(target.hp<=0) break;
		}
		c.ap+=this.ap==null?ap:this.ap;
		if(maneuver!=null) maneuver.postattacks(c,target,sequence,s);
		var delay=hit?Delay.BLOCK:Delay.WAIT;
		var sound=hit?action.soundhit:action.soundmiss;
		var message=c+" attacks "+target+"! "+String.join(", ",descriptions)+"...";
		if(hit) message+="\n"+target+" is "+target.getstatus()+".";
		return new DamageNode(c,target,s,chance,message,delay,sound);
	}

	List<ChanceNode> merge(List<ChanceNode> nodes){
		var merged=new HashMap<DamageNode,DamageNode>(nodes.size());
		for(var n:nodes){
			var dn=(DamageNode)n;
			var previous=merged.get(dn);
			if(previous==null)
				merged.put(dn,dn);
			else
				previous.chance+=dn.chance;
		}
		nodes.clear();
		nodes.addAll(merged.values());
		return nodes;
	}

	public List<ChanceNode> attack(Combatant attackerp,Combatant targetp,
			BattleState statep){
		var s=statep.clone();
		var c=s.clone(attackerp).clonesource();
		var target=s.clone(targetp).clonesource();
		if(maneuver!=null) maneuver.preattacks(c,target,sequence,s);
		var results=new HashMap<SequenceResult,Float>(20);
		for(var roll=1;roll<=20;roll++){
			var result=dealattacks(roll,target);
			var previous=results.get(result);
			var chance=1/20f;
			if(previous!=null) chance+=previous;
			results.put(result,chance);
		}
		if(Javelin.DEBUG) validate(results.values());
		confirm(results);
		if(Javelin.DEBUG) validate(results.values());
		var nodes=merge(results.entrySet().stream()
				.map(entry->apply(entry.getKey(),entry.getValue(),s,c,target))
				.collect(Collectors.toList()));
		if(Javelin.DEBUG)
			validate(nodes.stream().map(n->n.chance).collect(Collectors.toList()));
		return nodes;
	}
}

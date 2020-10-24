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
import javelin.controller.ai.AiThread;
import javelin.controller.ai.ChanceNode;
import javelin.controller.audio.Audio;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.skill.Bluff;
import javelin.view.mappanel.battle.overlay.AiOverlay;
import javelin.view.screen.StatisticsScreen;

/**
 * Despite not being very large, {@link AbstractAttack} is extremely complex.
 * This new utility class is being created in the hopes of simplifying it after
 * a number of incarnations and revisions.
 *
 * TODO ideally would have listeners instead of having the feature creep of
 * maneuvers, feats like cleave, skills like feign, etc, but where and when to
 * include those is critical to performance. During initialization would be the
 * cloest to 1:1 approach but not as perfomant as skipping feat/maneuver hooks
 * if that's not necessary at all.
 *
 * @author alex
 */
public class AttackResolver{
	class DamageNode extends ChanceNode{
		DamageNode(Combatant attacker,Combatant target,BattleState state,
				float chance,String message,boolean hit){
			super(state,chance,message,hit?Delay.BLOCK:Delay.WAIT);
			overlay=new AiOverlay(target);
			var action=AttackResolver.this.action;
			audio=target.hp<=0?new Audio("die",target)
					:new Audio(hit?action.soundhit:action.soundmiss,attacker);
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

	List<String> effects=new ArrayList<>(0);
	AttackSequence sequence;
	AbstractAttack action;
	List<Float> critical;
	Strike maneuver;

	/** {@link AttackSequence} Constructor. */
	public AttackResolver(AbstractAttack action,Combatant attacker,
			Combatant target,AttackSequence sequence,BattleState state){
		this.action=action;
		this.sequence=new AttackSequence(sequence);
		this.sequence.sort();
		critical=new ArrayList<>(this.sequence.size());
		maneuver=action.maneuver;
		if(maneuver!=null) ap=maneuver.ap;
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
			var m=target.source;
			if(m.misschance>0) bonus-=getmisspenalty(bonus,target);
			var ac=target.getac();
			r.chances.add(Javelin.getchance(ac-bonus));
			final Outcome o;
			if(roll==1)
				o=Outcome.MISS;
			else if(roll>=a.threat){
				o=m.immunitytocritical?Outcome.HIT:Outcome.CRITICAL_UNCONFIRMED;
				if(roll==20) critical.add(Action.bind((20+ac-bonus)/20f));
			}else if(roll+bonus>=ac)
				o=Outcome.HIT;
			else if(roll+bonus>=target.gettouchac())
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
			results.put(result,chance*(1-criticalchance)+previous);
		}
	}

	/** TODO shouldn't use random for damage/saves or AI will preempt outcomes. */
	String damage(Combatant c,Combatant target,Outcome o,Attack a,float ap,
			BattleState s){
		int damage;
		String description;
		if(o==Outcome.GRAZE){
			damage=a.getminimumdamage()+damagebonus;
			description="graze";
		}else if(o==Outcome.HIT){
			damage=a.rolldamage(AiThread.getrandom())+damagebonus;
			description="hit";
		}else if(o==Outcome.CRITICAL){
			//TODO critical sound would be nice
			damage=(a.rolldamage(AiThread.getrandom())+damagebonus)*a.multiplier;
			description="CRITICAL";
		}else
			throw new InvalidParameterException(o.toString());
		var apply=maneuver!=null&&o!=Outcome.GRAZE;
		if(apply) maneuver.prehit(c,target,a,s);
		var resistance=a.energy?target.source.energyresistance:target.source.dr;
		target.damage(Math.max(1,damage),s,resistance);
		var e=a.geteffect();
		if(e!=null&&!target.source.passive&&target.hp>0){
			var save=AiThread.getrandom().nextFloat()<=e.getsavechance(c,target);
			effects.add(e.cast(c,target,save,s,null));
		}
		if(apply) maneuver.posthit(c,target,a,s);
		if(target.hp<=0&&action.cleave) c.cleave(ap);
		return description;
	}

	String contextualize(Combatant c,Combatant target,boolean hit,
			ArrayList<String> attacks){
		var message=c+" attacks "+target+"!";
		message+="\n"+String.join("; ",attacks)+"...";
		if(!effects.isEmpty()) message+="\n"+String.join(" ",effects);
		effects.clear();
		if(hit) message+="\n"+target+" is "+target.getstatus()+".";
		return message;
	}

	ChanceNode apply(SequenceResult r,Float chance,BattleState s,Combatant c,
			Combatant target){
		s=s.clone();
		c=s.clone(c).clonesource();
		target=s.clone(target).clonesource();
		var attacks=new ArrayList<String>(sequence.size());
		var hit=false;
		var ap=0f;
		for(var i=0;i<sequence.size();i++){
			var a=sequence.get(i);
			var apcost=sequence.indexOf(a)==0?ActionCost.STANDARD
					:ActionCost.SWIFT/(sequence.size()-1);
			ap+=apcost;
			var chancetohit=" ("+r.chances.get(i)+" to hit)";
			var name=maneuver==null?a.name:maneuver.name;
			if(i==0) name=StatisticsScreen.capitalize(name);
			var o=r.outcomes.get(i);
			if(o==Outcome.MISS){
				if(action.feign&&target.source.dexterity>=12) Bluff.feign(c,target);
				attacks.add(name+": miss"+chancetohit);
				break;
			}
			hit=true;
			attacks.add(name+": "+damage(c,target,o,a,apcost,s)+chancetohit);
			if(target.hp<=0) break;
		}
		c.ap+=this.ap==null?ap:this.ap;
		if(maneuver!=null) maneuver.postattacks(c,target,sequence,s);
		var message=contextualize(c,target,hit,attacks);
		return new DamageNode(c,target,s,chance,message,hit);
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

	/** Initiates an Attack, {@link AttackSequence} or Strike. */
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
		confirm(results);
		var nodes=merge(results.entrySet().stream()
				.map(entry->apply(entry.getKey(),entry.getValue(),s,c,target))
				.collect(Collectors.toList()));
		return nodes;
	}
}

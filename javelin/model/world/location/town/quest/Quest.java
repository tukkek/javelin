package javelin.model.world.location.town.quest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.ContentSummary;
import javelin.controller.challenge.RewardCalculator;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;
import javelin.model.world.location.town.quest.basic.Discovery;
import javelin.model.world.location.town.quest.basic.Fetch;
import javelin.model.world.location.town.quest.basic.Kill;
import javelin.old.RPG;

/**
 * A task that can be completed for money and reputation. Non-hostile towns will
 * have an active number of them equal to their Rank tier. Quests are timed and
 * most of them require you to come back in that time frame to collect your
 * reward.
 *
 * @author alex
 */
public abstract class Quest implements Serializable{
	/** All available quest templates. */
	public final static Map<String,List<Class<? extends Quest>>> QUESTS=new HashMap<>();
	static final Class<? extends Quest> DEBUG=War.class;
	static final String BASIC="basic";

	static{
		QUESTS.put(BASIC,List.of(Kill.class,Fetch.class,Discovery.class));
		QUESTS.put(Trait.CRIMINAL,List.of());
		QUESTS.put(Trait.MAGICAL,List.of());
		QUESTS.put(Trait.EXPANSIVE,List.of());
		QUESTS.put(Trait.MERCANTILE,List.of());
		QUESTS.put(Trait.MILITARY,List.of(War.class));
		QUESTS.put(Trait.NATURAL,List.of());
		QUESTS.put(Trait.RELIGIOUS,List.of(Pilgrimage.class));
	}

	/** Town this quest was generated for. */
	public final Town town;
	/** Encounter level. Default is {@link Town#population}. */
	protected int el;
	/** Quest becomes invalid once it reaches zero. */
	public int daysleft;
	/**
	 * Name of the quest. Used as a locally-exclusive identifier per {@link Town}.
	 */
	public String name;

	/**
	 * Amount of gold to be awarded upon completion.
	 *
	 * @see #reward()
	 */
	public int reward;
	/**
	 * Utility value for maximum distance quests should be from their Town.
	 */
	protected int distance;

	/**
	 * For Reflection compatibility, all subclasses should respect this
	 * constructor signature.
	 *
	 * @param t Town this quest is active in.
	 */
	protected Quest(Town t){
		town=t;
		el=t.population;
		daysleft=Javelin.round(RPG.r(7,100));
		distance=town.getdistrict().getradius()*2;
	}

	/** @return If <code>false</code>, don't use this object as a quest. */
	public abstract boolean validate();

	/** A chance to further define details after validation. */
	protected void define(){

	}

	/**
	 * @return <code>true</code> if this is still listed as active in its
	 *         respective {@link Town}.
	 */
	public boolean isactive(){
		return daysleft>0&&town.quests.contains(this);
	}

	/**
	 * @return By default returns a proper amount of gold for the quest's
	 *         {@link #el} (capped by city size).
	 */
	protected int reward(){
		var gold=RewardCalculator.getgold(Math.min(town.population,el));
		return Javelin.round(gold);
	}

	/**
	 * Note that a quest can be fulfilled but if {@link #daysleft} has expired,
	 * players won't be able to complete it as it will have been removed from
	 * {@link #town}.
	 *
	 * @return If <code>true</code>, the quest is considered completed and a
	 *         {@link Squad} may claim the reward.
	 */
	abstract public boolean complete();

	/**
	 * @return <code>true</code> if a quest is to be cancelled permanently. For
	 *         example: a location needs to be captured but the location itself is
	 *         removed by some external force.
	 */
	public boolean cancel(){
		return false;
	}

	/**
	 * @return A descriptive, permanent name for this quest.
	 *
	 * @see #equals(Object)
	 * @see #validate()
	 */
	protected abstract String getname();

	@Override
	public boolean equals(Object obj){
		Quest q=obj instanceof Quest?(Quest)obj:null;
		return q!=null&&q.name.equals(name);
	}

	@Override
	public int hashCode(){
		return name.hashCode();
	}

	@Override
	public String toString(){
		return name;
	}

	/**
	 * @return A brand-new, valid quest or <code>null</code> if couldn't generate
	 *         any.
	 */
	public static Quest generate(Town t){
		Set<Class<? extends Quest>> quests;
		if(Javelin.DEBUG&&DEBUG!=null)
			quests=Set.of(DEBUG);
		else{
			quests=new HashSet<>(QUESTS.get(BASIC));
			for(var trait:t.traits)
				quests.addAll(QUESTS.get(trait));
		}
		try{
			for(var quest:RPG.shuffle(new ArrayList<>(quests))){
				var q=quest.getConstructor(Town.class).newInstance(t);
				if(!q.validate()) continue;
				q.name=q.getname();
				if(t.quests.contains(q)) continue;
				q.reward=q.reward();
				return q;
			}
		}catch(ReflectiveOperationException e){
			if(Javelin.DEBUG)
				throw new RuntimeException("Cannot generate Town quest.",e);
		}
		return null;
	}

	/** @return A player-friendly "expires in x [time unit]" notice. */
	public String getdeadline(){
		int amount;
		String unit;
		if(daysleft>=30){
			amount=Math.round(daysleft/30);
			unit=amount==1?"month":"months";
		}else if(daysleft>=7){
			amount=Math.round(daysleft/7);
			unit=amount==1?"week":"weeks";
		}else{
			amount=daysleft;
			unit=amount==1?"day":"days";
		}
		return "expires in "+amount+" "+unit;
	}

	/** @see ContentSummary */
	public static String printsummary(){
		var total=QUESTS.values().stream()
				.collect(Collectors.summingInt(l->l.size()));
		var traits=new ArrayList<>(QUESTS.keySet());
		traits.remove(BASIC);
		traits.sort(null);
		traits.add(0,BASIC);
		var detailed=traits.stream().map(t->QUESTS.get(t).size()+" "+t)
				.collect(Collectors.joining(", "));
		return total+" town quests ("+detailed+")";
	}
}

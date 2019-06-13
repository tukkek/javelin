package javelin.controller.upgrade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.factor.CrFactor;
import javelin.model.Realm;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.skill.Skill.SkillUpgrade;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.cultural.MagesGuild;

/**
 * Collects and distributes {@link Upgrade}s from different subsystems.
 *
 * @author alex
 */
public class UpgradeHandler{
	/** The class can be accessed through here. */
	public static final UpgradeHandler singleton=new UpgradeHandler();

	/** A named set of upgrades. */
	public class UpgradeSet extends HashSet<Upgrade>{
		/** Descriptive name for this set of upgrades. */
		public String name;
		boolean hidden;

		UpgradeSet(String name,boolean hidden){
			this.name=name;
			this.hidden=hidden;
			if(all.put(name,this)!=null&&Javelin.DEBUG)
				throw new RuntimeException("Name clash on #UpgradeHandler");
		}

		UpgradeSet(String name){
			this(name,false);
		}
	}

	/** All {@link UpgradeSet}s except those explicitly marked as internal. */
	HashMap<String,UpgradeSet> all=new HashMap<>();

	/** Linked to a {@link Town}'s realm. */
	public UpgradeSet water=new UpgradeSet("Water");
	/** Linked to a {@link Town}'s realm. */
	public UpgradeSet wind=new UpgradeSet("Wind");
	/** Linked to a {@link Town}'s realm. */
	public UpgradeSet good=new UpgradeSet("Good");
	/** Linked to a {@link Town}'s realm. */
	public UpgradeSet evil=new UpgradeSet("Evil");
	/** Linked HashSet a {@link Town}'s realm. */
	public UpgradeSet magic=new UpgradeSet("Magic");

	UpgradeHandler(){
		// prevents instantiation
	}

	/**
	 * @param r Given a realm...
	 * @return the upgrades that belong to it.
	 */
	public HashSet<Upgrade> getupgrades(Realm r){
		if(r==javelin.model.Realm.AIR)
			return wind;
		else if(r==Realm.WATER)
			return water;
		else if(r==Realm.GOOD)
			return good;
		else if(r==Realm.EVIL)
			return evil;
		else if(r==Realm.MAGIC)
			return magic;
		else
			return new HashSet<>();
	}

	/**
	 * Initializes class, if needed.
	 *
	 * @return This object, for call-chaining.
	 */
	public UpgradeHandler gather(){
		if(!magic.isEmpty()) return this;
		for(CrFactor factor:ChallengeCalculator.CR_FACTORS)
			factor.registerupgrades(this);
		return this;
	}

	static void addall(HashSet<Upgrade> fire2,
			HashMap<String,HashSet<Upgrade>> all,String string){
		all.put(string,fire2);
	}

	/**
	 * Doesn't count skills, see {@link #countskills()}.
	 *
	 * @return Total number of {@link Upgrade}s available.
	 */
	public int count(){
		var all=new HashSet<Upgrade>();
		for(HashSet<Upgrade> set:getall(true).values())
			all.addAll(set);
		return all.size();
	}

	/**
	 * @param hidden if <code>true</code>, all {@link UpgradeSet}s. If
	 *          <code>false</code>, will filter out those that are marked as
	 *          hidden.
	 */
	public HashMap<String,UpgradeSet> getall(boolean hidden){
		if(hidden) return all;
		var all=new HashMap<>(this.all);
		for(var set:this.all.values())
			if(set.hidden) all.remove(set.name);
		return all;
	}

	/**
	 * @return Total number of {@link Upgrade}s available.
	 */
	public int countskills(){
		var skills=new HashSet<Upgrade>();
		for(HashSet<Upgrade> l:getall(true).values())
			for(Upgrade u:l)
				if(u instanceof SkillUpgrade) skills.add(u);
		return skills.size();
	}

	/**
	 * @return All {@link FeatUpgrade}.
	 */
	public ArrayList<FeatUpgrade> getfeats(){
		ArrayList<FeatUpgrade> feats=new ArrayList<>();
		ArrayList<Upgrade> all=new ArrayList<>();
		for(HashSet<Upgrade> realm:getall(true).values())
			all.addAll(realm);
		for(Upgrade u:all)
			if(u instanceof FeatUpgrade) feats.add((FeatUpgrade)u);
		return feats;
	}

	/**
	 * @return All normal upgrades for the given {@link Realm} plus the relevant
	 *         {@link Spell}s which can only be found on {@link MagesGuild}s.
	 */
	public Collection<? extends Upgrade> getfullupgrades(Realm r){
		var upgrades=new HashSet<>(getupgrades(r));
		for(var s:Spell.ALL)
			if(s.realm.equals(r)) upgrades.add(s);
		return upgrades;
	}

	public HashSet<Upgrade> getalluncategorized(boolean hidden){
		HashSet<Upgrade> all=new HashSet<>();
		for(HashSet<Upgrade> upgrades:getall(hidden).values())
			all.addAll(upgrades);
		return all;
	}
}

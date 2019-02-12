package javelin.controller.upgrade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.factor.CrFactor;
import javelin.controller.kit.Kit;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.Realm;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.skill.Skill.SkillUpgrade;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.cultural.MagesGuild;
import javelin.model.world.location.town.labor.military.MartialAcademy;
import javelin.model.world.location.unique.SummoningCircle;

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

	LinkedList<Town> townqueue=new LinkedList<>();

	/** Linked to a {@link Town}'s realm. */
	public UpgradeSet fire=new UpgradeSet("Fire");
	/** Linked to a {@link Town}'s realm. */
	public UpgradeSet earth=new UpgradeSet("Earth");
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

	/** Linked to an {@link MartialAcademy}. */
	public UpgradeSet combatexpertise=new UpgradeSet("Expertise");
	/** Linked to an {@link MartialAcademy}. */
	public UpgradeSet powerattack=new UpgradeSet("Power");
	/** Linked to an {@link MartialAcademy}. */
	public UpgradeSet shots=new UpgradeSet("Shots");

	/** Spell school. */
	public UpgradeSet schooltotem=new UpgradeSet("Totem magic");
	/** Spell school. */
	public UpgradeSet schoolcompulsion=new UpgradeSet("Compulsion magic");
	/** Spell school. */
	public UpgradeSet schoolnecromancy=new UpgradeSet("Necromancy magic");
	/** Spell school. */
	public UpgradeSet schoolconjuration=new UpgradeSet("Conjuration magic");
	/** Spell school. */
	public UpgradeSet schoolevocation=new UpgradeSet("Evocation magic");
	/** Subdomain of conjuration. */
	public UpgradeSet schoolrestoration=new UpgradeSet("Restorarion magic");
	/** Subdomain of necromancy. */
	public UpgradeSet schoolwounding=new UpgradeSet("Wounding magic");
	/** Spell school; */
	public UpgradeSet schoolabjuration=new UpgradeSet("Abjuration magic");
	/** Spell school; */
	public UpgradeSet schoolhealwounds=new UpgradeSet("Healing magic");
	/** Spell school; */
	public UpgradeSet schooltransmutation=new UpgradeSet("Transmutation magic");
	/** Spell school; */
	public UpgradeSet schooldivination=new UpgradeSet("Divination magic");
	/**
	 * Used internally for summon spells. For learning {@link Summon}s see
	 * {@link SummoningCircle}.
	 */
	public UpgradeSet schoolsummoning=new UpgradeSet("Summoning magic",true);

	/** Internal upgrades. */
	public UpgradeSet internal=new UpgradeSet("Internal",true);

	/** @see ClassLevelUpgrade#classes */
	public UpgradeSet classes=new UpgradeSet("Classes",true);

	private UpgradeHandler(){
		// prevents instantiation
	}

	/**
	 * @param r Given a realm...
	 * @return the upgrades that belong to it.
	 */
	public HashSet<Upgrade> getupgrades(Realm r){
		if(r==javelin.model.Realm.AIR)
			return wind;
		else if(r==Realm.FIRE)
			return fire;
		else if(r==Realm.WATER)
			return water;
		else if(r==Realm.EARTH)
			return earth;
		else if(r==Realm.GOOD)
			return good;
		else if(r==Realm.EVIL)
			return evil;
		else if(r==Realm.MAGIC)
			return magic;
		else
			throw new RuntimeException("Uknown town!");
	}

	/**
	 * Initializes class, if needed.
	 * 
	 * @return This object, for call-chaining.
	 */
	public UpgradeHandler gather(){
		if(!fire.isEmpty()) return this;
		for(CrFactor factor:ChallengeCalculator.CR_FACTORS)
			factor.registerupgrades(this);
		ClassLevelUpgrade.init();
		for(var classlevel:ClassLevelUpgrade.classes)
			classes.add(classlevel);
		for(var kit:Kit.KITS)
			new UpgradeSet(kit.name).addAll(kit.getupgrades());
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
	 * @return All spells available in the game.
	 */
	public List<Spell> getspells(){
		ArrayList<Spell> spells=new ArrayList<>();
		for(HashSet<Upgrade> category:getall(true).values())
			for(Upgrade u:category)
				if(u instanceof Spell) spells.add((Spell)u);
		return spells;
	}

	/**
	 * @return All normal upgrades for the given {@link Realm} plus the relevant
	 *         {@link Spell}s which can only be found on {@link MagesGuild}s.
	 */
	public Collection<? extends Upgrade> getfullupgrades(Realm r){
		HashSet<Upgrade> upgrades=new HashSet<>(getupgrades(r));
		for(Spell s:getspells())
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

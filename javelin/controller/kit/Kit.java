package javelin.controller.kit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.kit.wizard.Abjurer;
import javelin.controller.kit.wizard.Conjurer;
import javelin.controller.kit.wizard.Diviner;
import javelin.controller.kit.wizard.Enchanter;
import javelin.controller.kit.wizard.Evoker;
import javelin.controller.kit.wizard.Necromancer;
import javelin.controller.kit.wizard.Transmuter;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.item.Tier;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.fortification.Academy.BuildAcademy;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.unique.AdventurersGuild;

/**
 * Kits represent sets of {@link Upgrade}s that constitute a role a character
 * may have a play in. As much inspired on AD&D kits as actual character
 * classes, these are used on the {@link AdventurersGuild} and {@link Academy}
 * types as means of upgrading {@link Combatant}s.
 *
 * Kits are usually created by piecing together 3 to 7 lowest-level upgrades.
 *
 * TODO at some point should reference all kits by Class and keep an internal
 * Map - instead, currently when we save and load, new instance are generated
 * needlessly.
 *
 * @author alex
 */
public abstract class Kit implements Serializable{
	/**
	 * All kits available in game.
	 *
	 * @see #validate()
	 */
	public static final List<Kit> KITS=List.of(Assassin.INSTANCE,
			Barbarian.INSTANCE,Bard.INSTANCE,Cleric.INSTANCE,Druid.INSTANCE,
			Fighter.INSTANCE,Monk.INSTANCE,Paladin.INSTANCE,Ranger.INSTANCE,
			Rogue.INSTANCE,Transmuter.INSTANCE,Enchanter.INSTANCE,
			Necromancer.INSTANCE,Conjurer.INSTANCE,Evoker.INSTANCE,Abjurer.INSTANCE,
			Diviner.INSTANCE);

	/**
	 * TODO temporaty class to help transtition from {@link UpgradeHandler} to a
	 * pure {@link Kit}-based system.
	 *
	 * @author alex
	 */
	protected class BuildSimpleGuild extends BuildAcademy{
		/** Constructor. */
		protected BuildSimpleGuild(Rank minimumrank){
			super(minimumrank);
		}

		@Override
		protected Academy generateacademy(){
			return createguild();
		}
	}

	/**
	 * TODO temporaty class to help transtition from {@link UpgradeHandler} to a
	 * pure {@link Kit}-based system.
	 *
	 * @author alex
	 */
	protected class SimpleGuild extends Guild{
		/** Constructor. */
		protected SimpleGuild(String string,Kit k){
			super(string,k);
		}

		@Override
		public String getimagename(){
			return "locationmartialacademy";
		}
	}

	static{
		UpgradeHandler.singleton.gather();
	}

	public String name;
	public HashSet<Upgrade> basic=new HashSet<>();
	public HashSet<Upgrade> extension=new HashSet<>();
	public ClassLevelUpgrade classlevel;
	public RaiseAbility ability;

	/**
	 * One title per {@link Tier}. A $ should be replaced by the
	 * {@link Monster#name}.
	 *
	 * @see #rename(Monster)
	 */
	protected String[] titles;

	public Kit(String name,ClassLevelUpgrade classadvancement,
			RaiseAbility raiseability){
		this.name=name;
		classlevel=classadvancement;
		basic.add(classadvancement);
		ability=raiseability;
		basic.add(ability);
		define();
		extend(UpgradeHandler.singleton);
		var lower=name.toLowerCase();
		titles=new String[]{"Inept $ "+lower,"Rookie $ "+lower,"$ "+lower,
				"Veteran $ "+lower};
	}

	/**
	 * Registers around 3-5 {@link Upgrade}s that all (or most) members of this
	 * Kit should share. Usually for CRs around 1-5.
	 */
	abstract protected void define();

	/**
	 * Add any other {@link Upgrade}s that extend this Kit into middle, high and
	 * epic levels.
	 */
	protected abstract void extend(UpgradeHandler h);

	public boolean ispreffered(int i){
		return false;
	}

	public int getpreferredability(Monster m){
		int preferred=Integer.MIN_VALUE;
		for(Upgrade u:basic)
			if(u instanceof RaiseAbility){
				int ability=((RaiseAbility)u).getattribute(m);
				if(ability>preferred) preferred=ability;
			}
		if(preferred==Integer.MIN_VALUE)
			throw new RuntimeException("Attribute not found for kit "+name);
		return preferred;
	}

	@Override
	public String toString(){
		return name;
	}

	/**
	 * @return <code>true</code> if this is a good choice for the given
	 *         {@link Monster}. The default implementation just compares the two
	 *         given ability scores to this class
	 *         {@link #getpreferredability(Monster)}.
	 */
	public boolean allow(int bestability,int secondbest,Monster m){
		int score=getpreferredability(m);
		return score==bestability||score==secondbest;
	}

	/**
	 * @return A list of kits that should be well suited for the given
	 *         {@link Monster}. Current Kit selection has been set up so that this
	 *         should never be empty.
	 */
	public static List<Kit> getpreferred(Monster m){
		ArrayList<Integer> attributes=new ArrayList<>(6);
		attributes.add(m.strength);
		attributes.add(m.dexterity);
		attributes.add(m.constitution);
		attributes.add(m.intelligence);
		attributes.add(m.wisdom);
		attributes.add(m.charisma);
		attributes.sort(null);
		int[] best=new int[]{attributes.get(4),attributes.get(5)};
		ArrayList<Kit> kits=new ArrayList<>(1);
		for(Kit k:KITS)
			if(k.allow(best[0],best[1],m)) kits.add(k);
		return kits;
	}

	public HashSet<Upgrade> getupgrades(){
		HashSet<Upgrade> upgrades=new HashSet<>(basic);
		upgrades.addAll(extension);
		return upgrades;
	}

	/**
	 * Sets {@link Monster#customName} to one of the appropriate {@link #titles}.
	 */
	public void rename(Monster m){
		var index=Tier.get(Math.round(m.cr)).ordinal();
		var title=titles[Math.min(index,titles.length-1)];
		var name=m.name;
		if(title.charAt(0)!='$') name=name.toLowerCase();
		m.customName=title.replace("$",name);
	}

	/**
	 * A {@link District} {@link Location} to learn this kit.
	 *
	 * TODO the default implemtation is temporary, see {@link BuildSimpleGuild} -
	 * move to abstract ASAP
	 */
	public Academy createguild(){
		return new SimpleGuild(name+"s guild",this);
	}

	/**
	 * @deprecated temporaty class to help transtition from {@link UpgradeHandler}
	 *             to a pure {@link Kit}-based system.
	 */
	@Deprecated
	public Labor buildguild(){
		return new BuildSimpleGuild(Rank.HAMLET);
	}

	/** Does a few sanity and design checks if in {@link Javelin#DEBUG} mode. */
	public void validate(){
		if(!Javelin.DEBUG) return;
		if(!Kit.KITS.contains(this))
			throw new RuntimeException("Kit not registered: "+name);
		var nupgrades=basic.size();
		if(!(3<=nupgrades&&nupgrades<=7)){
			String error=name+" has "+nupgrades+" basic upgrades!";
			throw new RuntimeException(error);
		}
	}

	/**
	 * @return All {@link Spell}s that are part of this kit. May be empty.
	 */
	public List<Spell> getspells(){
		return getupgrades().stream().filter(u->u instanceof Spell).map(u->(Spell)u)
				.collect(Collectors.toList());
	}
}

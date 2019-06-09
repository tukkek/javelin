package javelin.controller.kit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.kit.wizard.Summoner;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.item.Tier;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.unique.AdventurersGuild;

/**
 * Kits represent sets of {@link Upgrade}s that constitute a role a character
 * may have a play in. As much inspired on AD&D kits as actual character
 * classes, these are used on the {@link AdventurersGuild} and {@link Academy}
 * types as means of upgrading {@link Combatant}s.
 *
 * Kits are usually created by piecing together 3 to 7 lowest-level upgrades.
 *
 * @author alex
 */
public abstract class Kit implements Serializable{
	static{
		UpgradeHandler.singleton.gather();
	}

	public static final List<Kit> KITS=List.of(Assassin.INSTANCE,
			Barbarian.INSTANCE,Bard.INSTANCE,Cleric.INSTANCE,Druid.INSTANCE,
			Fighter.INSTANCE,Monk.INSTANCE,Paladin.INSTANCE,Ranger.INSTANCE,
			Rogue.INSTANCE,Summoner.INSTANCE);

	public String name;
	public HashSet<Upgrade> basic=new HashSet<>();
	public HashSet<Upgrade> extension=new HashSet<>();
	public ClassLevelUpgrade classlevel;

	String[] titles;

	public Kit(String name,ClassLevelUpgrade classadvancement,
			RaiseAbility raiseability,String title1,String title2,String title3,
			String title4){
		this.name=name;
		classlevel=classadvancement;
		basic.add(classadvancement);
		basic.add(raiseability);
		define();
		int nupgrades=basic.size();
		if(Javelin.DEBUG&&!(3<=nupgrades&&nupgrades<=7)){
			String error="Kit has "+nupgrades+" upgrades: "+name;
			throw new RuntimeException(error);
		}
		extend(UpgradeHandler.singleton);
		titles=new String[]{title1,title2,title3,title4,};
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

	public String gettitle(Monster m){
		int index=Tier.get(Math.round(m.cr)).ordinal();
		return titles[Math.min(index,titles.length-1)];
	}
}

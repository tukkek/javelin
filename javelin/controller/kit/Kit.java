package javelin.controller.kit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import javelin.Javelin;
import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.controller.upgrade.classes.ClassAdvancement;
import javelin.controller.upgrade.classes.Commoner;
import javelin.controller.upgrade.classes.Expert;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.damage.MeleeDamage;
import javelin.controller.upgrade.skill.Acrobatics;
import javelin.controller.upgrade.skill.Concentration;
import javelin.controller.upgrade.skill.Diplomacy;
import javelin.controller.upgrade.skill.DisableDevice;
import javelin.controller.upgrade.skill.Disguise;
import javelin.controller.upgrade.skill.GatherInformation;
import javelin.controller.upgrade.skill.Heal;
import javelin.controller.upgrade.skill.Knowledge;
import javelin.controller.upgrade.skill.Search;
import javelin.controller.upgrade.skill.Spellcraft;
import javelin.controller.upgrade.skill.Stealth;
import javelin.controller.upgrade.skill.Survival;
import javelin.controller.upgrade.skill.UseMagicDevice;
import javelin.model.feat.Feat;
import javelin.model.feat.ImprovedInitiative;
import javelin.model.feat.save.LightningReflexes;
import javelin.model.feat.skill.Acrobatic;
import javelin.model.feat.skill.Deceitful;
import javelin.model.spell.Summon;
import javelin.model.spell.conjuration.healing.wounds.CureLightWounds;
import javelin.model.spell.conjuration.healing.wounds.CureModerateWounds;
import javelin.model.spell.enchantment.compulsion.BarbarianRage;
import javelin.model.spell.enchantment.compulsion.Bless;
import javelin.model.spell.evocation.MagicMissile;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.town.labor.military.Academy;
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
public abstract class Kit implements Serializable {
	static {
		UpgradeHandler.singleton.gather();
	}

	public HashSet<Upgrade> upgrades = new HashSet<Upgrade>();
	public String name;
	public static final ArrayList<Kit> KITS = new ArrayList<Kit>();
	public static final Kit ASSASSIN = new Kit("assassin", Expert.SINGLETON,
			RaiseDexterity.SINGLETON) {
		@Override
		protected void define() {
			upgrades.add(Disguise.SINGLETON);
			upgrades.add(Stealth.SINGLETON);
			upgrades.add(RaiseCharisma.SINGLETON);
			upgrades.add(new FeatUpgrade(Deceitful.SINGLETON));
		}
	};
	public static final Kit BARBARIAN = new Kit("barbarian", Warrior.SINGLETON,
			RaiseStrength.SINGLETON) {
		@Override
		protected void define() {
			upgrades.add(Survival.SINGLETON);
			upgrades.add(new BarbarianRage());
		}
	};
	public static final Kit BARD = new Kit("bard", Expert.SINGLETON,
			RaiseCharisma.SINGLETON) {
		@Override
		protected void define() {
			upgrades.add(Diplomacy.SINGLETON);
			upgrades.add(GatherInformation.SINGLETON);
			upgrades.add(Knowledge.SINGLETON);
			upgrades.add(UseMagicDevice.SINGLETON);
		}
	};
	public static final Kit CLERIC = new Kit("cleric", Aristocrat.SINGLETON,
			RaiseWisdom.SINGLETON) {
		@Override
		protected void define() {
			upgrades.add(new CureModerateWounds());
			upgrades.add(Knowledge.SINGLETON);
			upgrades.add(Heal.SINGLETON);
		}
	};
	public static final Kit DRUID = new Kit("druid", Commoner.SINGLETON,
			RaiseWisdom.SINGLETON) {
		@Override
		protected void define() {
			upgrades.add(new Summon("Small monstrous centipede", 1));
			upgrades.add(new Summon("Dire rat", 1));
			upgrades.add(new Summon("Eagle", 1));
			upgrades.add(Survival.SINGLETON);
		}
	};
	public static final Kit FIGHTER = new Kit("fighter", Warrior.SINGLETON,
			RaiseStrength.SINGLETON) {
		@Override
		protected void define() {
			upgrades.add(new MeleeDamage());
			upgrades.addAll(UpgradeHandler.singleton.power);
		}
	};
	public static final Kit MONK = new Kit("monk", Warrior.SINGLETON,
			RaiseStrength.SINGLETON) {
		@Override
		protected void define() {
			upgrades.add(RaiseDexterity.SINGLETON);
			upgrades.add(Acrobatics.SINGLETON);
			for (Feat f : new Feat[] { Acrobatic.SINGLETON,
					ImprovedInitiative.SINGLETON,
					LightningReflexes.SINGLETON }) {
				upgrades.add(new FeatUpgrade(f));
			}
		}
	};
	public static final Kit PALADIN = new Kit("paladin", Warrior.SINGLETON,
			RaiseCharisma.SINGLETON) {
		@Override
		protected void define() {
			upgrades.add(new CureLightWounds());
			upgrades.add(new Bless());
		}
	};
	public static final Kit RANGER = new Kit("ranger", Warrior.SINGLETON,
			RaiseDexterity.SINGLETON) {
		@Override
		protected void define() {
			upgrades.add(Survival.SINGLETON);
			upgrades.addAll(UpgradeHandler.singleton.shots);
		}

		@Override
		public boolean allow(int bestability, int secondbest, Monster m) {
			return !m.ranged.isEmpty()
					&& super.allow(bestability, secondbest, m);
		}
	};
	public static final Kit ROGUE = new Kit("rogue", Expert.SINGLETON,
			RaiseDexterity.SINGLETON) {
		@Override
		protected void define() {
			upgrades.add(DisableDevice.SINGLETON);
			upgrades.add(Stealth.SINGLETON);
			upgrades.add(Search.SINGLETON);
		}
	};
	public static final Kit WIZARD = new Kit("wizard", Aristocrat.SINGLETON,
			RaiseIntelligence.SINGLETON) {
		@Override
		protected void define() {
			upgrades.add(new MagicMissile());
			upgrades.add(Concentration.SINGLETON);
			upgrades.add(Spellcraft.SINGLETON);
		}
	};

	static {
		for (Kit kit : new Kit[] { ASSASSIN, BARBARIAN, BARD, CLERIC, DRUID,
				FIGHTER, MONK, PALADIN, RANGER, ROGUE, WIZARD, }) {
			KITS.add(kit);
		}
	}

	public Kit(String name, ClassAdvancement classadvancement,
			RaiseAbility raiseability) {
		this.name = name;
		upgrades.add(classadvancement);
		upgrades.add(raiseability);
		define();
		int nupgrades = upgrades.size();
		if (!(3 <= nupgrades && nupgrades <= 7) && Javelin.DEBUG) {
			throw new RuntimeException(
					"Kit " + name + " has " + nupgrades + " upgrades");
		}
	}

	abstract protected void define();

	public boolean ispreffered(int i) {
		return false;
	}

	public int getpreferredability(Monster source) {
		int preferred = Integer.MIN_VALUE;
		for (Upgrade u : upgrades) {
			if (u instanceof RaiseAbility) {
				int ability = ((RaiseAbility) u).getattribute(source);
				if (ability > preferred) {
					preferred = ability;
				}
			}
		}
		if (preferred == Integer.MIN_VALUE) {
			throw new RuntimeException("Attribute not found for kit " + name);
		}
		return preferred;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return <code>true</code> if this is a good choice for the given
	 *         {@link Monster}. The default implementation just compares the two
	 *         given ability scores to this class
	 *         {@link #getpreferredability(Monster)}.
	 */
	public boolean allow(int bestability, int secondbest, Monster m) {
		int score = getpreferredability(m);
		return score == bestability || score == secondbest;
	}
}

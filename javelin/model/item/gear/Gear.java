package javelin.model.item.gear;

import javelin.controller.upgrade.Upgrade;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Slot;
import javelin.model.world.Caravan;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.town.Town;

/**
 * A special kind of item that is equipabble and is not allowed to be crafted on
 * {@link Town}s but can be found on other occasions such as in a
 * {@link Caravan}s' possession, {@link Chest} chests and at the Arcane
 * University.
 *
 * The name of this class is a big misnomer from d20 standards. Technically
 * there are just non-consumable, passive normal magic items. They're supposed
 * to be just a simple way to expand inventory management possibilities in a
 * hopefully worthwhile yet simple manner.
 *
 * Since stacking bonuses is complicated on d20 a few items have been adapted to
 * solve this with {@link Slot} types instead.
 *
 * Equipped artifacts are removed before applying an {@link Upgrade} and then
 * re-equipped afterwards.
 *
 * @see Combatant#equipped
 * @author alex
 */
public abstract class Gear extends Item{
	@SuppressWarnings("unused")
	public static void setup(){
		new AmuletOfHealth(+2,4_000);
		new AmuletOfHealth(+4,16_000);
		new AmuletOfHealth(+6,36_000);
		new BeltOfGiantStrength(+2,4_000);
		new BeltOfGiantStrength(+4,16_000);
		new BeltOfGiantStrength(+6,36_000);
		new CloakOfCharisma(+2,4_000);
		new CloakOfCharisma(+4,16_000);
		new CloakOfCharisma(+6,36_000);
		new CloakOfResistance(+1,1_000);
		new CloakOfResistance(+2,4_000);
		new CloakOfResistance(+3,9_000);
		new CloakOfResistance(+4,16_000);
		new GlovesOfDexterity(+2,4_000);
		new GlovesOfDexterity(+4,16_000);
		new GlovesOfDexterity(+6,36_000);
		new GogglesOfNight(12_000);
		new HeadbandOfIntellect(+2,4_000);
		new HeadbandOfIntellect(+4,16_000);
		new HeadbandOfIntellect(+6,36_000);
		new PeriaptOfWisdom(+2,4_000);
		new PeriaptOfWisdom(+4,16_000);
		new PeriaptOfWisdom(+6,36_000);
		new RingOfEnergyResistance(+2,18_000);
		new RingOfEnergyResistance(+4,28_000);
		new RingOfEnergyResistance(+6,44_000);
		new RingOfProtection(+1,2_000);
		new RingOfProtection(+2,8_000);
		new RingOfProtection(+3,18_000);
		new RingOfProtection(+4,32_000);
		new RingOfProtection(+5,50_000);
		new MantleOfSpellResistance(90_000);
		new WingsOfFlying(54_000);
	}

	public Slot slot;
	Combatant owner=null;

	protected Gear(String name,int price,Slot slotp,boolean register){
		super(name,price,register);
		ARTIFACT.add(this);
		usedinbattle=false;
		consumable=false;
		slot=slotp;
		waste=false;
	}

	protected Gear(String name,int price,Slot slotp){
		this(name,price,slotp,true);
	}

	@Override
	public boolean use(Combatant user){
		throw new RuntimeException("Not used in battle");
	}

	@Override
	public boolean usepeacefully(Combatant c){
		return equip(c);
	}

	/**
	 * Puts on an equipment piece, registering it at {@link Combatant#equipped}
	 * and activating it's effects. If this is already equipped will remove it, to
	 * work as well as a toggle on/off function.
	 *
	 * @param c Equipping unit.
	 * @return <code>false</code> if not {@link Monster#humanoid}, in which case
	 *         the operation is aborted.
	 */
	public boolean equip(Combatant c){
		failure=canuse(c);
		if(failure!=null) return false;
		if(c.equipped.contains(this)){// unequip
			remove(c);
			return true;
		}
		slot.clear(c);
		c.equipped.add(this);
		owner=c;
		apply(c);
		return true;
	}

	/** Unequip. */
	public void remove(Combatant c){
		negate(c);
		c.equipped.remove(this);
		owner=null;
	}

	/**
	 * Apply items bonuses to the...
	 *
	 * @param c combatant that is wearing this item.
	 */
	abstract protected void apply(Combatant c);

	/**
	 * Remove item bonuses from the...
	 *
	 * @param c combatant that is removing this item.
	 */
	abstract protected void negate(Combatant c);

	@Override
	public boolean equals(Object obj){
		Gear other=obj instanceof Gear?(Gear)obj:null;
		return other!=null&&name.equals(other.name);
	}

	@Override
	public String describefailure(){
		return failure;
	}

	@Override
	public String canuse(Combatant c){
		return c.source.body.slots.contains(slot)?null:"Doesn't have "+slot;
	}

	@Override
	public boolean sell(){
		return owner==null&&super.sell();
	}
}

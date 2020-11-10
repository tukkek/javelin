package javelin.model.item.gear;

import javelin.controller.upgrade.Upgrade;
import javelin.model.item.Item;
import javelin.model.unit.Body;
import javelin.model.unit.Combatant;
import javelin.model.unit.Slot;

/**
 * An equipabble item.
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
		new CloakOfResistance(+1,1_000);
		new CloakOfResistance(+2,4_000);
		new CloakOfResistance(+3,9_000);
		new CloakOfResistance(+4,16_000);
		new RingOfProtection(+1,2_000);
		new RingOfProtection(+2,8_000);
		new RingOfProtection(+3,18_000);
		new RingOfProtection(+4,32_000);
		new RingOfProtection(+5,50_000);
		new MantleOfSpellResistance(90_000);
	}

	/** Which part of the {@link Body} this goes to. */
	public Slot slot;
	/** Unit currently wearing this gear. */
	public Combatant owner=null;

	/** Constructor. */
	protected Gear(String name,int price,Slot slotp){
		super(name,price,true);
		usedinbattle=false;
		usedoutofbattle=false;
		consumable=false;
		slot=slotp;
		waste=false;
	}

	@Override
	public boolean use(Combatant user){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean usepeacefully(Combatant c){
		throw new UnsupportedOperationException();
	}

	/**
	 * Puts on an equipment piece, registering it at {@link Combatant#equipped}
	 * and activating it's effects. If this is already equipped will remove it, to
	 * work as well as a toggle on/off function.
	 *
	 * @param c Equipping unit.
	 * @return <code>false</code> if not {@link #canuse(Combatant)} .
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

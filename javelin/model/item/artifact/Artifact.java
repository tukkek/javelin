package javelin.model.item.artifact;

import javelin.controller.upgrade.Upgrade;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
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
public abstract class Artifact extends Item{
	public static void init(){
		new AmuletOfHealth(+2,4000);
		new AmuletOfHealth(+4,16000);
		new AmuletOfHealth(+6,36000);
		new BeltOfGiantStrength(+2,4000);
		new BeltOfGiantStrength(+4,16000);
		new BeltOfGiantStrength(+6,36000);
		new CloakOfCharisma(+2,4000);
		new CloakOfCharisma(+4,16000);
		new CloakOfCharisma(+6,36000);
		new CloakOfResistance(+1,1000);
		new CloakOfResistance(+2,4000);
		new CloakOfResistance(+3,9000);
		new CloakOfResistance(+4,16000);
		new GlovesOfDexterity(+2,4000);
		new GlovesOfDexterity(+4,16000);
		new GlovesOfDexterity(+6,36000);
		new GogglesOfNight(12000);
		new HeadbandOfIntellect(+2,4000);
		new HeadbandOfIntellect(+4,16000);
		new HeadbandOfIntellect(+6,36000);
		new PeriaptOfWisdom(+2,4000);
		new PeriaptOfWisdom(+4,16000);
		new PeriaptOfWisdom(+6,36000);
		new RingOfEnergyResistance(+2,18000);
		new RingOfEnergyResistance(+4,28000);
		new RingOfEnergyResistance(+6,44000);
		new RingOfProtection(+1,2000);
		new RingOfProtection(+2,8000);
		new RingOfProtection(+3,18000);
		new RingOfProtection(+4,32000);
		new RingOfProtection(+5,50000);
		new MantleOfSpellResistance(90000);
		new WingsOfFlying(54000);
	}

	public Slot slot;
	Combatant owner=null;

	public Artifact(String name,int price,Slot slotp){
		super(name,price,true);
		ARTIFACT.add(this);
		usedinbattle=false;
		consumable=false;
		slot=slotp;
		waste=false;
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
		if(canuse(c)!=null) return false;
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
		Artifact other=obj instanceof Artifact?(Artifact)obj:null;
		return other!=null&&name.equals(other.name);
	}

	@Override
	public String describefailure(){
		return "Only humanoids can equip artifacts!";
	}

	@Override
	public String canuse(Combatant c){
		return c.source.humanoid?null:"can't equip";
	}

	@Override
	public boolean sell(){
		return owner==null;
	}
}

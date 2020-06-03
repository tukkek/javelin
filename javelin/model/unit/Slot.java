package javelin.model.unit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javelin.model.item.artifact.Artifact;

/**
 * TODO pathfinder has a very useful table of body types to determine which
 * slots each creature has: "Magic Item Slots for Animals"
 * https://www.d20pfsrd.com/magic-items#Table-Random-Magic-Item-Generation
 *
 * TODO at some point may need to add main-hand and off-hand to represent
 * weapon(s) and shield
 *
 * @see Monster#body
 * @author alex
 */
public class Slot implements Serializable{
	/** headband, hat, helmet, or phylactery */
	public static final Slot HEAD=new Slot("head");
	/** eye lenses or goggles */
	public static final Slot EYES=new Slot("eyes");
	/** gloves or gauntlets */
	public static final Slot HANDS=new Slot("hands");
	/** one ring on each hand */
	public static final Slot RING=new Slot("ring"){
		@Override
		protected boolean conflicts(Slot slot){
			return false;
		}

		@Override
		public void clear(Combatant c){
			super.clear(c);
			var equipped=c.equipped.stream().filter(a->a.slot.equals(RING))
					.collect(Collectors.toList());
			for(int i=1;i<equipped.size();i++){
				var a=equipped.get(i);
				c.equipped.remove(a);
				a.remove(c);
			}
		}
	};
	/** boots or shoes */
	public static final Slot FEET=new Slot("feet");
	/** robe or suit of armor */
	public static final Slot ARMOR=new Slot("armor");
	/** belt */
	public static final Slot WAIST=new Slot("waist");
	/** vest, vestment, or shirt */
	public static final Slot TORSO=new Slot("torso");
	/** mulet, brooch, medallion, necklace, periapt, or scarab */
	public static final Slot NECK=new Slot("neck");
	/** cloak, cape, or mantle */
	public static final Slot SHOULDERS=new Slot("shoulders");
	/** bracers or bracelets on the arms or wrists */
	public static final Slot ARMS=new Slot("arms");
	/** infinite slot */
	public static final Slot SLOTLESS=new Slot("slotless"){
		@Override
		protected boolean conflicts(Slot slot){
			return false;
		}

		@Override
		public void clear(Combatant c){
			//don't
		}
	};

	String name;

	/** Constructor. */
	public Slot(String name){
		this.name=name;
	}

	/**
	 * To be called before equipping an {@link Artifact}.
	 *
	 * @see Artifact#usepeacefully(javelin.model.unit.Combatant)
	 * @param c Removes all incompatible items from this unit.
	 */
	public void clear(Combatant c){
		for(Artifact a:new ArrayList<>(c.equipped))
			if(conflicts(a.slot)){
				c.equipped.remove(a);
				a.remove(c);
			}
	}

	protected boolean conflicts(Slot slot){
		return name==slot.name;
	}

	@Override
	public boolean equals(Object obj){
		return obj instanceof Slot&&name==((Slot)obj).name;
	}

	@Override
	public int hashCode(){
		return name.hashCode();
	}

	@Override
	public String toString(){
		return name;
	}
}
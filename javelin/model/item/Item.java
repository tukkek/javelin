package javelin.model.item;

import java.io.Serializable;
import java.util.ArrayList;

import javelin.model.item.cure.CureCriticalWounds;
import javelin.model.item.cure.CureLightWounds;
import javelin.model.item.cure.CureModerateWounds;
import javelin.model.item.cure.CureSeriousWounds;
import javelin.model.unit.Combatant;

public abstract class Item implements Serializable, Cloneable {
	public static ArrayList<Item> all = new ArrayList<Item>();
	public static final CureLightWounds CURE_LIGHT_WOUNDS = new CureLightWounds();
	public String name;
	public int price;
	public String description;

	static {
		new CureModerateWounds();
		new CureCriticalWounds();
		new CureSeriousWounds();
		new RaiseScroll();
		new RessurectScroll();
		new SecureShelterScroll();
		new RecallScroll();
		new Barkskin();
		new ResistEnergy();
	}

	@Deprecated
	public Item(final String name, final int price, final String description) {
		super();
		this.name = name;
		this.price = price;
		this.description = description;
		all.add(this);
	}

	public Item(String name, int price) {
		this(name, price, null);
	}

	@Override
	public String toString() {
		return name;
	}

	public abstract boolean use(Combatant c);

	public boolean canuseneganged() {
		return false;
	}

	public abstract boolean isusedinbattle();

	public abstract boolean usepeacefully(Combatant m);

	@Override
	public boolean equals(final Object obj) {
		try {
			return ((Item) obj).name.equals(name);
		} catch (final ClassCastException e) {
			return false;
		}
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}

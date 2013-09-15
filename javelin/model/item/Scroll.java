package javelin.model.item;

/**
 * Can only be used out-of-combat.
 * 
 * @author alex
 */
public abstract class Scroll extends Item {
	public Scroll(final String name, final int price, final String description) {
		super(name, price, description);
	}

	@Override
	public boolean isusedinbattle() {
		return false;
	}
}

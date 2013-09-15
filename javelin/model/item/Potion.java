package javelin.model.item;

public abstract class Potion extends Item {
	public Potion(final String name, final int price) {
		super(name, price);
	}

	@Override
	public boolean isusedinbattle() {
		return true;
	}
}

package javelin.controller.quality;

import javelin.model.unit.Monster;

public class FastHealing extends Quality {
	public FastHealing(String name) {
		super(name);
	}

	@Override
	public void add(final String declaration, final Monster m) {
		try {
			m.fasthealing = Integer.parseInt(declaration.substring(
					declaration.lastIndexOf(' ') + 1, declaration.length()));
		} catch (final RuntimeException e) {
			throw new RuntimeException("Wrong number format for: " + m, e);
		}
	}
}
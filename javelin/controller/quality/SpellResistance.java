package javelin.controller.quality;

import javelin.model.unit.Monster;

public class SpellResistance extends Quality {

	public SpellResistance() {
		super("sr ");
	}

	@Override
	public void add(String declaration, Monster m) {
		if (m.sr != Integer.MAX_VALUE) {
			m.sr = Integer.parseInt(declaration.substring(declaration
					.indexOf(' ') + 1));
			if (m.sr <= 10) {
				/* CR is given only for SR11+ */
				m.sr = 0;
			}
		}
	}

}

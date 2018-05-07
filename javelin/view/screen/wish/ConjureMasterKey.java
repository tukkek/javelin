package javelin.view.screen.wish;

import javelin.model.item.key.door.MasterKey;
import javelin.model.unit.Combatant;

public class ConjureMasterKey extends Wish {
	public ConjureMasterKey(WishScreen s) {
		super("conjure master key", 'k', 2, false, s);
	}

	@Override
	protected boolean wish(Combatant target) {
		new MasterKey().grab();
		return true;
	}
}

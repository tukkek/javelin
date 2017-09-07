package javelin.model.item.relic;

import java.util.ArrayList;

import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.Realm;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import tyrant.mikera.engine.RPG;

/**
 * Damages all good creatures in battle (1-99% will save).
 * 
 * @author alex
 */
public class Skull extends Relic {
	/** Constructor. */
	public Skull() {
		super("Skull of Pain", Realm.EVIL);
		usedinbattle = true;
		usedoutofbattle = false;
	}

	@Override
	protected boolean activate(Combatant user) {
		ArrayList<Combatant> good = new ArrayList<Combatant>();
		for (Combatant c : Fight.state.getcombatants()) {
			Monster m = c.source;
			if (Boolean.TRUE.equals(m.good)) {
				good.add(c);
			}
		}
		if (good.isEmpty()) {
			Game.message("Nothing seems to happen...", Delay.BLOCK);
			return true;
		}
		float dc = 0;
		for (Combatant c : good) {
			int wisdom = Monster.getbonus(c.source.wisdom);
			if (wisdom != Integer.MAX_VALUE) {
				dc = Math.max(dc, 20 + wisdom);
			}
		}
		for (Combatant c : good) {
			int wisdom = Monster.getbonus(c.source.wisdom);
			c.hp -= c.maxhp * ((RPG.r(1, 20) + wisdom) / dc);
			if (c.hp < 1) {
				c.hp = 1;
			} else if (c.hp == c.maxhp) {
				c.hp -= 1;
			}
		}
		Game.message("Good creatures convulse in agony!", Delay.BLOCK);
		return true;
	}
}

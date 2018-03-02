package javelin.view.mappanel.battle.action;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.attack.Combatant;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.MoveOverlay;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.overlay.BattleWalker;
import javelin.view.mappanel.battle.overlay.BattleWalker.BattleStep;
import javelin.view.screen.BattleScreen;

public class MoveMouseAction extends BattleMouseAction {
	public MoveMouseAction() {
		clearoverlay = false;
	}

	@Override
	public boolean determine(Combatant current, Combatant target,
			BattleState s) {
		return target == null;
	}

	@Override
	public void act(final Combatant current, Combatant target, BattleState s) {
		if (MapPanel.overlay instanceof MoveOverlay) {
			final MoveOverlay walk = (MoveOverlay) MapPanel.overlay;
			if (!walk.path.steps.isEmpty()) {
				walk.clear();
				BattleScreen.perform(new Runnable() {
					@Override
					public void run() {
						int finalstep = walk.path.steps.size() - 1;
						final BattleStep to = walk.path.steps.get(finalstep);
						BattleState move = Fight.state;
						Combatant c = move.clone(current);
						c.location[0] = to.x;
						c.location[1] = to.y;
						c.ap += to.totalcost - BattleScreen.partialmove;
						Meld m = move.getmeld(to.x, to.y);
						if (m != null && c.ap >= m.meldsat) {
							Javelin.app.fight.meld(c, m);
						}
					}
				});
			}
		}
	}

	@Override
	public void onenter(Combatant current, Combatant target, Tile t,
			BattleState s) {
		Point from = new Point(current.location[0], current.location[1]);
		Point to = new Point(t.x, t.y);
		MoveOverlay.schedule(
				new MoveOverlay(new BattleWalker(from, to, current, s)));
	}
}
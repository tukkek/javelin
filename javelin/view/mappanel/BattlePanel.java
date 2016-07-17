package javelin.view.mappanel;

import java.awt.Graphics;

import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;

/**
 * TODO remove {@link BattleMap} and rename this hierarchy
 * 
 * @author alex
 */
public class BattlePanel extends MapPanel {
	public static BattleState state = null;

	public BattlePanel(BattleState s) {
		super(s.map.length, s.map[0].length);
		state = s.clonedeeply();
	}

	@Override
	protected Tile newtile(int x, int y) {
		return new BattleTile(x, y, this);
	}

	@Override
	public void refresh() {
		super.refresh();
		BattleState previous = state;
		updatestate();
		for (Combatant c : previous.getCombatants()) {
			tiles[c.location[0]][c.location[1]].repaint();
		}
		for (Combatant c : state.getCombatants()) {
			tiles[c.location[0]][c.location[1]].repaint();
		}
	}

	@Override
	public void paint(Graphics g) {
		updatestate();
		super.paint(g);
	}

	void updatestate() {
		state = BattleScreen.active.map.getState().clonedeeply();
		BattleTile.panel = this;
	}
}

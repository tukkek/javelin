package javelin.view.mappanel.world;

import java.awt.event.MouseEvent;

import javelin.controller.Point;
import javelin.controller.action.world.WorldMove;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.unit.Squad;
import javelin.model.world.WorldActor;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.mappanel.battle.overlay.MoveOverlay;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

public class WorldMouse extends Mouse {
	private boolean showingdescription = false;

	public WorldMouse(MapPanel panel) {
		super(panel);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!Game.getUserinterface().waiting) {
			return;
		}
		final WorldTile t = (WorldTile) e.getSource();
		if (!((MapPanel) BattleScreen.active.mappanel).tiles[t.x][t.y].discovered) {
			return;
		}
		if (e.getButton() == MouseEvent.BUTTON1) {
			final WorldActor target = WorldActor.get(t.x, t.y);
			if (target == null) {
				if (BattlePanel.overlay != null) {
					final MoveOverlay o = (MoveOverlay) BattlePanel.overlay;
					BattleScreen.perform(new Runnable() {
						@Override
						public void run() {
							int i = -1;
							for (Point p : o.affected) {
								i += 1;
								if (!WorldMove.move(p.x, p.y, false)) {
									break;
								}
							}
							RandomEncounter
									.encounter(o.path.steps.get(i).apcost);
							// BattlePanel.overlay.clear();
							BattleScreen.active.mappanel.refresh();
						}
					});
					return;
				}
			} else {
				if (target.isadjacent(Squad.active)) {
					BattleScreen.perform(new Runnable() {
						@Override
						public void run() {
							target.interact();
						}
					});
				} else {
					BattleScreen.perform(new Runnable() {
						@Override
						public void run() {
							Game.messagepanel.clear();
							Game.message("Too far...", null, Delay.WAIT);
						}
					});
				}
				return;
			}
		}
		super.mouseClicked(e);
	}

	public static boolean processing = false;

	public static Object LOCK = new Object();

	long lastcall = -Integer.MIN_VALUE;

	@Override
	public void mouseEntered(MouseEvent e) {
		if (!Game.getUserinterface().waiting) {
			return;
		}
		// if (e.getWhen() <= lastcall + 200) {
		// return;
		// }
		// lastcall = e.getWhen();
		if (MapPanel.overlay != null) {
			MapPanel.overlay.clear();
		}
		MoveOverlay.cancel();
		final WorldTile t = (WorldTile) e.getSource();
		if (!((MapPanel) BattleScreen.active.mappanel).tiles[t.x][t.y].discovered) {
			return;
		}
		final WorldActor target = WorldActor.get(t.x, t.y);
		if (target == null) {
			if (showingdescription) {
				showingdescription = false;
				Game.messagepanel.clear();
				((WorldScreen) WorldScreen.active).updateplayerinformation();
				Game.messagepanel.getPanel().repaint();
			}
			MoveOverlay.schedule(new MoveOverlay(
					new WorldMover(new Point(Squad.active.x, Squad.active.y),
							new Point(t.x, t.y))));
		} else {
			Game.messagepanel.clear();
			Game.message(target.describe(), null, Delay.NONE);
			Game.messagepanel.getPanel().repaint();
			showingdescription = true;
		}
	}
}

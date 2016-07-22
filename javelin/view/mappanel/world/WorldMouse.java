package javelin.view.mappanel.world;

import java.awt.event.MouseEvent;

import javelin.controller.Point;
import javelin.controller.action.world.WorldMove;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.unit.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Location;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.MoveOverlay;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

public class WorldMouse extends Mouse {
	private boolean showingdescription = false;

	public WorldMouse(MapPanel panel) {
		super(panel);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (overrideinput()) {
			return;
		}
		if (!Game.getUserinterface().waiting) {
			return;
		}
		final WorldTile t = (WorldTile) e.getSource();
		if (!t.discovered) {
			return;
		}
		if (e.getButton() == MouseEvent.BUTTON1) {
			final WorldActor target = WorldActor.get(t.x, t.y);
			if (target instanceof Squad) {
				return;
			}
			if (target == null) {
				if (move()) {
					return;
				}
			} else {
				if (target.isadjacent(Squad.active)) {
					BattleScreen.perform(new Runnable() {
						@Override
						public void run() {
							target.interact();
							Location l = target instanceof Location
									? (Location) target : null;
							if (l.allowentry && l.garrison.isEmpty()) {
								WorldMove.place(Game.hero(), l.x, l.y);
							}
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

	public static boolean move() {
		if (BattlePanel.overlay == null) {
			return false;
		}
		final MoveOverlay o = (MoveOverlay) BattlePanel.overlay;
		if (o.path.steps.isEmpty()) {
			return false;
		}
		BattleScreen.perform(new Runnable() {
			@Override
			public void run() {
				int i = -1;
				boolean interrupted = false;
				for (Point p : o.affected) {
					i += 1;
					if (!WorldMove.move(p.x, p.y, false)) {
						interrupted = true;
						break;
					}
				}
				RandomEncounter.encounter(o.path.steps.get(i).apcost);
				BattleScreen.active.mappanel.refresh();
				if (interrupted) {
					Point p = o.path.resetlocation();
					if (p != null) {
						o.reset();
						o.path.sourcex = p.x;
						o.path.sourcey = p.y;
						o.walk();
						BattlePanel.overlay = o;
					}
				}
			}
		});
		return true;
	}

	public static boolean processing = false;

	public static Object LOCK = new Object();

	long lastcall = -Integer.MIN_VALUE;

	@Override
	public void mouseEntered(MouseEvent e) {
		if (!Game.getUserinterface().waiting) {
			return;
		}
		if (MapPanel.overlay != null) {
			MapPanel.overlay.clear();
		}
		MoveOverlay.cancel();
		final WorldTile t = (WorldTile) e.getSource();
		if (!t.discovered) {
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

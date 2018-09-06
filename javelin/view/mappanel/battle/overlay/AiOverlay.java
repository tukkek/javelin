package javelin.view.mappanel.battle.overlay;

import java.awt.Image;
import java.util.Collection;

import javelin.controller.Point;
import javelin.controller.action.ai.AiAction;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.overlay.Overlay;

/**
 * Used to simply allow {@link AiAction}s to present some visual feedback. Takes
 * one or more {@link Point}s and draws a target on them.
 *
 * @author alex
 */
public class AiOverlay extends Overlay {
	public Image image = TargetOverlay.TARGET;

	public AiOverlay(int x, int y) {
		affected.add(new Point(x, y));
	}

	public AiOverlay(Collection<? extends Point> area) {
		affected.addAll(area);
	}

	public AiOverlay(Point p) {
		this(p.x, p.y);
	}

	public AiOverlay(Combatant c) {
		this(c.getlocation());
	}

	@Override
	public void overlay(Tile t) {
		if (affected.contains(new Point(t.x, t.y))) {
			draw(t, image);
		}
	}
}

package javelin.model.world.location.dungeon.temple;

import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.CrCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.fight.Fight;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Chest;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;
import javelin.model.world.location.dungeon.StairsUp;
import javelin.model.world.location.dungeon.temple.features.Altar;
import javelin.model.world.location.dungeon.temple.features.StairsDown;
import javelin.view.screen.DungeonScreen;

/**
 * Unlike normal dungeons {@link Temple}s have many floors (levels), chests with
 * rubies and an Altar on the deepest level.
 * 
 * @author alex
 */
public class TempleDungeon extends Dungeon {
	Temple temple;
	/** <code>true</code> if last dungeon level. */
	public boolean deepest;

	/**
	 * @param temple
	 *            Temples this floor is a part of.
	 * @param deepest
	 *            <code>true</code> if the last (bottom) floor.
	 */
	public TempleDungeon(Temple temple, boolean deepest) {
		this.temple = temple;
		this.deepest = deepest;
	}

	@Override
	protected void registerinstance() {
		// don't
	}

	@Override
	protected void deregisterinstance() {
		// don't
	}

	@Override
	public void activate(boolean loading) {
		if (temple.floor != null) {
			floor = temple.floor;
		}
		if (temple.wall != null) {
			wall = temple.wall;
		}
		if (loading || Dungeon.active != null) {
			super.activate(loading);
			return;
		}
		String difficulty = CrCalculator.describedifficulty(
				temple.el - CrCalculator.calculateel(Squad.active.members));
		Character prompt = Javelin.prompt("You're at the entrance of the "
				+ temple.descriptionknown + " (difficulty: " + difficulty
				+ "). Do you want to enter?\n"
				+ "Press ENTER to venture forth or any other key to cancel...");
		if (prompt.equals('\n')) {
			super.activate(loading);
		}
	}

	@Override
	protected void setlocation(boolean loading) {
		if (!loading) {
			if (Temple.leavingfight) {
				Temple.leavingfight = false;
			} else {
				Class<? extends Feature> reference = Temple.climbing
						? StairsDown.class : StairsUp.class;
				Temple.climbing = false;
				for (Feature f : features) {
					if (reference.isInstance(f)) {
						herolocation.x = f.x;
						herolocation.y = f.y;
						break;
					}
				}
			}
		}
		super.setlocation(loading);
	}

	@Override
	protected Feature createspecialchest(Point p) {
		if (deepest) {
			return new Altar(p, temple);
		}
		Chest c = new Chest("chest", p.x, p.y, 0, new ItemSelection());
		c.rubies = 1;
		return c;
	}

	@Override
	public void goup() {
		DungeonScreen.dontenter = true;
		int level = temple.floors.indexOf(this);
		if (level == 0) {
			super.goup();
		} else {
			Squad.active.ellapse(1);
			Temple.climbing = true;
			temple.floors.get(level - 1).activate(false);
		}
	}

	@Override
	public void godown() {
		DungeonScreen.dontenter = true;
		Squad.active.ellapse(1);
		temple.floors.get(temple.floors.indexOf(this) + 1).activate(false);
	}

	@Override
	protected void placefeatures() {
		if (!deepest) {
			features.add(new StairsDown("stairs up", findspot()));
		}
		super.placefeatures();
	}

	@Override
	protected List<Feature> getextrafeatures() {
		return temple.getfeatures(this);
	}

	@Override
	public Fight encounter() {
		return temple.encounter();
	}

	@Override
	public boolean hazard() {
		if (!temple.hazard(this)) {
			return false;
		}
		return true;
	}

	@Override
	protected void generate() {
		super.generate();
	}

	@Override
	public int getgoldpool() {
		return RewardCalculator.getgold(temple.el) * 4;
	}
}

package javelin.model.world.location.dungeon.temple;

import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.terrain.Marsh;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.item.relic.Skull;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Altar;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.StairsDown;
import javelin.model.world.location.dungeon.feature.StairsUp;
import javelin.old.RPG;

/**
 * Found drowning in the {@link Marsh}. Good creatures are never found in the
 * temple. An evil force can bring you back to the stairs up at any point (or
 * stairs down if you have the {@link Skull}).
 *
 * @see Temple
 * @see Monster#good
 * @author alex
 */
public class EvilTemple extends Temple {
	private static final String FLUFF = "You have heard of this fort once before, upon a dark stormy night.\n"
			+ "You recognize the looming towers from that tale. It was related to you as the Fortress of Regrets.\n"
			+ "It is said that a once powerful king oversaw his domain from his throne here but bad tidings befell him.\n"
			+ "The once great castle became a prison, torture chamber and hall of twisted pleasures as the kingdom's honor slowly faded into oblivion.";

	/** Constructor. */
	public EvilTemple(Integer level) {
		super(Realm.EVIL, level, new Skull(level), FLUFF);
		terrain = Terrain.MARSH;
		floor = "dungeonfloortempleevil";
		wall = "dungeonwalltempleevil";
		doorbackground = false;
	}

	@Override
	public boolean validate(List<Monster> foes) {
		for (Monster m : foes) {
			if (Boolean.FALSE.equals(m.good)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean hazard(TempleDungeon dungeon) {
		if (!RPG.chancein(dungeon.stepsperencounter * 10)) {
			return false;
		}
		Class<? extends Feature> targettype;
		if (Squad.active.equipment.get(Skull.class) == null) {
			targettype = StairsUp.class;
		} else if (dungeon.hasfeature(Altar.class)) {
			targettype = Altar.class;
		} else {
			targettype = StairsDown.class;
		}
		Feature target = null;
		for (Feature f : dungeon.features) {
			if (targettype.isInstance(f)) {
				target = f;
				break;
			}
		}
		if (target == null) {
			return false;
		}
		dungeon.herolocation.x = target.x;
		dungeon.herolocation.y = target.y;
		Javelin.message("A macabre force draws upon you...", true);
		return true;
	}

	@Override
	public Feature createfeature(Point p, Dungeon d) {
		return null;
	}
}

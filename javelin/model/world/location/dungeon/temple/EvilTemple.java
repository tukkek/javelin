package javelin.model.world.location.dungeon.temple;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.terrain.Marsh;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.item.relic.Skull;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;
import javelin.model.world.location.dungeon.StairsUp;
import javelin.model.world.location.dungeon.temple.features.Altar;
import javelin.model.world.location.dungeon.temple.features.StairsDown;
import tyrant.mikera.engine.RPG;

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
	public EvilTemple(Integer pop) {
		super(Realm.EVIL, pop, new Skull(), FLUFF);
		terrain = Terrain.MARSH;
		floor = "dungeonfloortempleevil";
		wall = "dungeonwalltempleevil";
	}

	@Override
	public boolean validate(ArrayList<Combatant> foes) {
		for (Combatant c : foes) {
			if (Boolean.FALSE.equals(c.source.good)) {
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
		if (Squad.active.equipment.containsitem(Skull.class) == null) {
			targettype = StairsUp.class;
		} else {
			targettype = dungeon.deepest ? Altar.class : StairsDown.class;
		}
		Feature target = null;
		for (Feature f : dungeon.features) {
			if (targettype.isInstance(f)) {
				target = f;
				break;
			}
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

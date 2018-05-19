package javelin.controller.scenario.dungeonworld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import javelin.controller.Point;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.model.Realm;
import javelin.model.world.World;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.town.labor.Trait;

public class ZoneGenerator extends FeatureGenerator {
	@Override
	public void spawn(float chance, boolean generatingworld) {
		// don't: static world
	}

	@Override
	public Town generate(LinkedList<Realm> realms,
			ArrayList<HashSet<Point>> regions, World w) {
		Point p = new Point(World.scenario.size / 2, World.scenario.size / 2);
		Town t = new Town(p, Realm.FIRE);
		t.place();
		return t;
	}

	Town process(ArrayList<Town> towns) {
		LinkedList<Trait> traits = new LinkedList<Trait>(Deck.TRAITS);
		Collections.shuffle(traits);

		Point p = new Point(World.scenario.size / 2, World.scenario.size / 2);
		Town t = new Town(p, Realm.FIRE);
		t.place();
		return t;
	}

}
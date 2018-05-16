/*
 * Created on 04-Jan-2005
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javelin.controller.old.Game;
import javelin.model.BattleMap;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Event;
import tyrant.mikera.tyrant.Portal;



/**
 * This test case generates the enitire Tyrant world and then recursively tests
 * each map for correctness
 * 
 * @author Mike
 * 
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public class WorldMap_TC extends TyrantTestCase {
	private HashMap hm;
	private int mapCount;

	public void testWorld() {
		hm = new HashMap();
		mapCount = 0;

		// create things to add to maps
		final Thing t = Lib.create("carrot");
		Game.instance().addMapObject(t, "dark tower:10");
		final Thing t2 = Lib.create("invincible portcullis");
		Game.instance().addMapObject(t2, "tutorial inn:1", 1, 2);

		// Create the world
		final BattleMap m = Game.instance().createWorld();

		// test compression
		Game.instance().compressAllData();

		mapTest(m);

		// check that we got all the way to The Tyrant's Lair
		final BattleMap darktower = (BattleMap) Game.instance().getMapStore().get(
				"dark tower:10");
		assertTrue(darktower != null);
		assertEquals(darktower, t.place);

		// check that we got all the way to The Tyrant's Lair
		final BattleMap inn = (BattleMap) Game.instance().getMapStore().get(
				"tutorial inn:1");
		assertTrue(inn != null);
		assertEquals(inn, t2.place);
		assertEquals(1, t2.x);
		assertEquals(2, t2.y);
	}

	/**
	 * Test all properties of the map
	 * 
	 * @param m
	 *            The map to test
	 */
	private void mapPropertyTest(final BattleMap m) {
		// test all map properties here
		assertTrue(m != null);
		assertTrue("Map [" + m.name() + "] has Level<=0", m.getLevel() >= 1);
		assertTrue("Map [" + m.name() + "] has Level>50", m.getLevel() <= 50);
		assertTrue("Map [" + m.name() + "] has no Description!", m
				.getstring("Description") != null);
	}

	/**
	 * Test all Things on the map
	 * 
	 * @param m
	 *            The map to test
	 */
	private void mapThingTest(final BattleMap m) {

		final Thing[] ts = m.getThings();

		assertTrue("Map.getThings() is null!!", ts != null);
		for (final Thing t : ts) {
			assertTrue("Thing in Map.getThings() is null!!", t != null);
		}
	}

	/**
	 * Test that each map can handle a time elapse event without crashing or
	 * anything like that
	 */
	private void mapTimeTest(final BattleMap m) {
		if (!m.getFlag("IsWorldMap")) {
			m.action(Event.createActionEvent(100));
		}
	}

	/**
	 * Test each map that is generated
	 * 
	 * @param m
	 */
	private void mapTest(final BattleMap m) {
		if (hm.get(m.name()) != null) {
			// map has already been tested!
			return;
		}
		mapCount++;

		if (mapCount > 1000) {
			throw new Error("Too many maps at [" + m.name() + "]");
		}

		hm.put(m.name(), m);

		mapPropertyTest(m);
		mapThingTest(m);
		mapTimeTest(m);

		final ArrayList al = m.getAllPortals();

		// portal destinations
		final HashSet dests = new HashSet();

		for (final Iterator it = al.iterator(); it.hasNext();) {
			final Thing p = (Thing) it.next();

			BattleMap newMap;

			try {
				newMap = Portal.getTargetMap(p);
			} catch (final Throwable t) {
				throw new Error("Error creating map for [" + p.name()
						+ "] on [" + m.name() + "]", t);
			}

			// test that no two portals has same target
			assertTrue("Two portals [" + p.name() + "] on [" + m.name()
					+ "] point to same target map [" + newMap.name() + "]!",
					!dests.get(newMap));
			dests.add(newMap);

			assertTrue("Portal [" + p.name() + "] has null getTargetMap()",
					true);
			mapTest(newMap);

		}
	}
}

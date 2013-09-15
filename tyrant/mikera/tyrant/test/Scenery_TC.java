package tyrant.mikera.tyrant.test;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Event;
import tyrant.mikera.tyrant.Scripts;

public class Scenery_TC extends TyrantTestCase {

    public void testGenerateInPlaceScript() {
        String mapString = 
            // 0123
              "----" + "\n" +
              "|.@|" + "\n" +
              "----";
        final Thing patch = Lib.create("carrot");
        patch.addHandler("OnAction",Scripts.generatorInPlace("raspberry",100));
        BattleMap map = new MapHelper().createMap(mapString);
        map.addThing(patch,1,1);
        Thing[] things = map.getThings(1,1);
        assertEquals(1,things.length); // our carrot
        // wait long enough to ensure the time * generation rate >= 1000000
        patch.handle(Event.createActionEvent(10000));
        things = map.getThings(1,1);
        assertEquals(2,things.length); // a carrot and a raspberry
    }
}

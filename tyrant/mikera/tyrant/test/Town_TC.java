package tyrant.mikera.tyrant.test;

import javelin.model.BattleMap;
import tyrant.mikera.tyrant.Town;

public class Town_TC extends TyrantTestCase {
    public void testTownBuilding() throws Exception {
        String mapString = 
            "----------------" + "\n" +
            "|..............|" + "\n" +
            "|..............|" + "\n" +
            "|..............|" + "\n" +
            "|..............|" + "\n" +
            "|..............|" + "\n" +
            "|..............|" + "\n" +
            "|..............|" + "\n" +
            "|..............|" + "\n" +
            "----------------";
        BattleMap map = new MapHelper().createMap(mapString);
        Town.addStandardRoom(map, 1, 1, 5, 5, 3, 5);
        //TODO Finish this testcase
    }
}

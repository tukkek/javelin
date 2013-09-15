package tyrant.mikera.tyrant.test;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Tile;
import tyrant.mikera.tyrant.Tutorial;

/**
 * @author Chris Grindstaff chris@gstaff.org
 */
public class MapHelper_TC extends TyrantTestCase {
    private MapHelper mapHelper;

    protected void setUp() throws Exception {
        super.setUp();
        mapHelper = new MapHelper();
    }
    
    public void testASCII() throws Exception {
        assertEquals(" ", Lib.create("nothing").get("ASCII"));
    }
    
    public void testCanSee() {
        String mapString = 
          // 0123456789
            "----------" + "\n" +
            "|...@....|" + "\n" +
            "|...-....|" + "\n" +
            "|...=....|" + "\n" +
            "|........|" + "\n" +
            "----------";
        BattleMap map = mapHelper.createMap(mapString);
        assertEquals(10, map.getWidth());
        assertEquals(6, map.getHeight());
        assertEquals("plain ring", map.getThings(4, 3)[0].name());
    }
    
    public void testCreatePools() {
        String mapString = 
            // 0123456789
              "----------" + "\n" +
              "|..%@~...|" + "\n" +
              "|...-....|" + "\n" +
              "|........|" + "\n" +
              "|........|" + "\n" +
              "----------";
        BattleMap map = new MapHelper().createMap(mapString);
        assertEquals("beefcake", map.getThings(3, 1)[0].getName(null));
        assertEquals(Tile.RIVER, map.getTile(5,1));
    }
    
    public void testCreate_door() {
        String mapString = 
          // 0123456789
            "-----" + "\n" +
            "|.@.|" + "\n" +
            "|-+-|" + "\n" +
            "|...|" + "\n" +
            "-----";
        BattleMap map = new MapHelper().createMap(mapString);
        Thing door = map.getThings(2, 2)[0];
        assertEquals("door", door.name());
        assertTrue(door.isBlocking());
    }
    
    public void testToString() throws Exception {
        String mapString = 
              "##########" + "\n" +
              "#..%@~...#" + "\n" +
              "#...#....#" + "\n" +
              "#........#" + "\n" +
              "#........#" + "\n" +
              "##########";
        BattleMap map = new MapHelper().createMap(mapString);
        assertEquals(mapString, mapHelper.mapToString(map));
    
    }
    
    public void testCountMatches() throws Exception {
        assertEquals(3, mapHelper.countMatches("\n\n\n", "\n"));
        assertEquals(2, mapHelper.countMatches("aaa  \n aaaaa\n", "\n"));
    }
    
    public void testMoreMaps() throws Exception {
        BattleMap map = Tutorial.buildTutorialMap();
        mapHelper.mapToString(map);
    }
    
}

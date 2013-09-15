package tyrant.mikera.tyrant.test;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.IThingFilter;
import tyrant.mikera.tyrant.IsFilter;
import tyrant.mikera.tyrant.NameFilter;
import tyrant.mikera.tyrant.OrFilter;
import tyrant.mikera.tyrant.RogueLikeFilter;

public class Filters_TC extends TyrantTestCase {
    private Thing carrot;
    private Thing cloak;
    private Thing coin;
    private Thing bone;

    public void testNameFilter() throws Exception {
        NameFilter nameFilter = new NameFilter();
        hero.addThing(carrot);
        assertTrue(nameFilter.accept(carrot, "a"));
        assertTrue(nameFilter.accept(carrot, "c"));
        assertTrue(nameFilter.accept(carrot, "carrot"));
        assertTrue(nameFilter.accept(carrot, "carrot    "));
    }
    
    public void testRougeLikeFilter() throws Exception {
        RogueLikeFilter filter = new RogueLikeFilter();
        setUp();
        hero.addThing(carrot);
        hero.addThing(cloak);
        hero.addThing(coin);
        hero.addThing(bone);
        assertTrue("" + carrot + "should be type of food", filter.accept(carrot, "%"));
        assertTrue("" + cloak + "should be type of armor", filter.accept(cloak, "["));
        assertTrue("" + coin + "should be type of money", filter.accept(coin, "$"));
        assertTrue("" + bone + "should be type of weapon", filter.accept(bone, "("));
    }

    protected void setUp() throws Exception {
        super.setUp();
        carrot = Lib.create("carrot");
        cloak = Lib.create("light cloak");
        coin = Lib.create("gold coin");
        bone = Lib.create("bone");
    }
    
    public void testOrFilter() throws Exception {
        IThingFilter orFilter = new OrFilter(new RogueLikeFilter(), new NameFilter());
        assertTrue("" + carrot + " should be accepted", orFilter.accept(carrot, "car"));
        assertTrue("" + carrot + " should be of type food", orFilter.accept(carrot, "%"));
    }
    
    public void testIsFilter() throws Exception {
        IThingFilter isFilter = new IsFilter();
        assertTrue("" + carrot + " should be accepted", isFilter.accept(carrot, "[IsFood]"));
        assertTrue("" + carrot + " should be of type food", isFilter.accept(carrot, "IsFood"));
        assertFalse(isFilter.accept(carrot, "isfood"));
        
        carrot.set("IsFood", false);
        assertFalse(isFilter.accept(carrot, "IsFood"));
    }
}

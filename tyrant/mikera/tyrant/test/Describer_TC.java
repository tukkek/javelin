package tyrant.mikera.tyrant.test;

import tyrant.mikera.engine.Describer;
import tyrant.mikera.engine.Description;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.*;

/**
 * @author Chris Grindstaff chris@gstaff.org
 */
public class Describer_TC extends TyrantTestCase {
    public void testSingle() {
        assertEquals("carrot", Describer.describe(person, Lib.create("carrot")));
        assertEquals("carrot", Describer.describe(person, Lib.create("1carrot")));
        assertEquals("234 carrots", Describer.describe(person, Lib.create("234 carrot")));
    }

    public void testCursed() {
        Thing carrot = Lib.create("carrot");
        carrot.set("IsStatusKnown", true);
        carrot.set("IsCursed", true);

        assertEquals("cursed carrot", Describer.describe(person, carrot));
    }

    public void testBlessed() {
        Thing carrot = Lib.create("carrot");
        carrot.set("IsStatusKnown", true);
        carrot.set("IsBlessed", true);

        assertEquals("blessed carrot", Describer.describe(person, carrot));
    }

    public void testRunic() {
        Thing carrot = Lib.create("carrot");
        carrot.set("IsStatusKnown", true);
        carrot.set("IsRunic", true);

        assertEquals("runic carrot", Describer.describe(person, carrot));
    }

    public void testBlessedRunic() {
        Thing carrot = Lib.create("carrot");
        carrot.set("IsStatusKnown", true);
        carrot.set("IsRunic", true);
        carrot.set("IsBlessed", true);

        assertEquals("blessed runic carrot", Describer.describe(person, carrot));
    }

    public void testDefiniteArticle() {
        assertEquals("the carrot", Describer.describe(person, Lib.create("carrot"), Description.ARTICLE_DEFINITE));
    }

    public void testIndefiniteArticle() {
        assertEquals("a carrot", Describer.describe(person, Lib.create("carrot"), Description.ARTICLE_INDEFINITE));
    }
    
    public void testIndefiniteArticle_withVowell() {
        assertEquals("an acid attack", Describer.describe(person, Lib.create("acid attack"), Description.ARTICLE_INDEFINITE));
    }

    public void testPossesiveArticle() {
        assertEquals("your carrot", Describer.describe(person, Lib.create("carrot"), Description.ARTICLE_POSSESIVE));
    }
    
    public void testProperNoun() {
        Thing ring = Lib.create("The One Ring");
        assertEquals("strangely plain gold ring", Describer.describe(person, ring));
        Item.identify(ring);
        assertEquals("The One Ring", Describer.describe(person, ring, Description.ARTICLE_POSSESIVE));
    }
    
    public void testQuantity() {
        Thing carrot = Lib.create("2 carrot");
        carrot.set("NameType", Description.NAMETYPE_QUANTITY);
        assertEquals("2 carrots", Describer.describe(person, carrot, Description.ARTICLE_DEFINITE));
    }
}

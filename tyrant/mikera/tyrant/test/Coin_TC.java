package tyrant.mikera.tyrant.test;

import java.util.StringTokenizer;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Coin;


/**
 * @author Chris Grindstaff chris@gstaff.org
 */

public class Coin_TC extends TyrantTestCase {

    public void testCreate_cooper() {
        Thing coins = Coin.createMoney(9);
        assertEquals("9 copper coins", coins.getName(null));
        assertEquals(9, coins.getNumber());
    }

    public void testCreate_silver() {
        Thing coins = Coin.createMoney(123);
        assertEquals("12 silver coins", coins.getName(null));
        assertEquals(12, coins.getNumber());
    }

    public void testCreate_gold() {
        Thing coins = Coin.createMoney(9999);
        assertEquals("99 gold coins", coins.getName(null));
        assertEquals(99, coins.getNumber());
    }

    public void testCreate_gold2() {
        Thing coins = Coin.createMoney(999999);
        assertEquals("9999 gold coins", coins.getName(null));
        assertEquals(9999, coins.getNumber());
    }

    /**
     * @see http://sourceforge.net/tracker/index.php?func=detail&aid=994528&group_id=16696&atid=116696
     */
    public void testRemove() {
        person.addThing(Lib.create("8 gold coin"));
        Coin.removeMoney(person, 2);
        assertEquals(weightOf("7 gold, 9 silver, 8 copper"), person.getInventoryWeight());
        assertEquals(798, Coin.getMoney(person));
    }

    public void testRemove_negative() {
        Coin.removeMoney(person, -2);
        assertEquals(weightOf("2 copper"), person.getInventoryWeight());
        assertEquals(2, Coin.getMoney(person));

    }

    public void testAdd() {
        Coin.addMoney(person, 10000);
        assertEquals(weightOf("10 sovereign"), person.getInventoryWeight());
        assertEquals(10000, Coin.getMoney(person));
    }

    public void testAdd_negative() {
        Coin.addMoney(person, 10);
        Coin.addMoney(person, -10);
        assertEquals(0, person.getInventoryWeight());
        assertEquals(0, Coin.getMoney(person));
    }

    private int weightOf(String toParse) {
        int total = 0;
        StringTokenizer tokenizer = new StringTokenizer(toParse, ",");
        while (tokenizer.hasMoreTokens()) {
            String name = tokenizer.nextToken().trim();
            name += name.endsWith(Coin.SOVEREIGN) ? "" : " coin";
            Thing money = Lib.create(name);
            total += money.getWeight();
        }
        return total;
    }
}

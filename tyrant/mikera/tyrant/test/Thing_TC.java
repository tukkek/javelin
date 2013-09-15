package tyrant.mikera.tyrant.test;

import java.util.Arrays;
import java.util.List;

import javelin.model.BattleMap;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.*;




/**
 * @author Chris Grindstaff chris@gstaff.org
 */
public class Thing_TC extends TyrantTestCase {

    public void testName() {
        assertEquals("rabbit", Lib.create("rabbit").getFullName(null));
    }

    public void testAdd() {
        Thing carrot = Lib.create("carrot");
        assertNull(carrot.place);
        person.addThing(carrot);
        assertEquals(person, carrot.place);
    }

    public void testAdd_noStackFlag_duplicate() {
        person.addThing(Lib.create("ginger root"));
        Thing ginger = Lib.create("ginger root");
        ginger.set("NoStack", true);
        person.addThing(ginger);
        assertEquals(1, person.invCount());
    }

    public void testAdd_noStackFlag() {
        person.addThing(Lib.create("carrot"));
        Thing ginger = Lib.create("ginger root");
        ginger.set("NoStack", true);
        person.addThing(ginger);
        assertEquals(2, person.invCount());
    }

    public void testAdd_withEffect() {
        Thing carrot = Lib.create("carrot");
        person.addThing(carrot);
        person.addThing(Lib.create("slow"));
        assertEquals(2, person.invCount());
        Thing hasteEffect = Lib.create("haste");
        person.addThing(hasteEffect);
        assertEquals(2, person.invCount());
        assertSame(carrot, person.getInventory()[0]);
        assertSame(hasteEffect, person.getInventory()[1]);
    }

    public void testBasicProperties() {
        String key = "a_special_key";
        person.set(key, person);
        assertSame(person, person.get(key));

        person.set(key, 321);
        assertEquals(321, person.getStat(key));
    }
    
    public void testScriptProperties() {
        Script sc1=Scripts.generator("IsMonster",100); 
        Script sc2=Scripts.generator("IsGoblinoid",200);
        Script sc3=Scripts.generator("IsDemonic",300);
        
        Thing p1=new Thing();
        assertTrue(p1.set("OnSomething", sc1));
        assertEquals(sc1,p1.get("OnSomething"));
        
        // set to identical value
        // true because local sets not checked
        assertTrue(p1.set("OnSomething", sc1));
        
        // initial inheritance
        Thing p2=new Thing(p1);
        assertEquals(sc1,p2.get("OnSomething"));
        
        // set to identical script value
        // false because already equal to inherited
        assertFalse(p2.set("OnSomething", sc1));
        
        // changed base
        assertTrue(p1.set("OnSomething", sc2));
        assertEquals(sc2,p2.get("OnSomething"));
        
        // override
        assertTrue(p2.set("OnSomething", sc3));
        assertEquals(sc3,p2.get("OnSomething"));
    }

    public void testAddAttribute() {
        assertNull(person.get("IsConfused"));
        person.addAttribute(Lib.create("confusion"));
        assertEquals(new Integer(1), person.get("IsConfused"));
    }

    public void testAddThingWithStacking() {
        Thing carrots = Lib.create("9 carrot");
        person.addThing(carrots);
        person.addThingWithStacking(Lib.create("5 carrot"));
        assertEquals(1, person.invCount());
        Thing inInventory = person.getInventory()[0];
        assertEquals(carrots, inInventory);
        assertEquals(14, inInventory.getNumber());
        assertEquals(14, carrots.getNumber());
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
          BattleMap map = new MapHelper().createMap(mapString);
          assertTrue(map.getThings(4, 1)[0].canSee(map, 4, 2));
          assertFalse(map.getThings(4, 1)[0].canSee(map, 4, 3));
          assertFalse(map.getThings(4, 1)[0].canSee(map, 4, 4));
      }

    public void testClearUsage() {
        Thing carrot = Lib.create("carrot");
        person.addThing(carrot);
        person.wield(carrot, RPG.WT_MAINHAND);
        assertEquals(carrot, person.getWielded(RPG.WT_MAINHAND));

        assertTrue(person.clearUsage(RPG.WT_MAINHAND));
        assertNull(person.getWielded(RPG.WT_MAINHAND));
    }

    public void testClearUsage_cursed() {
        Thing carrot = Lib.create("carrot");
        carrot.set("IsCursed", true);
        person.addThing(carrot);
        person.wield(carrot, RPG.WT_MAINHAND);
        assertEquals(carrot, person.getWielded(RPG.WT_MAINHAND));

        assertFalse(person.clearUsage(RPG.WT_MAINHAND));
        assertEquals(carrot, person.getWielded(RPG.WT_MAINHAND));
    }

    public void testDamage() {
    	Thing h=Lib.create("human");
        Thing carrot = Lib.create("carrot");
        h.addThing(carrot);
        h.wield(carrot, RPG.WT_MAINHAND);
        assertEquals(2, Damage.inflict(h,2, RPG.DT_IMPACT));
        assertFalse(carrot.isDead());
        assertTrue(h.getStat("HPS")>0);
        assertEquals(200, Damage.inflict(h,200, RPG.DT_DISINTEGRATE));
        // MA: not necessarily true - random effect
        // assertTrue(carrot.isDead());
        assertTrue(h.isDead());
    }

    /**
     *BUG http://sourceforge.net/tracker/index.php?func=detail&aid=982664&group_id=16696&atid=116696
     */
    public void testWield() {
        Thing shoes = Lib.create("dancing shoes");
        person.addThing(shoes);
        assertTrue(person.wield(shoes, RPG.WT_BOOTS));
        assertEquals(shoes, person.getWielded()[0]);
        
        Thing anotherPair = Lib.create("dancing shoes");
        person.addThingWithStacking(anotherPair);
        assertEquals(1, shoes.getNumber());
        assertEquals(2, person.invCount());
        
        person.wield(anotherPair, RPG.WT_BOOTS);
        assertEquals(1, person.getWielded().length);
        assertEquals(anotherPair, person.getWielded()[0]);
    }
    
    public void testStackingWithSimilarItems() {
        Thing carrot = Lib.create("carrot");
        carrot.set("IsCursed", true);
        /* TODO Shouldn't these 2 stack together until the user 
         * determines that one carrot is blessed and the other 
         * isn't? Otherwise you give that fact away.
         */
        person.addThing(carrot);
        person.addThingWithStacking(Lib.create("carrot"));
        assertEquals(1, carrot.getNumber());
    }
    
    public void testStackingWithSameRunes() {
        Thing carrot1 = Lib.create("carrot");
        carrot1.addThing(Lib.create("lightness rune"));
        Thing carrot2 = Lib.create("carrot");
        carrot2.addThing(Lib.create("lightness rune"));
        
        person.addThing(carrot1);
        person.addThingWithStacking(carrot2);
        
        assertEquals(2,carrot1.getNumber());
    }
    
    public void testDisplace_noRoom() throws Exception {
        String mapString = 
            "---" + "\n" +
            "|=|" + "\n" +
            "---";
        BattleMap map = new MapHelper().createMap(mapString);
        Thing ring = map.getThings(1, 1)[0];
        RPG.setRandSeed(0);
        ring.displace();
        assertLocation(ring, 1, 1);
    }
    
    public void testDisplace() throws Exception {
        String mapString = 
            "..." + "\n" +
            ".=." + "\n" +
            "...";
        BattleMap map = new MapHelper().createMap(mapString);
        Thing ring = map.getThings(1, 1)[0];
        ring.displace();
        assertFalse((ring.x==1)&&(ring.y==1));
    }
    
    public void testReplace() {
    	Thing a=new Thing();
    	Thing b=new Thing(a);
    	a.set("A",1);
    	b.set("B",1);
    	
    	Thing c=new Thing();
    	c.replaceWith(b);

    	assertEquals(1,c.getStat("A"));
    	assertEquals(1,c.getStat("B"));
    	
    	// test for correct inheritance from a
    	// but not from b
    	a.set("A",2);
    	b.set("B",2);
    	assertEquals(2,c.getStat("A"));
    	assertEquals(1,c.getStat("B"));
    	
    	// test that we are using a clone of b's local properties
    	c.set("B",3);
    	assertEquals(2,b.getStat("B"));
    }
    
    public void testExits() throws Exception {
        String mapString = 
            "##.##" + "\n" +
            "##.##" + "\n" +
            "@...#" + "\n" +
            "#####"; 
        new MapHelper().createMap(mapString);
        assertEquals(0, hero.orthogonalExits(1, 0).size());
        
        mapString = 
            "##.##" + "\n" +
            "#..##" + "\n" +
            ".@..#" + "\n" +
            "#..##"; 
        new MapHelper().createMap(mapString);
        assertEquals(2, hero.orthogonalExits(1, 0).size());
    }
    
    public void testFindAttributesStartingWith() throws Exception {
        Thing thing = new Thing();
        thing.set("Age", 17);
        thing.set("HairColor", "red");
        thing.set("Laptop", "T41p");
        thing.set("IsPerson", true);
        thing.set("IsProgrammer", true);
        
        String[] attributes = thing.findAttributesStartingWith("Is");
        assertEquals(2, attributes.length);
        List listOfAttributes = Arrays.asList(attributes);
        assertTrue(listOfAttributes.contains("IsPerson"));
        assertTrue(listOfAttributes.contains("IsProgrammer"));
    }
    
    public void testFindAttributesStartingWith_inherited() throws Exception {
        Thing parent = new Thing();
        parent.set("IsPerson", true);
        parent.set("IsProgrammer", true);
        Thing thing = new Thing(parent);
        
        assertEquals(2, thing.findAttributesStartingWith("Is").length);
    }
    
    public void testFindAttributesStartingWith_none() throws Exception {
        Thing parent = new Thing();
        parent.set("Age", 17);
        parent.set("HairColor", "red");
        parent.set("Laptop", "T41p");
        Thing thing = new Thing(parent);
        
        String[] attributes = thing.findAttributesStartingWith("If");
        assertEquals(0, attributes.length);
    }
    
    /**
     * Nothing
     * Right
     * Right Left
     * Left
     * Both
     */
    public void testInHandMessage() throws Exception {
        unwieldAll(hero);
        assertEquals("nothing in hand", hero.inHandMessage());

        //Right
        Thing cudgel = Lib.create("cudgel");
        hero.addThing(cudgel);
        hero.wield(cudgel, RPG.WT_MAINHAND);
        assertEquals("cudgel in right hand", hero.inHandMessage());
        
        //Right and Left
        Thing  bone = Lib.create("bone");
        hero.addThing(bone);
        hero.wield(bone, RPG.WT_SECONDHAND);
        assertEquals("cudgel in right hand, bone in left hand", hero.inHandMessage());
        
        //Left
        unwieldAll(hero);
        hero.wield(bone, RPG.WT_SECONDHAND);
        assertEquals("bone in left hand", hero.inHandMessage());

        Thing hugeBone = Lib.create("huge bone");
        hero.addThing(hugeBone);
        unwieldAll(hero);
        hero.wield(hugeBone, RPG.WT_TWOHANDS);
        assertEquals("huge bone in both hands", hero.inHandMessage());
        
    }

    private void unwieldAll(Thing thing) {
        thing.clearUsage(RPG.WT_TWOHANDS);
        thing.clearUsage(RPG.WT_MAINHAND);
        thing.clearUsage(RPG.WT_SECONDHAND);
    }

    public void testGetFlaggedItems() {
        Thing money = Coin.createMoney(21);
        Thing moreMoney = Coin.createMoney(200);
        Thing evenMoreMoney = Lib.create("6 silver coin");
        Thing food = Lib.create("chicken leg");
        Thing moreFood = Lib.create("dead fly");
        person.addThing(money);
        person.addThing(moreMoney);
        person.addThing(evenMoreMoney);
        person.addThing(food);
        person.addThing(moreFood);
        assertEquals(5, person.invCount());
        Thing[] moneyArray = person.getFlaggedItems("IsMoney");
        assertEquals(3,moneyArray.length);
        assertEquals("copper coin",moneyArray[0].getString("Name"));
        assertEquals(21,moneyArray[0].getStat("Number"));
        assertEquals("gold coin",moneyArray[1].getString("Name"));
        assertEquals(2,moneyArray[1].getStat("Number"));
        assertEquals("silver coin",moneyArray[2].getString("Name"));
        assertEquals(6,moneyArray[2].getStat("Number"));
        Thing[] foodArray = person.getFlaggedItems("IsFood");
        assertEquals(2,foodArray.length);
        assertEquals("chicken leg",foodArray[0].getString("Name"));
        assertEquals("dead fly",foodArray[1].getString("Name"));
        Thing[] emptyArray = person.getFlaggedItems("IsScroll");
        assertNull(emptyArray);
    }

    public void testGetFlaggedContents() {
        Thing money = Coin.createMoney(21);
        Thing moreMoney = Coin.createMoney(200);
        Thing evenMoreMoney = Lib.create("6 silver coin");
        Thing food = Lib.create("chicken leg");
        Thing moreFood = Lib.create("dead fly");
        person.addThing(money);
        person.addThing(moreMoney);
        person.addThing(evenMoreMoney);
        person.addThing(food);
        person.addThing(moreFood);
        assertEquals(5, person.invCount());
        Thing[] moneyArray = person.getFlaggedContents("IsMoney");
        assertEquals(3,moneyArray.length);
        assertEquals("copper coin",moneyArray[0].getString("Name"));
        assertEquals(21,moneyArray[0].getStat("Number"));
        assertEquals("gold coin",moneyArray[1].getString("Name"));
        assertEquals(2,moneyArray[1].getStat("Number"));
        assertEquals("silver coin",moneyArray[2].getString("Name"));
        assertEquals(6,moneyArray[2].getStat("Number"));
        Thing[] foodArray = person.getFlaggedContents("IsFood");
        assertEquals(2,foodArray.length);
        assertEquals("chicken leg",foodArray[0].getString("Name"));
        assertEquals("dead fly",foodArray[1].getString("Name"));
        Thing[] emptyArray = person.getFlaggedContents("IsScroll");
        assertEquals(0,emptyArray.length);
    }
/*
    public void testTimingComparison() {
        Thing money = Coin.createMoney(21);
        Thing moreMoney = Coin.createMoney(200);
        Thing evenMoreMoney = Lib.create("6 silver coin");
        Thing food = Lib.create("chicken leg");
        Thing moreFood = Lib.create("dead fly");
        System.gc();
        long startFlaggedItems = System.currentTimeMillis();
        for (int index = 0; index < 1000000; index++) {
            Thing owner = Lib.create("human");
            owner.addThing(money);
            owner.addThing(moreMoney);
            owner.addThing(evenMoreMoney);
            owner.addThing(food);
            owner.addThing(moreFood);
            //assertEquals(5, owner.invCount());
            Thing[] moneyArray = owner.getFlaggedItems("IsMoney");
            //assertEquals(3,moneyArray.length);
            Thing[] foodArray = owner.getFlaggedItems("IsFood");
            //assertEquals(2,foodArray.length);
            Thing[] emptyArray = owner.getFlaggedItems("IsScroll");
            //assertNull(emptyArray);
        }
        System.gc();
        long endFlaggedItems = System.currentTimeMillis();
        long startFlaggedContents = System.currentTimeMillis();
        for (int index = 0; index < 1000000; index++) {
            //System.out.print(".");
            Thing owner = new Thing(); //Lib.create("human");
            owner.addThing(money);
            owner.addThing(moreMoney);
            owner.addThing(evenMoreMoney);
            owner.addThing(food);
            owner.addThing(moreFood);
            //assertEquals(5, owner.invCount());
            Thing[] moneyArray = owner.getFlaggedContents("IsMoney");
            //assertEquals(3,moneyArray.length);
            Thing[] foodArray = owner.getFlaggedContents("IsFood");
            //assertEquals(2,foodArray.length);
            Thing[] emptyArray = owner.getFlaggedContents("IsScroll");
            //assertEquals(0,emptyArray.length);
        }
        System.gc();
        long endFlaggedContents = System.currentTimeMillis();
        System.out.println("Start getFlaggedContents: " + startFlaggedContents);
        System.out.println("End getFlaggedContents: " + endFlaggedContents);
        long contentsTime = endFlaggedContents - startFlaggedContents;
        System.out.println("Elapsed Time getFlaggedContents: " + contentsTime);
        System.out.println("Start getFlaggedItemss: " + startFlaggedItems);
        System.out.println("End getFlaggedItems: " + startFlaggedItems);
        long itemsTime = endFlaggedItems - startFlaggedItems;
        System.out.println("Elapsed Time getFlaggedItems: " + itemsTime);
    }
*/
}

package tyrant.mikera.tyrant.test;

import javelin.controller.old.Game;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Damage;

public class Damage_TC extends TyrantTestCase {
    public void testInventoryDamage() throws Exception {
        Thing rabbit = Lib.create("rabbit");
        Thing carrots = Lib.create("10 carrot");
        Thing parsnips = Lib.create("parsnip");
        rabbit.addThing(carrots);
        rabbit.addThing(parsnips);
        assertEquals(10, rabbit.inv()[0].getNumber());
        Damage.damageInventory(rabbit, 100000, RPG.DT_DISINTEGRATE, 100);
        assertEquals(0, rabbit.invCount());
    }
    public void testInventoryDamage_bagProtection() throws Exception {
    	Game.create();
    	
        Thing hero = Lib.create("rat");
        Thing carrots = Lib.create("10 carrot");
        Thing scroll = Lib.create("scroll of total identification");
        Thing bagScroll = Lib.create("invulnerable scroll case");
        
        int startInventory=hero.invCount();
        hero.addThing(carrots);
        hero.addThing(scroll);
        hero.addThing(bagScroll);
        hero.wield(bagScroll);
        assertEquals(startInventory+3, hero.invCount());
        Damage.damageInventory(hero, 100000, RPG.DT_DISINTEGRATE, 100);
        assertEquals(startInventory+1, hero.invCount());
    }
    public void testInventoryDamage_bagProtectionFire() throws Exception {
        Thing hero = Lib.create("rat");
        Thing carrots = Lib.create("10 carrot");
        Thing bagFood = Lib.create("waterproof food sack");
        Thing scroll = Lib.create("scroll of total identification");
        Thing bagScroll = Lib.create("fireproof scroll case");
        
        
        int startInventory=hero.invCount();
        hero.addThing(carrots);
        hero.addThing(scroll);
        hero.addThing(bagFood);
        hero.addThing(bagScroll);
        hero.wield(bagScroll);
        hero.wield(bagFood);
        assertEquals(startInventory+4, hero.invCount());
        Damage.damageInventory(hero, 900, RPG.DT_FIRE, 100);
        assertEquals(startInventory+1, hero.invCount());
    }
    public void testInventoryDamage_itemWithSeveral() throws Exception {
        Thing rabbit = Lib.create("rabbit");
        Thing carrots = Lib.create("10 carrot");
        Thing parsnips = Lib.create("10 parsnip");
        carrots.set("HPS", 4);
        carrots.set("HPSMAX", 4);
        parsnips.set("HPS", 2);
        parsnips.set("HPSMAX", 2);
        rabbit.addThing(carrots);
        rabbit.addThing(parsnips);
        Damage.damageInventory(rabbit, 8, RPG.DT_DISINTEGRATE, 100);
        assertEquals(8, carrots.getNumber());
        assertEquals(6, parsnips.getNumber());
    }
    
    public void testDestructible() {
    	// smash a carrot
    	Thing carrot=Lib.create("carrot");
    	assertTrue(0<Damage.inflict(carrot,1000,RPG.DT_IMPACT));
    	assertTrue(carrot.isDead());
    	
    	// not destructible due to IsDestructible flag
    	carrot=Lib.create("carrot");
    	carrot.set("IsDestructible",0);
    	assertEquals(0,Damage.inflict(carrot,1000,RPG.DT_IMPACT));
    	assertTrue(!carrot.isDead());
    	
    	// not destructible due to very high RES
    	carrot=Lib.create("carrot");
    	carrot.set("RES:impact",1000000);
    	assertEquals(0,Damage.inflict(carrot,10,RPG.DT_IMPACT));
    	assertTrue(!carrot.isDead());
    	
    }
}

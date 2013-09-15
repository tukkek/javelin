package tyrant.mikera.tyrant.test;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Event;
import tyrant.mikera.tyrant.EventHandler;
import tyrant.mikera.tyrant.Food;
import tyrant.mikera.tyrant.util.Count;


public class Food_TC extends TyrantTestCase {
    public void testEating() throws Exception {
        Thing salmon = Lib.create("large salmon");
        salmon.set("IsBlessed", true);
        hero.addThing(salmon);
        int howHungry = 10;
        int nutritionalValue = Food.nutritionValue(salmon, hero);
        assertTrue(nutritionalValue > howHungry);
        hero.set(RPG.ST_HUNGER, howHungry);
        Food.eat(hero, salmon);
        assertEquals(0, hero.getStat(RPG.ST_HUNGER));
        assertEquals(1, hero.getItem("large salmon").getNumber());
        assertEquals(nutritionalValue - howHungry, salmon.getStat("FoodValue"));
    }
    
    public void testEatingSalmon_withEffect() throws Exception {
        Thing salmon = Lib.create("large salmon");
        salmon.set("IsBlessed", true);
        hero.addThing(salmon);
        final Count timesCalled = new Count();
        salmon.addHandler("OnEaten", new EventHandler() {
            public boolean handle(Thing t, Event e) {
                timesCalled.increment();
                return false;
            }
        });
        hero.addThing(salmon);
        Food.eat(hero, salmon);
        assertEquals(1, timesCalled.value);
        Food.eat(hero, salmon);
        assertEquals(1, timesCalled.value);
    }
    
    public void testEating_andRemoving() throws Exception {
        Thing apple = Lib.create("apple");
        hero.addThing(apple);
        int howHungry = hero.getStat(RPG.ST_HUNGERTHRESHOLD) * 3;
        int nutritionalValue = Food.nutritionValue(apple, hero);
        assertTrue(nutritionalValue < howHungry);
        hero.set(RPG.ST_HUNGER, howHungry);
        Food.eat(hero, apple);
        assertEquals(howHungry - nutritionalValue, hero.getStat(RPG.ST_HUNGER));
        assertFalse(hero.hasItem("apple"));
    }
    
    public void testEating_effectCalledOnlyOnce() throws Exception {
        Thing apple = Lib.create("apple");
        final Count timesCalled = new Count();
        apple.addHandler("OnEaten", new EventHandler() {
            public boolean handle(Thing t, Event e) {
                timesCalled.increment();
                return false;
            }
        });
        hero.addThing(apple);
        Food.eat(hero, apple);
        assertEquals(1, timesCalled.value);
        Food.eat(hero, apple);
        assertEquals(1, timesCalled.value);
    }
}

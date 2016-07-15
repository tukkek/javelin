package tyrant.mikera.tyrant.test;

import javelin.controller.old.Game;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.*;

public class Hero_TC extends TyrantTestCase {
    public void testIsHero() {
        assertEquals(hero, Game.hero());
        assertTrue(hero.isHero());
    }
    
    public void testExperience() {
        Thing hero = Hero.createHero("bob", "human", "fighter");
        assertEquals(1, hero.getLevel());
        assertEquals(0, hero.getStat(RPG.ST_EXP));
        Hero.gainExperience(1000);
        assertEquals(8, hero.getLevel());
    }
    
    public void testKill() {
        Thing hero = Hero.createHero("bob", "human", "fighter");
        Thing goblin=Lib.create("goblin");

        assertTrue(hero.getStat(RPG.ST_EXP)==0);

        assertEquals(0,Hero.getKillCount(Lib.create("goblin")));
    
        // kill 1
        Hero.gainKillExperience(hero,goblin);
        assertEquals(1,Hero.getKillCount(Lib.create("goblin")));

        // kill 2
        Hero.gainKillExperience(hero,goblin);
        assertEquals(2,Hero.getKillCount(Lib.create("goblin")));

        assertTrue(hero.getStat(RPG.ST_EXP)>0);
    }
    
    public void testHeroChoices() {
    	String[] races=Hero.heroRaces();
    	for (int i=0; i<races.length; i++) {
    		String race=races[i];
    		String[] profs=Hero.heroProfessions(race);
        	for (int j=0; j<profs.length; j++) {
        		String prof=profs[j];
    		
        		Thing h=Hero.createHero("JUnitTester",race,prof);
        		
        		assertTrue(h!=null);
        		assertTrue(h.getFlag("IsHero"));
        	}
    	}
    	
    }
}

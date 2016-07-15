package tyrant.mikera.tyrant.perf;

import javelin.controller.action.Action;
import javelin.controller.old.Game;
import javelin.model.BattleMap;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.BaseObject;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Hero;
import tyrant.mikera.tyrant.QuestApp;
import tyrant.mikera.tyrant.test.MapHelper;
import tyrant.mikera.tyrant.test.NullHandler;
import tyrant.mikera.tyrant.test.TyrantTestCase;


public class KillAllBaddies implements IWork {
    private Thing hero;
    private BattleMap map;
    private BattleScreen gameScreen;

    public KillAllBaddies() {
    	// empty
    }
    
    public void run() {
        boolean originalGetSet = BaseObject.GET_SET_DEBUG;
        int ticks = -1;
        try {
            while (monstersAreLeft(map)) {
                ticks++;
//                    if(ticks % 10 == 0) {
//                        System.err.println(map);
//                    }
//                    BaseObject.GET_SET_DEBUG = true;
                Action direction = hero.x == map.getWidth() - 2 ? Action.MOVE_W : Action.MOVE_E;
                gameScreen.tryTick(hero, direction, false);
                BaseObject.GET_SET_DEBUG = false;
            }
        } finally {
            BaseObject.GET_SET_DEBUG = originalGetSet;
        }
//        System.out.println("ticks " + ticks);
//            LibInspector libInspector = new LibInspector();
//            libInspector.go(new String[] {"IsHostile"});
//            libInspector.go(new String[] {"IsMobile"});
    }

    public void setUp() {
        RPG.setRandSeed(0);
        Lib.clear();
        hero = Hero.createHero("bob", "human", "fighter");
        TyrantTestCase.setTestHero(hero);
        NullHandler.installNullMessageHandler();
        Game.setUserinterface(null);
        
        String mapString = 
            "################################" + "\n" +
            "#@.............................#" + "\n" +
            "################################";
        
        map = new MapHelper().createMap(mapString);
        for (int x = hero.x; x < map.getWidth(); x++) {
            if (!map.isBlocked(x, 1)) map.addThing(Lib.create("[IsMonster]"), x, 1);
        }
        hero.set("IsImmortal", true);
        gameScreen = new BattleScreen(new QuestApp());
        gameScreen.map = map;
    }
    
    private boolean monstersAreLeft(javelin.model.BattleMap map) {
        for (int i = 0; i < map.getThings().length; i++) {
            Thing thing = map.getThings()[i];
            if(thing.getFlag("IsHostile")) return true;
        }
        return false;
    }
    
    public String getMessage() {
        return "";
    }
}

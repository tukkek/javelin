package tyrant.mikera.tyrant.test;

import java.awt.Button;
import java.awt.event.KeyEvent;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Hero;
import tyrant.mikera.tyrant.Interface;

import junit.framework.TestCase;

/**
 * The sole purpose of this is to consistently seed the random # generator.
 * @author Chris Grindstaff chris@gstaff.org
 */
public class TyrantTestCase extends TestCase {
    private class ChainedInterface extends Interface {
        private final char aChar;
        private Interface next;
        private int keyEvent;
        
        
        private ChainedInterface(Interface next, int keyEventConstant) {
            this(keyEventConstant, '0');
            this.next = next;
        }

        private ChainedInterface(Interface next, char aChar) {
            this(KeyEvent.VK_P, aChar);
            this.next = next;
        }
        
        private ChainedInterface(int keyEvent, char aChar) {
            this.aChar = aChar;
            this.keyEvent = keyEvent;
        }

        public void getInput() {
            keyevent = new KeyEvent(button, 0, 0, 0, keyEvent, aChar);
            if(next != null) next.getInput();
        }
    }

    protected Thing person;
    protected java.util.List messages;
    protected static Thing hero;
    protected Button button = new Button();

    /**
     * Return the last message
     * 
     * @return
     */
    protected String lastMessage() {
    	int messageCount=messages.size();
    	if (messageCount==0) return null;
    	return ((String)messages.get(messageCount-1)).trim();
    }
    
    public static Thing getTestHero() {
    	return hero;
    }
    
    public static void setTestHero(Thing h) {
        hero = h;
        Game.instance().setHero(h);
    }
    
    protected void setUp() throws Exception {
        RPG.setRandSeed(0);
        setTestHero(Hero.createHero("bob", "human", "fighter"));
        RPG.setRandSeed(0);
        messages = NullHandler.installNullMessageHandler();
        Game.setUserinterface(null);
        person = Lib.create("human");
    }

    protected void tearDown() throws Exception {

    	// nothing to do
    }
    
    
    
    public void testNothing() {
        /* 
         * to appease JUnit, otherwise it will create a warning for this class.
         * Unfortunately this causes this test to be inherited by all my children.
         */
        assertTrue(true);
    }

    protected void assertLocation(Thing thing, int expectedX, int expectedY) {
        assertEquals(expectedX, thing.getMapX());
        assertEquals(expectedY, thing.getMapY());
    }
    
    protected void answerGetInputWithChar(final char aChar) {
        Interface current = Game.getUserinterface();
        if(current instanceof ChainedInterface) {
            Game.setUserinterface(new ChainedInterface(current, aChar));
        } else 
            Game.setUserinterface(new ChainedInterface(KeyEvent.VK_P, aChar));
    }
    
    protected void answerGetInputWith(int keyEventConstant) {
        Interface current = Game.getUserinterface();
        if(current instanceof ChainedInterface) {
            Game.setUserinterface(new ChainedInterface(current, keyEventConstant));
        } else 
            Game.setUserinterface(new ChainedInterface(keyEventConstant, '0'));
    }
}

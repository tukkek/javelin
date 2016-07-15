package tyrant.mikera.tyrant.test;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javelin.controller.old.Game;

import java.io.*;

import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.*;



/**
 * @author Chris Grindstaff chris@gstaff.org
 */
public class Game_TC extends TyrantTestCase {
    private ArrayList theMessages;

    protected void setUp() throws Exception {
        super.setUp();
        theMessages = new ArrayList();
        NullHandler.installNullMessageHandler(theMessages);
    }

    public void testMessage_single() {
        Game.messageTyrant("bob");
        assertEquals(1, theMessages.size());
        assertEquals("Bob\n", theMessages.get(0));
        assertEquals("Bob", Game.instance().getMessageList().get(0));
    }
    
    public void testSave() {
    	assertTrue(Game.instance()!=null);
    	
    	ByteArrayOutputStream bos=new ByteArrayOutputStream();
    	try {
    		Thing h=Game.hero();
    		
    		assertTrue(Game.save(new ObjectOutputStream(bos)));
    	
    		ByteArrayInputStream bis=new ByteArrayInputStream(bos.toByteArray());
    	
    		assertTrue(Game.restore(new ObjectInputStream(bis)));
    	
    		assertTrue(Game.hero()!=h);
    	} catch (IOException t) {
    		fail("IO Excepion");
    	}
    	
    	
    }

    public void testMessage_multiple() {
        Game.messageTyrant("bob");
        Game.messageTyrant("bob");
        Game.messageTyrant("bob");
        assertEquals(3, theMessages.size());
        assertEquals("Bob\n", theMessages.get(0));
        assertEquals("Bob (x3)", Game.instance().getMessageList().get(0));
    }

    public void testCreate() {
    	Game.create();
    	Game.setDebug(true);
    	assertTrue(Game.isDebug());
    	Game.create();
    	assertFalse(Game.isDebug());
    }
    
    public void testRollover() {
        for (int i = 0; i < 100; i++) {
            Game.messageTyrant("" + i);
        }
        assertEquals(100, Game.instance().getMessageList().size());
        assertEquals("0", Game.instance().getMessageList().get(0));

        Game.messageTyrant("rollover");
        assertEquals(100, Game.instance().getMessageList().size());
        assertEquals("1", Game.instance().getMessageList().get(0));
    }
    
    public void testOption() throws Exception {
        answerGetInputWithChar('p');
        assertEquals('p', Game.getOption("pr"));
        
        answerGetInputWith(KeyEvent.VK_ESCAPE);
        assertEquals('q', Game.getOption("yn"));

        answerGetInputWith(KeyEvent.VK_ENTER);
        assertEquals('e', Game.getOption("yn"));
    }
    
}

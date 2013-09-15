/*
 * Created on 18-Dec-2004
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant.test;

import tyrant.mikera.tyrant.Game;

/**
 * @author Mike
 */
public class NullHandler_TC extends TyrantTestCase {

    public void testMessages() {
        Game.instance().clearMessageList();
        
        assertEquals(null, lastMessage());
        Game.messageTyrant("Message One");
        assertEquals("Message One", lastMessage());
        Game.messageTyrant("Message Two");
        assertEquals("Message Two", lastMessage());
    }
}

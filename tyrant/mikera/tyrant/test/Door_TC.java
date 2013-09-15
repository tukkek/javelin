package tyrant.mikera.tyrant.test;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Event;

public class Door_TC extends TyrantTestCase {

    public void testRiddleDoor() {
        final Thing door = Lib.create("riddle door");
        door.set("RiddleStatName","RiddleSolved");
        assertEquals(false,door.getFlag("IsOpen"));
        Event bump = new Event("Bump");
        bump.set("Target",hero);
        door.handle(bump);
        assertEquals(false,door.getFlag("IsOpen"));
        hero.set("RiddleSolved",1);
        door.handle(bump);
        assertEquals(true,door.getFlag("IsOpen"));
    }

    public void testQuestDoor() {
        final Thing door = Lib.create("quest door");
        assertEquals(false,door.getFlag("IsOpen"));
        Event bump = new Event("Bump");
        bump.set("Target",hero);
        door.handle(bump);
        assertEquals(false,door.getFlag("IsOpen"));
        hero.set("HasQuest",1);
        door.handle(bump);
        assertEquals(true,door.getFlag("IsOpen"));
    }
}

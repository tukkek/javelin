//package tyrant.mikera.tyrant.test;
//
//import jtacticalrpg.controller.action.Action;
//import jtacticalrpg.controller.action.ActionMapping;
//
//
//public class ActionMapping_TC extends TyrantTestCase {
//    private ActionMapping map;
//    
//    protected void setUp() throws Exception {
//        super.setUp();
//        map = new ActionMapping();
//        map.addDefaultMappings();
//    }
//    
//    public void testAction() throws Exception {
//        assertEquals(Action.MOVE_N, map.convertKeyToAction('8'));
//        assertEquals(Action.MOVE_E, map.convertKeyToAction('6'));
//        assertEquals(Action.MOVE_NOWHERE, map.convertKeyToAction('5'));
//        assertEquals(Action.WAIT, map.convertKeyToAction('.'));
//    }
// }

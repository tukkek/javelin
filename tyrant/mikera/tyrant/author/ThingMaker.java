package tyrant.mikera.tyrant.author;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.TreeMap;

import javelin.controller.old.Game;
import javelin.model.BattleMap;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;




public class ThingMaker {
    public static final String SPACES_3 = "   ";
    public static final String FLAG_ENDING = "@";
    private Thing lastThing;
    
    public boolean isDesigner=true;
    
    public void storeThings(BattleMap map, StringBuffer buffer) {
        buffer.append(MapMaker.NL);
        buffer.append("---Things---");
        buffer.append(MapMaker.NL);
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Thing[] things = map.getThings(x, y);
                if (things.length == 0) continue;
                if (things[0] == Game.hero()) continue;
                storeThing(buffer, things, x, y);
            }
        }
        buffer.append("---Things---");
        buffer.append(MapMaker.NL);
    }

    private void storeThing(StringBuffer buffer, Thing[] things, int x, int y) {
        for (int i = 0; i < things.length; i++) {
            Thing thing = things[i];
            if(thing == Game.hero()) continue;
            buffer.append(x);
            buffer.append("x");
            buffer.append(y);
            buffer.append(" ");
            storeThingName(buffer, thing);
        }
    }

    private void storeThingName(StringBuffer buffer, Thing thing) {
        String name = thing.name();
        if(name.endsWith(ThingMaker.FLAG_ENDING)) {
            buffer.append("[");
            buffer.append(name.substring(0, name.length() - 1));
            buffer.append("]");
        } else {
            buffer.append(thing.name());
        }
        buffer.append(MapMaker.NL);
        storeThingsLocal(buffer, thing);
    }

    private void storeThingsLocal(StringBuffer buffer, Thing thing) {
        java.util.Map local = thing.getLocal();
        if(local == null || local.isEmpty()) return;
        TreeMap sortedLocal = new TreeMap(local);
        for (Iterator iter = sortedLocal.entrySet().iterator(); iter.hasNext();) {
            java.util.Map.Entry entry = (java.util.Map.Entry) iter.next();
            Object key = entry.getKey();
            if(key.equals("Name")) continue;
            buffer.append(SPACES_3);
            buffer.append(key);
            buffer.append(" = ");
            buffer.append(entry.getValue());
            buffer.append(MapMaker.NL);
        }
    }

    public void addThingsToMap(BattleMap map, String mapText) {
        int[] range = MapMaker.extract("---Things---", mapText, null);
        if(range != null) {
            String thingsText = mapText.substring(range[0], range[1]).trim();
            createThings(map, thingsText);
        }
    }
    
    private void createThings(BattleMap map, String thingsText) {
        BufferedReader reader = new BufferedReader(new StringReader(thingsText));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                if(Character.isSpaceChar(line.charAt(0))) {
                    // this is an attribute
                    String[] splits = line.trim().split("=");
                    Object value = splits[1].trim();
                    if(Character.isDigit(((String) value).charAt(0))) {
                        value = Integer.valueOf((String) value);
                    }
                    lastThing.set(splits[0].trim(), value);
                } else {
                    line = line.trim();
                    int firstSpace = line.indexOf(' ');
                    if (firstSpace == -1) return;
                    String[] location = line.substring(0, firstSpace).trim().split("x");
                    int x = Integer.parseInt(location[0]);
                    int y = Integer.parseInt(location[1]);
                    String thingToAdd = line.substring(firstSpace + 1);
                    lastThing = createDesignerThing(thingToAdd.trim());
                    map.addThing(lastThing, x, y);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        

    }
    
    private Thing createDesignerThing(String s) {
    	if (isDesigner&&(s.charAt(0)=='[')) {
    		return Lib.create(s.substring(1,s.length()-1)+FLAG_ENDING);
    	}
    	return Lib.create(s);
    }
}

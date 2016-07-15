package tyrant.mikera.tyrant.author;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javelin.controller.old.Game;
import javelin.model.BattleMap;

import tyrant.mikera.engine.BaseObject;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.tyrant.*;
import tyrant.mikera.tyrant.util.Text;




public class MapMaker {
    private static int nextChar = 'a';
    private java.util.Map tiles = new HashMap();
    public static String NL = System.getProperty("line.separator");
    private java.util.Map legend = new HashMap();
    private ThingMaker thingMaker = new ThingMaker();
    
    public String store(BattleMap map) {
        StringBuffer buffer = new StringBuffer();
        storeTiles(map, buffer);
        storeLegend(buffer);
        storeProperties(map, buffer);
        thingMaker.storeThings(map, buffer);
        return buffer.toString();
    }

    

    private void storeLegend(StringBuffer buffer) {
        buffer.append(NL);
        buffer.append("---Legend---");
        buffer.append(NL);
        SortedMap sorted = new TreeMap(legend);
        for (Iterator iter = sorted.entrySet().iterator(); iter.hasNext();) {
            java.util.Map.Entry entry = (java.util.Map.Entry) iter.next();
            buffer.append(entry.getKey());
            buffer.append(" = ");
            buffer.append(entry.getValue());
            buffer.append(NL);
        }
        buffer.append("---Legend---");
        buffer.append(NL);
    }
    
    private void storeProperties(BattleMap map, StringBuffer buffer) {
        buffer.append(NL);
        buffer.append("---Properties---");
        buffer.append(NL);
        java.util.Map sorted = map.getCollapsedMap();
        for (Iterator iter = sorted.entrySet().iterator(); iter.hasNext();) {
            java.util.Map.Entry entry = (java.util.Map.Entry) iter.next();
            buffer.append(entry.getKey());
            buffer.append(" = ");
            buffer.append(entry.getValue());
            buffer.append(NL);
        }
        buffer.append("---Properties---");
        buffer.append(NL);
    }

    public void storeTiles(BattleMap map, StringBuffer buffer) {
        nextChar = 'a';
        buffer.append("---Tiles---");
        buffer.append(NL);
        for(int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                int tile = map.getTile(x, y);
                Character tileChar = charForTile(tile);
                buffer.append(tileChar);
            }
        buffer.append(NL);
        }
        buffer.append("---Tiles---");
        buffer.append(NL);
    }

    private Character charForTile(int tile) {
        String name = Tile.tileNameFor(tile);
        Character tileChar = (Character) tiles.get(name);
        if(tileChar == null) {
            tileChar = new Character((char)nextChar++);
            tiles.put(name, tileChar);
            legend.put(String.valueOf(tileChar), name);
        }
        return tileChar;
    }

    public java.util.Map getLegend() {
        return legend;
    }

    /**
     * Creates a map from a text string. 
     * 
     * Note that we have to say whether we are in designer mode or not
     * to determine whether or not the [IsXXX] objects are instantiated
     * via Lib.create() or remain as placeholders for further Designer action
     * 
     * @param mapText Text representation of map
     * @param isDesigner Whether to load designer objects. Set to true if you want to edit the map. Set to false if you want [IsXXX] objects to be instantiated.
     * @return
     */
    public BattleMap create(String mapText, boolean isDesigner) {
        // map tile legend
    	int[] range = extract("---Legend---", mapText, null);
        if(range == null) return null;
        String legendText = mapText.substring(range[0], range[1]).trim();
        createLegend(legendText);
        
        // tiles
        range = extract("---Tiles---", mapText, null);
        String tilesString = null;
        if(range != null) {
            tilesString = mapText.substring(range[0], range[1]).trim();
        }
        
        // properties
        range = extract("---Properties---", mapText, null);
        String propertyText=null;
        if(range != null) {
        	propertyText = mapText.substring(range[0], range[1]).trim();
        }
        
        // create map with tiles
        BattleMap map = createMap(tilesString);
        
        // set properties
        map.setProperties(createProperties(propertyText));
        
        // add things
        thingMaker.isDesigner=isDesigner;
        thingMaker.addThingsToMap(map, mapText);
        
        return map;
    }
    
    private BattleMap createMap(String tileString) {
        String[] lines=tileString.split("\n");
    	int h=lines.length;
        for (int i=0; i<h; i++) {
        	lines[i]=lines[i].trim();
        }
    	int w=lines[0].length();
    	if (lines[h-1].length()<w) h--;
    	
    	BattleMap map = new BattleMap(w,h);
        if(tileString == null) throw new Error("Null tiles");

        for (int y=0; y<h; y++) {
        	String line = lines[y];
        	for (int x = 0; x < w; x++) {
        		String c=line.substring(x, x + 1);
        		String tileName=(String)legend.get(c);
        		BaseObject tile=Lib.get(tileName);
        		if (tile==null) throw new Error("Legend ["+c+"] not recognised");
        		map.setTile(x, y, tile.getStat("TileValue"));
        	}
        } 
        return map;
    }

    private void createLegend(String legendText) {
        if (legendText == null || legendText.length() == 0) return;
        BufferedReader reader = new BufferedReader(new StringReader(legendText));
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] splits = line.split("=");
                String key = splits[0].trim();
                String value = splits[1].trim();
                legend.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private TreeMap createProperties(String propertyText) {
    	TreeMap map=new TreeMap();
    	
        if (propertyText == null || propertyText.length() == 0) return map;
        BufferedReader reader = new BufferedReader(new StringReader(propertyText));
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] splits = line.split("=");
                String key = splits[0].trim();
                Object value = Text.parseObject(splits[1].trim());
                map.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static int[] extract(String marker, String content, int[] range) {
        int end = range == null ? 0 : range[1];
        int indexOfLegendBegin = content.indexOf(marker, end);
        if (indexOfLegendBegin == -1) return null;
        int indexOfLegendEnd = content.indexOf(marker, indexOfLegendBegin + marker.length());
        if (indexOfLegendEnd == -1) return null;
        return new int[]{indexOfLegendBegin + marker.length(), indexOfLegendEnd};
    }
    
    public BattleMap promptAndLoad() {
        String filename = "map.txt";
        FileDialog fileDialog = new FileDialog(new Frame(), "Load map", FileDialog.LOAD);
        fileDialog.setFile(filename);
        fileDialog.setVisible(true);

        if (fileDialog.getFile() == null) return null;
        filename = fileDialog.getDirectory() + fileDialog.getFile();

        try {
            FileInputStream f = new FileInputStream(filename);
            StringBuffer contents = new StringBuffer();
            String line = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(f));
            while((line = reader.readLine()) != null) {
                contents.append(line);
                contents.append(MapMaker.NL);
            }
            BattleMap map = create(contents.toString(), true);
            Game.messageTyrant("Map loaded - " + filename);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            Game.messageTyrant("Error while loading map, check console");
            return null;
        }
    
    }
}

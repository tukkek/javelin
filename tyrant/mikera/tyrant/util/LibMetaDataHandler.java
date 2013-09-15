package tyrant.mikera.tyrant.util;

//import mikera.tyrant.Describer;

import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Iterator;

import tyrant.mikera.engine.Description;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Modifier;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Chest;
import tyrant.mikera.tyrant.Effect;
import tyrant.mikera.tyrant.Personality;
import tyrant.mikera.tyrant.Poison;
import tyrant.mikera.tyrant.Potion;
import tyrant.mikera.tyrant.Scripts;

/**
 *
 * @author  Carsten Muessig <carsten.muessig@gmx.net>
 */
public class LibMetaDataHandler {
    
    private static final String THING = "thing";
    private static final String ITEM = "item";
    private static final String BEING = "being";
    private static final String EFFECT = "effect";
    private static final String TEMPORARY_EFFECT = "temporary effect";
    private static final String FOOD = "food";
    private static final String SCROLL = "scroll";
    private static final String MISSILE = "missile";
    private static final String RANGED_WEAPON = "ranged weapon";
    private static final String POTION = "potion";
    private static final String THROWING_WEAPON = "throwing weapon";
    private static final String WAND = "wand";
    private static final String RING = "ring";
    private static final String COIN = "coin";
    private static final String ARMOUR = "armour";
    private static final String HELMET = "helmet";
    private static final String BOOTS = "boots";
    private static final String WEAPON = "weapon";
    private static final String SWORD = "sword";
    private static final String HAMMER = "hammer";
    private static final String AXE = "axe";
    private static final String POLEARM = "polearm";
    private static final String MACE = "mace";
    private static final String UNARMED_WEAPON = "unarmed weapon";
    private static final String SECRET = "secret";
    private static final String SECRET_DOOR = "secret door";
    private static final String SECRET_PASSAGE = "secret passage";
    private static final String SECRET_ITEM = "secret item";
    private static final String SPELLBOOK = "spellbook";
    private static final String CHEST = "chest";
    private static final String DECORATION = "decoration";
    private static final String SCENERY = "scenery";
    private static final String DOOR = "door";
    private static final String FURNITURE = "furniture";
    private static final String SIGN = "sign";
    private static final String PLANT = "plant";
    private static final String TREE = "tree";
    private static final String PORTAL = "portal";
    private static final String INVISIBLE_PORTAL = "invisible portal";
    private static final String STAIRS = "stairs";
    private static final String TRAP = "trap";
    private static final String MONSTER = "monster";
    private static final String HUMANOID = "humanoid";
    private static final String BANDIT = "bandit";
    private static final String GOBLINOID = "goblinoid";
    private static final String GOBLIN = "goblin";
    private static final String ORC = "orc";
    private static final String INSECT = "insect";
    private static final String SNAKE = "snake";
    private static final String CRITTER = "critter";
    private static final String PERSON = "person";
    private static TreeMap DESCRIPTIONS = null;
    
    protected static void createLibraryItems(LinkedHashMap plugInData) {
        System.out.println("Inserting "+plugInData.size()+" items into the library");
        Iterator it = plugInData.keySet().iterator();
        while(it.hasNext()) {
            String timeStampAndMetaDataName = (String)it.next();
            TreeMap itemData = (TreeMap)plugInData.get(timeStampAndMetaDataName);
            String metaDataName = timeStampAndMetaDataName.substring(timeStampAndMetaDataName.indexOf("$")+1);
            MetaData metaData = LibMetaData.instance().get(metaDataName);
            createLibraryItem(itemData, metaData);
        }
    }
    
    private static void createLibraryItem(TreeMap itemData, MetaData metaData) {
        String itemName = (String)itemData.remove("Name");
        Thing t = new Thing();
        t.set("Name", itemName);
        boolean inserted = false;
        System.out.println(" Inserting "+itemName);
        Iterator it = itemData.keySet().iterator();
        while(it.hasNext()) {
            inserted = false;
            String property = (String)it.next();
            Object o = itemData.get(property);
            MetaDataEntry mde = metaData.get(property);
            Object value = mde.getValue();
            if(value instanceof Integer)
                o = new Integer((String)o);
            if(value instanceof Double)
                o = new Double((String)o);
            if(o instanceof TreeMap) {
                System.out.println("DEBUG: found meta data: "+property+": "+o);
                t.set(property, createLibraryItemFromMetaData(property, (TreeMap)o));
                inserted = true;
            }
            // TO DO: maybe there's a better way to create the library items
            //        (in a single method or so)
            if(o.getClass().getName().startsWith("mikera.tyrant")) {
                System.out.println("DEBUG: found tyrant object: "+property+": "+o+" = "+value);
                if(((String)o).indexOf("ChestCreate")>=0)
                    t.set(property, new Chest.ChestCreation());
                if(((String)o).indexOf("ChestOpen")>=0)
                    t.set(property, new Chest.ChestOpen());
                if(((String)o).indexOf("ChestClosed")>=0)
                    t.set(property, new Chest.ChestClosed());
                if(((String)o).indexOf("HealingScript")>=0)
                    t.set(property, new Potion.HealingScript());
                inserted = true;
            }
            if(!inserted)
                t.set(property, o);
        }
        Lib.add(t);
    }
    
    private static Object createLibraryItemFromMetaData(String property, TreeMap itemData) {
        // TO DO: maybe there's a better way to create the library items
        //        (in a single method or so)
    	// TO DO: update to latest Tyrant version
        Thing effect;
        if(property.equals("AddEffectScript")) {
            Integer chance = (Integer)itemData.get("Chance");
            effect = Lib.create((String)itemData.get("Effect"));
            if(chance==null)
                return Scripts.addEffect((String)itemData.get("TargetProperty"), effect);
            return Scripts.addEffect((String)itemData.get("TargetProperty"), effect, chance.intValue());
        }
        else if(property.equals("TemporaryEffect")) {
            Integer multiplier = (Integer)itemData.get("Multiplier"), chance = (Integer)itemData.get("Chance");
            if(multiplier==null)
                effect = Effect.temporary(Modifier.bonus((String)itemData.get("Stat"), ((Integer)itemData.get("Bonus")).intValue()), ((Integer)itemData.get("Time")).intValue());
            else
                effect = Effect.temporary(Modifier.linear((String)itemData.get("Stat"), multiplier.intValue(), ((Integer)itemData.get("Bonus")).intValue()), ((Integer)itemData.get("Time")).intValue());
            if(chance==null)
                return Scripts.addEffect((String)itemData.get("TargetProperty"), effect);
            return Scripts.addEffect((String)itemData.get("TargetProperty"), effect, chance.intValue());
        }
        else if(property.equals("SimpleModifier")) {
            Integer multiplier = (Integer)itemData.get("Multiplier");
            if(multiplier==null)
                return Modifier.bonus((String)itemData.get("Stat"), ((Integer)itemData.get("Bonus")).intValue());
            return Modifier.linear((String)itemData.get("Stat"), multiplier.intValue(), ((Integer)itemData.get("Bonus")).intValue());
        }
        else if(property.equals("Personality")) {
            Integer state = (Integer)itemData.get("State");
            if(state==null)
                return new Personality(((Integer)itemData.get("Type")).intValue());
            return new Personality(((Integer)itemData.get("Type")).intValue(), ((Integer)itemData.get("SubType")).intValue());
        }
        return null;
    }
    
    protected static MetaData createMetaDataFromObject(Object o) {
    	// TO DO: update to latest Tyrant version
        MetaData metaData = new MetaData();        
    	String className = o.getClass().getName();
    	if(className.indexOf("ChestOpen")>=0){
    		metaData.add("TypeInfo", "ChestOpen", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    		return metaData;
    	}
    	if(className.indexOf("ChestCreated")>=0){
    		metaData.add("TypeInfo", "ChestCreate", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    		return metaData;
    	}
    	if(className.indexOf("ChestClosed")>=0){
    		metaData.add("TypeInfo", "ChestClosed", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
    		return metaData;
    	}
    	if(className.indexOf("HealingScript")>=0) {
    		metaData.add("TypeInfo", "HealingScript", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    	return metaData;
    } if(className.indexOf("AddEffectScript")>=0){
    		metaData.add("TypeInfo", "AddEffectScript", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    		metaData.add("Effect", "Poison", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    		metaData.add("TargetProperty", "Target", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    		metaData.add("Chance", new Integer(100),null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
    		return metaData;
    	}
/*	TO DO: modify to current version
 
      	if(className.indexOf("Effect")>=0){
    		metaData.add("TypeInfo", "TemporaryEffect", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    		metaData.add("Time", new Integer(2000), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    		metaData.add("Modifier", Modifier.simple("dummy",-1), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    		metaData.add("Stat", "MoveSpeed", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    		metaData.add("Bonus", new Integer(2000), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    		metaData.add("Multiplier", new Integer(100), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
    		metaData.add("TargetProperty", "Target", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    		metaData.add("Chance", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
    		return metaData;
    	}*/
    	if(className.indexOf("Modifier")>=0) {
    		metaData.add("TypeInfo", "SimpleModifier", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    		metaData.add("Stat", new String(), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    		metaData.add("Bonus", new Integer(0), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    		metaData.add("Multiplier", new Integer(0), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
    		return metaData;
    	}
    	if(className.indexOf("Personality")>=0) {
    		metaData.add("TypeInfo", "Personality", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
    		metaData.add("Type", new Integer(0), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
    		metaData.add("SubType", new Integer(0), new Integer[]{new Integer(0), new Integer(1), new Integer(2), new Integer(3), new Integer(4), new Integer(5), new Integer(6), new Integer(7), new Integer(8)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
    		metaData.add("State", new Integer(0), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
    		return metaData;
    	}
    	return null;
    }
    
    protected static LibMetaData createLibMetaData(LibMetaData lmd) {
    	// TO DO: update to latest Tyrant version, also the called methods at the bottom
        MetaData metaData = new MetaData();
        metaData.add("IsThing", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ImageSource", "Items", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(0), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Number", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(THING, metaData);
        
        metaData = new MetaData(lmd.get("thing"));
        metaData.add("IsItem", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ImageSource", "Items", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Name", "Thing", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("NameType", new Integer(Description.NAMETYPE_NORMAL), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Z", new Integer(Thing.Z_ITEM), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(ITEM, metaData);
        
        metaData = new MetaData(lmd.get("thing"));
        metaData.add("IsBeing",new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ImageSource", "Creatures", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(340), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsMobile",new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsBlocking", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("MoveCost", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("DeathDecoration", "blood pool", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("NameType", new Integer(Description.NAMETYPE_NORMAL), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Z", new Integer(Thing.Z_MOBILE), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(BEING, metaData);
        
        lmd = createEffectMetaData(lmd);
        lmd = createPoisonMetaData(lmd);
        lmd = createFoodMetaData(lmd);
        lmd = createScrollMetaData(lmd);
        lmd = createMissileMetaData(lmd);
        lmd = createRangedWeaponMetaData(lmd);
        lmd = createPotionMetaData(lmd);
        lmd = createWandMetaData(lmd);
        lmd = createRingMetaData(lmd);
        lmd = createCoinMetaData(lmd);
        lmd = createArmourMetaData(lmd);
        lmd = createWeaponMetaData(lmd);
        lmd = createSecretMetaData(lmd);
        lmd = createSpellBookMetaData(lmd);
        lmd = createChestMetaData(lmd);
        lmd = createDecorationMetaData(lmd);
        lmd = createSceneryMetaData(lmd);
        lmd = createPortalMetaData(lmd);
        lmd = createTrapMetaData(lmd);
        createMonsterMetaData(lmd);
        createPersonMetaData(lmd);
        return lmd;
    }
    
    private static LibMetaData createEffectMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("thing"));
        metaData.add("IsEffect",new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ImageSource","Effects", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(45), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("EffectName","effect", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(EFFECT, metaData);
        
        metaData = new MetaData(lmd.get("effect"));
        metaData.add("IsTemporaryEffect", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsActive", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(TEMPORARY_EFFECT, metaData);
        return lmd;
    }
    
    private static LibMetaData createPoisonMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("effect"));
        metaData.add("IsPoison", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsActive", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("OnAction", new Poison.PoisonAction(), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Damage", new Integer(3), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("DamageType", "poison", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("DamageMessage", "You feel the poison weakening you...", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("EffectName", "poisoned", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LifeTime", new Integer(30000), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Strength", new Integer(200), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("AttributeAddMessage", "You feel poisoned!", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(POTION, metaData);
        return lmd;
    }
    
    private static LibMetaData createFoodMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("item"));
        metaData.add("IsFood", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsEdible", new Integer(1), new Integer[] {new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(226), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(4), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Nutrition", new Integer(10), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ItemWeight", new Integer(1000), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Frequency", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(FOOD, metaData);
        return lmd;
    }
    
    private static LibMetaData createScrollMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("item"));
        metaData.add("IsScroll", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsReadable", new Integer(1), new Integer[] {new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsMagicItem", new Integer(1), new Integer[] {new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(280), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(2), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ItemWeight", new Integer(200), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Frequency", new Integer(40), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ScrollPower", new Integer(10), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMax", new Integer(30), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("NamePlural", "scrolls of spell name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("UName", "scroll titled title", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("UNamePlural", "scrolls titled title", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ScrollSpell", "spell name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(SCROLL, metaData);
        return lmd;
    }
    
    private static LibMetaData createMissileMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("item"));
        metaData.add("IsMissile", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(80), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("MissileRecovery", new Integer(50), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("MissileType", "bullet", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("WieldType", new Integer(RPG.WT_MISSILE), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(4), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ItemWeight", new Integer(500), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Frequency", new Integer(40), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("RSKMul", new Integer(30), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("RSKBonus",new Integer(0), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("RSTMul",new Integer(30), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("RSTBonus",new Integer(0), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(MISSILE, metaData);
        return lmd;
    }
    
    private static LibMetaData createRangedWeaponMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("item"));
        metaData.add("IsRangedWeapon", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("RangedWeaponType", "arrow", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(120), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("WieldType", new Integer(RPG.WT_RANGEDWEAPON), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(120), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ItemWeight", new Integer(4000), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Frequency", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("RSKMul", new Integer(40), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("RSKBonus", new Integer(0), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("RSTMul", new Integer(40), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("RSTBonus", new Integer(0), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(RANGED_WEAPON, metaData);
        return lmd;
    }
    
    private static LibMetaData createPotionMetaData(LibMetaData lmd) {
        MetaData healingScript = new MetaData();
        healingScript.add("TypeInfo", "HealingScript", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        
        MetaData addEffect = new MetaData();
        addEffect.add("TypeInfo", "AddEffectScript", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        addEffect.add("Effect", "Poison", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        addEffect.add("TargetProperty", "Target", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        addEffect.add("Chance", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
        
        MetaData temporaryEffect = new MetaData();
        temporaryEffect.add("TypeInfo", "TemporaryEffect", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        temporaryEffect.add("Time", new Integer(2000), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        temporaryEffect.add("Modifier", Modifier.bonus("dummy",-1), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        temporaryEffect.add("Stat", "MoveSpeed", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        temporaryEffect.add("Bonus", new Integer(2000), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        temporaryEffect.add("Multiplier", new Integer(100), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
        temporaryEffect.add("TargetProperty", "Target", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        temporaryEffect.add("Chance", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
        
        MetaData metaData = new MetaData(lmd.get("item"));
        metaData.add("IsPotion", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsDrinkable", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsMagicItem", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(240), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(3), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ItemWeight", new Integer(500), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Frequency", new Integer(60), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("OnDrunk", new MetaData(), new MetaData[]{healingScript, addEffect, temporaryEffect}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.OPTIONAL_PROPERTY);
        metaData.add("HealingPower", new Integer(8), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
        lmd.add(POTION, metaData);
        
        metaData = new MetaData(lmd.get("missile"));
        metaData.add("IsThrowingWeapon", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("MissileType", "thrown", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(4), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("MissileRecovery", new Integer(80), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(15), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ItemWeight", new Integer(1000), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("RSKMul", new Integer(50), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("RSTMul", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Frequency", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(THROWING_WEAPON, metaData);
        return lmd;
    }
    
    private static LibMetaData createWandMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("item"));
        metaData.add("IsWand", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsMagicItem", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(288), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(10), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ItemWeight", new Integer(800), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Frequency", new Integer(60), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Charges", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(288), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("UName","wand name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Name","wand of wand name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("NamePlural","wands of wand name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("WandSpell", "spell name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(WAND, metaData);
        return lmd;
    }
    
    private static LibMetaData createRingMetaData(LibMetaData lmd) {
        MetaData modifier = new MetaData();
        modifier.add("TypeInfo", "SimpleModifier", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        modifier.add("Stat", new String(), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        modifier.add("Bonus", new Integer(0), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        modifier.add("Multiplier", new Integer(0), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
        
        MetaData metaData = new MetaData(lmd.get("item"));
        metaData.add("IsRing", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("WieldType", new Integer(RPG.WT_LEFTRING), new Integer[]{new Integer(RPG.WT_LEFTRING), new Integer(RPG.WT_RIGHTRING)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsMagicItem", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(200), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(36), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ItemWeight", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Frequency", new Integer(30), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("WieldedModifiers", modifier, null, MetaDataEntry.FIX_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
        lmd.add(RING, metaData);
        return lmd;
    }
    
    private static LibMetaData createCoinMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("item"));
        metaData.add("IsMoney", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(140), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(6), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ItemWeight", new Integer(20), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Value", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Z", new Integer(Thing.Z_ITEM-2), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Frequency", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(COIN, metaData);
        return lmd;
    }
    
    private static LibMetaData createArmourMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("item"));
        metaData.add("IsArmour", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(340), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("WieldType", new Integer(RPG.WT_TORSO), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(70), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ItemWeight", new Integer(20000), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Frequency", new Integer(50), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(ARMOUR, metaData);
        
        metaData = new MetaData(lmd.get("armour"));
        metaData.add("IsHelmet", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(323), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("WieldType", new Integer(RPG.WT_HEAD), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(40), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ItemWeight", new Integer(5000), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(HELMET, metaData);
        
        MetaData modifier = new MetaData();
        modifier.add("TypeInfo", "SimpleModifier", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        modifier.add("Stat", new String(), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        modifier.add("Bonus", new Integer(0), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        modifier.add("Multiplier", new Integer(0), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
        
        metaData = new MetaData(lmd.get("armour"));
        metaData.add("IsFootwear", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(360), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("NameType", new Integer(Description.NAMETYPE_QUANTITY), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("WieldType", new Integer(RPG.WT_BOOTS), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(24), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(5), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ItemWeight", new Integer(2800), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("UName","boots", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsMagicItem", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
        metaData.add("WieldedModifiers", modifier, null, MetaDataEntry.FIX_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
        lmd.add(BOOTS, metaData);
        return lmd;
    }
    
    private static LibMetaData createWeaponMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("item"));
        metaData.add("IsWeapon", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(2), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("WieldType", new Integer(RPG.WT_MAINHAND), new Integer[]{new Integer(RPG.WT_MAINHAND), new Integer(RPG.WT_TWOHANDS)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(40), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ItemWeight", new Integer(6000), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Frequency", new Integer(50), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("AttackCost", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ASKMul", new Integer(80), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ASKBonus", new Integer(0), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ASTMul", new Integer(80), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ASTBonus", new Integer(0), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("DSKMul", new Integer(40), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("DSKBonus", new Integer(0), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(WEAPON, metaData);
        
        metaData = new MetaData(lmd.get("weapon"));
        metaData.add("IsSword", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Name", "material + name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Material", "material", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("UName", "uname + name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(SWORD, metaData);
        
        metaData = new MetaData(lmd.get("weapon"));
        metaData.add("IsHammer", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Name", "material + name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Material", "material", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("UName", "uname + name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(HAMMER, metaData);
        
        metaData = new MetaData(lmd.get("weapon"));
        metaData.add("IsAxe", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Name", "material + name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Material", "material", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("UName", "uname + name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(AXE, metaData);
        
        metaData = new MetaData(lmd.get("weapon"));
        metaData.add("IsPolearm", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Name", "material + name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Material", "material", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("UName", "uname + name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(POLEARM, metaData);
        
        metaData = new MetaData(lmd.get("weapon"));
        metaData.add("IsMace", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Name", "material + name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Material", "material", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("UName", "uname + name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(MACE, metaData);
        
        MetaData addEffectScript = new MetaData();
        addEffectScript.add("TypeInfo", "AddEffectScript", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        addEffectScript.add("TargetProperty", new String(), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        addEffectScript.add("TargetName", new String(), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        addEffectScript.add("Chance", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
        
        metaData = new MetaData(lmd.get("weapon"));
        metaData.add("IsUnarmedWeapon", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("OnWeaponDamage", addEffectScript, null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(UNARMED_WEAPON, metaData);
        return lmd;
    }
    
    private static LibMetaData createSecretMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("thing"));
        metaData.add("IsSecret", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsInvisible", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ImageSource","Effects", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(5), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(SECRET, metaData);
        
        metaData = new MetaData(lmd.get("secret"));
        metaData.add("IsSecretDoor", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(SECRET_DOOR, metaData);
        
        metaData = new MetaData(lmd.get("secret"));
        metaData.add("IsSecretPassage", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(SECRET_PASSAGE, metaData);
        
        metaData = new MetaData(lmd.get("secret"));
        metaData.add("IsHiddenItem", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HiddenThing", "thing name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(SECRET_ITEM, metaData);
        return lmd;
    }
    
    private static LibMetaData createSpellBookMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("item"));
        metaData.add("IsSpellBook", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsReadable", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsMagicItem", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(285), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(12), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ItemWeight", new Integer(3000), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Frequency", new Integer(50), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin",new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMax",new Integer(30), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Name","spellbook of spell name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("NamePlural","spellbooks of spell names", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("UName", "title", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("BookSpell", "spell name", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(SPELLBOOK, metaData);
        return lmd;
    }
    
    private static LibMetaData createChestMetaData(LibMetaData lmd) {
        MetaData chestCreate = new MetaData();
        chestCreate.add("TypeInfo", "ChestCreate", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        
        MetaData chestOpen = new MetaData();
        chestOpen.add("TypeInfo", "ChestOpen", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        
        MetaData chestClosed = new MetaData();
        chestClosed.add("TypeInfo", "ChestClosed", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
        
        MetaData metaData = new MetaData(lmd.get("item"));
        metaData.add("IsChest", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsOpenable", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ImageSource","Scenery", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(121), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ItemWeight", new Integer(10000), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Z", new Integer(Thing.Z_ITEM-2), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ImageOpen", new Integer(2), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        //metaData.add("OnCreate", new Chest.ChestCreation(), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        //metaData.add("OnOpen", new Chest.ChestOpen(), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
        //metaData.add("OnClosed", new Chest.ChestClosed(), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
        lmd.add(CHEST, metaData);
        return lmd;
    }
    
    private static LibMetaData createDecorationMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("thing"));
        metaData.add("IsDecoration", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ImageSource", "Scenery", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsTransparent", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Z", new Integer(Thing.Z_ONFLOOR), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(DECORATION, metaData);
        return lmd;
    }
    
    private static LibMetaData createSceneryMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("thing"));
        metaData.add("IsScenery", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ImageSource","Scenery", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsBlocking", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsOwned", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Z", new Integer(Thing.Z_ITEM-1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(10), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(SCENERY, metaData);
        
        metaData = new MetaData(lmd.get("scenery"));
        metaData.add("IsDoor", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsOpenable", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(144), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ImageOpen", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Z", new Integer(Thing.Z_ITEM+1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsOpen", new Integer(0), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsViewBlocking", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsBlocking", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(30), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(DOOR, metaData);
        
        metaData = new MetaData(lmd.get("scenery"));
        metaData.add("IsFurniture", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(200), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(7), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(FURNITURE, metaData);
        
        metaData = new MetaData(lmd.get("scenery"));
        metaData.add("IsSign", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(64), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(20), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsBlocking", new Integer(0), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(SIGN, metaData);
        
        metaData = new MetaData(lmd.get("scenery"));
        metaData.add("IsPlant", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(81), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsViewBlocking", new Integer(0), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsBlocking", new Integer(0), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(4), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(PLANT, metaData);
        
        metaData = new MetaData(lmd.get("scenery"));
        metaData.add("IsTree", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(83), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("HPS", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsViewBlocking", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsBlocking", new Integer(1), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(TREE, metaData);
        //to do: do we need meta data for fire?
        return lmd;
    }
    
    private static LibMetaData createPortalMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("scenery"));
        metaData.add("IsPortal", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsBlocking", new Integer(0), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ImageSource", "Scenery", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.OPTIONAL_PROPERTY, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(PORTAL, metaData);
        
        metaData = new MetaData(lmd.get("portal"));
        metaData.add("IsInvisible", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(INVISIBLE_PORTAL, metaData);
        
        metaData = new MetaData(lmd.get("portal"));
        metaData.add("IsStairs", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsBlocking", new Integer(0), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsInvisible", new Integer(0), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(STAIRS, metaData);
        return lmd;
    }
    
    private static LibMetaData createTrapMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("secret"));
        metaData.add("IsTrap", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(47), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsWarning", new Integer(1), new Integer[] {new Integer(0), new Integer(1)}, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsActivated", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Z", new Integer(Thing.Z_FLOOR), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(TRAP, metaData);
        return lmd;
    }
    
    private static LibMetaData createMonsterMetaData(LibMetaData lmd) {
        MetaData metaData = new MetaData(lmd.get("being"));
        metaData.add("IsMonster", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsMobile", new Integer(1), new Integer[] {new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsHostile", new Integer(1), new Integer[] {new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMax", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ImageSource", "Creatures", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(340), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsActive", new Integer(1), new Integer[] {new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("MoveSpeed", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("AttackSpeed", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("SK", new Integer(9), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("ST", new Integer(8), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("AG", new Integer(9), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("TG", new Integer(8), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IN", new Integer(7), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("WP", new Integer(8), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("CH", new Integer(6), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("CR", new Integer(7), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(MONSTER, metaData);
        
        metaData = new MetaData(lmd.get("monster"));
        metaData.add("IsHumanoid", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsIntelligent", new Integer(1), new Integer[] {new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(5), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMax", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(260), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(HUMANOID, metaData);
        
        metaData = new MetaData(lmd.get("humanoid"));
        metaData.add("IsBandit", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(7), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMax", new Integer(14), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(80), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(BANDIT, metaData);
        
        metaData = new MetaData(lmd.get("humanoid"));
        metaData.add("IsGoblinoid", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(240), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(GOBLINOID, metaData);
        
        metaData = new MetaData(lmd.get("goblinoid"));
        metaData.add("IsGoblin", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("DeathDecoration", "slime pool", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(6), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMax", new Integer(15), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(244), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(GOBLIN, metaData);
        
        metaData = new MetaData(lmd.get("goblin"));
        metaData.add("IsOrc", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(8), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMax", new Integer(17), null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(242), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(ORC, metaData);
        
        metaData = new MetaData(lmd.get("monster"));
        metaData.add("IsInsect", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("DeathDecoration", "slime pool", null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Frequency", new Integer(40), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(3), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMax", new Integer(15), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(INSECT, metaData);
        
        metaData = new MetaData(lmd.get("monster"));
        metaData.add("IsSnake", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Frequency", new Integer(25), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("MoveSpeed", new Integer(70), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Image", new Integer(286), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(2), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMax", new Integer(8), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        //      to do: store meta data of unarmed weapon
        //      metaData.add("UnarmedWeapon", new Script(), null, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.OPTIONAL_PROPERTY);
        lmd.add(SNAKE, metaData);
        
        metaData = new MetaData(lmd.get("monster"));
        metaData.add("IsCritter", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Frequency", new Integer(100), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMin", new Integer(1), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("LevelMax", new Integer(20), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(CRITTER, metaData);
        return lmd;
    }
    
    private static LibMetaData createPersonMetaData(LibMetaData lmd) {
        MetaData personality = new MetaData();
        personality.add("TypeInfo", "Personality", null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        personality.add("Type", new Integer(0), new Integer[]{new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        personality.add("SubType", new Integer(0), new Integer[]{new Integer(0), new Integer(1), new Integer(2), new Integer(3), new Integer(4), new Integer(5), new Integer(6), new Integer(7), new Integer(8)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        personality.add("State", new Integer(0), null, MetaDataEntry.POSITIVE_VALUE, MetaDataEntry.OPTIONAL_PROPERTY);
        
        MetaData metaData = new MetaData(lmd.get("monster"));
        metaData.add("IsPerson", new Integer(1), null, MetaDataEntry.FIX_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsHostile", new Integer(0), new Integer[] {new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("IsIntelligent", new Integer(1), new Integer[] {new Integer(0), new Integer(1)}, MetaDataEntry.CERTAIN_VALUES, MetaDataEntry.MANDATORY_PROPERTY);
        metaData.add("Personality", personality, null, MetaDataEntry.ANY_VALUE, MetaDataEntry.MANDATORY_PROPERTY);
        lmd.add(PERSON, metaData);
        return lmd;
    }
    
    protected static TreeMap createPropertyDescriptions() {
    	if(DESCRIPTIONS==null) {
        TreeMap descriptions = new TreeMap();
        descriptions.put("SK", "Skill");
        descriptions.put("ST", "Strength");
        descriptions.put("AG", "Agility");
        descriptions.put("TG", "Toughness");
        descriptions.put("IN", "Intelligence");
        descriptions.put("WP", "Willpower");
        descriptions.put("CH", "Charisma");
        descriptions.put("CR", "Craft");
        descriptions.put("HPS", "Hit points");
        descriptions.put("HPSMAX", "Maximum hit points");
        descriptions.put("MPS", "Magic points");
        descriptions.put("MPSMAX", "Maximum magic points");
        descriptions.put("APS", "Action points (100 � 1 turn at normal speed)");
        descriptions.put("IsDecoration", "");
        descriptions.put("NoStack", "Don�t allow multiple adds to same square");
        descriptions.put("IsBlocking", "Prevents free movement into same square");
        descriptions.put("IsViewBlocking", "");
        descriptions.put("IsInvisible", "");
        descriptions.put("Level", "level of object");
        descriptions.put("Frequency", "How common the thing is");
        descriptions.put("LevelMin", "Minimum level for generation (required!)");
        descriptions.put("LevelMax", "Maximum level for generation");
        descriptions.put("IsActive", "Gets game updates");
        descriptions.put("IsMobile", "Moves around, has DirectionX and DirectionY set on motion, can fight");
        descriptions.put("DirectionX", "Horizontal direction of last move");
        descriptions.put("DirectionY", "Vertical direction of last move");
        descriptions.put("IsDoor", "");
        descriptions.put("IsSecretDoor", "");
        descriptions.put("IsOpen", "");
        descriptions.put("IsOpenable", "Can be opened e.g. door, chest");
        descriptions.put("IsLocked", "");
        descriptions.put("LockDifficulty", "");
        descriptions.put("IsPortal", "");
        descriptions.put("PortalTargetMap", "Opposite portal object");
        descriptions.put("PortalTargetX", "");
        descriptions.put("PortalTargetY", "");
        descriptions.put("DestinationLevel", "Specific level for target dungeon");
        descriptions.put("DestinationLevelIncrement", "Level increment over current portal level");
        descriptions.put("WorldMap",  "Hero only � top level world map");
        descriptions.put("IsPenetrable", "Is possible to move through (with some resistance), requires IsBlocking");
        descriptions.put("IsPushable", "");
        descriptions.put("IsWarning", "Warning for monsters");
        descriptions.put("IsUsable", "");
        descriptions.put("IsSecret", "Responds to 'search'");
        descriptions.put("IsSecretPassage", "");
        descriptions.put("IsHiddenItem", "");
        descriptions.put("HiddenThing", "Actual hidden thing");
        descriptions.put("HiddenItem", "Name of hidden item to create (null is random)");
        descriptions.put("IsItem", "Indicates whether the object is a pick-uppable item");
        descriptions.put("IsArtifact", "");
        descriptions.put("IsRusty", "");
        descriptions.put("IsIdentified", "");
        descriptions.put("Damage", "");
        descriptions.put("ItemWeight", "Weight per unit");
        descriptions.put("Number", "");
        descriptions.put("Value", "Value per unit");
        descriptions.put("Image", "Number of image in bitmap image (20 per row)");
        descriptions.put("ImageSource", "Name of bitmap image");
        descriptions.put("IsWieldable", "");
        descriptions.put("WieldType", "");
        descriptions.put("IsDrinkable", "");
        descriptions.put("IsReadable", "");
        descriptions.put("IsRangedWeapon", "");
        descriptions.put("IsMagicItem", "");
        descriptions.put("IsFootwear", "");
        descriptions.put("ARM", "Basic armour");
        descriptions.put("ARM:[damtype]", "Armour value vs. particular damage type");
        descriptions.put("RES:[damtype]", "Damage resistance");
        descriptions.put("IsArt", "");
        descriptions.put("IsSpell", "");
        descriptions.put("Level", "Achieved skill level");
        descriptions.put("SpellCost", "Cost in Mps");
        descriptions.put("SpellTarget", "Int type � self, target, item etc.");
        descriptions.put("SpellUsage", "Int type � offensive, defensive etc.");
        descriptions.put("SpellCastTime", "");
        descriptions.put("SpellRadius", "");
        descriptions.put("BoltImage", "");
        descriptions.put("IsSkill", "");
        descriptions.put("SkillCost", "Cost to increase skill by one level");
        descriptions.put("IsScroll", "");
        descriptions.put("ScrollSpell", "Spell name");
        descriptions.put("ScrollPower",  "Scroll cast power (IN)");
        descriptions.put("IsFood", "");
        descriptions.put("IsEdible", "");
        descriptions.put("Hunger", "Hunger level (in ticks), increases over time");
        descriptions.put("HungerThreshold", "");
        descriptions.put("Nutrition", "Food value provided if eaten");
        descriptions.put("IsInsect", "");
        descriptions.put("IsGoblinoid", "");
        descriptions.put("IsGoblin", "");
        descriptions.put("IsOrc", "");
        descriptions.put("IsSpider", "");
        descriptions.put("IsSnake", "");
        descriptions.put("IsGenerator", "Marks a generator, also needs IsActive set");
        descriptions.put("GenerationRate", "Rate (per million ticks = per 10,000 turns)");
        descriptions.put("GenerationType",  "Type of creature to generate");
        descriptions.put("DecayRate", "Rate (per million ticks = per 10,000 turns)");
        descriptions.put("DecayType", "Type of object to generate on decay");
        descriptions.put("DecayMessage", "");
        descriptions.put("AreaDamage", "Damage per turn (100 ticks)");
        descriptions.put("AreaDamageType", "");
        descriptions.put("AreaDamageMessage", "Message to hero");
        descriptions.put("LifeTime", "Lifetime in ticks");
        descriptions.put("AIMode", "Guard, Wander, Attack, Follow");
        descriptions.put("IsHostile", "Is hostile to player");
        descriptions.put("IsInhabitant", "Is a local inhabitant (NPC)");
        descriptions.put("IsInsane", "Attacks anything and everything");
        descriptions.put("GroupNumber", "Typical group size = d(gn)");
        descriptions.put("Leader", "Reference to leader");
        descriptions.put("GuardX1", "");
        descriptions.put("GuardY1", "");
        descriptions.put("GuardX2", "");
        descriptions.put("GuardY2", "");
        descriptions.put("IsEffect","");
        descriptions.put("IsPoison", "");
        descriptions.put("Damage", "Average damage in HPS");
        descriptions.put("Strength", "Number of hits per million ticks = per 10,000 turns");
        descriptions.put("DamageType", "Damage type e.g. 'poison'");
        descriptions.put("DamageMessage", "Message to display when damage occurs");
        descriptions.put("IsRangedWeapon", "");
        descriptions.put("IsMissile", "");
        descriptions.put("IsThrowingWeapon", "");
        descriptions.put("MissileType", "Name of bolt type e.g. 'arrow'");
        descriptions.put("RangedWeaponType", "Needs to match MissileType of appropriate missiles");
        descriptions.put("FireCost", "APS cost to fire");
        descriptions.put("RSKMul", "");
        descriptions.put("RSKBonus", "");
        descriptions.put("RSTMul", "");
        descriptions.put("RSTBonus", "");
        descriptions.put("ThrowRange", "");
        descriptions.put("Range", "");
        descriptions.put("MissileRecovery", "Percentage chance of missile surviving");
        descriptions.put("Name", "");
        descriptions.put("Description", "Description to display");
        descriptions.put("IsHostile", "Are local inhabitants hostile?");
        descriptions.put("Target", "Target of event");
        descriptions.put("Time", "Length of time over which event occurs");
        descriptions.put("OnCreate", "Called when object created");
        descriptions.put("OnAction", "Called when time progresses on map");
        descriptions.put("OnWeaponAttack", "Called when a weapon attacks a target");
        descriptions.put("OnWeaponHit", "Called when a weapon hits a target");
        descriptions.put("OnDeath", "Called when a thing is killed");
        descriptions.put("Stat", "Name of stat being modified");
        descriptions.put("Source", "Thing causing the modification");
        descriptions.put("Priority", "Order of stat modifier application, High numbers have priority, and are called first to return the stat value");
        descriptions.put("IsFurniture", "");
        descriptions.put("IsSign", "");
        descriptions.put("IsTree", "");
        descriptions.put("IsPlant", "");
        descriptions.put("IsCritter", "");
        descriptions.put("IsBandit", "");
        descriptions.put("IsPerson", "");
        descriptions.put("IsUnarmedWeapon", "");
        descriptions.put("IsStairs", "");
        descriptions.put("IsScenery", "");
        descriptions.put("IsBeing", "");
        descriptions.put("IsThing", "");
        descriptions.put("IsTemporaryEffect", "");
        descriptions.put("IsFire", "");
        descriptions.put("Z", "Z ordering");
        DESCRIPTIONS = descriptions;
    	}
        return DESCRIPTIONS;
    }
}
package tyrant.mikera.tyrant;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Modifier;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;


/**
Clothing exists so our hero is not running around naked under his armour.  Oh,
that chafing is just so irritating.  Each type of clothing has its appropriate
wield/wear slot in RPG.  

@author       Rick Blaine
@version      Feb 16, 2005
 */
public class Clothing {

    public static void init() {

        Thing thing = Lib.extend("base clothing", "base item");
        thing.set("Image", 345);
        thing.set("IsClothing", 1);
        thing.set("IsStoreItem",0); // not for sale (yet)
        thing.set("WieldType", RPG.WT_TORSO);
        thing.set("HPS", 50);
        thing.set("LevelMin",1);
        thing.set("ItemWeight", 200);
        thing.set("ValueBase",10);
        thing.set("Frequency", 20);
        Lib.add(thing);

        initBelts();
        initShirts();
        initSkirts();
        initTrousers();
   }

    // We need this reference in order to initialize the arrays
    private static Clothing anInstance = new Clothing();

    // Color bit masks
    private static final int WHITE      = 1;
    private static final int BLACK      = 2;
    private static final int BLUE       = 2 << 1;
    private static final int GREEN      = 2 << 2;
    private static final int RED        = 2 << 3;
    private static final int YELLOW     = 2 << 4;
    private static final int BROWN      = 2 << 5;
    private static final int PLAID      = 2 << 6;
    private static final int PAISLEY    = 2 << 7;
    private static final int POLKADOT   = 2 << 8;
    private static final int CHECKERED  = 2 << 9;
    private static final int STRIPED    = 2 << 10;
    private static final int PURPLE     = 2 << 11;
    private static final int ORANGE     = 2 << 12;
    private static final int PINK       = 2 << 13;
    private static final int CHARCOAL   = 2 << 14;
    private static final int TURQUOISE  = 2 << 15;
    private static final int TAN        = 2 << 16;
    private static final int TEAL       = 2 << 17;
    private static final int GRAY       = 2 << 18;
    private static final int ALMOND     = 2 << 19;
    private static final int CREAM      = 2 << 20;
    private static final int NONE       = 2 << 29;

    private static final int NORMAL     = BLUE | GREEN | RED | YELLOW | ORANGE |
                                          PINK | TEAL;
    private static final int EARTH      = WHITE | BLACK | BROWN | CHARCOAL |
                                          TAN | GRAY | ALMOND | CREAM;
    private static final int ROYAL      = PURPLE | TURQUOISE;
    private static final int PATTERNS   = PLAID | PAISLEY | POLKADOT | CHECKERED |
                                          STRIPED;
    private static final int ALL_COLORS = NORMAL | EARTH | ROYAL | PATTERNS;

    private class FabricColor {
        String name = null;
        int mask = 0;
        int charismaBonus = 0;
        int stealthBonus = 0;
        int resistanceBonus = 0;

        public FabricColor (String name,int mask,int charisma,int stealth,int resistance) {
            this.name = name;
            this.mask = mask;
            this.charismaBonus = charisma;
            this.stealthBonus = stealth;
            this.resistanceBonus = resistance;
        }
    }

    private static final int FIRE         = 1;
    private static final int ICE          = 2;
    private static final int WATER        = 3;
    private static final int SHOCK        = 4;
    private static final int PIERCING     = 5;
    private static final int POISON       = 6;
    private static final int CHILL        = 7;
    private static final int ACID         = 8;
    private static final int WEIGHT       = 9;
    private static final int IMPACT       = 10;
    private static FabricColor[] clothingColors =
    {
        // name, mask, charismaBonus, stealthBonus, resistance
        anInstance.new FabricColor("blue",BLUE,0,1,ICE),
        anInstance.new FabricColor("white",WHITE,0,0,0),
        anInstance.new FabricColor("red",RED,0,-1,FIRE),
        anInstance.new FabricColor("green",GREEN,0,0,0),
        anInstance.new FabricColor("yellow",YELLOW,0,0,0),
        anInstance.new FabricColor("black",BLACK,0,1,0),
        anInstance.new FabricColor("brown",BROWN,0,2,0),
        anInstance.new FabricColor("plaid",PLAID,0,-2,0),
        anInstance.new FabricColor("paisley",PAISLEY,1,-2,0),
        anInstance.new FabricColor("polka dot",POLKADOT,-1,-3,0),
        anInstance.new FabricColor("checkered",CHECKERED,0,-2,0),
        anInstance.new FabricColor("striped",STRIPED,0,-1,0),
        anInstance.new FabricColor("purple",PURPLE,2,0,0),
        anInstance.new FabricColor("orange",ORANGE,0,0,0),
        anInstance.new FabricColor("pink",PINK,0,0,0),
        anInstance.new FabricColor("charcoal",CHARCOAL,0,3,0),
        anInstance.new FabricColor("turquoise",TURQUOISE,0,0,0),
        anInstance.new FabricColor("tan",TAN,0,1,0),
        anInstance.new FabricColor("teal",TEAL,0,0,0),
        anInstance.new FabricColor("gray",GRAY,0,3,0),
        anInstance.new FabricColor("almond",ALMOND,0,1,0),
        anInstance.new FabricColor("cream",CREAM,0,1,0),
        anInstance.new FabricColor("",NONE,0,0,0)
    };

    // Fabric bit masks
    private static final int COTTON      = 1;
    private static final int SILK        = 2;
    private static final int WOOL        = 2 << 1;
    private static final int TERRYCLOTH  = 2 << 2;
    private static final int FLANNEL     = 2 << 3;
    private static final int CANVAS      = 2 << 4;
    private static final int SATIN       = 2 << 5;
    private static final int POLYESTER   = 2 << 6;
    private static final int DENIM       = 2 << 7;
    private static final int CORDUROY    = 2 << 8;
    private static final int CHAMOIS     = 2 << 9;
    private static final int FLEECE      = 2 << 10;
    private static final int LINEN       = 2 << 11;
    private static final int VELVET      = 2 << 12;
    private static final int TWILL       = 2 << 13;
    private static final int POPLIN      = 2 << 14;
    private static final int BROADCLOTH  = 2 << 15;
    private static final int VELOUR      = 2 << 16;
    private static final int FUR         = 2 << 17;
    private static final int FAUX_FUR    = 2 << 18;
    private static final int LEATHER     = 2 << 19;
    private static final int SNAKESKIN   = 2 << 20;
    private static final int KHAKI       = 2 << 21;
    private static final int SHARKSKIN   = 2 << 22;
    private static final int LIZARDSKIN  = 2 << 23;
    private static final int DOWN        = 2 << 24;
    private static final int SHEEPSKIN   = 2 << 25;
    // share none with FabricColor masks
    //private static final int NONE       = 2 << 29;

    private static final int BASIC       = COTTON | WOOL | LINEN;
    private static final int COMMON      = DENIM | CORDUROY | CANVAS | KHAKI;
    private static final int SHIRTS      = POLYESTER | BROADCLOTH | TWILL | POPLIN;
    private static final int EXOTIC      = SILK | SATIN;
    private static final int COMFY       = TERRYCLOTH | FLANNEL | FLEECE | CHAMOIS;
    private static final int ELVIS       = VELVET | VELOUR;
    private static final int ANIMAL      = FUR | FAUX_FUR | LEATHER | SNAKESKIN |
                                           SHARKSKIN | LIZARDSKIN | SHEEPSKIN;
    private static final int ALL_FABRICS = BASIC | COMMON | SHIRTS | EXOTIC |
                                           COMFY | ELVIS | ANIMAL;

    private class Fabric {
        String name = null;
        int mask = 0;
        int frequency = 0;
        int sturdiness = 0;
        int colorMask = 0;
        int resistanceBonus = 0;

        public Fabric (String name,int mask,int frequency,
                       int sturdiness,int colorMask,int resistance) {
            this.name = name;
            this.mask = mask;
            this.frequency = frequency;
            this.sturdiness = sturdiness;
            this.colorMask = colorMask;
            this.resistanceBonus = resistance;
        }
    }

    private static Fabric[] clothingMaterials =
    {
        // name,mask,frequency,sturdiness,colorMask,resistance
        anInstance.new Fabric("cotton",COTTON,25,6,ALL_COLORS,0),
        anInstance.new Fabric("silk",SILK,5,5,ALL_COLORS,0),
        anInstance.new Fabric("wool",WOOL,5,7,ALL_COLORS,0),
        anInstance.new Fabric("terrycloth",TERRYCLOTH,5,7,NORMAL | EARTH,0),
        anInstance.new Fabric("flannel",FLANNEL,5,7,NORMAL | PATTERNS,0),
        anInstance.new Fabric("canvas",CANVAS,5,10,EARTH,0),
        anInstance.new Fabric("satin",SATIN,5,5,NORMAL | EARTH,0),
        anInstance.new Fabric("polyester",POLYESTER,5,6,ALL_COLORS,0),
        anInstance.new Fabric("denim",DENIM,20,10,EARTH | BLUE,0),
        anInstance.new Fabric("corduroy",CORDUROY,5,9,NORMAL,0),
        anInstance.new Fabric("chamois",CHAMOIS,5,9,NORMAL,WATER),
        anInstance.new Fabric("fleece",FLEECE,5,8,NORMAL | EARTH,0),
        anInstance.new Fabric("linen",LINEN,5,7,EARTH,0),
        anInstance.new Fabric("velvet",VELVET,5,7,NORMAL | ROYAL,SHOCK),
        anInstance.new Fabric("twill",TWILL,5,7,NORMAL | EARTH,0),
        anInstance.new Fabric("poplin",POPLIN,5,7,ALL_COLORS,0),
        anInstance.new Fabric("broadcloth",BROADCLOTH,15,7,ALL_COLORS,0),
        anInstance.new Fabric("velour",VELOUR,5,6,NORMAL | ROYAL,0),
        anInstance.new Fabric("fur",FUR,5,10,NONE,ICE),
        anInstance.new Fabric("faux fur",FAUX_FUR,5,7,NONE,0),
        anInstance.new Fabric("leather",LEATHER,5,20,RED | BROWN | BLACK | NONE,PIERCING),
        anInstance.new Fabric("snakeskin",SNAKESKIN,5,20,NONE,POISON),
        anInstance.new Fabric("sharkskin",SHARKSKIN,5,25,NONE,CHILL),
        anInstance.new Fabric("lizardskin",LIZARDSKIN,5,25,NONE,ACID),
        anInstance.new Fabric("khaki",KHAKI,5,10,NORMAL | EARTH,FIRE),
        anInstance.new Fabric("down",DOWN,5,6,NORMAL | EARTH,WEIGHT),
        anInstance.new Fabric("sheepskin",SHEEPSKIN,5,15,EARTH | NONE,IMPACT),
        anInstance.new Fabric("",NONE,5,10,NONE,0)
    };
    
    private static void initBelts() {
        Thing thing;
        thing = Lib.extend("base belt","base clothing");
        thing.set("IsBelt",1);
        thing.set("WieldType",RPG.WT_BELT);
        thing.set("LegalFabrics",ANIMAL);
        thing.set("Image",358);
        Lib.add(thing);

        thing = Lib.extend("belt","base belt");
        addClothingVariants(thing);
        // weight belt is so obvious
        addMagicVariants("IsBelt",50,Modifier.percent("CarryFactor",110),""," of lifting",5,false);
        addMagicVariants("IsBelt",10,Modifier.percent("CarryFactor",120),""," of heavy lifting",15,false);
        // For the cursed variant we add a space at the end so names can be visually the same
        addMagicVariants("IsBelt",20,Modifier.percent("CarryFactor",60),""," of lifting ",5,true);
        // because you expected it
        addMagicVariants("IsBelt",10,Modifier.bonus(RPG.ST_ST,RPG.d(2,3)),""," of strength",8,false);
        addMagicVariants("IsBelt",10,Modifier.bonus(RPG.ST_ST,-RPG.d(2,3)),""," of strength ",8,true);
    }

    private static void initTrousers() {
        Thing thing;
        thing = Lib.extend("base trousers","base clothing");
        thing.set("IsTrousers",1);
        thing.set("LegalFabrics",BASIC | COMMON);
        thing.set("WieldType",RPG.WT_LEGS);
        thing.set("Image",379);
        Lib.add(thing);

        thing = Lib.extend("trousers","base trousers");
        addClothingVariants(thing);
        // charm his pants off
        addMagicVariants("IsTrousers",10,Modifier.bonus(Skill.SEDUCTION,1),""," of seduction",7,false);
        addMagicVariants("IsTrousers",10,Modifier.bonus(Skill.SEDUCTION,-1),""," of seduction ",7,true);
    }

    private static void initShirts() {
        Thing thing;
        thing = Lib.extend("base shirt","base clothing");
        thing.set("IsShirt",1);
        thing.set("WieldType",RPG.WT_TORSO);
        thing.set("LegalFabrics",ALL_FABRICS);
        thing.set("Image",359);
        Lib.add(thing);

        thing = Lib.extend("shirt","base shirt");
        addClothingVariants(thing);
        // shirt off my back
        addMagicVariants("IsShirt",10,Modifier.bonus(Skill.TRADING,1),""," of trading",8,false);
        addMagicVariants("IsShirt",10,Modifier.bonus(Skill.TRADING,-1),""," of trading ",8,true);
    }

    private static void initSkirts() {
        Thing thing;
        thing = Lib.extend("base skirt","base clothing");
        thing.set("IsSkirt",1);
        thing.set("LegalFabrics",ALL_FABRICS);
        thing.set("WieldType",RPG.WT_LEGS);
        thing.set("Image",377);
        thing.multiplyStat("Frequency",0.25);
        Lib.add(thing);

        thing = Lib.extend("skirt","base skirt");
        addClothingVariants(thing);
        // charm the skirt off her
        addMagicVariants("IsSkirt",10,Modifier.bonus(Skill.SEDUCTION,1),""," of seduction",7,false);
        addMagicVariants("IsSkirt",10,Modifier.bonus(Skill.SEDUCTION,-1),""," of seduction ",7,true);
    }

    private static void addClothingVariants(Thing baseThing) {

        Fabric fabric = null;
        String color = null;
        String name = null;
        for (int i = 0; i < clothingMaterials.length; i++) {
            if ((clothingMaterials[i].mask & baseThing.getStat("LegalFabrics")) > 0) {
                Thing fabricThing = (Thing)baseThing.clone();
                fabric = clothingMaterials[i];
                for (int j = 0; j < clothingColors.length; j++) {
                    if ((fabric.colorMask & clothingColors[j].mask) > 0) {
                        Thing thing = (Thing)fabricThing.clone();
                        color = clothingColors[j].name;
                        if (fabric.name.equals("")) {
                            if (color.equals("")) {
                                name = thing.name();
                            } else {
                                name = color + " " + thing.name();
                            }
                        } else if (color.equals("")) {
                            name = fabric.name + " " + thing.name();
                        } else {
                            name = color + " " + fabric.name + " " + thing.name();
                        }
                        if (clothingColors[j].charismaBonus != 0) {
                            thing.add("WieldedModifiers",Modifier.bonus("CH",clothingColors[j].charismaBonus));
                            thing.set("IsMagicItem",1);
                        }
                        if (clothingColors[j].stealthBonus != 0) {
                            // TODO activate stealth bonus when stealth is implemented.  This should not touch
                            // the stealth skill as that is too much of an impact.  
                            //thing.add("WieldedModifiers",Modifier.bonus(??,clothingColors[j].stealthBonus));
                            thing.set("IsMagicItem",1);
                        }
                        int resist = clothingColors[j].resistanceBonus;
                        if (resist == 0) {
                        	// nothing
                        } else if (resist == FIRE) {
                            thing.add("WieldedModifiers",Modifier.bonus("ARM:fire",RPG.d(2,3)));
                            thing.set("IsMagicItem",1);
                        } else if (resist == ICE) {
                            thing.add("WieldedModifiers",Modifier.bonus("ARM:ice",RPG.d(2,3)));
                            thing.set("IsMagicItem",1);
                        }
                        resist = fabric.resistanceBonus;
                        if (resist == 0) {
                        	// nothing
                        } else if (resist == FIRE) {
                            thing.add("WieldedModifiers",Modifier.bonus("ARM:fire",RPG.d(2,3)));
                            thing.set("IsMagicItem",1);
                        } else if (resist == ICE) {
                            thing.add("WieldedModifiers",Modifier.bonus("ARM:ice",RPG.d(2,3)));
                            thing.set("IsMagicItem",1);
                        } else if (resist == WATER) {
                            thing.add("WieldedModifiers",Modifier.bonus("ARM:water",RPG.d(2,3)));
                            thing.set("IsMagicItem",1);
                        } else if (resist == SHOCK) {
                            thing.add("WieldedModifiers",Modifier.bonus("ARM:shock",RPG.d(2,3)));
                            thing.set("IsMagicItem",1);
                        } else if (resist == PIERCING) {
                            thing.add("WieldedModifiers",Modifier.bonus("ARM:piercing",RPG.d(2,3)));
                            thing.set("IsMagicItem",1);
                        } else if (resist == POISON) {
                            thing.add("WieldedModifiers",Modifier.bonus("ARM:poison",RPG.d(2,3)));
                            thing.set("IsMagicItem",1);
                        } else if (resist == CHILL) {
                            thing.add("WieldedModifiers",Modifier.bonus("ARM:chill",RPG.d(2,3)));
                            thing.set("IsMagicItem",1);
                        } else if (resist == ACID) {
                            thing.add("WieldedModifiers",Modifier.bonus("ARM:acid",RPG.d(2,3)));
                            thing.set("IsMagicItem",1);
                        } else if (resist == IMPACT) {
                            thing.add("WieldedModifiers",Modifier.bonus("ARM:impact",RPG.d(2,3)));
                            thing.set("IsMagicItem",1);
                        } else if (resist == WEIGHT) {
                            thing.add("WieldedModifiers",Modifier.percent("CarryFactor",105));
                            thing.set("IsMagicItem",1);
                        }
                        thing.set("Name",name);
                        thing.set("Material",fabric.name);
                        thing.set("Color",color);

                        thing.multiplyStat("Frequency",fabric.frequency/100.0);

                        // The base is 50, so a sturdiness of 10 = 5 HPS
                        thing.multiplyStat("HPS",fabric.sturdiness/100.0);
                        Lib.add(thing);
                    }
                }
            }
        }
    }
    
    private static void addMagicVariants(String type,
                                         int percent,
                                         Modifier modifier,
                                         String prefix,
                                         String suffix,
                                         int level,
                                         boolean cursed) {

        // When I tried this as a List with an iterator I got
        // a ConcurrentModificationException from the AbstractList
        // so we'll walk an array.  A puzzle...
        Thing[] candidates = (Thing[])Lib.instance().getTypeArray(type,1).toArray(new Thing[1]);
        Thing newThing = null;
        String name = null;
        //while (iterator.hasNext()) {
            //thing = (Thing)iterator.next();
        for (int i = 0; i < candidates.length; i++) {
            if (RPG.d100() < percent) {
                if (!candidates[i].getFlag("IsMagicVariant")) {
                    // if we are making multiple magic variants we don't
                    // want to do something like "of lifting of heavy lifting"
                    // so we leave a marker when something is created here
                    name = candidates[i].getstring("Name");
                    newThing = (Thing)candidates[i].clone();
                    newThing.multiplyStat("Frequency",0.1); // 1 in 10 of these
                    newThing.add("WieldedModifiers",modifier);
                    newThing.set("Uname",name);
                    newThing.set("Name",prefix + name + suffix);
                    newThing.set("IsMagic",1);
                    newThing.set("LevelMin",level);
                    newThing.set("IsMagicVariant",1);
                    if (cursed) {
                        newThing.set("IsCursed",1);
                    }
                    //System.out.println(name + " :: " + newThing.getString("Name"));
                    Lib.add(newThing);
                }
            }
        }
    }

    public static final void main (String[] args)
    {
        Clothing.init();
    }

}
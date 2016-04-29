// package tyrant.mikera.tyrant;
//
// import tyrant.mikera.engine.Lib;
// import tyrant.mikera.engine.Modifier;
// import tyrant.mikera.engine.RPG;
// import tyrant.mikera.engine.Thing;
//
/// **
// * @author Mike
// *
// * The artifact class creates and handles unique artifact weapons
// *
// * Artifacts should be immutable and unchangeable, e.g. - Immune to
// * damage - Unaffected by runes - Impossible to change status i.e.
// * cursed/blessed
// *
// *
// * This includes random artifacts
// */
//
// public class Artifact {
// public static void init() {
// initRings();
// initSwords();
// initAmulets();
// initArmour();
// initShields();
// initEquipment();
// initFootwear();
// initWeapons();
// initMissiles();
// initCrowns();
// initGloves();
// }
//
// public static void initFootwear() {
// Thing t;
//
// t = Lib.extend("The Seven League Boots", "leather boots");
// t.set("LevelMin", 1);
// t.set("Armour", 4);
// t.add("WieldedModifiers", Modifier.bonus(RPG.ST_MOVESPEED, 200));
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("Nitrozac's Boots", "leather boots");
// t.set("LevelMin", 17);
// t.set("Image", 364);
// t.set("IsRandomArtifact", 1);
// t.set("Armour", 40);
// t.add("WieldedModifiers", Modifier.bonus(RPG.ST_MOVESPEED, 50));
// addArtifact(t);
//
// t = Lib.extend("The Winged Sandals", "leather boots");
// t.set("LevelMin", 27);
// t.set("Armour", 5);
// t.add("WieldedModifiers", Modifier.bonus("IsFlying", 1));
// t.add("WieldedModifiers", Modifier.bonus("Speed", 20));
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// }
//
// public static void initGloves() {
// Thing t;
//
// t = Lib.extend("The Firefists", "red steel gauntlets");
// t.multiplyStat("Armour", 2.0);
// t.add("WieldedModifiers", Modifier.bonus(Skill.FEROCITY, 1));
// t.set("OnWeaponHit", Scripts.spellEffect("Target", "Fireball", 25));
// t.set("LevelMin", 20);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
// }
//
// public static void initShields() {
// Thing t;
//
// t = Lib.extend("Jorgen's Buckler", "krithium buckler");
// t.multiplyStat("Armour", 2.0);
// t.add("WieldedModifiers", Modifier.bonus(Skill.DEFENCE, 1));
// t.add("WieldedModifiers", Modifier.bonus(Skill.ALERTNESS, 1));
// t.set("LevelMin", 1);
// t.set("IsRandomArtifact", 1);
// t.set("Image", 660);
// addArtifact(t);
//
// t = Lib.extend("The Black Buckler", "black steel buckler");
// t.multiplyStat("Armour", 1.7);
// t.add("WieldedModifiers", Modifier.bonus(Skill.DEFENCE, 2));
// t.add("WieldedModifiers", Modifier.bonus("Luck", 20));
// t.set("LevelMin", 10);
// t.set("IsRandomArtifact", 1);
// t.set("Image", 660);
// addArtifact(t);
//
// t = Lib.extend("Gorandil", "parillite large shield");
// t.multiplyStat("Armour", 1.2);
// t.add("WieldedModifiers", Modifier.bonus(Skill.DEFENCE, 2));
// t.add("WieldedModifiers", Modifier.bonus("Speed", 10));
// t.add("WieldedModifiers", Modifier.bonus("Luck", 20));
// t.set("LevelMin", 35);
// t.set("IsRandomArtifact", 1);
// t.set("Image", 399);
// addArtifact(t);
// }
//
// public static void initArmour() {
// Thing t;
//
// t = Lib.extend("Korvon's Armour", "steel chain mail");
// t.set("UName", "finely crafted chain mail");
// t.multiplyStat("Armour", 5);
// t.add("WieldedModifiers", Modifier.bonus(Skill.DEFENCE, 1));
// t.add("WieldedModifiers", Modifier.bonus("RES:fire", 10));
// t.set("LevelMin", 1);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("Kabirion", "mithril plate mail");
// t.set("UName", "engraved elven plate mail");
// t.multiplyStat("Armour", 2);
// t.add("WieldedModifiers", Modifier.bonus("Luck", 10));
// t.set("LevelMin", 10);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("Kethirion", "krithium horned helm");
// t.set("UName", "huge dwarvish horned helm");
// t.multiplyStat("Armour", 3);
// t.add("WieldedModifiers", Modifier.bonus("Luck", 10));
// t.set("LevelMin", 15);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("The Tyrant's Armour", "black steel plate armour");
// t.set("UName", "heavy plated armour");
// t.multiplyStat("Armour", 3);
// t.add("WieldedModifiers", Modifier.bonus(Skill.DEFENCE, 1));
// t.set("LevelMin", 25);
// addArtifact(t);
//
// t = Lib.extend("The Cloak Of The Night", "light cloak");
// t.set("UName", "flowing dark cloak");
// t.set("Armour", 10);
// t.add("WieldedModifiers", Modifier.bonus(Skill.DEFENCE, 1));
// t.add("WieldedModifiers", Modifier.bonus(Skill.STEALTH, 2));
// t.set("LevelMin", 5);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("The Helm Of Aramis", "parillite helmet");
// t.set("UName", "brightly shining engraved helmet");
// t.multiplyStat("Armour", 3);
// t.add("WieldedModifiers", Modifier.bonus("Luck", 20));
// t.add("WieldedModifiers", Modifier.bonus(Skill.HEALING, RPG.d(6)));
// t.set("LevelMin", 35);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// }
//
// public static void initCrowns() {
// Thing t;
//
// t = Lib.extend("The Crown of Daedor", "base headgear");
// t.add("WieldedModifiers", Modifier.bonus("TG", 20));
// t.add("WieldedModifiers", Modifier.bonus("WP", 20));
// t.add("WieldedModifiers", Modifier.bonus("Luck", 30));
// t.add("WieldedModifiers", Modifier.bonus("RES:disintegrate", 5));
// t.set("Armour", 10);
// t.set("Image", 427);
// t.set("LevelMin", 30);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("The Crown of Fire", "base headgear");
// t.add("WieldedModifiers", Modifier.bonus("RES:fire", 1000));
// t.add("WieldedModifiers", Modifier.addHandler("OnAction",
// Monster.breathAttack("fire", "blazing flames", 4, 50, 1)));
// t.set("Armour", 6);
// t.set("Image", 428);
// t.set("LevelMin", 35);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("The Crown of Ages", "base headgear");
// t.add("WieldedModifiers", Modifier.bonus("IN", 30));
// t.add("WieldedModifiers", Modifier.bonus("Speed", 30));
// t.add("WieldedModifiers", Modifier.bonus("TrueView", 5));
// t.set("Image", 425);
// t.set("Armour", 8);
// t.set("LevelMin", 40);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// }
//
// public static void initAmulets() {
// Thing t = Lib.extend("The Emerald Of Life", "base necklace");
// t.set("UName", "stunning emerald necklace");
// t.set("Image", 406);
// t.set("Armour", 40);
// t.add("WieldedModifiers", Modifier.bonus("RES:disintegrate", 30));
// t.add("WieldedModifiers", Modifier.bonus("RES:chill", 30));
// t.add("WieldedModifiers", Modifier.bonus(Skill.HEALING, 3));
// t.set("LevelMin", 40);
// addArtifact(t);
// }
//
// public static void initEquipment() {
// Thing t;
//
// t = Lib.extend("The Spade Of Aces", "wooden spade");
// t.set("UName", "well used spade");
// t.set("SlayingStats", "IsGoblin*2");
// t.multiplyStat(RPG.ST_ASKMULTIPLIER, 2);
// t.multiplyStat(RPG.ST_ASTMULTIPLIER, 2);
// t.multiplyStat(RPG.ST_DSKMULTIPLIER, 2);
// t.add("WieldedModifiers", Modifier.bonus(Skill.MINING, 2));
// t.set("LevelMin", 25);
// t.set("DigCost", 100);
// addArtifact(t);
// }
//
// public static void initSwords() {
// Thing t;
//
// t = Lib.extend("Orcslayer", "mithril sword");
// t.set("UName", "glowing elven sword");
// t.set("SlayingStats", "IsOrc*3");
// t.multiplyStat(RPG.ST_ASKMULTIPLIER, 1.5);
// t.multiplyStat(RPG.ST_ASTMULTIPLIER, 1.5);
// t.set("LevelMin", 1);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("Thurgin's Bane", "krithium mace");
// t.set("UName", "heavy orcish mace");
// t.set("SlayingStats", "IsDwarf*3");
// t.multiplyStat(RPG.ST_ASKMULTIPLIER, 1.5);
// t.multiplyStat(RPG.ST_ASTMULTIPLIER, 1.5);
// t.set("LevelMin", 1);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("Herugrim", "steel sword");
// t.set("UName", "horse-engraved sword");
// t.multiplyStat(RPG.ST_ASKMULTIPLIER, 2.0);
// t.multiplyStat(RPG.ST_ASTMULTIPLIER, 2.0);
// t.add("WieldedModifiers", Modifier.bonus("MoveSpeed", 40));
// t.set("LevelMin", 5);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("Sting", "mithril dagger");
// t.set("UName", "gleaming hobbit's dagger");
// t.set("SlayingStats", "IsGoblinoid*3");
// t.multiplyStat(RPG.ST_ASKMULTIPLIER, 2.0);
// t.multiplyStat(RPG.ST_ASTMULTIPLIER, 2.0);
// t.multiplyStat(RPG.ST_ATTACKCOST, 0.5);
// t.set("LevelMin", 10);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("Morglay", "black steel short sword");
// t.set("UName", "fearsome black shortsword");
// t.multiplyStat(RPG.ST_ATTACKCOST, 0.7);
// t.set("OnTouch", Scripts.addEffect("Target", "strong poison"));
// t.set("LevelMin", 5);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("Purifier", "blue steel two-handed sword");
// t.set("UName", "brightly shining two-handed sword");
// t.set("SlayingStats", "IsUndead*3,IsDemonic*3");
// t.multiplyStat(RPG.ST_ASKMULTIPLIER, 1.5);
// t.multiplyStat(RPG.ST_ASTMULTIPLIER, 1.5);
// t.multiplyStat(RPG.ST_ATTACKCOST, 0.8);
// t.set("IsBlessed", 1);
// t.set("LevelMin", 20);
// t.set("IsRandomArtifact", 1);
// t.set("Image", 173);
// addArtifact(t);
//
// t = Lib.extend("Anduril", "elven steel longsword");
// t.set("UName", "shining elven longsword");
// t.set("SlayingStats", "IsDemonic*2");
// t.multiplyStat(RPG.ST_ASKMULTIPLIER, 1.5);
// t.multiplyStat(RPG.ST_ASTMULTIPLIER, 1.5);
// t.add("WieldedModifiers", Modifier.bonus(Skill.BRAVERY, 2));
// t.add("WieldedModifiers", Modifier.bonus("Luck", 20));
// t.set("IsBlessed", 1);
// t.set("LevelMin", 25);
// t.set("IsRandomArtifact", 1);
// t.set("Image", 173);
// addArtifact(t);
//
// t = Lib.extend("Excalibur", "mithril sword");
// t.set("UName", "shimmering sword");
// t.multiplyStat(RPG.ST_ASKMULTIPLIER, 3.0);
// t.multiplyStat(RPG.ST_ASTMULTIPLIER, 3.0);
// t.multiplyStat(RPG.ST_ATTACKCOST, 0.8);
// t.set("IsRandomArtifact", 1);
// t.set("IsBlessed", 1);
// t.set("LevelMin", 30);
// t.set("Image", 170);
// addArtifact(t);
//
// t = Lib.extend("Gimbermaal", "black steel two-handed sword");
// t.set("UName", "huge black two-handed sword");
// t.set("SlayingStats", "IsHumanoid*2,IsUndead*3");
// t.multiplyStat(RPG.ST_ASKMULTIPLIER, 2);
// t.multiplyStat(RPG.ST_ASTMULTIPLIER, 2);
// t.multiplyStat(RPG.ST_DSKMULTIPLIER, 2);
// t.set("IsRandomArtifact", 1);
// t.set("LevelMin", 35);
// t.set("Image", 173);
// addArtifact(t);
//
// t = Lib.extend("Yanthrall's Sword", "parillite longsword");
// t.set("UName", "gleaming engraved longsword");
// t.set("SlayingStats", "IsHumanoid*3,IsDemonic*3");
// t.add("WieldedModifiers", Modifier.bonus(Skill.BRAVERY, 4));
// t.add("WieldedModifiers", Modifier.bonus("Luck", 30));
// t.add("WieldedModifiers", Modifier.bonus("Speed", 10));
// t.multiplyStat(RPG.ST_ASKMULTIPLIER, 2);
// t.multiplyStat(RPG.ST_ASTMULTIPLIER, 2);
// t.multiplyStat(RPG.ST_DSKMULTIPLIER, 2);
// t.multiplyStat(RPG.ST_ATTACKCOST, 0.5);
// t.set("IsBlessed", 1);
// t.set("LevelMin", 40);
// t.set("Image", 166);
// addArtifact(t);
//
// }
//
// public static void initMissiles() {
// Thing t;
//
// t = Lib.extend("Cupid's Arrow", "arrow");
// t.set("Image", 80);
// t.set("Number", 1);
// t.set("LevelMin", 1);
// // t.set("OnWeaponHit",new Script() {
// // private static final long serialVersionUID = 1L;
// //
// // public boolean handle(Thing t, Event e) {
// // Thing target=e.getThing("Target");
// // Thing shooter=e.getThing("Shooter");
// //
// // if (shooter==null) return true;
// //
// // AI.setFollower(target,shooter);
// // shooter.message(target.getTheName()+" seems deeply enamoured with you");
// //
// // return true;
// // }
// // });
// addArtifact(t);
// }
//
// public static void initWeapons() {
// Thing t;
//
// t = Lib.extend("The Tyrant's Mace", "black steel two-handed mace");
// t.set("UName", "malevolent two-handed mace");
// t.set("SlayingStats", "IsHumanoid*2");
// t.multiplyStat(RPG.ST_ASKMULTIPLIER, 1.2);
// t.multiplyStat(RPG.ST_ASTMULTIPLIER, 1.2);
// t.multiplyStat(RPG.ST_ATTACKCOST, 0.8);
// t.set("LevelMin", 25);
// t.set("Image", 164);
// t.set("WeaponDamageType", "impact");
// addArtifact(t);
//
// t = Lib.extend("Death's Scythe", "scythe");
// t.set("UName", "space distorting black sycthe");
// t.set("SlayingStats", "IsLiving*3");
// t.multiplyStat(RPG.ST_ASKMULTIPLIER, 3);
// t.multiplyStat(RPG.ST_ASTMULTIPLIER, 3);
// t.multiplyStat(RPG.ST_DSKMULTIPLIER, 3);
// t.multiplyStat(RPG.ST_ATTACKCOST, 0.8);
// t.set("LevelMin", 35);
// t.set("Image", 172);
// addArtifact(t);
// }
//
// public static void initRings() {
// Thing t;
//
// t = Lib.extend("Fortune", "silver ring");
// t.set("UName", "strangely plain silver ring");
// t.add("WieldedModifiers", Modifier.bonus("AG", 10));
// t.add("WieldedModifiers", Modifier.bonus("Luck", 40));
// t.set("LevelMin", 1);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("Narya", "gold ring");
// t.set("UName", "burning gold ring");
// t.add("WieldedModifiers", Modifier.bonus("ST", 20));
// t.add("WieldedModifiers", Modifier.bonus("RES:fire", 1000));
// t.set("LevelMin", 20);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("Nenya", "silver ring");
// t.set("UName", "slippery silver ring");
// t.add("WieldedModifiers", Modifier.bonus(Skill.SWIMMING, 10));
// t.add("WieldedModifiers", Modifier.bonus("AG", 20));
// t.add("WieldedModifiers", Modifier.bonus("RES:water", 1000));
// t.set("LevelMin", 25);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("Vilya", "silver ring");
// t.set("UName", "sparkling silver ring");
// t.add("WieldedModifiers", Modifier.bonus("IsFlying", 1));
// t.add("WieldedModifiers", Modifier.bonus("IN", 10));
// t.add("WieldedModifiers", Modifier.bonus("Speed", 10));
// t.add("WieldedModifiers", Modifier.bonus("RES:shock", 10));
// t.set("LevelMin", 30);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("The One Ring", "gold ring");
// t.set("IsCursed", 1);
// t.set("UName", "strangely plain gold ring");
// t.add("WieldedModifiers", Modifier.bonus("SK", 10));
// t.add("WieldedModifiers", Modifier.bonus("ST", 10));
// t.add("WieldedModifiers", Modifier.bonus("AG", 10));
// t.add("WieldedModifiers", Modifier.bonus("TG", 10));
// t.add("WieldedModifiers", Modifier.bonus("IN", 10));
// t.add("WieldedModifiers", Modifier.bonus("WP", 10));
// t.add("WieldedModifiers", Modifier.bonus("CH", 10));
// t.add("WieldedModifiers", Modifier.bonus("CR", 10));
// t.add("WieldedModifiers", Modifier.bonus("Luck", 40));
// t.set("LevelMin", 35);
// addArtifact(t);
//
// t = Lib.extend("The Xing Ring", "gold ring");
// t.set("UName", "brilliant diamond ring");
// t.set("Image", 207);
// t.add("WieldedModifiers", Modifier.linear("IN", 200, 10));
// t.set("LevelMin", 30);
// t.set("IsRandomArtifact", 1);
// addArtifact(t);
//
// t = Lib.extend("Destiny", "silver ring");
// t.set("UName", "finely engraved silver ring");
// t.add("WieldedModifiers", Modifier.linear("WP", 150, 0));
// t.add("WieldedModifiers", Modifier.bonus("Luck", 100));
// t.set("LevelMin", 40);
// addArtifact(t);
// }
//
// private static void addArtifact(Thing t) {
// makeArtifact(t);
// Lib.addNewArtifact(t);
//
// }
//
// private static void makeArtifact(Thing t) {
// t.set("IsUnique", 1);
// t.set("IsArtifact", 1);
// t.set("IsDestructible", 0);
// t.set("IsTheftProof", 1);
// t.set("Frequency", 0);
//
// // override old defaultthings
// // e.g. prevent runes from appearing
// t.set("DefaultThings", t.getLocal().get("DefaultThings"));
//
// // very valuable
// t.set("ValueBase", 10000);
// t.set("LevelMax", t.getStat("LevelMin") + 15);
// }
// }

// package javelin.controller;
//
// import java.util.LinkedList;
// import java.util.TreeMap;
//
// import javelin.model.unit.Monster;
//
/// **
// * Checkout NPCs at Towns, villages... from tyrant!
// *
// * @author alex
// */
// public class AvatarHandler {
//
// TreeMap<String, LinkedList<LinkedList<String>>> pool = new TreeMap<String,
// LinkedList<LinkedList<String>>>();
// private final TreeMap<String, String> registered = new TreeMap<String,
// String>();
// static TreeMap<String, String> typetranslation = new TreeMap<String,
// String>();
// static {
// /* TODO needs to redistribute avatars since we have a lot more types now */
// typetranslation.put("skeleton", "undead");
// typetranslation.put("zombie", "undead");
// typetranslation.put("orc", "undead");
//
// typetranslation.put("animal", "animal");
// typetranslation.put("dire animal", "animal");
//
// typetranslation.put("vermin", "vermin");
// typetranslation.put("formian", "vermin");
//
// typetranslation.put("yuan-ti", "devil");
// typetranslation.put("devil", "devil");
// typetranslation.put("xorn", "devil");
// typetranslation.put("sahuagin", "devil");
// typetranslation.put("demon", "devil");
// typetranslation.put("slaad", "devil");
// typetranslation.put("hag", "devil");
//
// typetranslation.put("elemental", "elemental");
// typetranslation.put("mephit", "elemental");
// typetranslation.put("genie", "elemental");
//
// typetranslation.put("", "misc.");
// typetranslation.put("animated object", "misc.");
//
// typetranslation.put("dwarf", "celestial");
// typetranslation.put("elf", "celestial");
// typetranslation.put("gnome", "celestial");
// typetranslation.put("halfling", "celestial");
// typetranslation.put("sprite", "celestial");
// typetranslation.put("celestial", "celestial");
// typetranslation.put("planetouched", "celestial");
//
// typetranslation.put("dragon", "dragon");
//
// // final Set<String> keys = typetranslation.keySet();
// // final Collection<String> values = typetranslation.values();
// // boolean error = false;
// // for (final Monster m : Javelin.allmonsters) {
// // final String type = m.group.toLowerCase();
// // if (!keys.contains(type) && !values.contains(type)) {
// // System.err.println("No avatar for type: " + type);
// // error = true;
// // }
// // }
// // if (error) {
// // throw new RuntimeException("Missing avatars.");
// // }
// }
//
// /**
// * Distributes avatars among combat participants
// *
// * @param list
// * participants
// */
// public AvatarHandler() {
// populate();
// // handlefixed(blue);
// // handlefixed(red);
// }
//
// public void populate() {
// populate("animal", new String[][] { { "big rat", "black cat",
// "big dog", "dog", "eagle", "field mouse", "giant rat",
// "giant toad", "hawk", "python", "rabbit", "snail", "chaos rat",
// "cave lion", "cheetah", "crocodile", "fire snake",
// "giant frog", "tarantula", "brown bear", "black bear",
// "demon spectre" } });
//
// populate("celestial", new String[][] { { "butterfly", "flying goblin",
// "jeweller", "kobold chieftain", "mercenary", "nymph", "pirate",
// "swordsman", "chaos cultist", "bandit", "mercenary captain",
// "ogre", "thug", "wandering teacher", "dark angel",
// "chaos champion", "chaos knight", "greater frost demon" } });
//
// populate("devil", new String[][] { { "blue imp", "fire imp", "kobold",
// "mercenary commander", "pink urchin", "rock urchin",
// "bile demon", "bilious demon", "greater demon", "gryphon",
// "lesser demon", "purple horror", "baal-rukh",
// "horrendous gobbler", "soul eater" } });
//
// populate("dragon", new String[][] { { "greater fire imp", "wasp demon",
// "blue dragon", "black dragon", "green dragon", "swamp dragon",
// "frost dragon", "greater fire demon" } });
//
// populate("elemental", new String[][] { { "grey slime", "slug",
// "earth elemental", "fire elemental", "greater voidling",
// "holy ghost", "ice elemental", "air elemental",
// "electric vortex", "fire vortex", "red dragon" }, });
//
// populate("misc.",
// new String[][] { { "floating eye", "greater tree monster",
// "seed triffid", "beholder", "malevolent eye", "mutant",
// "shrooma", "vileweed", "magipede" } });
//
// populate("undead",
// new String[][] { { "cutpurse", "zombie", "big goblin",
// "delver", "frost imp", "goblin", "goblin archer",
// "orc", "orc warrior", "wood urchin", "skeleton",
// "big orc", "hill giant", "horrendous zombie",
// "goblin champion", "goblin warrior", "hydra",
// "skeletal dragon", "skeleton hero" } });
//
// populate("vermin", new String[][] { { "bee swarm", "bug", "beetle",
// "baby giant spider", "cockroach", "demon centipede",
// "baby ralkan", "big bug", "centipede", "chaos beetle",
// "demon snake", "giant tarantula", "black widow", "chaos worm",
// "forest ralkan", "giant spider" } });
// }
//
// TreeMap<String, String> fixed = new TreeMap<String, String>();
//
// // private void handlefixed(final List<Monster> team) {
// // for (final Monster m : team) {
// // if (m.avatar != null) {
// // final String type = entriestype.get(m.avatar);
// // if (type != null) {
// // final LinkedList<String> tier = pool.get(type).get(
// // entriestier.get(m.avatar));
// // final int index = tier.indexOf(m.avatar);
// // if (index >= 0) {
// // tier.remove(index);
// // }
// // }
// // fixed.put(m.getName().getName(), m.avatar);
// // }
// // }
// // }
//
// TreeMap<String, String> entriestype = new TreeMap<String, String>();
// final TreeMap<String, Integer> entriestier = new TreeMap<String, Integer>();
//
// /**
// * TODO needed because using the same Thing name for more than a combatant
// * makes them change from blue to red if they die during any part of the AI
// * thinking. this probably has something to do with the Combatant
// * duplication passing the same Thing along to each new BattleState.
// */
// LinkedList<String> dumblist = new LinkedList<String>();
//
// public void populate(final String type, final String[][] tiers) {
// // final LinkedList<LinkedList<String>> tierlist = new
// // LinkedList<LinkedList<String>>();
// // pool.put(type, tierlist);
// // int tieri = -1;
// for (final String[] tier : tiers) {
// // tieri += 1;
// // final LinkedList<String> entries = new LinkedList<String>();
// // tierlist.add(entries);
// for (final String entry : tier) {
// dumblist.add(entry);
// // entries.add(entry);
// // entriestype.put(entry, type);
// // entriestier.put(entry, tieri);
// }
// // Collections.shuffle(entries);
// }
// }
//
// private String translate(final Monster m) {
// return typetranslation.get(m.group.toLowerCase());
// }
//
// /**
// * TODO currently just getting the different tiers and pooling them
// * together. Each tier should be used at a given power level.
// */
// public String assign(final Monster monster) {
// // final String name = monster.getName().getName();
// // final boolean isregistered = registered.containsKey(name);
// // if (!isregistered) {
// // String avatar = null;
// // final String fixedavatar = fixed.get(name);
// // if (fixedavatar != null) {
// // avatar = fixedavatar;
// // } else {
// // final String type = translate(monster);
// // final LinkedList<LinkedList<String>> tiers = pool.get(type);
// // if (tiers == null) {
// // System.err.println("No tier: " + type);
// // }
// // for (final LinkedList<String> tier : tiers) {
// // if (!tier.isEmpty()) {
// // avatar = tier.pop();
// // break;
// // }
// // }
// // }
// // if (avatar == null) {
// // throw new RuntimeException("Not enough avatars!");
// // }
// // registered.put(name, avatar);
// // }
// // return registered.get(name);
// return dumblist.pop();
// }
// }

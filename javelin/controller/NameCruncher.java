// package javelin.controller;
//
// import tyrant.mikera.engine.Lib;
// import tyrant.mikera.engine.RPG;
//
// public class NameCruncher {
// private static final String[] NAMES = new String[] { "swamp dragon",
// "bilious demon", "fire worm", "flutterby", "final giant cockroach",
// "final large skeleton", "final kobold warrior", "pirate captain",
// "tree monster", "goblin leader", "final haunted skeleton",
// "chaos cultist", "spider", "rock urchin", "horntoad",
// "electric vortex", "voidling", "green dragon", "final blue imp",
// "wandering teacher", "final giant spider", "monster centipede",
// "goblin rockthrower", "mercenary commander", "red dragon",
// "mutant", "big goblin", "red spider", "final kobold chieftain",
// "malevolent eye", "final black cat", "field mouse",
// "final red slime", "greater demon", "beholder", "final cave lion",
// "skeletal dragon", "grizzly bear", "small yellow bug",
// "final grass snake", "goblin archer", "kobold chieftain", "bandit",
// "vileweed", "brown bear", "centipede", "purple horror",
// "final baby ralkan", "lesser skeleton", "fire imp",
// "baby giant spider", "giant worm", "wood urchin", "shrooma",
// "greater frost demon", "small goblin", "chaos hero", "grass snake",
// "final fire imp", "leprechaun", "small rat", "black cat", "nymph",
// "final purple slime", "ice vortex", "pit beast", "goblin champion",
// "dog", "red snake", "greater blue imp", "flying goblin", "big rat",
// "final big bug", "pirate leader", "yellow slime", "forest ralkan",
// "final goblin warrior", "earth elemental", "mercenary",
// "skeleton hero", "giant spider", "giant tarantula", "demon snake",
// "final frost imp", "hornet swarm", "wolf spider", "jeweller",
// "mercenary captain", "hound", "sewer rat", "slug", "swordsman",
// "big bug", "final dark angel", "magipede", "greater voidling",
// "scorpion", "black dragon", "tree hurler", "bile demon",
// "greater fire imp", "final goblin slinger", "red slime", "pirate",
// "black widow", "fire elemental", "fearsome skeleton",
// "demon spectre", "final tree monster", "giant centipede",
// "blue imp", "thug", "final fire snake", "witch's cat",
// "black bear", "chaos champion", "ice bug", "yellow bug",
// "haunted skeleton", "insect", "big dog", "final small spider",
// "horrendous gobbler", "fearsome zombie", "large skeleton", "ogre",
// "final fly swarm", "poison vortex", "final fearsome skeleton",
// "fire vortex", "orc warrior", "butterfly", "crocodile",
// "blue dragon", "final goblin archer", "final ice bug",
// "purple slime", "hawk", "cheetah", "bee swarm", "warlock",
// "chaos knight", "snail", "big orc", "polar bear",
// "final floating eye", "final magic eye", "wildcat", "orc",
// "kobold", "skeleton warrior", "ice elemental", "zombie",
// "greater final blue imp", "pink urchin", "greater tree monster",
// "chaos beetle", "wasp swarm", "small snake", "soul eater",
// "giant toad", "giant cockroach", "goblin slinger", "spectre lord",
// "fire wasp swarm", "triffid", "holy ghost", "chaos rat", "archer",
// "final lesser demon", "snake", "final big goblin", "goblin",
// "wasp demon", "corpse worm", "horrendous zombie", "frost dragon",
// "bug", "cutpurse", "gryphon", "baal-rukh", "demon centipede",
// "frost imp", "floating eye", "greater final fire imp", "python",
// "ralkan", "goblin warrior", "magic eye", "delver", "lesser zombie",
// "eagle", "fire snake", "ice bear", "giant rat", "vileworm",
// "dark angel", "final tree hurler", "chaos warrior", "chaos worm",
// "seed triffid", "greater fire demon", "rabbit", "cockroach",
// "skeleton", "baby ralkan", "beetle", "fly swarm",
// "final grey slime", "hill giant", "final skeleton warrior",
// "weedy goblin", "hydra", "tarantula", "merchant", "cave lion",
// "bandit archer", "air elemental", "kobold warrior",
// "master voidling", "skeleton lord", "final yellow slime",
// "large spider", "final red snake", "giant frog", "grey slime",
// "kestrel", "lesser demon", "large zombie", };
//
// static public String crunchAName() {
// while (true) {
// final String name = RPG.pick(NAMES);
// if (Lib.get(name) != null) {
// return name;
// }
// }
// }
// }

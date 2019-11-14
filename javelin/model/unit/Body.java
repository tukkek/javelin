package javelin.model.unit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javelin.Javelin;

/**
 * https://www.d20pfsrd.com/magic-items/
 *
 * TODO some slots are restricted to saddle/horseshoes only, might want to
 * update and create a new slot?
 *
 * @author alex
 */
public class Body implements Serializable{
	public static final HashMap<String,Body> TYPES=new HashMap<>(12);
	/** bat, chicken, dodo, hawk */
	public static final Body AVIAN=new Body("avian",true,Set.of(Slot.ARMOR,
			Slot.WAIST,Slot.TORSO,Slot.EYES,Slot.HEAD,Slot.NECK,Slot.RING,Slot.ARMS));
	/** kangaroo, tyrannosaurus, velociraptor */
	public static final Body BIPED_CLAW=new Body("biped, claws",true,
			Set.of(Slot.ARMOR,Slot.WAIST,Slot.TORSO,Slot.EYES,Slot.HEAD,Slot.NECK,
					Slot.RING,Slot.SHOULDERS,Slot.ARMS));
	/** humanoid */
	public static final Body BIPED_HANDS=new Body("biped, hands",true,
			Set.of(Slot.ARMOR,Slot.ARMS,Slot.EYES,Slot.FEET,Slot.HANDS,Slot.HEAD,
					Slot.NECK,Slot.RING,Slot.SHOULDERS,Slot.TORSO,Slot.WAIST));
	/** humanoid but with tail instead of legs */
	public static final Body SALAMANDER=new Body("salamander",true,
			Set.of(Slot.ARMOR,Slot.ARMS,Slot.EYES,Slot.HANDS,Slot.HEAD,Slot.NECK,
					Slot.RING,Slot.SHOULDERS,Slot.TORSO,Slot.WAIST));
	/** fish, dolphin, seal */
	public static final Body PISCINE=new Body("piscine",false,
			Set.of(Slot.WAIST,Slot.TORSO,Slot.EYES));
	/** fox, hare, armadillo, cat, rat */
	public static final Body QUADRUPED_CLAW=new Body("quadruped, claws",false,
			Set.of(Slot.ARMOR,Slot.WAIST,Slot.TORSO,Slot.EYES,Slot.HEAD,Slot.NECK,
					Slot.SHOULDERS,Slot.ARMS));
	/** camel, elephant, hippopotamus, mammoth, rhinoceros */
	public static final Body QUADRUPED_FEET=new Body("quadruped, feet",false,
			Set.of(Slot.ARMOR,Slot.WAIST,Slot.TORSO,Slot.EYES,Slot.HEAD,Slot.NECK,
					Slot.SHOULDERS,Slot.ARMS));
	/** ant, mantis, wasp, */
	public static final Body HEXAPOD=new Body("hexapod",false,
			Set.of(Slot.ARMOR,Slot.WAIST,Slot.TORSO,Slot.EYES,Slot.HEAD,Slot.NECK,
					Slot.SHOULDERS,Slot.ARMS));
	/** bison, boar, buffalo, cattle, elk, giraffe, horse, llama */
	public static final Body QUADRUPED_HOOVES=new Body("quadruped, hooves",false,
			Set.of(Slot.ARMOR,Slot.WAIST,Slot.TORSO,Slot.EYES,Slot.FEET,Slot.HEAD,
					Slot.NECK,Slot.SHOULDERS));
	/** turtle, toad */
	public static final Body QUADRUPED_SQUAT=new Body("qaadruped, squat",false,Set
			.of(Slot.ARMOR,Slot.EYES,Slot.HEAD,Slot.NECK,Slot.SHOULDERS,Slot.ARMS));
	/** Alligator, crocodile, chameleon, gecko, lizard */
	public static final Body SAURIAN=new Body("saurian",false,
			Set.of(Slot.ARMOR,Slot.WAIST,Slot.TORSO,Slot.EYES,Slot.HEAD,Slot.NECK));
	/** snake, eel, gar, leech, slug */
	public static final Body SERPENTINE=new Body("serpentine",false,
			Set.of(Slot.WAIST,Slot.EYES,Slot.HEAD));
	/** fungus, cactus, fern */
	public static final Body PLANT=new Body("plant",false,Set.of(Slot.WAIST));
	/** beetle, centipede, crab, scorpion, spider, octopus, squid */
	public static final Body VERMINOUS=new Body("verminous",false,
			Set.of(Slot.WAIST,Slot.EYES));
	/** Bizarre, deformed, non-physical. */
	public static final Body AMORPHOUS=new Body("amorphous",false,Set.of());
	/** Alias for {@link #BIPED_HANDS}. */
	public static final Body HUMANOID=BIPED_HANDS;

	static{ //aliases
		TYPES.put("vermin",VERMINOUS);
		TYPES.put("humanoid",BIPED_HANDS);
		TYPES.put("reptile",SAURIAN);
		TYPES.put("mammal",QUADRUPED_CLAW);
		TYPES.put("equine",QUADRUPED_HOOVES);
	}

	/** Name of body type. */
	public String type;
	/** Slots available. */
	public Set<Slot> slots=new HashSet<>(12);
	/** <code>true</code> if can carry/grasp items. */
	public boolean hold;

	Body(String type,boolean hold,Set<Slot> slots){
		super();
		type=type.toLowerCase();
		this.type=type;
		this.hold=hold;
		this.slots.add(Slot.SLOTLESS);
		this.slots.addAll(slots);
		if(TYPES.put(type,this)!=null&&Javelin.DEBUG)
			throw new RuntimeException("Repeat body type: "+type);
	}

	@Override
	public boolean equals(Object o){
		return o instanceof Body&&((Body)o).type.equals(type);
	}

	@Override
	public int hashCode(){
		return type.hashCode();
	}
}

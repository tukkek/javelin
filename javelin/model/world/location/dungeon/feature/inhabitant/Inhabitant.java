package javelin.model.world.location.dungeon.feature.inhabitant;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.controller.challenge.RewardCalculator;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.table.dungeon.feature.FeatureModifierTable;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * A friendly (or at least neutral) {@link Monster} living inside a
 * {@link DungeonFloor}. Tries to utilize one of the creatures in
 * {@link DungeonFloor#encounters} but will generate a new one if necessary.
 *
 * @author alex
 */
public abstract class Inhabitant extends Feature{
	/** @see Inhabitant */
	public Combatant inhabitant;

	/**
	 * Will advance any selected {@link Monster} up to this level using
	 * {@link NpcGenerator}.
	 *
	 * @see #select()
	 */
	float crmin;
	/**
	 * Used as an upper bound if having to generate a monsster.
	 */
	float crmax;
	/**
	 * Between 0-100% normal treasure, to prevent players from blindly attacking
	 * them for treasure. However, allows players to recuperate any gold they
	 * provide the NPC if they choose to do so (as long as the value is kept
	 * up-to-date).
	 */
	protected int gold;
	/**
	 * DC 10 + {@link DungeonFloor#level} + {@link DungeonFeatureModifier}.
	 * Subclasses may alter them as needed.
	 */
	protected int diplomacydc;

	public Inhabitant(float crmin,float crmax,String description,DungeonFloor f){
		super(description);
		this.crmin=crmin;
		this.crmax=crmax;
		remove=false;
		inhabitant=select(f);
		avatarfile=inhabitant.source.avatarfile;
		Skill.DIPLOMACY.getupgrade().upgrade(inhabitant);
		diplomacydc=inhabitant.taketen(Skill.DIPLOMACY)
				+f.gettable(FeatureModifierTable.class).roll();
		int d100=RPG.r(0,100);
		gold=RewardCalculator.getgold(inhabitant.source.cr)*d100/100;
	}

	/**
	 * Both CR parameters may be stretched internally to find a suitable
	 * candidate.
	 *
	 * @return An intelligent {@link Monster}, which is valid even if
	 *         {@link #dungeon} is a {@link TempleFloor}. If it can't find one in
	 *         {@link DungeonFloor#encounters}, generates one instead.
	 */
	public Combatant select(DungeonFloor f){
		HashSet<String> invalid=new HashSet<>();
		for(Combatant c:f.getcombatants()){
			Monster m=c.source;
			String name=m.name;
			if(invalid.contains(name)||!validate(m,f)){
				invalid.add(name);
				continue;
			}
			Combatant npc=NpcGenerator.generatenpc(m,crmin);
			if(npc!=null) return npc;
		}
		return new Combatant(generate(crmin,crmax,f),true);
	}

	Monster generate(float crmin,float crmax,DungeonFloor f){
		ArrayList<Monster> candidates=new ArrayList<>();
		for(Float cr:Monster.BYCR.keySet())
			if(crmin<=cr&&cr<=crmax) candidates.addAll(Monster.BYCR.get(cr));
		Collections.shuffle(candidates);
		for(Monster m:candidates)
			if(validate(m,f)) return m;
		return generate(crmin-1,crmax+1,f);
	}

	/** @see Dungeon#validate(List) */
	protected boolean validate(Monster m,DungeonFloor f){
		return m.think(-1)&&f.dungeon.validate(List.of(new Combatant(m,true)));
	}

	@Override
	public String toString(){
		return inhabitant+" "+description.toLowerCase();
	}

	@Override
	public Image getimage(){
		return Images.get(List.of("monster",avatarfile));
	}
}
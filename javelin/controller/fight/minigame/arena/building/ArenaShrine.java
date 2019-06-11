package javelin.controller.fight.minigame.arena.building;

import java.util.List;
import java.util.stream.Collectors;

import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.old.RPG;

/**
 * Works similarly to a fountain but casts a spell instead.
 *
 * TODO would be nice to have spent/unspent images
 *
 * @author alex
 */
public class ArenaShrine extends ArenaFountain{
	static final List<Spell> RITUALS=Spell.BYNAME.values().stream()
			.filter(s->s.castonallies&&s.castinbattle).collect(Collectors.toList());

	Spell ritual=RPG.pick(RITUALS);

	/** Constructor. */
	public ArenaShrine(){
		super("Shrine","locationshrine","locationshrineempty","Cast a spell: ");
		source.customName+=" ("+ritual.name+")";
		actiondescription+=ritual.name+".";
	}

	@Override
	protected String activate(Combatant current,List<Combatant> nearby){
		return ritual.castpeacefully(null,current,nearby);
	}
}

package javelin.controller.kit.wizard;

import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;

/**
 * Uses {@link Summon} {@link Spell}s.
 *
 * @author alex
 */
public class Summoner extends Wizard{
	public static final Summoner INSTANCE=new Summoner();

	/** Constructor. */
	Summoner(){
		super("Summoner",RaiseCharisma.SINGLETON,"Caller");
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.addAll(Javelin.ALLMONSTERS.stream().filter(m->!m.passive)
				.map(m->new Summon(m.name,1)).collect(Collectors.toList()));
	}
}

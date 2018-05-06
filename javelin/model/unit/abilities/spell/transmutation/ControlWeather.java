package javelin.model.unit.abilities.spell.transmutation;

import java.util.Arrays;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * http://www.d20srd.org/srd/spells/controlWeather.htm
 * 
 * TODO terrains like desert don't allow rain, would be nicer to turn
 * {@link Terrain#getweather()} into something smarter to handle all cases
 * 
 * @author alex
 */
public class ControlWeather extends Spell {
	/** Constructor. */
	public ControlWeather() {
		super("Control weather", 7, ChallengeCalculator.ratespelllikeability(7),
				Realm.AIR);
		isscroll = true;
		isritual = true;
		castinbattle = false;
		castonallies = false;
		castoutofbattle = true;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		int to = Javelin.choose("What shall the weather be?",
				Arrays.asList(new String[] { "Clear", "Rain", "Storm" }), false,
				true);
		if (to == 0) {
			Weather.current = Weather.DRY;
			return "The sky clears!";
		}
		if (to == 1) {
			Weather.current = Weather.RAIN;
			return "A light drizzle begins...";
		}
		Weather.current = Weather.STORM;
		return "A mighty thunder roars!";
	}
}

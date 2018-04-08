package javelin.controller.fight.minigame.battlefield;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.CrCalculator;
import javelin.controller.exception.GaveUp;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import tyrant.mikera.engine.RPG;

public class Reinforcement {
	public ArrayList<Combatant> commander = new ArrayList<Combatant>();
	public ArrayList<Combatant> elites;
	public ArrayList<Combatant> footsoldiers = new ArrayList<Combatant>();

	public Reinforcement(int el) {
		el = Math.min(el, BattlefieldFight.HIGHESTEL);
		generatecommander(el);
		generateelites(el);
		generatefootsoldiers(el);
	}

	Reinforcement(float el) {
		this(Math.round(el));
	}

	void generatecommander(int el) {
		for (float cr = CrCalculator.eltocr(el); commander
				.isEmpty(); cr -= 1) {
			List<Monster> tier = Javelin.MONSTERSBYCR.get(cr);
			if (tier == null) {
				continue;
			}
			commander.add(new Combatant(RPG.pick(tier), true));
		}
	}

	void generateelites(int el) {
		for (int target = el; elites == null;) {
			try {
				elites = EncounterGenerator.generate(target, BattlefieldFight.TERRAIN);
				if (elites.size() == 1) {
					elites = null;
				}
			} catch (GaveUp e) {
				continue;
			}
		}
	}

	void generatefootsoldiers(int elp) {
		int el = Math.max(1, elp - RPG.r(5, 10));
		ArrayList<Combatant> footsoldiers = null;
		while (footsoldiers == null) {
			try {
				footsoldiers = EncounterGenerator.generate(el, BattlefieldFight.TERRAIN);
				if (footsoldiers.size() == 1) {
					footsoldiers = null;
				}
			} catch (GaveUp e) {
				el += RPG.chancein(2) ? +1 : -1;
			}
		}
		this.footsoldiers.addAll(footsoldiers);
		while (CrCalculator.calculateel(this.footsoldiers) < elp) {
			for (Combatant c : footsoldiers) {
				this.footsoldiers.add(new Combatant(c.source, true));
			}
		}
	}
}
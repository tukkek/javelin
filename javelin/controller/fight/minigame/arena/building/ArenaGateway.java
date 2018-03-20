package javelin.controller.fight.minigame.arena.building;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.factor.FeatsFactor;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.controller.quality.FastHealing;
import javelin.model.state.BattleState;
import javelin.model.unit.Building;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.feat.Toughness;
import tyrant.mikera.engine.RPG;

public class ArenaGateway extends Building {
	public float heal;
	public Monster summon;
	public float summonchance;
	public int summonap;

	ArenaGateway() {
		super(Javelin.getmonster("building"), false);
		source.avatarfile = "flagpolered";
		source.ac = 10;
		source.customName = "Rally point";
	}

	public static ArenaGateway generate(float cr) {
		if (cr < CRADJUSTMENT + 2) {
			return null;
		}
		ArenaGateway g = new ArenaGateway();
		g.source.challengerating = cr;
		float hpfactor = cr / 2;
		cr -= hpfactor;
		g.maxhp = Math.round(Toughness.HP * (hpfactor / FeatsFactor.CR));
		g.hp = g.maxhp;
		g.heal = hpfactor;
		cr -= g.heal * FastHealing.CR;
		if (cr < 1) {
			return null;
		}
		for (int i = 0; i < 100; i++) {
			float summonchance = RPG.r(50, 100) / 100f;
			int summoncr = Summon.gemonstertcr(Math.round(cr), summonchance)
					/ 5;
			if (summoncr < 1) {
				continue;
			}
			List<Monster> tier = Javelin.MONSTERSBYCR.get(new Float(summoncr));
			if (tier == null) {
				continue;
			}
			g.summon = RPG.pick(tier);
			g.summonchance = summonchance;
			return g;
		}
		return null;
	}

	@Override
	public void act(BattleState s) {
		super.act(s);
		ArenaGateway c = (ArenaGateway) s.clone(this);
		c.summonap += 1;
		c.ap += 1;
		c.hp += c.heal;
		if (c.hp > c.maxhp) {
			c.hp = c.maxhp;
		}
	}

	public static Point place() {
		Point p = null;
		placing: while (p == null) {
			p = new Point(RPG.r(ArenaFight.AREA[0].x, ArenaFight.AREA[1].x),
					RPG.r(ArenaFight.AREA[0].y, ArenaFight.AREA[1].y));
			if (!ArenaFight.validate(p)) {
				p = null;
				continue placing;
			}
			for (Point adjacent : Point.getadjacent()) {
				adjacent.x += p.x;
				adjacent.y += p.y;
				if (!ArenaFight.validate(adjacent)) {
					p = null;
					continue placing;
				}
			}
		}
		return p;
	}

	public static void summon(ArrayList<Combatant> redteam, ArenaFight f) {
		for (Combatant c : new ArrayList<Combatant>(redteam)) {
			ArenaGateway gate = c instanceof ArenaGateway ? (ArenaGateway) c
					: null;
			if (gate != null) {
				gate.summon(f);
			}
		}
	}

	void summon(ArenaFight f) {
		if (summonap < 1) {
			return;
		}
		summonap -= 1;
		if (RPG.random() < summonchance) {
			ArrayList<Combatant> list = new ArrayList<Combatant>(1);
			Combatant c = new Combatant(summon, true);
			c.summoned = true;
			list.add(c);
			f.enter(list, Fight.state.redTeam,
					ArenaFight.displace(getlocation()));
		}
	}
}

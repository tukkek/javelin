package javelin.controller.kit;

import javelin.controller.DamageEffect;
import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.classes.Expert;
import javelin.controller.upgrade.damage.EffectUpgrade;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.skill.Deceitful;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.unique.AssassinsGuild;

public class Assassin extends Kit{
	public static final Kit INSTANCE=new Assassin();

	private Assassin(){
		super("assassin",Expert.SINGLETON,RaiseDexterity.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(Skill.DISGUISE.getupgrade());
		basic.add(Skill.STEALTH.getupgrade());
		basic.add(RaiseCharisma.SINGLETON);
		basic.add(new FeatUpgrade(Deceitful.SINGLETON));
		basic.add(new EffectUpgrade(DamageEffect.POISON));

	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.addAll(h.evil);
		extension.addAll(h.wind);
	}

	@Override
	public boolean allow(int bestability,int secondbest,Monster m){
		return m.alignment.isevil()&&super.allow(bestability,secondbest,m);
	}

	@Override
	public Academy createguild(){
		return new AssassinsGuild();
	}
}
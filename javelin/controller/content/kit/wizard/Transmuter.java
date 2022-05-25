package javelin.controller.content.kit.wizard;

import javelin.controller.content.upgrade.ability.RaiseWisdom;
import javelin.model.unit.abilities.spell.transmutation.ControlWeather;
import javelin.model.unit.abilities.spell.transmutation.Darkvision;
import javelin.model.unit.abilities.spell.transmutation.Fly;
import javelin.model.unit.abilities.spell.transmutation.Longstrider;
import javelin.model.unit.abilities.spell.transmutation.OverlandFlight;
import javelin.model.unit.abilities.spell.transmutation.totem.BearsEndurance;
import javelin.model.unit.abilities.spell.transmutation.totem.BullsStrength;
import javelin.model.unit.abilities.spell.transmutation.totem.CatsGrace;
import javelin.model.unit.abilities.spell.transmutation.totem.EaglesSplendor;
import javelin.model.unit.abilities.spell.transmutation.totem.FoxsCunning;
import javelin.model.unit.abilities.spell.transmutation.totem.OwlsWisdom;

/**
 * Transmutation wizard.
 *
 * @author alex
 */
public class Transmuter extends Wizard{
  /** Singleton instance. */
  public static final Transmuter INSTANCE=new Transmuter();

  Transmuter(){
    super("Transmuter",RaiseWisdom.SINGLETON);
  }

  @Override
  protected void extend(){
    super.extend();
    extension.add(new BearsEndurance());
    extension.add(new BullsStrength());
    extension.add(new CatsGrace());
    extension.add(new EaglesSplendor());
    extension.add(new FoxsCunning());
    extension.add(new OwlsWisdom());
    extension.add(new Darkvision());
    extension.add(new ControlWeather());
    extension.add(new Fly());
    extension.add(new Longstrider());
    extension.add(new OverlandFlight());
  }
}

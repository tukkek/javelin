package javelin.controller.content.quality;

import javelin.Javelin;
import javelin.controller.content.quality.resistance.EnergyResistance;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.attack.expertise.ImprovedGrapple;

public class Constrict extends Quality{
  public Constrict(){
    super("constrict");
  }

  @Override
  public void add(String declaration,Monster m){
    if(Javelin.DEBUG&&declaration.split(" ").length>3)
      throw new RuntimeException(
          "#constrict malformed declaration for "+m+": "+declaration);
    declaration=declaration.substring(declaration.indexOf(" ")+1);
    var damageend=declaration.indexOf(" ");
    if(damageend<0) damageend=declaration.length();
    var damage=declaration.substring(0,damageend);
    m.constrict=new javelin.model.unit.abilities.Constrict();
    m.constrict.damage=parsedamage(damage);
    for(String energy:EnergyResistance.ENERGYTYPES)
      if(declaration.contains(energy)){
        m.constrict.energy=true;
        break;
      }
    m.addfeat(ImprovedGrapple.SINGLETON);
  }

  int parsedamage(String damage){
    var d=damage.indexOf("d");
    var plus=damage.indexOf("+");
    if(plus<0) plus=damage.indexOf("-");
    var end=damage.length();
    var die=Integer.parseInt(damage.substring(0,d));
    var faces=Integer.parseInt(damage.substring(d+1,plus>=0?plus:end));
    var bonus=plus>0?Integer.parseInt(damage.substring(plus,end)):0;
    return Math.max(1,Math.round(die*(faces+1)/2f+bonus));
  }

  @Override
  public boolean has(Monster m){
    return m.constrict!=null;
  }

  @Override
  public float rate(Monster m){
    return m.constrict.damage*.05f;
  }

  @Override
  public String describe(Monster m){
    return m.constrict.toString();
  }
}

package javelin.model.transport;

import java.util.ArrayList;

import javelin.controller.content.terrain.Terrain;
import javelin.model.unit.Combatant;

/** Moves on water and very slowly at land (while it's being transported. */
public class Ship extends Transport{
  /** Constructor. */
  public Ship(){
    super("Ship",100,100,16,10000);
    sails=true;
  }

  @Override
  public int getspeed(ArrayList<Combatant> tripulation){
    var speed=super.getspeed(tripulation);
    return onwater()?speed:speed/10;
  }

  @Override
  public boolean battle(){
    return !onwater();
  }

  boolean onwater(){
    return Terrain.current().equals(Terrain.WATER);
  }
}

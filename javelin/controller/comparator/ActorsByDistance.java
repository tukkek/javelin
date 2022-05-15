package javelin.controller.comparator;

import java.util.Comparator;

import javelin.model.world.Actor;

/**
 * Sorts by distance, closest one will be first.
 *
 * @author alex
 */
public class ActorsByDistance implements Comparator<Actor>{
  final Actor reference;

  /** Constructor. */
  public ActorsByDistance(Actor reference){
    this.reference=reference;
  }

  @Override
  public int compare(Actor o1,Actor o2){
    return Integer.compare(o1.distanceinsteps(reference.x,reference.y),
        o2.distanceinsteps(reference.x,reference.y));
  }
}

package javelin.model.unit;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;

/** List of {@link Monster}s. */
public class Monsters extends ArrayList<Monster>{
  /** Constructor. */
  public Monsters(List<Monster> list){
    super(list);
  }

  /** @see ChallengeCalculator#calculateelfromcrs(List, boolean) */
  public int getel(){
    var crs=stream().map(m->m.cr).toList();
    return ChallengeCalculator.calculateelfromcrs(crs);
  }
}
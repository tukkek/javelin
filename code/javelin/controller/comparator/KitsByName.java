package javelin.controller.comparator;

import java.util.Comparator;

import javelin.controller.content.kit.Kit;

/**
 * @see Kit#name
 * @author alex
 */
public class KitsByName implements Comparator<Kit>{
  /** Singleton. */
  public static final KitsByName INSTANCE=new KitsByName();

  KitsByName(){
    // private constructor
  }

  @Override
  public int compare(Kit o1,Kit o2){
    return o1.name.compareTo(o2.name);
  }
}

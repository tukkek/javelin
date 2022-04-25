package javelin.controller.content.map;

import java.util.ArrayList;
import java.util.Collection;

import javelin.old.RPG;

/**
 * @see #pick()
 *
 * @author alex
 */
public class Maps extends ArrayList<Map>{
  /** Constructor. */
  public Maps(){
    //default
  }

  /** Constructor. */
  public Maps(Collection<? extends Map> c){
    super(c);
  }

  /**
   * @return a random map from this list.
   */
  public Map pick(){
    var clone=new ArrayList<>(this);
    for(Map m:this) if(!m.validate()) clone.remove(m);
    return RPG.pick(clone);
  }
}

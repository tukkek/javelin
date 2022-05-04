package javelin.controller.content.map;

import java.util.ArrayList;
import java.util.Collection;

import javelin.old.RPG;

/**
 * @see #pick()
 *
 * @author alex
 */
public class Maps extends ArrayList<Class<? extends Map>>{
  /** Empty instance. */
  public static final Maps EMPTY=new Maps();

  /** Constructor. */
  public Maps(){
    //default
  }

  /** Constructor. */
  public Maps(Collection<Class<? extends Map>> c){
    super(c);
  }

  /** @return A random, valid item. */
  public Map pick(){
    try{
      for(var type:RPG.shuffle(new ArrayList<>(this))){
        var m=type.getConstructor().newInstance();
        if(m.validate()) return m;
      }
      return null;
    }catch(ReflectiveOperationException e){
      throw new RuntimeException(e);
    }
  }
}

package javelin.model.world.location.town.diplomacy.quest.find;

import java.util.HashSet;

import javelin.model.world.location.ResourceSite;
import javelin.model.world.location.ResourceSite.Resource;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;

/**
 * Connect a {@link Resource} type to a {@link Town}.
 *
 * @see Trait#MERCANTILE
 * @author alex
 */
public class Connect extends FindQuest{
  Resource target=null;

  @Override
  protected void define(Town t){
    super.define(t);
    var r=new HashSet<>(ResourceSite.RESOURCES.values());
    r.removeAll(t.resources);
    if(r.isEmpty()) return;
    var targets=ResourceSite.getall();
    var site=select(targets);
    target=site.type;
    name="Connect %s".formatted(target.name.toLowerCase());
  }

  @Override
  public boolean validate(){
    return super.validate()&&target!=null;
  }

  @Override
  protected boolean complete(){
    return town.resources.contains(target);
  }
}

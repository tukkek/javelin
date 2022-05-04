package javelin.controller.content.map;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.content.map.location.town.TownMap;
import javelin.controller.exception.GaveUp;
import javelin.model.state.Square;
import javelin.old.RPG;

/**
 * Represents a section of a {@link Map}, with one or more {@link #segments}
 * inside it. Useful for creating buildings inside a {@link TownMap},
 * {@link Square#flooded} areas...
 */
public class Section{
  /**
   * A {@link Collection} of {@link Section}s, with helper methods.
   *
   * @author alex
   */
  public static class Sections extends ArrayList<Section>{
    Class<? extends Section> type;
    Map map;

    /** Constructor. */
    public Sections(Class<? extends Section> type,Map m){
      this.type=type;
      map=m;
    }

    boolean grow(Map m){
      for(var s:RPG.shuffle(this)) if(s.grow(m)) return true;
      return false;
    }

    /**
     * Given a number of seed {@link Point}s, expands up to a certain coverage
     * of the given area, creating individual, contigual segments.
     *
     * @throws GaveUp If cannot grow any of the seeds anymore. This method isn't
     *   deterministc so retries are possible but not guaranteed to work.
     */
    public void segment(int seeds,double occupy,Set<Point> area) throws GaveUp{
      try{
        if(area.size()<seeds)
          throw new IllegalArgumentException("Not enough area.");
        var c=type.getConstructor(Point.class);
        for(var d:RPG.shuffle(new ArrayList<>(area)).subList(0,seeds))
          add(c.newInstance(d));
        var target=area.size()*occupy;
        while(stream().mapToInt(s->s.area.size()).sum()<target)
          if(!grow(map)) throw new GaveUp();
      }catch(ReflectiveOperationException e){
        throw new RuntimeException(e);
      }
    }

    /** @return Every {@link Section#area} collected. */
    public Set<Point> getarea(){
      return stream().flatMap(s->s.area.stream()).collect(toSet());
    }
  }

  /** Grows one {@link Point#getadjacent()} at a time rather than all. */
  public static class ThinSection extends Section{
    /** Constructor. */
    public ThinSection(Point p){
      super(p);
    }

    @Override
    List<Point> grow(Point p,Map m){
      var g=super.grow(p,m);
      return g.isEmpty()?g:RPG.shuffle(g).subList(0,1);
    }
  }

  /** Area grown from the initial seed. */
  public HashSet<Point> area=new HashSet<>();
  /** @see #segment(Map, Class) */
  public HashSet<Point> segments=new HashSet<>();

  /** Constructor. */
  public Section(Point p){
    area.add(p);
  }

  List<Point> grow(Point p,Map m){
    return p.getadjacent().stream()
        .filter(next->next.validate(0,0,DndMap.SIZE,DndMap.SIZE)
            &&!m.map[next.x][next.y].flooded&&!area.contains(next))
        .collect(Collectors.toList());
  }

  /**
   * @return <code>false</code> if a new {@link Square} cannot be grown into
   *   (exhaustive).
   */
  public boolean grow(Map m){
    for(var p:RPG.shuffle(new ArrayList<>(area))){
      var adjacent=grow(p,m);
      if(!adjacent.isEmpty()){
        area.addAll(adjacent);
        return true;
      }
    }
    return false;
  }

  /** Creates new {@link Section}s insinde the {@link #area}. */
  public Set<Point> segment(Map m,Class<? extends Section> type) throws GaveUp{
    var sections=new Sections(type,m);
    sections.segment(RPG.r(1,8),.4,area);
    for(var s:sections) segments.addAll(s.area);
    return segments;
  }
}

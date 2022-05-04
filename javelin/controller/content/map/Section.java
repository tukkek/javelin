package javelin.controller.content.map;

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
  /** @see #segment(Map) */
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
  public Set<Point> segment(Map m) throws GaveUp{
    for(var s:Section.segment(RPG.r(1,8),.4,area,m)) segments.addAll(s.area);
    return segments;
  }

  static boolean grow(ArrayList<Section> sections,Map m){
    for(var s:RPG.shuffle(sections)) if(s.grow(m)) return true;
    return false;
  }

  /**
   * Given a number of seed {@link Point}s, occupies at random a certain
   * coverage of the given area, creaitng individual segments.
   *
   * @throws GaveUp If cannot grow any of the seeds anymore. This method isn't
   *   deterministc so retries are possible but not guaranteed to work.
   */
  public static List<Section> segment(int seeds,double occupy,
      Collection<Point> area,Map m,Class<? extends Section> type) throws GaveUp{
    try{
      var sections=new ArrayList<Section>(seeds);
      if(area.size()<seeds) throw new GaveUp();
      var c=type.getConstructor(Point.class);
      for(var d:RPG.shuffle(new ArrayList<>(area)).subList(0,seeds))
        sections.add(c.newInstance(d));
      var target=area.size()*occupy;
      while(sections.stream().mapToInt(s->s.area.size()).sum()<target)
        if(!grow(sections,m)) throw new GaveUp();
      return sections;
    }catch(ReflectiveOperationException e){
      throw new RuntimeException(e);
    }
  }

  /** TODO remove and inline {@link Section} parameter on callers */
  public static List<Section> segment(int seeds,double occupy,
      Collection<Point> area,Map m) throws GaveUp{
    return segment(seeds,occupy,area,m,Section.class);
  }
}

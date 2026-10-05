package javelin.model.world.location.town;

import java.io.Serializable;

/**
 * A representation of {@link Town#population}. Each rank may have its own
 * properties, from a distinct visual icon to {@link District} radius, etc.
 *
 * @author alex
 */
public class Rank implements Serializable{
	/** A very small town. */
	public static final Rank HAMLET=new Rank(1,"Hamlet",1,5);
	/** A small to medium town. */
	public static final Rank VILLAGE=new Rank(2,"Village",6,10);
	/** A medium to big town. */
	public static final Rank TOWN=new Rank(3,"Town",11,15);
	/** A very big town. */
	public static final Rank CITY=new Rank(4,"City",16,Integer.MAX_VALUE);
	/** All Town ranks from smallest to biggest. */
	public static final Rank[] RANKS=new Rank[]{HAMLET,VILLAGE,TOWN,CITY};

	/** Name of this town rank ("Village"). */
	public String title;
	/** Minimum {@link Town#population} included as this rank (inclusive). */
	public int minpopulation;
	/** Maximum {@link Town#population} included as this rank (inclusive). */
	public int maxpopulation;
	/** Numerical rank, from 1 ({@link #HAMLET}) to 4 ({@link #CITY}). */
	public int rank;

	/** See field documentation. */
	Rank(int rank,String name,int minsize,int maxsize){
		title=name;
		minpopulation=minsize;
		maxpopulation=maxsize;
		this.rank=rank;
	}

	@Override
	public String toString(){
		return title;
	}

	/** @return {@link District} radius for this rank. */
	public int getradius(){
		return rank<=2?2:3;
	}
}
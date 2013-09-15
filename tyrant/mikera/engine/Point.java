package tyrant.mikera.engine;


/**
 * Just a simple (x,y) point
 * 
 * Used by maps for passing return values, 
 * e.g. Map.findFreeSquare()
 * 
 * @author Mike
 *
 */
public final class Point implements java.io.Serializable {
	private static final long serialVersionUID = 3256720701829165623L;
    public int x=0;
	public int y=0;

	public Point() {
		// default point is (0,0)
	}

	public Point(int px, int py) {
		x = px;
		y = py;
	}

	public Point(Thing t) {
		x = t.x;
		y = t.y;
	}
	
	/**
	 * Calculate distance based on horizontal/vertical aligned square
	 * @param a point to compare with
	 * @return distance
	 */
	public int hvDistance(Point a) {
		return RPG.max(RPG.abs(x-a.x),RPG.abs(y-a.y));
	}

	public Point(Point p) {
		x = p.x;
		y = p.y;
	}

	public void offset(int dx, int dy) {
		x += dx;
		y += dy;
	}
    
    public String toString() {
        return x + "@" + y;
    }
    
    public static Point randomDirection(boolean diagonals) {
    	if (!diagonals) {
    		return randomDirection4();
    	} 
    	
    	return randomDirection8();
    }
    
    public static Point randomDirection4() {
		int dx = 0;
		int dy = 0;
		switch (RPG.d(4)) {
			case 1 :
				dx = 1;
				break; //W
			case 2 :
				dx = -1;
				break; //E
			case 3 :
				dy = 1;
				break; //N
			case 4 :
				dy = -1;
				break; //S
		}
		return new Point(dx,dy);
    }
    
    public static Point randomDirection8() {
		int dx = 0;
		int dy = 0;
		switch (RPG.d(8)) {
			case 1 :
				dx = 1;
				break; //W
			case 2 :
				dx = -1;
				break; //E
			case 3 :
				dy = 1;
				break; //N
			case 4 :
				dy = -1;
				break; //S
			case 5 :
				dx = 1;
				dy=1;
				break; //W
			case 6 :
				dx = -1;
				dy=-1;
				break; //E
			case 7 :
				dy = 1;
				dx=-1;
				break; //N
			case 8 :
				dy = -1;
				dx=1;
				break; //S
		}
		return new Point(dx,dy);
		 
	}
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point pt = (Point) obj;
            return (x == pt.x) && (y == pt.y);
        }
        return super.equals(obj);
    }    
    
    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(x);
        bits ^= java.lang.Double.doubleToLongBits(y) * 31;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }
}
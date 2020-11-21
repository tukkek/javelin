package javelin.controller.map.location;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.fight.Fight;
import javelin.controller.map.Map;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;

/**
 * A map read and interpreted from a text file on the "maps" folder.
 *
 * @author alex
 */
public class LocationMap extends Map{
	/**
	 * Possible starting positions for the player team. If empty, will be ignored.
	 */
	public ArrayList<Point> spawnblue=new ArrayList<>(0);
	/** Same as {@link #spawnblue} but for enemies. */
	public ArrayList<Point> spawnred=new ArrayList<>(0);

	/** Constructor. */
	public LocationMap(String name){
		super(name,0,0);
	}

	@Override
	public void generate(){
		ArrayList<String> map=new ArrayList<>();
		try{
			String filename=name.replaceAll(" ","").toLowerCase();
			BufferedReader reader=new BufferedReader(
					new FileReader(new File("maps",filename+".txt")));
			for(String line=reader.readLine();line!=null;line=reader.readLine())
				map.add(line);
			reader.close();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		int height=map.get(0).length();
		int width=map.size();
		if(height!=width) throw new RuntimeException(
				"Maps need to be square (same width and height).");
		this.map=new Square[height][width];
		for(int x=0;x<width;x++){
			char[] line=map.get(x).toCharArray();
			for(int y=0;y<line.length;y++){
				var s=new Square();
				this.map[y][x]=s;
				processtile(s,y,x,line[y]);
			}
		}
	}

	protected Square processtile(Square s,int x,int y,char c){
		if(c=='~')
			s.flooded=true;
		else if(c=='#')
			s.blocked=true;
		else if(c=='x')
			s.obstructed=true;
		else if(c=='1')
			spawnblue.add(new Point(x,y));
		else if(c=='2') spawnred.add(new Point(x,y));
		return s;
	}

	@Override
	public List<Point> getspawn(List<Combatant> team){
		return team==Fight.state.redTeam?spawnred:spawnblue;
	}
}

package javelin.controller.walker.pathing;

import java.util.ArrayList;

import javelin.controller.walker.Step;
import javelin.controller.walker.Walker;

public interface Pathing {
	ArrayList<Step> step(int x, int y, ArrayList<Step> steps, Walker w);
}
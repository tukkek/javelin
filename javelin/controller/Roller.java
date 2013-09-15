package javelin.controller;

import java.util.Random;

public class Roller {
	public static final Random RANDOM = new Random();

	public static Integer rollDie(final int i) {
		return RANDOM.nextInt(i) + 1;
	}
}

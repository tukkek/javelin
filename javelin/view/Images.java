package javelin.view;

import java.awt.Image;
import java.awt.MediaTracker;

import tyrant.mikera.tyrant.QuestApp;

/**
 * Index of in-memory image objects.
 * 
 * @author alex
 */
public class Images {

	public static Image guardian;
	public static Image penalized;
	public static Image crafting;
	public static Image banner;
	public static Image upgrading;
	public static Image dead;
	public static Image crystal;
	public static Image hostile;
	public static Image town;
	public static Image dungeon;
	public static Image haxor;
	public static Image lair;
	public static Image outpost;
	public static Image portal;
	public static Image inn;
	public static Image shrine;
	public static Image academy;
	public static Image university;
	public static Image dwelling;
	public static Image arena;

	public static void initimages() {
		Images.penalized = QuestApp.getImage("/images/spiralbig.png");
		Images.crafting = QuestApp.getImage("/images/crafting.png");
		Images.upgrading = QuestApp.getImage("/images/upgrading.png");
		Images.banner = QuestApp.getImage("/images/banner.png");
		Images.dead = QuestApp.getImage("/images/dead.png");
		Images.crystal = QuestApp.getImage("/images/meld.png");
		Images.hostile = QuestApp.getImage("/images/hostile.png");
		Images.town = QuestApp.getImage("/images/town.png");
		Images.dungeon = QuestApp.getImage("/images/dungeon.png");
		Images.haxor = QuestApp.getImage("/images/haxor.png");
		Images.lair = QuestApp.getImage("/images/lair.png");
		Images.outpost = QuestApp.getImage("/images/outpost.png");
		Images.portal = QuestApp.getImage("/images/portal.png");
		Images.inn = QuestApp.getImage("/images/inn.png");
		Images.shrine = QuestApp.getImage("/images/shrine.png");
		Images.guardian = QuestApp.getImage("/images/guardian.png");
		Images.university = QuestApp.getImage("/images/university.png");
		Images.dwelling = QuestApp.getImage("/images/dwelling.png");
		Images.academy = QuestApp.getImage("/images/academy.png");
		Images.arena = QuestApp.getImage("/images/arena.png");
	}

	public static void addimages(final MediaTracker mediaTracker) {
		mediaTracker.addImage(Images.penalized, 1);
		mediaTracker.addImage(Images.crafting, 1);
		mediaTracker.addImage(Images.upgrading, 1);
		mediaTracker.addImage(Images.banner, 1);
		mediaTracker.addImage(Images.dead, 1);
		mediaTracker.addImage(Images.crystal, 1);
		mediaTracker.addImage(Images.hostile, 1);
		mediaTracker.addImage(Images.town, 1);
		mediaTracker.addImage(Images.dungeon, 1);
		mediaTracker.addImage(Images.haxor, 1);
		mediaTracker.addImage(Images.lair, 1);
		mediaTracker.addImage(Images.outpost, 1);
		mediaTracker.addImage(Images.portal, 1);
		mediaTracker.addImage(Images.inn, 1);
		mediaTracker.addImage(Images.shrine, 1);
		mediaTracker.addImage(Images.academy, 1);
		mediaTracker.addImage(Images.university, 1);
		mediaTracker.addImage(Images.dwelling, 1);
		mediaTracker.addImage(Images.arena, 1);
	}
}

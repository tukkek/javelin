package javelin.controller.generator.dungeon.template;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;

public class StaticTemplate extends Template {
	public static class TemplateReader extends SimpleFileVisitor<Path> {
		ArrayList<File> files;

		public TemplateReader(ArrayList<File> files) {
			this.files = files;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			String filename = file.getFileName().toString();
			if (filename.endsWith(".template")) {
				files.add(new File(file.getParent().toString(), filename));
			}
			return super.visitFile(file, attrs);
		}

	}

	public static final ArrayList<Template> CRAWL = new ArrayList<Template>();
	static final HashMap<Character, Character> TRANSLATE = new HashMap<Character, Character>();

	static {
		TRANSLATE.put(' ', WALL);
		TRANSLATE.put('.', FLOOR);
		TRANSLATE.put('#', WALL);

		TRANSLATE.put('+', FLOOR); // TODO cant handle custom door yet;
		TRANSLATE.put('~', FLOOR); // TODO water;
		TRANSLATE.put('!', FLOOR); // TODO decotartion

		ArrayList<File> files = new ArrayList<File>(300);
		try {
			Files.walkFileTree(Paths.get("maps/templates/crawl"),
					new TemplateReader(files));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		for (File f : files) {
			StaticTemplate t = new StaticTemplate(f);
			t.generate();
			if (validate(t)) {
				t.create();
				CRAWL.add(t);
			} else {
				System.err.println("Failed to load static template: " + t.name);
			}
		}
	}

	char[][] original;
	String name;

	public StaticTemplate(File file) {
		name = file.toString();
		String[] map = read(file).split("\n");
		if (map.length == 0) {
			original = null;
			return;
		}
		original = new char[map.length][];
		for (int i = 0; i < map.length; i++) {
			original[i] = map[i].toCharArray();
		}
		height = original[0].length;
		if (height == 0) {
			original = null;
			return;
		}
		for (char[] line : original) {
			if (line.length != height) {
				original = null;
				return;
			}
			for (int y = 0; y < line.length; y++) {
				line[y] = TRANSLATE.get(line[y]);
			}
		}
	}

	@Override
	public void generate() {
		tiles = original;
		if (tiles != null) {
			width = tiles.length;
			height = tiles[0].length;
		}
	}

	public String read(File file) {
		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				if (!line.trim().isEmpty()) {
					builder.append(line + "\n");
				}
			}
			reader.close();
			return builder.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static boolean validate(StaticTemplate t) {
		if (t.original == null || t.original.length == 0) {
			return false;
		}
		int size = t.original.length * t.original[0].length;
		if (!(3 <= size && size < 42)) {
			return false;
		}
		for (char[] line : t.original) {
			if (line.length != t.height) {
				return false;
			}
		}
		// if (t.name.equals("maps/templates/crawl/traps/grate4.template")) {
		// System.out.println("debug");
		// }
		if (t.count(WALL) == size) {
			return false;
		}
		return true;
	}

	@Override
	public void close() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
					if (tiles[x][y] != WALL) {
						super.close();
						return;
					}
				}
			}
		}
	}
}

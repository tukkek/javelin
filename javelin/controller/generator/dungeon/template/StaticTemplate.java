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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import javelin.controller.exception.GaveUp;
import tyrant.mikera.engine.RPG;

public class StaticTemplate extends Template {
	public static final boolean DEBUG = false;
	public static final StaticTemplate FACTORY = new StaticTemplate();

	static final ArrayList<StaticTemplate> STATIC = new ArrayList<StaticTemplate>();
	static final HashMap<Character, Character> TRANSLATE = new HashMap<Character, Character>();

	static {
		TRANSLATE.put(' ', WALL);
		TRANSLATE.put('.', FLOOR);
		TRANSLATE.put('#', WALL);

		TRANSLATE.put('+', FLOOR); // TODO cant handle custom door yet;
		TRANSLATE.put('~', FLOOR); // TODO water;
		TRANSLATE.put('!', FLOOR); // TODO decotartion
	}

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

	boolean factory = false;
	char[][] original;
	String name;

	public static void load() {
		if (!STATIC.isEmpty()) {
			return;
		}
		ArrayList<String> errors = new ArrayList<String>();
		ArrayList<File> files = new ArrayList<File>(300);
		try {
			Files.walkFileTree(Paths.get("maps/templates/"),
					new TemplateReader(files));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		for (File f : files) {
			StaticTemplate t = new StaticTemplate(f);
			if (t.original == null) {
				continue;
			}
			StaticTemplate clone = (StaticTemplate) t.create();
			if (clone == null) {
				errors.add("rm " + t.name);
			} else {
				STATIC.add(clone);
			}
		}
		if (DEBUG) {
			Collections.shuffle(errors);
			for (String error : errors) {
				System.err.println(error);
			}
			System.out.println("Errors " + errors.size());
			System.out.println("Loaded " + STATIC.size());
		}
	}

	private StaticTemplate(File file) {
		name = file.toString();
		String content = read(file);
		while (content.indexOf("\n\n") >= 0) {
			content = content.replaceAll("\n\n", "\n");
		}
		if (content.endsWith("\n")) {
			content = content.substring(0, content.length() - 1);
		}
		String[] map = content.split("\n");
		if (map.length == 0) {
			original = null;
			return;
		}
		width = map.length;
		original = new char[map.length][];
		for (int i = 0; i < map.length; i++) {
			original[i] = map[i].toCharArray();
			height = Math.max(original[i].length, height);
		}
		int area = width * height;
		if (!(9 <= area && area <= 100)) {
			original = null;
			return;
		}
		for (int i = 0; i < original.length; i++) {
			original[i] = Arrays.copyOf(original[i], height);
			for (int y = 0; y < height; y++) {
				Character c = TRANSLATE.get(original[i][y]);
				original[i][y] = c == null ? WALL : c;
			}
		}
	}

	private StaticTemplate() {
		// just used to deliver other instances
		factory = true;
	}

	@Override
	public void generate() {
		tiles = original;
		if (tiles != null) {
			width = tiles.length;
			height = tiles[0].length;
		}
		try {
			if (!validatestatic()) {
				original = null;
			}
		} catch (GaveUp e) {
			original = null;
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

	@Override
	protected boolean validate() throws GaveUp {
		if (validatestatic() && super.validate()) {
			return true;
		}
		throw new GaveUp();
	}

	boolean validatestatic() throws GaveUp {
		if (original == null || tiles == null || original.length == 0) {
			return false;
		}
		int size = width * height;
		// if (!(9 <= size && size < 200)) {
		// return false;
		// }
		for (int i = 1; i < original.length; i++) {
			char[] line = original[i];
			if (line.length != original[0].length) {
				return false;
			}
		}
		if (count(WALL) == size) {
			return false;
		}
		return true;
	}

	@Override
	void makedoors() throws GaveUp {
		if (original != null) {
			super.makedoors();
		}
	}

	@Override
	public Template create() {
		return factory ? RPG.pick(STATIC).create() : super.create();
	}
}

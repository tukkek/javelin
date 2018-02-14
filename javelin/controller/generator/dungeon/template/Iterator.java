package javelin.controller.generator.dungeon.template;

public interface Iterator {
	public class TemplateTile {
		public int x;
		public int y;
		public char c;

		public TemplateTile(int x, int y, char c) {
			this.x = x;
			this.y = y;
			this.c = c;
		}
	}

	void iterate(TemplateTile t);
}

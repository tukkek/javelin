package tyrant.mikera.tyrant.perf;

import tyrant.mikera.engine.Lib;

public class Lib_Perf extends Perf {
	public Lib_Perf() {
		// empty
	}

	public static void main(final String[] args) {
		new Lib_Perf().go(args);
	}

	public void go(String[] args) {
		final IWork work;

		if (args == null || args.length == 0) {
			args = new String[] { KillAllBaddies.class.getName(), "10" };
		}
		if (args.length != 2) {
			printUsage();
			return;
		}

		work = findWork(args[0]);
		oneLineSummary(shortName(work.getClass()), work.getMessage(),
				timeToRun(work, Integer.parseInt(args[1])));
	}

	private String shortName(final Class aClass) {
		final String name = aClass.getName();
		final int lastDot = name.lastIndexOf('.');
		return name.substring(lastDot + 1);
	}

	private void printUsage() {
		System.out.println("Usage java " + Lib_Perf.class.getName()
				+ " 'name of work' iterations");
		System.out.println("Example: " + Lib_Perf.class.getName() + " "
				+ KillAllBaddies.class.getName() + " 10");
		System.out.println("This will execute KillAllBaddies 10 times.");
	}

	private IWork findWork(final String className) {
		try {
			return (IWork) Class.forName(className).newInstance();
		} catch (final ClassNotFoundException cnfe) {
			final Class theClass = KillAllBaddies.class;
			System.out.println("Class " + className + " not found, using "
					+ theClass.getName() + " instead.");
			try {
				return (IWork) theClass.newInstance();
			} catch (final Exception e1) {
				e1.printStackTrace();
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected Runnable createLib() {
		final Runnable work = new Runnable() {
			public void run() {
				Lib.clear();
				Lib.instance();
			}
		};
		return work;
	}
}

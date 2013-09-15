package tyrant.mikera.tyrant.perf;

import java.text.NumberFormat;

import tyrant.mikera.engine.BaseObject;
import tyrant.mikera.tyrant.util.PrintfFormat;


public class Perf {

    public Perf() {
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setGroupingUsed(false);
    }

    protected NumberFormat numberFormat;

    protected void oneLineSummary(String className, String message, WorkDone[] workFinished) {
        long sum = 0;
        long sumOfSquares = 0;
        for (int i = 0; i < workFinished.length; i++) {
            WorkDone done = workFinished[i];
            sum += done.elapsed();
            sumOfSquares += done.elapsed() * done.elapsed();
        }
        double standardDeviation = Math.sqrt(((float) workFinished.length * sumOfSquares - sum * sum) / (workFinished.length * workFinished.length));
        PrintfFormat printf = new PrintfFormat("%-15s %-15s Runs: %5d     Avg: %5.2fms     Std: %5.2fms     Optimize: %s");
        float average = sum / workFinished.length;
        System.out.println(printf.sprintf(new Object[] {className, message, new Integer(workFinished.length), 
                new Float(average), new Float(standardDeviation), Boolean.valueOf(BaseObject.OPTIMIZE)}));
    }

    protected WorkDone[] timeToRun(IWork work, int iterations) {
        WorkDone[] done = new WorkDone[iterations];
        for (int i = 0; i < done.length; i++) {
            work.setUp();
            WorkDone timed = new WorkDone();
            done[i] = timed;
            timed.execute(work);
//            System.out.println(timed.elapsed() + "ms");
        }
        return done;
    }

    public class WorkDone {
        private long start;
        private long end;
        private long elapsed = -1;

        public long elapsed() {
            if (elapsed == -1) elapsed = end - start;
            return elapsed;
        }

        void execute(Runnable work) {
            start = System.currentTimeMillis();
            try {
                work.run();
            } finally {
                end = System.currentTimeMillis();
            }
        }
    }
}
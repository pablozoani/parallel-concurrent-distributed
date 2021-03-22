package tools;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

public class EvaluationTools {

    private static int step = 1;

    public static void warmup() {
        for (int j = 0; j < 100_000_000; j++) Math.random();
    }

    public static <T> EvaluationResult<T> evaluate(
        final String title,
        final int size,
        final Supplier<T> evaluation
    ) {
        printStep(title);
        final StepTimer stepTimer = new StepTimer();
        stepTimer.start();
        final T result = evaluation.get();
        final long time = stepTimer.finish();
        EvaluationResult<T> output = new EvaluationResult<>(title, size, result, time);
        printResult(output);
        return output;
    }

    public static <T> ResultTuple<T> evaluateBoth(
        final String title1,
        final int size1,
        final Supplier<T> evaluation1,
        final String title2,
        final int size2,
        final Supplier<T> evaluation2
    ) {
        final EvaluationResult<T> resultA = evaluate(title1, size1, evaluation1);
        final EvaluationResult<T> resultB = evaluate(title2, size2, evaluation2);
        printTimeRatio(resultA.time, resultB.time);
        return new ResultTuple(resultA, resultB);
//        return Arrays.asList(resultA, resultB);
    }

    /**
     * Creates an array of doubles from 1 to arrayLength inclusive.
     *
     * @param arrayLength The length of the array.
     * @return A populated array of doubles.
     */
    public static double[] createRangeArray(final int arrayLength) {
        final double[] output = new double[arrayLength];
        for (int i = 0; i < arrayLength; i++) output[i] = i + 1.0;
        return output;
    }

    public static <T> void printEvaluationResults(List<ResultTuple<T>> results) {
        for (int i = 0; i < results.size(); i++) {
            printer().printf(
                "%02d - At %11.3fms parallel speed is %7.2f%%%n",
                i + 1,
                results.get(i).A.time / 1e6,
                100 * results.get(i).A.time / (double) results.get(i).B.time
            );
        }
    }

    /**
     * Prints the results of a computation.
     *
     * @param title     Computation name.
     * @param size      Size of the computation.
     * @param timeNanos Elapsed time in nanoseconds.
     */
    public static void printResult(String title, int size, long timeNanos) {
        System.out.printf(
            "%s of size %,d - elapsed time: %4.4fms%n",
            title, size, timeNanos / 1e6
        );
    }

    public static void printResult(EvaluationResult<?> result) {
        printResult(result.title, result.size, result.time);
    }

    public static void printTimeRatio(final long time1, final long time2) {
        String s = String.format(
            "time 1: %4.4fms, time 2: %4.4fms, ratio: %1.2f - parallel speed is %.2f%%%n",
            time1 / 1e6, time2 / 1e6,
            time1 / (double) time2,
            100 * time1 / (double) time2
        );
        printer().print("\n\n");
        for (int i = 0; i < s.length(); i++) {
            System.out.print("-");
        }
        printer().print("\n");
        System.out.print(s);
        printer().print("\n\n");
    }

    /**
     * @return the standard output stream.
     */
    public static PrintStream printer() {
        return System.out;
    }

    public static int max(int... nrs) {
        if (nrs.length < 1) throw new RuntimeException("nrs.length < 1");
        int out = nrs[0];
        for (int i = 1; i < nrs.length; i++) if (nrs[i] > out) out = nrs[i];
        return out;
    }

    public static int maxlen(double[]... nrs) {
        if (nrs.length < 1) throw new RuntimeException("nrs.length < 1");
        int out = nrs[0].length;
        for (int i = 1; i < nrs.length; i++)
            if (nrs[i].length > out) out = nrs[i].length;
        return out;
    }

    /**
     * Get the reciprocal sum of all the inputs in the specified sub array
     *
     * @param inputs              The array with values to be summed.
     * @param startIndexInclusive Beginning of the sub array.
     * @param endIndexExclusive   End of the sub array.
     * @return The sum of the elements in the specified range.
     */
    public static double sequentialReciprocalArraySum(final double[] inputs,
        int startIndexInclusive, int endIndexExclusive
    ) {
        double output = 0;
        for (int i = startIndexInclusive; i < endIndexExclusive; i++)
            output += 1 / inputs[i];
        return output;
    }

    /**
     * Print a step with a title.
     *
     * @param stepTitle The title of the step.
     */
    public static void printStep(String stepTitle) {
        System.out.println();
        String toPrint = step++ + " - " + stepTitle;
        printlnOf('-', toPrint.length());
        System.out.println(toPrint);
        printlnOf('-', toPrint.length());
    }

    public static void printlnOf(char c, int length) {
        for (int i = 0; i < length; i++) System.out.print(c);
        System.out.println();
    }

    /**
     * Computes the size of each chunk, given the number of chunks to create
     * across a given number of elements.
     *
     * @param nChunks The number of chunks to create
     * @param nElements The number of elements to chunk across
     * @return The default chunk size
     */
    public static int getChunkSize(final int nChunks, final int nElements) {
        return (nElements + nChunks - 1) / nChunks;
    }

    /**
     * Computes the inclusive element index that the provided chunk starts at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the start of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The inclusive index that this chunk starts at in the set of
     *         nElements
     */
    public static int getChunkStartInclusive(
        final int chunk,
        final int nChunks,
        final int nElements
    ) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }
    
    /**
     * Computes the exclusive element index that the provided chunk ends at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the end of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The exclusive end index for this chunk
     */
    public static int getChunkEndExclusive(
        final int chunk,
        final int nChunks,
        final int nElements
    ) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }

    public static final class EvaluationResult<T> {
        public final String title;
        public final int size;
        public final T result;
        public final long time;

        public EvaluationResult(
            String title,
            int size,
            T result,
            long time
        ) {
            this.title = title;
            this.size = size;
            this.result = result;
            this.time = time;
        }
    }

    public static class ResultTuple<T> {
        public final EvaluationResult<T> A;
        public final EvaluationResult<T> B;
        public ResultTuple(EvaluationResult<T> A, EvaluationResult<T> B) {
            this.A = A;
            this.B = B;
        }
    }

    public static <T> ResultTuple<T> averageResults(
        Supplier<ResultTuple<T>> supplier,
        final int nIterations
    ) {
        if (nIterations < 1) throw new RuntimeException("At least one iteration is needed");
        ResultTuple<T> out = null;
        long timeA = 0, timeB = 0;
        for (int i = 0; i < nIterations; i++) {
            ResultTuple<T> temp = supplier.get();
            if (out != null)
                if (!temp.A.title.equals(out.A.title) || !temp.B.title.equals(out.B.title) ||
                    temp.A.size != out.A.size || temp.B.size != out.B.size
                ) throw new RuntimeException("Inconsistent result inputs");
            out = temp;
            timeA += out.A.time;
            timeB += out.B.time;
        }
        timeA /= nIterations;
        timeB /= nIterations;
        return new ResultTuple<>(
            new EvaluationResult<>(out.A.title, out.A.size, out.A.result, timeA),
            new EvaluationResult<>(out.B.title, out.B.size, out.B.result, timeB)
        );
    }

    public static class StepTimer {

        private long startTime;

        public void start() {
            startTime = System.nanoTime();
        }

        public long finish() {
            return System.nanoTime() - startTime;
        }
    }

    public static void printNumberOfThreads(int nThreads) {
        printer().println("number of processors available: " + nThreads);
    }

    public static void printNumberOfThreads() {
        printNumberOfThreads(Runtime.getRuntime().availableProcessors());
    }

    public static void printCurrentThread() {
        printer().println("current thread is: " + Thread.currentThread().getName());
    }

    public static void warmupEnd() {
        String toPrint = "WARM UP END";
        printlnOf('*', toPrint.length());
        printer().println(toPrint);
        printlnOf('*', toPrint.length());
    }
}

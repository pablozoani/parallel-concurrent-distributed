package lectures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static tools.EvaluationTools.*;

public class Lecture2_1 {

//    number of processors available: 4
//        01 - At     125.017ms parallel speed is  119.23%
//        02 - At     233.114ms parallel speed is  151.97%
//        03 - At     440.918ms parallel speed is  184.59%
//        04 - At     822.447ms parallel speed is  198.83%
//        05 - At    1643.016ms parallel speed is  198.73%
    public static void main(String[] args) { // -ea -Xms4g -Xmx4g
        final int nThreads = Runtime.getRuntime().availableProcessors();
        final int scale = 20_000_000;
        final int nIterations = 1;
        List<ResultTuple<Double>> resultTuples = Arrays.asList(
            averageResults(() -> run(1 * scale, nThreads), nIterations),
            averageResults(() -> run(2 * scale, nThreads), nIterations),
            averageResults(() -> run(4 * scale, nThreads), nIterations),
            averageResults(() -> run(8 * scale, nThreads), nIterations),
            averageResults(() -> run(16 * scale, nThreads), nIterations)
        );
        printNumberOfThreads(nThreads);
        printEvaluationResults(resultTuples);
    }

    /**
     * @param size number of elements to evaluate.
     * @return ratio of the two elapsed times.
     */
    public static ResultTuple<Double> run(final int size, final int nThreads) {
        final ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        final double[] doubles = createRangeArray(size);

        final ResultTuple<Double> resultTuple = evaluateBoth(
            "Sequential", size, () -> sequentialReciprocalArraySum(doubles, 0, doubles.length),
            "Parallel", size, () -> {
                final int nChunks = nThreads;
                final int chunkSize = doubles.length / nChunks;
                final List<Future<Double>> futures = new ArrayList<>();
                for (int i = 0; i < nChunks; i++) {
                    final int chunkNumber = i;
                    final Future<Double> future = executorService.submit(() ->
                        sequentialReciprocalArraySum(
                            doubles,
                            chunkSize * chunkNumber,
                            chunkSize * (chunkNumber + 1)
                        )
                    );
                    futures.add(future);
                }
                double result2 = 0;
                try {
                    for (Future<Double> f : futures) result2 += f.get();
                    return result2;
                } catch (ExecutionException | InterruptedException exc) {
                    throw new RuntimeException(exc);
                }
            }
        );

        executorService.shutdown();
        return resultTuple;
    }
}

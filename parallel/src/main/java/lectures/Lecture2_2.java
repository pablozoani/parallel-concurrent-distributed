package lectures;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import static tools.EvaluationTools.*;

public class Lecture2_2 {
//    number of processors available: 4
//        01 - At     122.188ms parallel speed is   73.27%
//        02 - At     228.708ms parallel speed is  125.72%
//        03 - At     443.766ms parallel speed is  184.54%
//        04 - At     823.329ms parallel speed is  187.74%
//        05 - At    1645.419ms parallel speed is  193.46%
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

    public static ResultTuple<Double> run(int size, final int parallelism) {
        final double[] inputs = createRangeArray(size);
        ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism);

        ResultTuple<Double> resultTuple = evaluateBoth(
            "Sequential", size, () -> sequentialReciprocalArraySum(inputs, 0, inputs.length),
            "Parallel", size, () -> forkJoinPool.invoke(new ReciprocalArraySumTask(inputs, 0, inputs.length))
        );

        forkJoinPool.shutdown();
        return resultTuple;
    }

    public static class ReciprocalArraySumTask extends RecursiveTask<Double> {

        private static final int SEQUENTIAL_THRESHOLD = 1000;

        private final double[] inputs;

        private final int startIndexInclusive;

        private final int endIndexExclusive;

        public ReciprocalArraySumTask(
            double[] inputs,
            int startIndexInclusive,
            int endIndexExclusive
        ) {
            this.inputs = inputs;
            this.startIndexInclusive = startIndexInclusive;
            this.endIndexExclusive = endIndexExclusive;
        }

        @Override
        protected Double compute() {
            if (endIndexExclusive - startIndexInclusive <= SEQUENTIAL_THRESHOLD)
                return sequentialReciprocalArraySum(
                    inputs,
                    startIndexInclusive,
                    endIndexExclusive
                );
            int mid = (startIndexInclusive + endIndexExclusive) / 2;
            ReciprocalArraySumTask left = new ReciprocalArraySumTask(
                inputs,
                startIndexInclusive,
                mid
            );
            ReciprocalArraySumTask right = new ReciprocalArraySumTask(
                inputs,
                mid,
                endIndexExclusive
            );
            left.fork();
            return right.compute() + left.join();
        }
    }
}

package lectures;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static tools.EvaluationTools.*;

public class Lecture2_3 {

//    number of processors available: 4
//        01 - At      17.818ms parallel speed is  159.04%
//        02 - At      18.425ms parallel speed is  207.09%
//        03 - At      36.949ms parallel speed is  141.45%
//        04 - At     136.661ms parallel speed is  167.01%
//        05 - At     314.047ms parallel speed is  215.36%
//        06 - At     653.008ms parallel speed is  197.75%
//        07 - At    1665.374ms parallel speed is  194.97%
    public static void main(String[] args) { // -ea -Xms4g -Xmx4g
        final int nThreads = Runtime.getRuntime().availableProcessors();
        final int scale = 40_000;
        final int nIterations = 3;
        List<ResultTuple<Double>> resultTuples = Arrays.asList(
            averageResults(() -> run(1 * scale), nIterations),
            averageResults(() -> run(2 * scale), nIterations),
            averageResults(() -> run(4 * scale), nIterations),
            averageResults(() -> run(8 * scale), nIterations),
            averageResults(() -> run(16 * scale), nIterations),
            averageResults(() -> run(32 * scale), nIterations),
            averageResults(() -> run(64 * scale), nIterations)
        );
        printNumberOfThreads(nThreads);
        printEvaluationResults(resultTuples);
    }

    public static ResultTuple<Double> run(int size) {
        ResultTuple<Double> output = evaluateBoth(
            "Sequential", size, () -> IntStream
                .range(1, size)
                .filter(i -> isPrime(i))
                .mapToDouble(i -> 1.0 / i)
                .reduce(0, (a, b) -> a + b),
            "Parallel", size, () -> IntStream
                .range(1, size)
                .parallel()
                .filter(i -> isPrime(i))
                .mapToDouble(i -> 1.0 / i)
                .reduce(0, (a, b) -> a + b)
        );
        return output;
    }

    private static boolean isPrime(int i) {
        if (i <= 1) return false;
        for (int j = 2; j <= Math.sqrt(i); j++) if (i % j == 0) return false;
        return true;
    }
}

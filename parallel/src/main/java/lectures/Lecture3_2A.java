package lectures;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static tools.EvaluationTools.*;
import static tools.MatrixTools.*;

public class Lecture3_2A {

//    number of processors available: 4
//        01 - At      17.439ms parallel speed is   50.12%
//        02 - At      15.981ms parallel speed is   80.35%
//        03 - At      68.808ms parallel speed is  124.48%
//        04 - At     702.926ms parallel speed is  208.62%
//        05 - At    8227.173ms parallel speed is  205.39%
    public static void main(String[] args) { // -ea -Xms4g -Xmx4g
        final int nThreads = Runtime.getRuntime().availableProcessors();
        final int scale = 50;
        final int nIterations = 1;
        List<ResultTuple<double[][]>> resultTuples = Arrays.asList(
            averageResults(() -> run(1 * scale), nIterations),
            averageResults(() -> run(2 * scale), nIterations),
            averageResults(() -> run(4 * scale), nIterations),
            averageResults(() -> run(8 * scale), nIterations),
            averageResults(() -> run(16 * scale), nIterations)
        );
        printNumberOfThreads(nThreads);
        printEvaluationResults(resultTuples);
    }

    public static ResultTuple<double[][]> run(final int size) {
        final double[][] inputs = createRangeSquareMatrix(size);

        final ResultTuple<double[][]> resultTuple = evaluateBoth(
            "Sequential", size, () -> productOf(inputs, inputs),
            "Parallel", size, () -> {
                final double[][] output = resultMatrixOf(inputs, inputs);
                IntStream.range(0, heightOf(output))
                    .parallel()
                    .forEach(i -> IntStream.range(0, widthOf(output))
                        .parallel()
                        .forEach(j -> computeMatrixCell(inputs, inputs, i, j, output))
                    );
                return output;
            }
        );
        if (!matrixEquals(resultTuple.A.result, resultTuple.B.result))
            throw new RuntimeException("Inconsistent output!");

        return resultTuple;
    }

}

package lectures;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import static tools.EvaluationTools.*;
import static tools.MatrixTools.*;

public class Lecture3_2B {

//    number of processors available: 4
//        01 - At       7.361ms parallel speed is   16.19%
//        02 - At      12.699ms parallel speed is   23.22%
//        03 - At     124.396ms parallel speed is  161.22%
//        04 - At     882.458ms parallel speed is  290.94%
//        05 - At    8347.422ms parallel speed is  201.81%
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
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        final ForkJoinPool forkJoinPool = new ForkJoinPool(availableProcessors);
        final double[][] inputs = createRangeSquareMatrix(size);
        ResultTuple<double[][]> resultTuple = evaluateBoth(
            "sequential", size, () -> {
                final double[][] output = productOf(inputs, inputs);
                return output;
            },
            "parallel", size, () -> {
                final double[][] output = resultMatrixOf(inputs, inputs);
                final MatrixMultiplication matrixMultiplication = new MatrixMultiplication(
                    inputs,
                    inputs,
                    output,
                    availableProcessors
                );
                forkJoinPool.invoke(matrixMultiplication);
                return output;
            }
        );
        if (!matrixEquals(resultTuple.A.result, resultTuple.A.result))
            throw new RuntimeException("Inconsistent output!");
        forkJoinPool.shutdown();
//        printMatrix(output);
        return resultTuple;
    }

    public static class MatrixMultiplication extends RecursiveAction {

        private final double[][] A, B, C;

        private final int i, j;

        private final int numberOfTasks;

        public MatrixMultiplication(
            final double[][] A,
            final double[][] B,
            final double[][] C,
            final int numberOfTasks
        ) {
            if (numberOfTasks < 1)  throw new IllegalArgumentException();
            this.i = 0;
            this.j = 0;
            this.A = A;
            this.B = B;
            this.C = C;
            this.numberOfTasks = numberOfTasks;
        }

        private MatrixMultiplication(
            final double[][] A,
            final double[][] B,
            final int i,
            final int j,
            final double[][] C
        ) {
            this.A = A;
            this.B = B;
            this.i = i;
            this.j = j;
            this.numberOfTasks = 0;
            this.C = C;
        }

        @Override
        public void compute() {
            if (numberOfTasks == 0) {
                computeMatrixCell(A, B, i, j, C);
            } else {
                final int height = heightOf(A);
                final int width = widthOf(B);
                MatrixMultiplication[] tasks = new MatrixMultiplication[height * width];
                int counter = 0;
                for (int i = 0; i < height; i++)
                    for (int j = 0; j < width; j++)
                        tasks[counter++] = new MatrixMultiplication(A, B, i, j, C);
                invokeAll(tasks);
            }
        }
    }
}

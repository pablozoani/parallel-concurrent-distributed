package cases;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static tools.EvaluationTools.*;

public class WithCompletableFutures {

    public static void main(String[] args) {
        run(10_000_000);
        warmupEnd();
        var results = Arrays.asList(
            averageResults(() -> run(200_000_000), 1)
        );
        printNumberOfThreads();
        printEvaluationResults(results);
    }

    public static ResultTuple<double[]> run(final int size) {
        final double power = Math.PI;

        ResultTuple<double[]> resultTuple = evaluateBoth(
            "Sequential", size, () -> {
                double[] toBeRaised = createRangeArray(size);
                for (int i = 0; i < toBeRaised.length; i++)
                    toBeRaised[i] = Math.pow(toBeRaised[i], power);
                return toBeRaised;
            },
            "Parallel", size, () -> {
                double[] toBeRaised = createRangeArray(size);
                final int numberOfElements = toBeRaised.length;
                final int numberOfChunks = Runtime.getRuntime().availableProcessors();
                parallelLoop(
                    numberOfElements,
                    numberOfChunks,
                    i -> toBeRaised[i] = Math.pow(toBeRaised[i], power)
                );
                return toBeRaised;
            }
        );
        return resultTuple;
    }

    public static void parallelLoop(
        final int numberOfElements,
        final int numberOfChunks,
        final Consumer<Integer> loopBody
    ) {
        parallelLoop0(numberOfElements, 0, numberOfChunks, loopBody);
    }

    private static void parallelLoop0(
        final int numberOfElements,
        final int chunkNumber,
        final int numberOfChunks,
        final Consumer<Integer> loopBody
    ) {
        printCurrentThread();
        final int nextChunkNumber = chunkNumber + 1;
        final var future = nextChunkNumber >= numberOfChunks ? null :
            CompletableFuture.runAsync(() -> parallelLoop0(
                numberOfElements,
                nextChunkNumber,
                numberOfChunks,
                loopBody
            ));
        final int startIndexInclusive = chunkNumber * numberOfElements / numberOfChunks;
        final int endIndexExclusive = Math.min(
            nextChunkNumber * numberOfElements / numberOfChunks,
            numberOfElements
        );
        for (int i = startIndexInclusive; i < endIndexExclusive; i++) loopBody.accept(i);
        if (future != null) future.join();
    }
}

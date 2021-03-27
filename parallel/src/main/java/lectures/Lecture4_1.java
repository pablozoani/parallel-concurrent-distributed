package lectures;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

import static tools.EvaluationTools.*;

public class Lecture4_1 {

    public static void main(String[] args) {
        run(250_000_000);
    }

    public static ResultTuple<double[]> run(final int size) {
        final int nTasks = Runtime.getRuntime().availableProcessors();
        final ResultTuple<double[]> resultTuple = evaluateBoth(
            "Sequential", size, () -> {
                final double[] output = createRangeArray(size);
                for (int j = 0; j < output.length; j++)
                    for (int k = 0; k < 20; k++)
                        output[j] = output[j] * output[j] / 2.0;
                return output;
            },
            "Parallel", size, () -> {
                final double[] output = new double[size];
                inPhases(nTasks,
                    i -> {
                        final int start = i * output.length / nTasks;
                        final int end = (i + 1) * output.length / nTasks;
                        System.out.println(
                            "PHASE A " + i + " - start: " + start + " - end: " + end +
                                " - Thread: " + Thread.currentThread().getName()
                        );
                        for (int j = start; j < end; j++) output[j] = j + 1.0;
                    },
                    i -> {
                        final int start = i * output.length / nTasks;
                        final int end = (i + 1) * output.length / nTasks;
                        System.out.println(
                            "PHASE B " + i + " - start: " + start + " - end: " + end +
                            " - Thread: " + Thread.currentThread().getName()
                        );
                        for (int j = start; j < end; j++)
                            for (int k = 0; k < 20; k++)
                                output[j] = output[j] * output[j] / 2.0;
                    }
                );
                return output;
            }
        );
        for (int i = 0; i < resultTuple.A.result.length; i++)
            if (resultTuple.A.result[i] != resultTuple.B.result[i])
                throw new RuntimeException("Inconsistent outputs");
        return resultTuple;
    }

    @SafeVarargs
    public static void inPhases(final int n, final Consumer<Integer>... phases) {
        assert n > 0;
        final Phaser phaser = new Phaser(n);
        inPhases0(0, n, phaser, phases);
    }

    private static void inPhases0(
        final int index,
        final int n,
        final Phaser phaser,
        final Consumer<Integer>[] phases
    ) {
        if (index >= n) return;
        final int nextIndex = index + 1;
        final CompletableFuture<Void> completableFuture = nextIndex < n ?
            CompletableFuture.runAsync(() -> inPhases0(nextIndex, n, phaser, phases)) : null;
        if (phases.length > 0) phases[0].accept(index);
        for (int i = 1; i < phases.length; i++) {
            phaser.arriveAndAwaitAdvance();
            phases[i].accept(index);
        }
        if (completableFuture != null) completableFuture.join();
    }
}

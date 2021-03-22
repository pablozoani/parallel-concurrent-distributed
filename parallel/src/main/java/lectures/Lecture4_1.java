package lectures;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

import static tools.EvaluationTools.*;

public class Lecture4_1 {

    public static void main(String[] args) {
        run(20);
    }

    public static ResultTuple<double[]> run(final int size) {
        final int nTasks = Runtime.getRuntime().availableProcessors();
        final ResultTuple<double[]> resultTuple = evaluateBoth(
            "Sequential", size, () -> {
                final double[] output = createRangeArray(size);
                for (int j = 0; j < output.length; j++) output[j] = 1.0 / output[j];
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
                        for (int j = start; j < end; j++) output[j] = j + 1;
                    },
                    i -> {
                        final int start = i * output.length / nTasks;
                        final int end = (i + 1) * output.length / nTasks;
                        System.out.println(
                            "PHASE B " + i + " - start: " + start + " - end: " + end +
                            " - Thread: " + Thread.currentThread().getName()
                        );
                        for (int j = start; j < end; j++) output[j] = 1.0 / output[j];
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
        final CompletableFuture<Void> completableFuture = index + 1 < n ?
            CompletableFuture.runAsync(() -> inPhases0(index + 1, n, phaser, phases)) : null;
        if (phases.length > 0) phases[0].accept(index);
        for (int i = 1; i < phases.length; i++) {
            phaser.arriveAndAwaitAdvance();
            phases[i].accept(index);
        }
        phaser.arriveAndDeregister();
        if (completableFuture != null) completableFuture.join();
    }
}

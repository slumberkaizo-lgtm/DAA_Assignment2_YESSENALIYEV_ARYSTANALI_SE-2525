package benchmark;

import metrics.Metrics;
import structures.DynamicArray;
import structures.MyLinkedList;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/** Runs the currently implemented benchmark workloads. */
public class Benchmark {
    private static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    private static final int ACCESS_COUNT = 10_000;
    private static final int WARMUP_RUNS = 1;
    private static final int MEASURED_RUNS = 5;

    public record Result(String workload, String variant, String structure, int n,
                         double timeMillis, long steps, long moves, long comparisons) {
    }

    private record Run(long elapsedNanos, long steps, long moves, long comparisons) {
    }

    public static void main(String[] args) {
        Result[] results = runRandomAccess();
        System.out.println("W1 Random Access: 1 warm-up, 5 measured runs, median time");
        System.out.printf("%-10s %-15s %8s %12s %15s %10s %12s%n",
                "workload", "structure", "n", "time_ms", "steps", "moves", "comparisons");
        for (Result result : results) {
            System.out.printf(Locale.ROOT, "%-10s %-15s %8d %12.3f %15d %10d %12d%n",
                    result.workload(), result.structure(), result.n(), result.timeMillis(),
                    result.steps(), result.moves(), result.comparisons());
        }
    }

    public static Result[] runRandomAccess() {
        Random random = new Random(42);
        Result[] results = new Result[SIZES.length * 2];
        int resultIndex = 0;
        for (int n : SIZES) {
            int[] values = new int[n];
            for (int i = 0; i < n; i++) {
                values[i] = random.nextInt();
            }
            int[] indexes = new int[ACCESS_COUNT];
            long expectedChecksum = 0;
            long expectedListSteps = 0;
            for (int i = 0; i < indexes.length; i++) {
                indexes[i] = random.nextInt(n);
                expectedChecksum += values[indexes[i]];
                expectedListSteps += indexes[i];
            }

            results[resultIndex++] = measureRandomAccess(values, indexes, expectedChecksum,
                    indexes.length, true);
            results[resultIndex++] = measureRandomAccess(values, indexes, expectedChecksum,
                    expectedListSteps, false);
        }
        return results;
    }

    private static Result measureRandomAccess(int[] values, int[] indexes,
                                              long expectedChecksum, long expectedSteps,
                                              boolean useArray) {
        long[] times = new long[MEASURED_RUNS];
        Run measured = null;
        for (int run = 0; run < WARMUP_RUNS + MEASURED_RUNS; run++) {
            Run current = useArray
                    ? runArrayAccess(values, indexes, expectedChecksum, expectedSteps)
                    : runListAccess(values, indexes, expectedChecksum, expectedSteps);
            if (run >= WARMUP_RUNS) {
                times[run - WARMUP_RUNS] = current.elapsedNanos();
                measured = current;
            }
        }
        Arrays.sort(times);
        // Every run uses identical inputs and validates the same exact counters.
        return new Result("W1", "-", useArray ? "DynamicArray" : "MyLinkedList",
                values.length, times[MEASURED_RUNS / 2] / 1_000_000.0,
                measured.steps(), measured.moves(), measured.comparisons());
    }

    private static Run runArrayAccess(int[] values, int[] indexes,
                                      long expectedChecksum, long expectedSteps) {
        DynamicArray array = new DynamicArray();
        for (int value : values) {
            array.add(value);
        }
        Metrics metrics = array.getMetrics();
        metrics.reset();

        long checksum = 0;
        long start = System.nanoTime();
        for (int index : indexes) {
            checksum += array.get(index);
        }
        long elapsed = System.nanoTime() - start;

        verifyAccess(checksum, expectedChecksum, metrics, expectedSteps);
        return new Run(elapsed, metrics.getSteps(), metrics.getMoves(), metrics.getComparisons());
    }

    private static Run runListAccess(int[] values, int[] indexes,
                                     long expectedChecksum, long expectedSteps) {
        MyLinkedList list = new MyLinkedList();
        for (int value : values) {
            list.add(value);
        }
        Metrics metrics = list.getMetrics();
        metrics.reset();

        long checksum = 0;
        long start = System.nanoTime();
        for (int index : indexes) {
            checksum += list.get(index);
        }
        long elapsed = System.nanoTime() - start;

        verifyAccess(checksum, expectedChecksum, metrics, expectedSteps);
        return new Run(elapsed, metrics.getSteps(), metrics.getMoves(), metrics.getComparisons());
    }

    private static void verifyAccess(long checksum, long expectedChecksum,
                                     Metrics metrics, long expectedSteps) {
        if (checksum != expectedChecksum) {
            throw new IllegalStateException("W1 returned an incorrect checksum");
        }
        // Expected counts are validation only; results use the structures' actual counters.
        if (metrics.getSteps() != expectedSteps || metrics.getMoves() != 0
                || metrics.getComparisons() != 0) {
            throw new IllegalStateException("W1 recorded unexpected operation counts");
        }
    }
}

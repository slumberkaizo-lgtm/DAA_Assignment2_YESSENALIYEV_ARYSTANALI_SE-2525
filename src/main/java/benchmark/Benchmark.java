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
    private static final int SEARCH_COUNT = 1_000;
    private static final int WARMUP_RUNS = 1;
    private static final int MEASURED_RUNS = 5;

    public record Result(String workload, String variant, String structure, int n,
                         double timeMillis, long steps, long moves, long comparisons) {
    }

    private record Run(long elapsedNanos, long steps, long moves, long comparisons) {
    }

    public static void main(String[] args) {
        Result[] accessResults = runRandomAccess();
        Result[] searchResults = runSearch();
        Result[] results = Arrays.copyOf(accessResults, accessResults.length + searchResults.length);
        System.arraycopy(searchResults, 0, results, accessResults.length, searchResults.length);
        System.out.println("W1 Random Access / W2 Search: 1 warm-up, 5 measured runs, median time");
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

    public static Result[] runSearch() {
        Random random = new Random(42);
        Result[] results = new Result[SIZES.length * 2];
        int resultIndex = 0;
        for (int n : SIZES) {
            int[] values = new int[n];
            for (int i = 0; i < n; i++) {
                values[i] = random.nextInt(Integer.MAX_VALUE);
            }
            int[] queries = new int[SEARCH_COUNT];
            long expectedComparisons = 0;
            for (int i = 0; i < queries.length; i += 2) {
                queries[i] = values[random.nextInt(n)];
                queries[i + 1] = -1 - random.nextInt(Integer.MAX_VALUE);
                // A present query stops at its first occurrence, including duplicates.
                int firstIndex = 0;
                while (values[firstIndex] != queries[i]) {
                    firstIndex++;
                }
                expectedComparisons += firstIndex + 1L;
                expectedComparisons += n;
            }

            results[resultIndex++] = measureSearch(values, queries, expectedComparisons, true);
            results[resultIndex++] = measureSearch(values, queries, expectedComparisons, false);
        }
        return results;
    }

    private static Result measureSearch(int[] values, int[] queries,
                                         long expectedComparisons, boolean useArray) {
        long[] times = new long[MEASURED_RUNS];
        Run measured = null;
        for (int run = 0; run < WARMUP_RUNS + MEASURED_RUNS; run++) {
            Run current = useArray
                    ? runArraySearch(values, queries, expectedComparisons)
                    : runListSearch(values, queries, expectedComparisons);
            if (run >= WARMUP_RUNS) {
                times[run - WARMUP_RUNS] = current.elapsedNanos();
                measured = current;
            }
        }
        Arrays.sort(times);
        return new Result("W2", "-", useArray ? "DynamicArray" : "MyLinkedList",
                values.length, times[MEASURED_RUNS / 2] / 1_000_000.0,
                measured.steps(), measured.moves(), measured.comparisons());
    }

    private static Run runArraySearch(int[] values, int[] queries, long expectedComparisons) {
        DynamicArray array = new DynamicArray();
        for (int value : values) {
            array.add(value);
        }
        Metrics metrics = array.getMetrics();
        metrics.reset();
        boolean[] answers = new boolean[queries.length];

        long start = System.nanoTime();
        for (int i = 0; i < queries.length; i++) {
            answers[i] = array.contains(queries[i]);
        }
        long elapsed = System.nanoTime() - start;

        verifySearch(answers, metrics, expectedComparisons, expectedComparisons);
        return new Run(elapsed, metrics.getSteps(), metrics.getMoves(), metrics.getComparisons());
    }

    private static Run runListSearch(int[] values, int[] queries, long expectedComparisons) {
        MyLinkedList list = new MyLinkedList();
        for (int value : values) {
            list.add(value);
        }
        Metrics metrics = list.getMetrics();
        metrics.reset();
        boolean[] answers = new boolean[queries.length];

        long start = System.nanoTime();
        for (int i = 0; i < queries.length; i++) {
            answers[i] = list.contains(queries[i]);
        }
        long elapsed = System.nanoTime() - start;

        // Successful searches return before following the matching node's next link.
        long expectedSteps = expectedComparisons - queries.length / 2;
        verifySearch(answers, metrics, expectedSteps, expectedComparisons);
        return new Run(elapsed, metrics.getSteps(), metrics.getMoves(), metrics.getComparisons());
    }

    private static void verifySearch(boolean[] answers, Metrics metrics,
                                      long expectedSteps, long expectedComparisons) {
        for (int i = 0; i < answers.length; i++) {
            if (answers[i] != (i % 2 == 0)) {
                throw new IllegalStateException("W2 returned an incorrect answer at query " + i);
            }
        }
        // Expected counts validate the actual counters; they do not supply result values.
        if (metrics.getSteps() != expectedSteps || metrics.getMoves() != 0
                || metrics.getComparisons() != expectedComparisons) {
            throw new IllegalStateException("W2 recorded unexpected operation counts");
        }
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

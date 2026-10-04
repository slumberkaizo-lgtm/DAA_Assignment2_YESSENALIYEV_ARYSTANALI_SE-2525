package benchmark;

import metrics.Metrics;
import structures.DynamicArray;
import structures.MinHeap;
import structures.MyLinkedList;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/** Runs the currently implemented benchmark workloads. */
public class Benchmark {
    private static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    private static final int ACCESS_COUNT = 10_000;
    private static final int SEARCH_COUNT = 1_000;
    private static final int INSERT_REMOVE_COUNT = 1_000;
    private static final int WARMUP_RUNS = 1;
    private static final int MEASURED_RUNS = 5;

    public record Result(String workload, String variant, String structure, int n,
                         double timeMillis, long steps, long moves, long comparisons) {
    }

    private record Run(long elapsedNanos, long steps, long moves, long comparisons) {
    }

    private record InsertRemoveExpected(int[] remaining, int[] removed) {
    }

    public static void main(String[] args) {
        Result[] accessResults = runRandomAccess();
        Result[] searchResults = runSearch();
        Result[] insertRemoveResults = runInsertRemove();
        Result[] priorityResults = runPriorityProcessing();
        Result[] results = Arrays.copyOf(accessResults,
                accessResults.length + searchResults.length + insertRemoveResults.length
                        + priorityResults.length);
        System.arraycopy(searchResults, 0, results, accessResults.length, searchResults.length);
        System.arraycopy(insertRemoveResults, 0, results,
                accessResults.length + searchResults.length, insertRemoveResults.length);
        System.arraycopy(priorityResults, 0, results,
                accessResults.length + searchResults.length + insertRemoveResults.length,
                priorityResults.length);
        System.out.println("W1 / W2 / W3 / W4: 1 warm-up, 5 measured runs, median time");
        System.out.printf("%-10s %-8s %-15s %8s %12s %15s %12s %12s%n",
                "workload", "variant", "structure", "n", "time_ms", "steps", "moves", "comparisons");
        for (Result result : results) {
            System.out.printf(Locale.ROOT, "%-10s %-8s %-15s %8d %12.3f %15d %12d %12d%n",
                    result.workload(), result.variant(), result.structure(), result.n(), result.timeMillis(),
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

    public static Result[] runInsertRemove() {
        Random random = new Random(42);
        Result[] results = new Result[SIZES.length * 4];
        int resultIndex = 0;
        for (int n : SIZES) {
            int[] values = new int[n];
            for (int i = 0; i < n; i++) {
                values[i] = random.nextInt();
            }
            int[] inserted = new int[INSERT_REMOVE_COUNT];
            for (int i = 0; i < inserted.length; i++) {
                inserted[i] = random.nextInt();
            }
            for (boolean atHead : new boolean[]{true, false}) {
                InsertRemoveExpected expected = simulateInsertRemove(values, inserted, atHead);
                results[resultIndex++] = measureInsertRemove(values, inserted, expected, atHead, true);
                results[resultIndex++] = measureInsertRemove(values, inserted, expected, atHead, false);
            }
        }
        return results;
    }

    public static Result[] runPriorityProcessing() {
        Random random = new Random(42);
        Result[] results = new Result[SIZES.length];
        for (int i = 0; i < SIZES.length; i++) {
            int[] values = new int[SIZES[i]];
            for (int j = 0; j < values.length; j++) {
                values[j] = random.nextInt();
            }
            int[] expected = values.clone();
            Arrays.sort(expected);
            results[i] = measurePriorityProcessing(values, expected);
        }
        return results;
    }

    private static Result measurePriorityProcessing(int[] values, int[] expected) {
        long[] times = new long[MEASURED_RUNS];
        Run baseline = null;
        Run measured = null;
        for (int run = 0; run < WARMUP_RUNS + MEASURED_RUNS; run++) {
            Run current = runHeapPriorityProcessing(values, expected);
            if (baseline == null) {
                baseline = current;
            } else if (current.steps() != baseline.steps() || current.moves() != baseline.moves()
                    || current.comparisons() != baseline.comparisons()) {
                throw new IllegalStateException("W4 operation counts differ between runs");
            }
            if (run >= WARMUP_RUNS) {
                times[run - WARMUP_RUNS] = current.elapsedNanos();
                measured = current;
            }
        }
        Arrays.sort(times);
        return new Result("W4", "-", "MinHeap", values.length,
                times[MEASURED_RUNS / 2] / 1_000_000.0,
                measured.steps(), measured.moves(), measured.comparisons());
    }

    private static Run runHeapPriorityProcessing(int[] values, int[] expected) {
        MinHeap heap = new MinHeap();
        Metrics metrics = heap.getMetrics();
        metrics.reset();
        int[] extracted = new int[values.length];

        // W4 measures both building the heap and extracting every element.
        long start = System.nanoTime();
        for (int value : values) {
            heap.insert(value);
        }
        for (int i = 0; i < extracted.length; i++) {
            extracted[i] = heap.extractMin();
        }
        long elapsed = System.nanoTime() - start;

        Run result = new Run(elapsed, metrics.getSteps(), metrics.getMoves(), metrics.getComparisons());
        for (int i = 1; i < extracted.length; i++) {
            if (extracted[i - 1] > extracted[i]) {
                throw new IllegalStateException("W4 extraction is not non-decreasing at index " + i);
            }
        }
        if (!Arrays.equals(extracted, expected) || heap.size() != 0) {
            throw new IllegalStateException("W4 extracted values differ from the sorted input");
        }
        if (result.steps() <= 0 || result.moves() <= 0 || result.comparisons() <= 0) {
            throw new IllegalStateException("W4 recorded unexpected operation counts");
        }
        return result;
    }

    private static InsertRemoveExpected simulateInsertRemove(int[] values, int[] inserted,
                                                              boolean atHead) {
        int[] reference = Arrays.copyOf(values, values.length + inserted.length);
        int size = values.length;
        for (int value : inserted) {
            int index = atHead ? 0 : size / 2;
            System.arraycopy(reference, index, reference, index + 1, size - index);
            reference[index] = value;
            size++;
        }
        int[] removed = new int[INSERT_REMOVE_COUNT];
        for (int i = 0; i < removed.length; i++) {
            int index = atHead ? 0 : size / 2;
            removed[i] = reference[index];
            System.arraycopy(reference, index + 1, reference, index, size - index - 1);
            size--;
        }
        return new InsertRemoveExpected(Arrays.copyOf(reference, size), removed);
    }

    private static Result measureInsertRemove(int[] values, int[] inserted,
                                               InsertRemoveExpected expected,
                                               boolean atHead, boolean useArray) {
        long[] times = new long[MEASURED_RUNS];
        Run baseline = null;
        Run measured = null;
        for (int run = 0; run < WARMUP_RUNS + MEASURED_RUNS; run++) {
            Run current = useArray
                    ? runArrayInsertRemove(values, inserted, expected, atHead)
                    : runListInsertRemove(values, inserted, expected, atHead);
            if (baseline == null) {
                baseline = current;
            } else if (current.steps() != baseline.steps() || current.moves() != baseline.moves()
                    || current.comparisons() != baseline.comparisons()) {
                throw new IllegalStateException("W3 operation counts differ between runs");
            }
            if (run >= WARMUP_RUNS) {
                times[run - WARMUP_RUNS] = current.elapsedNanos();
                measured = current;
            }
        }
        Arrays.sort(times);
        return new Result("W3", atHead ? "head" : "middle",
                useArray ? "DynamicArray" : "MyLinkedList", values.length,
                times[MEASURED_RUNS / 2] / 1_000_000.0,
                measured.steps(), measured.moves(), measured.comparisons());
    }

    private static Run runArrayInsertRemove(int[] values, int[] inserted,
                                             InsertRemoveExpected expected, boolean atHead) {
        DynamicArray array = new DynamicArray();
        for (int value : values) {
            array.add(value);
        }
        Metrics metrics = array.getMetrics();
        metrics.reset();
        int[] removed = new int[INSERT_REMOVE_COUNT];

        long start = System.nanoTime();
        for (int value : inserted) {
            int index = atHead ? 0 : array.size() / 2;
            array.add(index, value);
        }
        for (int i = 0; i < removed.length; i++) {
            int index = atHead ? 0 : array.size() / 2;
            removed[i] = array.remove(index);
        }
        long elapsed = System.nanoTime() - start;

        // Capture the workload counters before validation reads any elements.
        Run result = new Run(elapsed, metrics.getSteps(), metrics.getMoves(), metrics.getComparisons());
        verifyInsertRemove(removed, array.size(), expected, result);
        for (int i = 0; i < expected.remaining().length; i++) {
            if (array.get(i) != expected.remaining()[i]) {
                throw new IllegalStateException("W3 DynamicArray contents differ at index " + i);
            }
        }
        return result;
    }

    private static Run runListInsertRemove(int[] values, int[] inserted,
                                            InsertRemoveExpected expected, boolean atHead) {
        MyLinkedList list = new MyLinkedList();
        for (int value : values) {
            list.add(value);
        }
        Metrics metrics = list.getMetrics();
        metrics.reset();
        int[] removed = new int[INSERT_REMOVE_COUNT];

        long start = System.nanoTime();
        for (int value : inserted) {
            int index = atHead ? 0 : list.size() / 2;
            list.add(index, value);
        }
        for (int i = 0; i < removed.length; i++) {
            int index = atHead ? 0 : list.size() / 2;
            removed[i] = list.remove(index);
        }
        long elapsed = System.nanoTime() - start;

        Run result = new Run(elapsed, metrics.getSteps(), metrics.getMoves(), metrics.getComparisons());
        verifyInsertRemove(removed, list.size(), expected, result);
        // Drain the disposable list in O(n) outside the timed section.
        for (int i = 0; i < expected.remaining().length; i++) {
            if (list.remove(0) != expected.remaining()[i]) {
                throw new IllegalStateException("W3 MyLinkedList contents differ at index " + i);
            }
        }
        if (list.size() != 0) {
            throw new IllegalStateException("W3 MyLinkedList validation did not drain the list");
        }
        return result;
    }

    private static void verifyInsertRemove(int[] removed, int size,
                                            InsertRemoveExpected expected, Run result) {
        if (!Arrays.equals(removed, expected.removed()) || size != expected.remaining().length) {
            throw new IllegalStateException("W3 returned incorrect removed values or final size");
        }
        if (result.steps() <= 0 || result.moves() <= 0 || result.comparisons() != 0) {
            throw new IllegalStateException("W3 recorded unexpected operation counts");
        }
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

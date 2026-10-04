package metrics;

import org.junit.jupiter.api.Test;
import structures.DynamicArray;
import structures.MinHeap;
import structures.MyLinkedList;

import static org.junit.jupiter.api.Assertions.*;

class MetricsTest {
    @Test
    void countersAccumulateAndReset() {
        Metrics metrics = new Metrics();
        assertCounts(metrics, 0, 0, 0);
        metrics.recordStep();
        metrics.recordStep();
        metrics.recordMove();
        metrics.recordComparison();
        assertCounts(metrics, 2, 1, 1);
        metrics.reset();
        assertCounts(metrics, 0, 0, 0);
    }

    @Test
    void arrayCountsReadsAndSearchComparisons() {
        DynamicArray array = new DynamicArray();
        array.add(4);
        array.add(8);
        array.add(12);
        assertCounts(array.getMetrics(), 0, 0, 0);

        assertEquals(8, array.get(1));
        assertCounts(array.getMetrics(), 1, 0, 0);
        array.getMetrics().reset();
        assertTrue(array.contains(8));
        assertCounts(array.getMetrics(), 2, 0, 2);
        array.getMetrics().reset();
        assertFalse(array.contains(99));
        assertCounts(array.getMetrics(), 3, 0, 3);
    }

    @Test
    void arrayCountsInsertionAndRemovalShifts() {
        DynamicArray array = new DynamicArray();
        array.add(10);
        array.add(20);
        array.add(30);

        array.add(1, 15);
        assertCounts(array.getMetrics(), 2, 2, 0);
        array.getMetrics().reset();
        assertEquals(15, array.remove(1));
        assertCounts(array.getMetrics(), 3, 2, 0);
        array.getMetrics().reset();
        assertEquals(30, array.remove(2));
        assertCounts(array.getMetrics(), 1, 0, 0);
    }

    @Test
    void arrayCountsEachResizeCopyAndIndexedShift() {
        for (boolean indexed : new boolean[]{false, true}) {
            DynamicArray array = new DynamicArray();
            for (int i = 0; i < 10; i++) {
                array.add(i);
            }
            array.getMetrics().reset();
            if (indexed) {
                array.add(0, -1);
                assertCounts(array.getMetrics(), 20, 20, 0);
            } else {
                array.add(10);
                assertCounts(array.getMetrics(), 10, 10, 0);
            }
            assertEquals(11, array.size());
        }
    }

    @Test
    void listCountsHeadTailAndNextUpdatesOnInsertion() {
        MyLinkedList list = new MyLinkedList();
        list.add(10);
        assertCounts(list.getMetrics(), 0, 2, 0);
        list.getMetrics().reset();
        list.add(30);
        assertCounts(list.getMetrics(), 0, 2, 0);
        list.getMetrics().reset();
        list.add(0, 5);
        assertCounts(list.getMetrics(), 0, 2, 0);
        list.getMetrics().reset();
        list.add(2, 20);
        assertCounts(list.getMetrics(), 2, 2, 0);
        list.getMetrics().reset();
        list.add(list.size(), 40);
        assertCounts(list.getMetrics(), 0, 2, 0);
    }

    @Test
    void listCountsTraversalAndSearchComparisons() {
        MyLinkedList list = new MyLinkedList();
        list.add(10);
        list.add(20);
        list.add(30);
        list.getMetrics().reset();

        assertEquals(30, list.get(2));
        assertCounts(list.getMetrics(), 2, 0, 0);
        list.getMetrics().reset();
        assertTrue(list.contains(10));
        assertCounts(list.getMetrics(), 0, 0, 1);
        list.getMetrics().reset();
        assertTrue(list.contains(30));
        assertCounts(list.getMetrics(), 2, 0, 3);
        list.getMetrics().reset();
        assertFalse(list.contains(99));
        assertCounts(list.getMetrics(), 3, 0, 3);
    }

    @Test
    void listCountsLinksOnMiddleTailAndHeadRemoval() {
        MyLinkedList list = new MyLinkedList();
        for (int i = 0; i < 4; i++) {
            list.add(i);
        }
        list.getMetrics().reset();
        assertEquals(1, list.remove(1));
        assertCounts(list.getMetrics(), 2, 1, 0);
        list.getMetrics().reset();
        assertEquals(3, list.remove(2));
        assertCounts(list.getMetrics(), 3, 2, 0);
        list.getMetrics().reset();
        assertEquals(0, list.remove(0));
        assertCounts(list.getMetrics(), 1, 1, 0);
        list.getMetrics().reset();
        assertEquals(2, list.remove(0));
        assertCounts(list.getMetrics(), 1, 2, 0);
    }

    @Test
    void heapCountsBubbleUpReadsComparisonsAndSwapMoves() {
        MinHeap heap = new MinHeap();
        heap.insert(8);
        assertCounts(heap.getMetrics(), 0, 0, 0);
        heap.insert(3);
        assertCounts(heap.getMetrics(), 4, 2, 1);
        heap.getMetrics().reset();
        heap.insert(10);
        assertCounts(heap.getMetrics(), 2, 0, 1);
        heap.getMetrics().reset();
        assertEquals(3, heap.peekMin());
        assertCounts(heap.getMetrics(), 1, 0, 0);
    }

    @Test
    void heapCountsBubbleDownWithOneChildAndSingletonExtraction() {
        MinHeap heap = new MinHeap();
        for (int value : new int[]{8, 3, 10}) {
            heap.insert(value);
        }
        heap.getMetrics().reset();
        assertEquals(3, heap.extractMin());
        assertCounts(heap.getMetrics(), 6, 3, 1);
        heap.getMetrics().reset();
        assertEquals(8, heap.extractMin());
        assertCounts(heap.getMetrics(), 2, 1, 0);
        heap.getMetrics().reset();
        assertEquals(10, heap.extractMin());
        assertCounts(heap.getMetrics(), 1, 0, 0);
    }

    @Test
    void heapCountsBothChildComparisonsAndEitherSwapDirection() {
        for (int[] values : new int[][]{{1, 2, 3, 4}, {1, 3, 2, 4}}) {
            MinHeap heap = new MinHeap();
            for (int value : values) {
                heap.insert(value);
            }
            heap.getMetrics().reset();
            assertEquals(1, heap.extractMin());
            assertCounts(heap.getMetrics(), 8, 3, 2);
        }
    }

    @Test
    void heapCountsComparisonsWhenBubbleDownNeedsNoSwap() {
        MinHeap heap = new MinHeap();
        for (int value : new int[]{1, 2, 2, 2}) {
            heap.insert(value);
        }
        heap.getMetrics().reset();
        assertEquals(1, heap.extractMin());
        assertCounts(heap.getMetrics(), 6, 1, 2);
    }

    @Test
    void heapCountsEachResizeCopy() {
        MinHeap heap = new MinHeap();
        for (int i = 0; i < 10; i++) {
            heap.insert(i);
        }
        heap.getMetrics().reset();
        heap.insert(10);
        assertCounts(heap.getMetrics(), 12, 10, 1);
        assertEquals(11, heap.size());
    }

    @Test
    void rejectedOperationsAndEmptySearchesDoNotIncrementCounters() {
        DynamicArray array = new DynamicArray();
        MyLinkedList list = new MyLinkedList();
        MinHeap heap = new MinHeap();
        assertThrows(IndexOutOfBoundsException.class, () -> array.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> array.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> array.add(1, 5));
        assertFalse(array.contains(5));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(1, 5));
        assertFalse(list.contains(5));
        assertThrows(IllegalStateException.class, heap::peekMin);
        assertThrows(IllegalStateException.class, heap::extractMin);
        assertCounts(array.getMetrics(), 0, 0, 0);
        assertCounts(list.getMetrics(), 0, 0, 0);
        assertCounts(heap.getMetrics(), 0, 0, 0);
    }

    @Test
    void metricsAreIndependentAndResetPreservesContents() {
        DynamicArray array = new DynamicArray();
        MyLinkedList list = new MyLinkedList();
        MinHeap heap = new MinHeap();
        array.add(7);
        list.add(8);
        heap.insert(9);
        array.get(0);
        array.getMetrics().reset();
        list.getMetrics().reset();
        heap.getMetrics().reset();
        assertCounts(array.getMetrics(), 0, 0, 0);
        assertCounts(list.getMetrics(), 0, 0, 0);
        assertCounts(heap.getMetrics(), 0, 0, 0);

        assertEquals(7, array.get(0));
        assertCounts(list.getMetrics(), 0, 0, 0);
        assertCounts(heap.getMetrics(), 0, 0, 0);
        assertCounts(new DynamicArray().getMetrics(), 0, 0, 0);
        assertEquals(8, list.get(0));
        assertEquals(9, heap.peekMin());
        assertEquals(1, array.size());
        assertEquals(1, list.size());
        assertEquals(1, heap.size());
    }

    private static void assertCounts(Metrics metrics, long steps, long moves, long comparisons) {
        assertEquals(steps, metrics.getSteps(), "steps");
        assertEquals(moves, metrics.getMoves(), "moves");
        assertEquals(comparisons, metrics.getComparisons(), "comparisons");
    }
}

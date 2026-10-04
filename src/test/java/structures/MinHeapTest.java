package structures;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MinHeapTest {
    @Test
    void emptyHeapThrowsWithoutChangingSize() {
        MinHeap heap = new MinHeap();

        assertEquals(0, heap.size());
        assertThrows(IllegalStateException.class, heap::peekMin);
        assertThrows(IllegalStateException.class, heap::extractMin);
        assertEquals(0, heap.size());
        assertHeapProperty(heap);
    }

    @Test
    void singleElementCanBeInsertedPeekedAndExtracted() {
        MinHeap heap = new MinHeap();
        insertAndCheck(heap, 42);

        assertEquals(42, heap.peekMin());
        assertEquals(42, heap.peekMin());
        assertEquals(1, heap.size());
        assertEquals(42, extractAndCheck(heap));
        assertEmpty(heap);
    }

    @Test
    void peekDoesNotRemoveTheMinimum() {
        MinHeap heap = new MinHeap();
        for (int value : new int[]{8, -2, 5, 0}) {
            insertAndCheck(heap, value);
        }

        for (int i = 0; i < 5; i++) {
            assertEquals(-2, heap.peekMin());
            assertEquals(4, heap.size());
            assertHeapProperty(heap);
        }
        assertEquals(-2, extractAndCheck(heap));
        assertEquals(0, heap.peekMin());
    }

    @Test
    void duplicatesArePreservedDuringExtraction() {
        assertSortedExtraction(new int[]{4, 4, -1, 4, -1, 0, 0});
    }

    @Test
    void equalValuesMaintainHeapProperty() {
        int[] values = new int[100];
        Arrays.fill(values, 7);
        assertSortedExtraction(values);
    }

    @Test
    void negativeZeroAndExtremeIntValuesAreSupported() {
        assertSortedExtraction(new int[]{
                Integer.MAX_VALUE, -5, 0, Integer.MIN_VALUE, -1,
                Integer.MIN_VALUE, Integer.MAX_VALUE
        });
    }

    @Test
    void ascendingAndDescendingInputsMaintainHeapProperty() {
        int[] ascending = new int[100];
        int[] descending = new int[100];
        for (int i = 0; i < 100; i++) {
            ascending[i] = i - 50;
            descending[i] = 49 - i;
        }

        assertSortedExtraction(ascending);
        assertSortedExtraction(descending);
    }

    @Test
    void smallHeapsHandleOneChildAndEitherSmallerChild() {
        // These inputs exercise single-child, left-child and right-child paths.
        for (int[] values : new int[][]{
                {2, 1}, {1, 2, 3}, {1, 2, 3, 4},
                {1, 3, 2, 4}, {5, 4, 3, 2, 1}, {0, 1, 1, 2, 2, 3}
        }) {
            assertSortedExtraction(values);
        }
    }

    @Test
    void randomValuesSurviveMultipleResizesAndExtractInSortedOrder() {
        Random random = new Random(42);
        int[] values = new int[1_000];
        for (int i = 0; i < values.length; i++) {
            values[i] = random.nextInt();
        }

        assertSortedExtraction(values);
    }

    @Test
    void drainedHeapCanBeReused() {
        MinHeap heap = new MinHeap();
        for (int i = 30; i >= 0; i--) {
            insertAndCheck(heap, i);
        }
        for (int i = 0; i <= 30; i++) {
            assertEquals(i, extractAndCheck(heap));
        }
        assertEmpty(heap);

        insertAndCheck(heap, 9);
        insertAndCheck(heap, -4);
        assertEquals(-4, extractAndCheck(heap));
        assertEquals(9, extractAndCheck(heap));
        assertEmpty(heap);
    }

    @Test
    void mixedOperationsMatchPriorityQueueReference() {
        MinHeap heap = new MinHeap();
        PriorityQueue<Integer> reference = new PriorityQueue<>();
        Random random = new Random(42);

        for (int operation = 0; operation < 2_000; operation++) {
            if (reference.isEmpty() || random.nextInt(3) != 0) {
                int value = random.nextInt(101) - 50;
                reference.add(value);
                insertAndCheck(heap, value);
            } else {
                assertEquals(reference.remove().intValue(), extractAndCheck(heap));
            }
            assertEquals(reference.size(), heap.size());
            if (!reference.isEmpty()) {
                assertEquals(reference.peek().intValue(), heap.peekMin());
            }
        }

        while (!reference.isEmpty()) {
            assertEquals(reference.remove().intValue(), extractAndCheck(heap));
        }
        assertEmpty(heap);
    }

    private static void assertSortedExtraction(int[] values) {
        MinHeap heap = new MinHeap();
        int minimum = Integer.MAX_VALUE;
        for (int value : values) {
            insertAndCheck(heap, value);
            minimum = Math.min(minimum, value);
            assertEquals(minimum, heap.peekMin());
        }

        int[] expected = values.clone();
        Arrays.sort(expected);
        int previous = Integer.MIN_VALUE;
        for (int value : expected) {
            assertEquals(value, heap.peekMin());
            int current = extractAndCheck(heap);
            assertEquals(value, current);
            assertTrue(previous <= current, "Extraction must be non-decreasing");
            previous = current;
        }
        assertEmpty(heap);
    }

    private static void insertAndCheck(MinHeap heap, int value) {
        int oldSize = heap.size();
        heap.insert(value);
        assertEquals(oldSize + 1, heap.size());
        assertHeapProperty(heap);
    }

    private static int extractAndCheck(MinHeap heap) {
        int oldSize = heap.size();
        int minimum = heap.extractMin();
        assertEquals(oldSize - 1, heap.size());
        assertHeapProperty(heap);
        return minimum;
    }

    private static void assertEmpty(MinHeap heap) {
        assertEquals(0, heap.size());
        assertThrows(IllegalStateException.class, heap::peekMin);
        assertThrows(IllegalStateException.class, heap::extractMin);
        assertEquals(0, heap.size());
        assertHeapProperty(heap);
    }

    private static void assertHeapProperty(MinHeap heap) {
        try {
            Field dataField = MinHeap.class.getDeclaredField("data");
            dataField.setAccessible(true);
            int[] data = (int[]) dataField.get(heap);
            assertTrue(heap.size() >= 0 && heap.size() <= data.length);
            for (int child = 1; child < heap.size(); child++) {
                int parent = (child - 1) / 2;
                assertTrue(data[parent] <= data[child],
                        "Heap property violated at parent " + parent + ", child " + child);
            }
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Cannot inspect heap storage", exception);
        }
    }
}

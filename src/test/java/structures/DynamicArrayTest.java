package structures;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DynamicArrayTest {
    @Test
    void emptyArrayHasZeroSizeAndContainsNothing() {
        DynamicArray array = new DynamicArray();

        assertEquals(0, array.size());
        assertFalse(array.contains(0));
        assertFalse(array.contains(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> array.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> array.remove(0));
    }

    @Test
    void singleElementCanBeAddedReadAndRemoved() {
        DynamicArray array = new DynamicArray();
        array.add(42);

        assertContents(array, 42);
        assertTrue(array.contains(42));
        assertEquals(42, array.remove(0));
        assertContents(array);
        assertFalse(array.contains(42));

        array.add(0, -7);
        assertContents(array, -7);
    }

    @Test
    void appendPreservesOrderAndSupportsAllIntValues() {
        DynamicArray array = new DynamicArray();
        int[] values = {Integer.MIN_VALUE, -5, 0, 12, Integer.MAX_VALUE};
        for (int value : values) {
            array.add(value);
        }

        assertContents(array, values);
        for (int value : values) {
            assertTrue(array.contains(value));
        }
        assertFalse(array.contains(11));
    }

    @Test
    void indexedInsertSupportsEmptyFirstMiddleAndEndPositions() {
        DynamicArray array = new DynamicArray();
        array.add(0, 20);
        assertContents(array, 20);
        array.add(0, 10);
        assertContents(array, 10, 20);
        array.add(array.size(), 40);
        assertContents(array, 10, 20, 40);
        array.add(2, 30);
        assertContents(array, 10, 20, 30, 40);
    }

    @Test
    void removeFirstMiddleAndLastPreservesRemainingOrder() {
        DynamicArray array = new DynamicArray();
        for (int value : new int[]{10, 20, 30, 40, 50}) {
            array.add(value);
        }

        assertEquals(10, array.remove(0));
        assertContents(array, 20, 30, 40, 50);
        assertEquals(30, array.remove(1));
        assertContents(array, 20, 40, 50);
        assertEquals(50, array.remove(array.size() - 1));
        assertContents(array, 20, 40);
        array.add(60);
        assertContents(array, 20, 40, 60);
    }

    @Test
    void duplicatesRemainUntilTheirLastOccurrenceIsRemoved() {
        DynamicArray array = new DynamicArray();
        array.add(-3);
        array.add(0);
        array.add(-3);

        assertEquals(-3, array.remove(0));
        assertContents(array, 0, -3);
        assertTrue(array.contains(-3));
        assertEquals(-3, array.remove(1));
        assertContents(array, 0);
        assertFalse(array.contains(-3));
    }

    @Test
    void containsDoesNotSearchUnusedOrRemovedCells() {
        DynamicArray array = new DynamicArray();
        array.add(9);
        array.add(15);
        assertFalse(array.contains(0));

        array.remove(1);
        assertFalse(array.contains(15));
        assertFalse(array.contains(0));
        array.remove(0);
        assertFalse(array.contains(9));
    }

    @Test
    void invalidIndexesThrowWithoutChangingContents() {
        for (int count : new int[]{0, 1, 5}) {
            DynamicArray array = new DynamicArray();
            int[] expected = new int[count];
            for (int i = 0; i < count; i++) {
                expected[i] = i + 10;
                array.add(expected[i]);
            }

            for (int index : new int[]{Integer.MIN_VALUE, -1, count, count + 1, Integer.MAX_VALUE}) {
                assertThrows(IndexOutOfBoundsException.class, () -> array.get(index));
                assertThrows(IndexOutOfBoundsException.class, () -> array.remove(index));
                assertContents(array, expected);
            }
            for (int index : new int[]{Integer.MIN_VALUE, -1, count + 1, Integer.MAX_VALUE}) {
                assertThrows(IndexOutOfBoundsException.class, () -> array.add(index, 99));
                assertContents(array, expected);
            }
        }
    }

    @Test
    void appendPreservesEveryValueAcrossMultipleResizes() {
        DynamicArray array = new DynamicArray();
        for (int i = 0; i < 1_000; i++) {
            array.add(i - 500);
            assertEquals(i + 1, array.size());
            for (int j = 0; j <= i; j++) {
                assertEquals(j - 500, array.get(j));
            }
        }
    }

    @Test
    void indexedInsertCanResizeAFullArrayAndShiftItsContents() {
        for (int index : new int[]{0, 5, 10}) {
            DynamicArray array = new DynamicArray();
            for (int i = 0; i < 10; i++) {
                array.add(i);
            }

            array.add(index, -1);

            assertEquals(11, array.size());
            for (int i = 0; i < array.size(); i++) {
                int expected = i < index ? i : (i == index ? -1 : i - 1);
                assertEquals(expected, array.get(i));
            }
        }
    }

    @Test
    void arrayCanBeDrainedAndReusedAfterMultipleResizes() {
        DynamicArray array = new DynamicArray();
        for (int i = 0; i < 100; i++) {
            array.add(i);
        }
        for (int i = 0; i < 100; i++) {
            assertEquals(i, array.remove(0));
            assertEquals(99 - i, array.size());
        }

        assertContents(array);
        assertFalse(array.contains(99));
        assertThrows(IndexOutOfBoundsException.class, () -> array.get(0));
        array.add(0, 123);
        array.add(456);
        assertContents(array, 123, 456);
    }

    @Test
    void mixedOperationsMatchArrayListReference() {
        DynamicArray array = new DynamicArray();
        ArrayList<Integer> reference = new ArrayList<>();
        Random random = new Random(42);

        for (int operation = 0; operation < 2_000; operation++) {
            int value = random.nextInt(101) - 50;
            switch (random.nextInt(5)) {
                case 0 -> {
                    array.add(value);
                    reference.add(value);
                }
                case 1 -> {
                    int index = random.nextInt(reference.size() + 1);
                    array.add(index, value);
                    reference.add(index, value);
                }
                case 2 -> {
                    if (!reference.isEmpty()) {
                        int index = random.nextInt(reference.size());
                        assertEquals(reference.remove(index).intValue(), array.remove(index));
                    }
                }
                case 3 -> {
                    if (!reference.isEmpty()) {
                        int index = random.nextInt(reference.size());
                        assertEquals(reference.get(index).intValue(), array.get(index));
                    }
                }
                case 4 -> assertEquals(reference.contains(value), array.contains(value));
            }

            assertEquals(reference.size(), array.size());
            for (int i = 0; i < reference.size(); i++) {
                assertEquals(reference.get(i).intValue(), array.get(i));
            }
        }
    }

    private static void assertContents(DynamicArray array, int... expected) {
        assertEquals(expected.length, array.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], array.get(i), "Value at index " + i);
        }
    }
}

package structures;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MyLinkedListTest {
    @Test
    void emptyListHasZeroSizeAndContainsNothing() {
        MyLinkedList list = new MyLinkedList();

        assertContents(list);
        assertFalse(list.contains(0));
        assertFalse(list.contains(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
    }

    @Test
    void singleElementCanBeAddedReadAndRemoved() {
        MyLinkedList list = new MyLinkedList();
        list.add(42);

        assertContents(list, 42);
        assertTrue(list.contains(42));
        assertEquals(42, list.remove(0));
        assertContents(list);
        assertFalse(list.contains(42));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
    }

    @Test
    void appendPreservesOrderAndSupportsAllIntValues() {
        MyLinkedList list = new MyLinkedList();
        int[] values = {Integer.MIN_VALUE, -5, 0, 12, Integer.MAX_VALUE};
        for (int value : values) {
            list.add(value);
        }

        assertContents(list, values);
        for (int value : values) {
            assertTrue(list.contains(value));
        }
        assertFalse(list.contains(11));
    }

    @Test
    void indexedInsertSupportsEmptyHeadMiddleAndTailPositions() {
        MyLinkedList list = new MyLinkedList();
        list.add(0, 20);
        assertContents(list, 20);
        list.add(0, 10);
        assertContents(list, 10, 20);
        list.add(list.size(), 40);
        assertContents(list, 10, 20, 40);
        list.add(2, 30);
        assertContents(list, 10, 20, 30, 40);
        list.add(50);
        assertContents(list, 10, 20, 30, 40, 50);
    }

    @Test
    void removeHeadMiddleAndTailPreservesRemainingOrder() {
        MyLinkedList list = new MyLinkedList();
        for (int value : new int[]{10, 20, 30, 40, 50}) {
            list.add(value);
        }

        assertEquals(10, list.remove(0));
        assertContents(list, 20, 30, 40, 50);
        assertEquals(30, list.remove(1));
        assertContents(list, 20, 40, 50);
        assertEquals(50, list.remove(list.size() - 1));
        assertContents(list, 20, 40);
    }

    @Test
    void removingTailAllowsFurtherAppends() {
        MyLinkedList list = new MyLinkedList();
        list.add(10);
        list.add(20);
        list.add(30);

        assertEquals(30, list.remove(2));
        list.add(40);
        assertContents(list, 10, 20, 40);
        assertEquals(40, list.remove(2));
        assertEquals(20, list.remove(1));
        list.add(list.size(), 50);
        assertContents(list, 10, 50);
    }

    @Test
    void removingOnlyNodeAllowsAppendAndIndexedInsertIntoEmptyList() {
        MyLinkedList list = new MyLinkedList();
        list.add(1);
        assertEquals(1, list.remove(0));

        list.add(2);
        list.add(3);
        assertContents(list, 2, 3);
        assertEquals(3, list.remove(1));
        assertEquals(2, list.remove(0));

        list.add(0, 4);
        list.add(5);
        assertContents(list, 4, 5);
    }

    @Test
    void duplicatesRemainUntilTheirLastOccurrenceIsRemoved() {
        MyLinkedList list = new MyLinkedList();
        list.add(-3);
        list.add(0);
        list.add(-3);

        assertEquals(-3, list.remove(0));
        assertContents(list, 0, -3);
        assertTrue(list.contains(-3));
        assertEquals(-3, list.remove(1));
        assertContents(list, 0);
        assertFalse(list.contains(-3));
    }

    @Test
    void readsAndSearchesLeaveListUnchanged() {
        MyLinkedList list = new MyLinkedList();
        list.add(-8);
        list.add(0);
        list.add(12);

        for (int i = 0; i < 3; i++) {
            assertEquals(-8, list.get(0));
            assertEquals(0, list.get(1));
            assertEquals(12, list.get(2));
            assertTrue(list.contains(-8));
            assertTrue(list.contains(0));
            assertTrue(list.contains(12));
            assertFalse(list.contains(99));
            assertContents(list, -8, 0, 12);
        }
        list.add(13);
        assertContents(list, -8, 0, 12, 13);
    }

    @Test
    void invalidIndexesThrowWithoutChangingContents() {
        for (int count : new int[]{0, 1, 5}) {
            MyLinkedList list = new MyLinkedList();
            int[] expected = new int[count];
            for (int i = 0; i < count; i++) {
                expected[i] = i + 10;
                list.add(expected[i]);
            }

            for (int index : new int[]{Integer.MIN_VALUE, -1, count, count + 1, Integer.MAX_VALUE}) {
                assertThrows(IndexOutOfBoundsException.class, () -> list.get(index));
                assertThrows(IndexOutOfBoundsException.class, () -> list.remove(index));
                assertContents(list, expected);
            }
            for (int index : new int[]{Integer.MIN_VALUE, -1, count + 1, Integer.MAX_VALUE}) {
                assertThrows(IndexOutOfBoundsException.class, () -> list.add(index, 99));
                assertContents(list, expected);
            }
            list.add(count, 99);
            assertEquals(count + 1, list.size());
            assertEquals(99, list.get(count));
            for (int i = 0; i < count; i++) {
                assertEquals(expected[i], list.get(i));
            }
        }
    }

    @Test
    void listCanBeDrainedFromHeadAndReused() {
        MyLinkedList list = new MyLinkedList();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        for (int i = 0; i < 100; i++) {
            assertEquals(i, list.remove(0));
            assertEquals(99 - i, list.size());
            assertFalse(list.contains(i));
        }

        assertContents(list);
        list.add(123);
        list.add(456);
        assertContents(list, 123, 456);
    }

    @Test
    void mixedOperationsMatchLinkedListReference() {
        MyLinkedList list = new MyLinkedList();
        LinkedList<Integer> reference = new LinkedList<>();
        Random random = new Random(42);

        for (int operation = 0; operation < 2_000; operation++) {
            int value = random.nextInt(101) - 50;
            switch (random.nextInt(5)) {
                case 0 -> {
                    list.add(value);
                    reference.add(value);
                }
                case 1 -> {
                    int index = random.nextInt(reference.size() + 1);
                    list.add(index, value);
                    reference.add(index, value);
                }
                case 2 -> {
                    if (!reference.isEmpty()) {
                        int index = random.nextInt(reference.size());
                        assertEquals(reference.remove(index).intValue(), list.remove(index));
                    }
                }
                case 3 -> {
                    if (!reference.isEmpty()) {
                        int index = random.nextInt(reference.size());
                        assertEquals(reference.get(index).intValue(), list.get(index));
                    }
                }
                case 4 -> assertEquals(reference.contains(value), list.contains(value));
            }

            assertEquals(reference.size(), list.size());
            for (int i = 0; i < reference.size(); i++) {
                assertEquals(reference.get(i).intValue(), list.get(i));
            }
        }
    }

    private static void assertContents(MyLinkedList list, int... expected) {
        assertEquals(expected.length, list.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], list.get(i), "Value at index " + i);
        }
    }
}

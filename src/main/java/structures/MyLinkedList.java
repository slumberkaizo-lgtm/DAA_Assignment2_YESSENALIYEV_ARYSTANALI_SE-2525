package structures;

import metrics.Metrics;

/** A singly linked list of primitive int values. */
public class MyLinkedList {
    private static class Node {
        int value;
        Node next;

        Node(int value) {
            this.value = value;
        }
    }

    private Node head;
    private Node tail;
    private int size;
    private final Metrics metrics = new Metrics();

    public Metrics getMetrics() {
        return metrics;
    }

    public int size() {
        return size;
    }

    public void add(int x) {
        Node node = new Node(x);
        if (size == 0) {
            head = node;
            metrics.recordMove();
        } else {
            tail.next = node;
            metrics.recordMove();
        }
        tail = node;
        metrics.recordMove();
        size++;
    }

    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
        if (index == size) {
            add(x);
            return;
        }

        Node node = new Node(x);
        if (index == 0) {
            node.next = head;
            metrics.recordMove();
            head = node;
            metrics.recordMove();
        } else {
            Node previous = nodeAt(index - 1);
            metrics.recordStep();
            node.next = previous.next;
            metrics.recordMove();
            previous.next = node;
            metrics.recordMove();
        }
        size++;
    }

    /** Removes and returns the value at the given index. */
    public int remove(int index) {
        checkElementIndex(index);
        Node removed;
        if (index == 0) {
            removed = head;
            metrics.recordStep();
            head = head.next;
            metrics.recordMove();
            if (size == 1) {
                tail = null;
                metrics.recordMove();
            }
        } else {
            Node previous = nodeAt(index - 1);
            metrics.recordStep();
            removed = previous.next;
            metrics.recordStep();
            previous.next = removed.next;
            metrics.recordMove();
            if (removed == tail) {
                tail = previous;
                metrics.recordMove();
            }
        }
        size--;
        return removed.value;
    }

    public int get(int index) {
        checkElementIndex(index);
        return nodeAt(index).value;
    }

    public boolean contains(int x) {
        Node current = head;
        while (current != null) {
            metrics.recordComparison();
            if (current.value == x) {
                return true;
            }
            metrics.recordStep();
            current = current.next;
        }
        return false;
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }

    private Node nodeAt(int index) {
        Node current = head;
        for (int i = 0; i < index; i++) {
            metrics.recordStep();
            current = current.next;
        }
        return current;
    }
}

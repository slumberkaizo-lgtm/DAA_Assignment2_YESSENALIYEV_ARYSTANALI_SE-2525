package structures;

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

    public int size() {
        return size;
    }

    public void add(int x) {
        Node node = new Node(x);
        if (size == 0) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
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
            head = node;
        } else {
            Node previous = nodeAt(index - 1);
            node.next = previous.next;
            previous.next = node;
        }
        size++;
    }

    /** Removes and returns the value at the given index. */
    public int remove(int index) {
        checkElementIndex(index);
        Node removed;
        if (index == 0) {
            removed = head;
            head = head.next;
            if (size == 1) {
                tail = null;
            }
        } else {
            Node previous = nodeAt(index - 1);
            removed = previous.next;
            previous.next = removed.next;
            if (removed == tail) {
                tail = previous;
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
            if (current.value == x) {
                return true;
            }
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
            current = current.next;
        }
        return current;
    }
}

package structures;

/** An array-based binary min heap of primitive int values. */
public class MinHeap {
    private int[] data = new int[10];
    private int size;

    public int size() {
        return size;
    }

    public void insert(int x) {
        ensureCapacity();
        data[size] = x;
        size++;
        bubbleUp(size - 1);
    }

    public int peekMin() {
        checkNotEmpty();
        return data[0];
    }

    public int extractMin() {
        checkNotEmpty();
        int minimum = data[0];
        size--;
        if (size > 0) {
            data[0] = data[size];
        }
        data[size] = 0;
        if (size > 0) {
            bubbleDown(0);
        }
        return minimum;
    }

    private void bubbleUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (data[parent] <= data[index]) {
                break;
            }
            swap(parent, index);
            index = parent;
        }
    }

    private void bubbleDown(int index) {
        // Only indexes below size / 2 have children.
        while (index < size / 2) {
            int left = 2 * index + 1;
            int right = left + 1;
            int smallerChild = left;
            if (right < size && data[right] < data[left]) {
                smallerChild = right;
            }
            if (data[index] <= data[smallerChild]) {
                break;
            }
            swap(index, smallerChild);
            index = smallerChild;
        }
    }

    private void swap(int first, int second) {
        int temporary = data[first];
        data[first] = data[second];
        data[second] = temporary;
    }

    private void checkNotEmpty() {
        if (size == 0) {
            throw new IllegalStateException("Heap is empty");
        }
    }

    private void ensureCapacity() {
        if (size == data.length) {
            int[] expanded = new int[data.length * 2];
            for (int i = 0; i < size; i++) {
                expanded[i] = data[i];
            }
            data = expanded;
        }
    }
}

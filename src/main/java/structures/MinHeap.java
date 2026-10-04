package structures;

import metrics.Metrics;

/** An array-based binary min heap of primitive int values. */
public class MinHeap {
    private int[] data = new int[10];
    private int size;
    private final Metrics metrics = new Metrics();

    public Metrics getMetrics() {
        return metrics;
    }

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
        metrics.recordStep();
        return data[0];
    }

    public int extractMin() {
        checkNotEmpty();
        metrics.recordStep();
        int minimum = data[0];
        size--;
        if (size > 0) {
            metrics.recordStep();
            data[0] = data[size];
            metrics.recordMove();
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
            metrics.recordStep();
            metrics.recordStep();
            metrics.recordComparison();
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
            if (right < size) {
                metrics.recordStep();
                metrics.recordStep();
                metrics.recordComparison();
                if (data[right] < data[left]) {
                    smallerChild = right;
                }
            }
            metrics.recordStep();
            metrics.recordStep();
            metrics.recordComparison();
            if (data[index] <= data[smallerChild]) {
                break;
            }
            swap(index, smallerChild);
            index = smallerChild;
        }
    }

    private void swap(int first, int second) {
        metrics.recordStep();
        int temporary = data[first];
        metrics.recordStep();
        data[first] = data[second];
        metrics.recordMove();
        data[second] = temporary;
        metrics.recordMove();
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
                metrics.recordStep();
                expanded[i] = data[i];
                metrics.recordMove();
            }
            data = expanded;
        }
    }
}

package structures;

import metrics.Metrics;

/** A resizable array of primitive int values. */
public class DynamicArray {
    private int[] data = new int[10];
    private int size;
    private final Metrics metrics = new Metrics();

    public Metrics getMetrics() {
        return metrics;
    }

    public int size() {
        return size;
    }

    public void add(int x) {
        ensureCapacity();
        data[size] = x;
        size++;
    }

    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
        ensureCapacity();
        for (int i = size; i > index; i--) {
            metrics.recordStep();
            data[i] = data[i - 1];
            metrics.recordMove();
        }
        data[index] = x;
        size++;
    }

    /** Removes and returns the value at the given index. */
    public int remove(int index) {
        checkElementIndex(index);
        metrics.recordStep();
        int removed = data[index];
        for (int i = index; i < size - 1; i++) {
            metrics.recordStep();
            data[i] = data[i + 1];
            metrics.recordMove();
        }
        size--;
        data[size] = 0;
        return removed;
    }

    public int get(int index) {
        checkElementIndex(index);
        metrics.recordStep();
        return data[index];
    }

    public boolean contains(int x) {
        for (int i = 0; i < size; i++) {
            metrics.recordStep();
            metrics.recordComparison();
            if (data[i] == x) {
                return true;
            }
        }
        return false;
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
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

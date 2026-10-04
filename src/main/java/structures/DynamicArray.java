package structures;

/** A resizable array of primitive int values. */
public class DynamicArray {
    private int[] data = new int[10];
    private int size;

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
            data[i] = data[i - 1];
        }
        data[index] = x;
        size++;
    }

    /** Removes and returns the value at the given index. */
    public int remove(int index) {
        checkElementIndex(index);
        int removed = data[index];
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
        }
        size--;
        data[size] = 0;
        return removed;
    }

    public int get(int index) {
        checkElementIndex(index);
        return data[index];
    }

    public boolean contains(int x) {
        for (int i = 0; i < size; i++) {
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
                expanded[i] = data[i];
            }
            data = expanded;
        }
    }
}

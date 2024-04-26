package Assets;

public class PrimitiveIntArrayList {
    private final int STARTING_CAPACITY = 50;
    private final double CAPACITY_GROWTH_FACTOR = 2.0;
    public int[] data;
    public int size;
    public int capacity;

    public PrimitiveIntArrayList()
    {
        clear();
    }

    public void resize(int newCapacity) {
        newCapacity = Math.max(newCapacity, size);
        if (newCapacity != capacity) {
            capacity = newCapacity;
            int[] newData = new int[capacity];
            if (size >= 0) System.arraycopy(data, 0, newData, 0, size);
            data = newData;
        }
    }

    private void forceResize(int newCapacity) {
        if (newCapacity != capacity) {
            capacity = newCapacity;
            int[] newData = new int[capacity];
            if (size >= 0) System.arraycopy(data, 0, newData, 0, size);
            data = newData;
        }
    }

    public void add(int value) {
        if (size == capacity) {
            resize((int) (capacity * CAPACITY_GROWTH_FACTOR));
        }
        data[size] = value;
        size++;
    }

    public void clear() {
        size = 0;
        capacity = STARTING_CAPACITY;
        data = new int[STARTING_CAPACITY];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void trim() {
        forceResize(size);
    }
}

package Assets;

public class PrimitiveFloatArrayList {
    private final int STARTING_CAPACITY = 50;
    private final double CAPACITY_GROWTH_FACTOR = 2.0;
    public float[] data;
    public int size;
    public int capacity;

    public PrimitiveFloatArrayList()
    {
        clear();
    }

    // capacity argument cannot be 0
    public PrimitiveFloatArrayList(int size)
    {
        this.size = size;
        this.capacity = size;
        this.data = new float[capacity];
    }

    public void resize(int newCapacity) {
        newCapacity = Math.max(newCapacity, size);
        if (newCapacity != capacity) {
            capacity = newCapacity;
            float[] newData = new float[capacity];
            if (size >= 0) System.arraycopy(data, 0, newData, 0, size);
            data = newData;
        }
    }

    private void forceResize(int newCapacity) {
        if (newCapacity != capacity) {
            capacity = newCapacity;
            float[] newData = new float[capacity];
            if (size >= 0) System.arraycopy(data, 0, newData, 0, size);
            data = newData;
        }
    }

    public void add(float value) {
        if (size == capacity) {
            resize((int) (capacity * CAPACITY_GROWTH_FACTOR));
        }
        data[size] = value;
        size++;
    }

    public void clear() {
        size = 0;
        capacity = STARTING_CAPACITY;
        data = new float[STARTING_CAPACITY];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void trim() {
        forceResize(size);
    }
}

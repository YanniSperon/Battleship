package Data;

import java.io.Serializable;

public class Coordinate implements Serializable {
    public int X;
    public int Y;

    public Coordinate(int x, int y) {
        X = x;
        Y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Coordinate)) {
            return false;
        }

        Coordinate c = (Coordinate) o;

        return Integer.compare(X, c.X) == 0
                && Integer.compare(Y, c.Y) == 0;
    }
}

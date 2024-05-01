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

    public Coordinate add(Coordinate otherCoord) {
        return new Coordinate(X + otherCoord.X, Y + otherCoord.Y);
    }

    public Coordinate add(int x, int y) {
        return new Coordinate(X + x, Y + y);
    }

    public Coordinate multiply(int val) {
        return new Coordinate(X * val, Y * val);
    }

    public static int coordinatesToIndex(Coordinate c) {
        return c.Y * 10 + c.X;
    }

    public static Coordinate indexToCoordinates(int index) {
        return new Coordinate(index % 10, index / 10);
    }

    @Override
    public String toString() {
        return "Coordinate(X: \"" + X + "\" Y: \"" + Y + "\")";
    }
}

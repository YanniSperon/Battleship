package Data;

import java.io.Serializable;
import java.util.ArrayList;

public class Piece implements Serializable {
    public int size = 0;
    public ArrayList<Coordinate> positions = new ArrayList<Coordinate>();

    public Piece() {
    }
}

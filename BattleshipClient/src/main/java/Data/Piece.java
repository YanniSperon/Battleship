package Data;

import javafx.geometry.Point3D;

import java.io.Serializable;
import java.util.ArrayList;

public class Piece implements Serializable {
    public enum PieceType {
        CARRIER, BATTLESHIP, CRUISER, SUBMARINE, DESTROYER, NONE
    }
    public enum PieceOrientation {
        RIGHT, DOWN
    }
    public Coordinate front = new Coordinate(0, 0);
    public PieceOrientation orientation = PieceOrientation.RIGHT;
    public PieceType type = PieceType.NONE;

    public Piece() {
    }

    public int getSize() {
        switch (type) {
            case CARRIER:
                return 5;
            case BATTLESHIP:
                return 4;
            case CRUISER:
            case SUBMARINE:
                return 3;
            case DESTROYER:
            default:
                return 2;
        }
    }

    public ArrayList<Coordinate> getPositions() {
        ArrayList<Coordinate> finalPositions = new ArrayList<Coordinate>();

        Coordinate directionalOffset;
        if (orientation == PieceOrientation.DOWN) {
            // Going down
            directionalOffset = new Coordinate(0, 1);
        } else {
            // Going to right
            directionalOffset = new Coordinate(1, 0);
        }
        for (int i = 0; i < getSize(); ++i) {
            finalPositions.add(front.add(directionalOffset.multiply(i)));
        }
        return finalPositions;
    }

    @Override
    public String toString() {
        return "Piece(type: \"" + type.toString() + "\" front: \"" + front.toString() + "\" orientation: \"" + orientation + "\")";
    }
}

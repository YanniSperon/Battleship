package Data;

import java.util.UUID;

public class PlacePiece extends Payload {
    public Piece piece = null;
    public UUID opponent = null;

    public PlacePiece() {
        super(Type.PLACE_PIECE);
    }
}

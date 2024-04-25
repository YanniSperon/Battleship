package Data;

import java.util.UUID;

public class PlacePieceResult extends Payload {
    public UUID opponent = null;
    public boolean status = false;

    public PlacePieceResult() {
        super(Type.PLACE_PIECE_RESULT);
    }
}

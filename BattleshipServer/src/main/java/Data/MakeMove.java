package Data;

import java.util.UUID;

public class MakeMove extends Payload {
    public Move move = new Move(null);
    public UUID otherUser = null;

    public MakeMove() {
        super(Type.MAKE_MOVE);
    }
}

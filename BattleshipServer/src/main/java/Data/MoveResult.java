package Data;

import java.util.UUID;

public class MoveResult extends Payload {
    public boolean status = false;
    public boolean didHit = false;
    public UUID opponent = null;

    public MoveResult() {
        super(Type.MOVE_RESULT);
    }
}

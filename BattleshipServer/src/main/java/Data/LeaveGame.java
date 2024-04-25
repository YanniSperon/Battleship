package Data;

import java.util.UUID;

public class LeaveGame extends Payload {
    public UUID otherUser = null;

    public LeaveGame() {
        super(Type.LEAVE_GAME);
    }
}
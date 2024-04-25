package Data;

import java.util.UUID;

public class JoinPrivateGameResult extends Payload {
    public boolean success = false;
    public UUID otherUser = null;

    public JoinPrivateGameResult() {
        super(Type.JOIN_PRIVATE_GAME_RESULT);
    }
}

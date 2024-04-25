package Data;

import java.util.UUID;

public class GameFound extends Payload {
    public UUID user1 = null;
    public UUID user2 = null;

    public GameFound() {
        super(Type.GAME_FOUND);
    }
}

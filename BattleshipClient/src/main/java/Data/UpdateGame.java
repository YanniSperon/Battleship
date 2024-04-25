package Data;

import java.util.UUID;

public class UpdateGame extends Payload {
    public UUID user1 = null;
    public UUID user2 = null;
    public Game game = null;

    public UpdateGame() {
        super(Type.UPDATE_GAME);
    }
}

package Data;

import java.util.UUID;

public class UpdateDirectMessage extends Payload {
    public UUID user1ID = null;
    public UUID user2ID = null;
    public Chat chat = null;

    public UpdateDirectMessage() {
        super(Type.UPDATE_DIRECT_MESSAGE);
    }
}

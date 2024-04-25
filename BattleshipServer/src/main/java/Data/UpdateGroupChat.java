package Data;

import java.util.UUID;

public class UpdateGroupChat extends Payload {
    public UUID groupID = null;
    public Chat chat = null;

    public UpdateGroupChat() {
        super(Type.UPDATE_GROUP_CHAT);
    }
}

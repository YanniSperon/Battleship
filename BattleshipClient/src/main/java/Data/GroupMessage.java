package Data;

import java.util.UUID;

public class GroupMessage extends Payload {
    public Message message = new Message();
    public UUID receivingGroup = null;

    public GroupMessage() {
        super(Type.GROUP_MESSAGE);
    }
}
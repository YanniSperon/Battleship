package Data;

import java.util.UUID;

public class DirectMessage extends Payload {
    public Message message = new Message();
    public UUID receiver = null;

    public DirectMessage() {
        super(Type.DIRECT_MESSAGE);
    }
}
package Data;

import java.util.UUID;

public class Connected extends Payload {
    public UUID userID = null;

    public Connected() {
        super(Type.CONNECTED);
    }
}

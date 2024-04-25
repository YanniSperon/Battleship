package Data;

import java.util.HashMap;
import java.util.UUID;

public class UpdateUsers extends Payload {
    public HashMap<UUID, User> users = new HashMap<UUID, User>();

    public UpdateUsers() {
        super(Type.UPDATE_USERS);
    }
}
package Data;

import java.util.HashMap;
import java.util.UUID;

public class UpdateGroups extends Payload {
    public HashMap<UUID, Group> groups = new HashMap<UUID, Group>();

    public UpdateGroups() {
        super(Type.UPDATE_GROUPS);
    }
}
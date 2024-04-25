package Data;

import java.util.ArrayList;
import java.util.UUID;

public class GroupAdd extends Payload {
    public UUID groupID = null;
    public ArrayList<UUID> membersToAdd = new ArrayList<UUID>();

    public GroupAdd() {
        super(Type.GROUP_ADD);
    }
}

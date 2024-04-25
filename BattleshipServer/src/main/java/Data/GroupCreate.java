package Data;

import java.util.ArrayList;
import java.util.UUID;

public class GroupCreate extends Payload {
    public String groupName = null;
    public UUID creatorID = null;
    public boolean allowInvites = false;
    public boolean isPrivate = false;
    public ArrayList<UUID> members = new ArrayList<UUID>(); // members does not include the creator

    public GroupCreate() {
        super(Type.GROUP_CREATE);
    }
}

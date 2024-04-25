package Data;

import java.util.UUID;

public class GroupKick extends Payload {
    public UUID groupID = null;
    public UUID kickedMemberID = null;

    public GroupKick() {
        super(Type.GROUP_KICK);
    }
}
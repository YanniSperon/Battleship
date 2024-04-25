package Data;

import java.util.UUID;

public class GroupLeave extends Payload {
    public UUID groupID = null;

    public GroupLeave() {
        super(Type.GROUP_LEAVE);
    }
}
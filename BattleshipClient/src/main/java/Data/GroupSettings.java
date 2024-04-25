package Data;

import java.util.UUID;

public class GroupSettings extends Payload {
    public UUID groupID = null;
    public boolean allowInvites = false;
    public boolean isPrivate = false;

    public GroupSettings() {
        super(Type.GROUP_SETTINGS);
    }
}

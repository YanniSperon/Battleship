package Data;

import java.util.UUID;

public class GroupDelete extends Payload {
    public UUID groupID = null;

    public GroupDelete() {
        super(Type.GROUP_DELETE);
    }
}
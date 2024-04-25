package Data;

import java.io.Serializable;

public class Payload implements Serializable {
    private static final long serialVersionUID = 42L;
    public enum Type {
        // Bidirectional
        INVALID_OPERATION,
        // Server Outbound
        OPERATION_RESULT, UPDATE_DIRECT_MESSAGE, UPDATE_GROUP_CHAT, UPDATE_GROUPS, UPDATE_USERS, CONNECTED,
        // Server Inbound
        LOGIN_ATTEMPT, GROUP_CREATE, GROUP_MESSAGE, GROUP_LEAVE, GROUP_KICK, GROUP_ADD, GROUP_DELETE, GROUP_SETTINGS, DIRECT_MESSAGE, BLOCK_USER
    }

    public Type type = Type.INVALID_OPERATION;

    public Payload(Type type) {
        this.type = type;
    }
}

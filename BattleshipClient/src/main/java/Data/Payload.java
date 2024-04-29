package Data;

import java.io.Serializable;

public class Payload implements Serializable {
    private static final long serialVersionUID = 42L;
    public enum Type {
        // CHAT
        INVALID_OPERATION,
        // Server Outbound
        LOGIN_RESULT, UPDATE_DIRECT_MESSAGE, UPDATE_GROUP_CHAT, UPDATE_GROUPS, UPDATE_USERS, CONNECTED, GROUP_CREATE_RESULT,
        // Server Inbound
        LOGIN_ATTEMPT, GROUP_CREATE, GROUP_MESSAGE, GROUP_LEAVE, GROUP_KICK, GROUP_ADD, GROUP_DELETE, GROUP_SETTINGS, DIRECT_MESSAGE, BLOCK_USER,

        // GAME
        // Server Outbound
        UPDATE_GAME, GAME_FOUND, START_PRIVATE_GAME_RESULT, JOIN_PRIVATE_GAME_RESULT, PLACE_PIECE_RESULT, MOVE_RESULT, TURN_SWITCH,
        // Server Inbound
        FIND_GAME, LEAVE_GAME, START_PRIVATE_GAME, MAKE_MOVE, JOIN_PRIVATE_GAME, PLACE_PIECE
    }

    public Type type = Type.INVALID_OPERATION;

    public Payload(Type type) {
        this.type = type;
    }
}

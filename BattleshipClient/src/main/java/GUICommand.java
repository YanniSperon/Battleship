import java.io.Serializable;

public class GUICommand implements Serializable {
    public enum Type {
        // CHAT AND LOGIN
        LOGIN_SUCCESS, LOGIN_ERROR,
        REFRESH,
        GROUP_CREATE_SUCCESS, GROUP_CREATE_ERROR,

        // GAME
        GAME_FOUND,
        PRIVATE_STARTED,
        JOIN_PRIVATE_SUCCESS, JOIN_PRIVATE_ERROR,
        PLACE_PIECE_SUCCESS, PLACE_PIECE_ERROR,
        MOVE_SUCCESS_HIT, MOVE_SUCCESS_MISS, MOVE_FAIL
    }

    public Type type;
    GUICommand(Type type)
    {
        this.type = type;
    }
}

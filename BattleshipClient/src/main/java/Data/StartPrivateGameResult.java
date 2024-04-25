package Data;

public class StartPrivateGameResult extends Payload {
    public String joinableID = null;

    public StartPrivateGameResult() {
        super(Type.START_PRIVATE_GAME_RESULT);
    }
}

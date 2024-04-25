package Data;

public class JoinPrivateGame extends Payload {
    public String code = null;
    public JoinPrivateGame() {
        super(Type.JOIN_PRIVATE_GAME);
    }
}

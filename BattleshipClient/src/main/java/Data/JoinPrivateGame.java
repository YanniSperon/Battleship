package Data;

public class JoinPrivateGame extends Payload {
    public String code;
    public JoinPrivateGame(String code) {
        super(Type.JOIN_PRIVATE_GAME);
        this.code = code;
    }
}

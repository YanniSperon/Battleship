package Data;

public class LoginResult extends Payload {
    public boolean status = false;

    public LoginResult() {
        super(Type.LOGIN_RESULT);
    }
}

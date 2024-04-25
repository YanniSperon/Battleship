package Data;

public class LoginAttempt extends Payload {
    public String username = null;

    public LoginAttempt() {
        super(Type.LOGIN_ATTEMPT);
    }
}
package Data;

public class GroupCreateResult extends Payload {
    public boolean status = false;

    public GroupCreateResult() {
        super(Type.GROUP_CREATE_RESULT);
    }
}

package Data;

public class OperationResult extends Payload {
    public boolean status = false;

    public OperationResult() {
        super(Type.OPERATION_RESULT);
    }
}

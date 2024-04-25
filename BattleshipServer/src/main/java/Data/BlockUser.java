package Data;

import java.util.UUID;

public class BlockUser extends Payload {
    public UUID userToBlock = null;
    public boolean shouldBlock = false;

    public BlockUser() {
        super(Type.BLOCK_USER);
    }
}
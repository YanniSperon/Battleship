package Data;

import java.io.Serializable;
import java.util.UUID;

public class Move implements Serializable {
    public UUID senderID;
    public Coordinate position = null;

    public Move(UUID sender) {
        senderID = sender;
    }
}

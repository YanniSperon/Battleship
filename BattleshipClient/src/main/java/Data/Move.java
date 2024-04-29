package Data;

import java.io.Serializable;
import java.util.UUID;

public class Move implements Serializable {
    public UUID senderID;
    public boolean isHit = false;
    public Coordinate position = null;

    public Move(UUID sender) {
        senderID = sender;
    }

    @Override
    public String toString() {
        return "Move(position: \"" + position + "\" senderID: \"" + senderID.toString() + "\")";
    }
}

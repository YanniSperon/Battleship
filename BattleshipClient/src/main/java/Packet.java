import Data.Payload;

import java.io.Serializable;

public class Packet implements Serializable {

    private static final long serialVersionUID = 42L;
    public Payload data;

    Packet(Payload message)
    {
        this.data = message;
    }

    @Override
    public String toString() {
        return "Packet(Type:" + data.type + ", Data:" + Integer.toHexString(hashCode()) + ')';
    }
}

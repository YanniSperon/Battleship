package Data;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {
    public UUID sender;
    public String content;
}

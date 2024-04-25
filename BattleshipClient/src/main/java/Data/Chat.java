package Data;

import java.io.Serializable;
import java.util.ArrayList;

public class Chat implements Serializable {
    public ArrayList<Message> messages = new ArrayList<Message>();
}

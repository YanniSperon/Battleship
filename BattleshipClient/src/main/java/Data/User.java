package Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class User implements Serializable {
    public UUID uuid;
    public String username = null;
    public ArrayList<UUID> blockedUsers = new ArrayList<UUID>();

    public User(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean canCommunicateWith(User otherUser) {
        return !blockedUsers.contains(otherUser.uuid);
    }

    public void blockUser(UUID other) {
        blockedUsers.add(other);
    }

    public void unblockUser(UUID other) {
        blockedUsers.remove(other);
    }

    @Override
    public String toString() {
        return "User(Name: \"" + username + "\", ID: \"" + uuid + "\")";
    }

    public String toDisplayString() {
        return username;
    }
}

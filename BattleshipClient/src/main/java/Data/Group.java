package Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class Group implements Serializable {
    public UUID uuid;
    public String name = null;
    public UUID creator = null;
    public ArrayList<UUID> users = new ArrayList<UUID>();
    public boolean allowInvites = false;
    public boolean isPrivate = false;

    public Group(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean containsUser(UUID id) {
        if (creator.equals(id)) {
            return true;
        }
        for (UUID entry : users) {
            if (entry.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean canUserAddMembers(UUID id) {
        if (allowInvites) {
            return true;
        } else {
            return creator.equals(id);
        }
    }

    public boolean canUserKickMembers(UUID id) {
        return creator.equals(id);
    }

    public boolean canUserChangeSettings(UUID id) {
        return creator.equals(id);
    }

    public boolean shouldShowInBrowser() {
        return !isPrivate;
    }

    // Returns true if the user was in the group, false otherwise
    public boolean removeUserIfNecessary(UUID id) {
        if (creator.equals(id)) {
            if (!users.isEmpty()) {
                creator = users.get(0);
            } else {
                creator = null;
            }
            return true;
        }
        return users.remove(id);
    }

    public boolean isGroupEmpty() {
        return creator == null;
    }

    public boolean canUserDeleteGroup(UUID id) {
        return creator.equals(id);
    }

    @Override
    public String toString() {
        return "Group(Name: \"" + name + "\", ID: \"" + uuid + "\")";
    }

    public String toDisplayString() {
        return name;
    }
}

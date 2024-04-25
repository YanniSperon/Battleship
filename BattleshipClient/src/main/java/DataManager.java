import Data.Chat;
import Data.Game;
import Data.Group;
import Data.User;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    public final HashMap<UUID, Group> groups;
    public final HashMap<UUID, User> users;
    public final HashMap<Pair<UUID, UUID>, Chat> directMessages;
    public final HashMap<Pair<UUID, UUID>, Game> games;
    public final HashMap<UUID, Chat> groupChats;

    DataManager() {
        groups = new HashMap<UUID, Group>();
        users = new HashMap<UUID, User>();
        directMessages = new HashMap<Pair<UUID, UUID>, Chat>();
        groupChats = new HashMap<UUID, Chat>();
        games = new HashMap<Pair<UUID, UUID>, Game>();
    }

    public boolean containsUsername(String username) {
        boolean res = false;
        for (Map.Entry<UUID, User> pair : users.entrySet()) {
            if (pair.getValue().username.equals(username)) {
                res = true;
            }
        }
        return res;
    }

    public boolean containsGroupName(String name) {
        boolean res = false;
        for (Map.Entry<UUID, Group> pair : groups.entrySet()) {
            if (pair.getValue().name.equals(name)) {
                res = true;
            }
        }
        return res;
    }

    public Chat getDM(UUID u1, UUID u2) {
        if (u1 == null || u2 == null) {
            return null;
        }
        if (u1.compareTo(u2) < 0) {
            UUID temp = u1;
            u1 = u2;
            u2 = temp;
        }
        Pair<UUID, UUID> p = new Pair<UUID, UUID>(u1, u2);
        if (!directMessages.containsKey(p)) {
            return directMessages.put(p, new Chat());
        }
        return directMessages.get(p);
    }

    public void setDirectMessage(UUID u1, UUID u2, Chat chat) {
        if (u1 == null || u2 == null || chat == null) {
            return;
        }
        if (u1.compareTo(u2) < 0) {
            UUID temp = u1;
            u1 = u2;
            u2 = temp;
        }
        Pair<UUID, UUID> p = new Pair<UUID, UUID>(u1, u2);
        directMessages.put(p, chat);
    }

    public Chat getGroupChat(UUID group) {
        if (group == null) {
            return null;
        }
        if (!groupChats.containsKey(group)) {
            groupChats.put(group, new Chat());
        }
        return groupChats.get(group);
    }

    public void setGroupChat(UUID group, Chat chat) {
        if (group == null || chat == null) {
            return;
        }
        groupChats.put(group, chat);
    }

    public boolean isValidUser(UUID user) {
        return users.containsKey(user);
    }

    public boolean isValidGroup(UUID group) {
        return groups.containsKey(group);
    }

    public UUID getGlobalGroup() {
        for (Map.Entry<UUID, Group> pair : groups.entrySet()) {
            Group g = pair.getValue();
            if (g.name.equals("Global")) {
                return pair.getKey();
            }
        }
        return null;
    }

    public UUID getByUsername(String username) {
        for (Map.Entry<UUID, User> pair : users.entrySet()) {
            if (pair.getValue().username != null && pair.getValue().username.equals(username)) {
                return pair.getKey();
            }
        }
        return null;
    }

    public UUID getByGroupName(String groupName) {
        for (Map.Entry<UUID, Group> pair : groups.entrySet()) {
            if (pair.getValue().name != null && pair.getValue().name.equals(groupName)) {
                return pair.getKey();
            }
        }
        return null;
    }

    public Game getGame(UUID u1, UUID u2) {
        if (u1 == null || u2 == null) {
            return null;
        }
        if (u1.compareTo(u2) < 0) {
            UUID temp = u1;
            u1 = u2;
            u2 = temp;
        }
        Pair<UUID, UUID> p = new Pair<UUID, UUID>(u1, u2);
        if (!games.containsKey(p)) {
            return games.put(p, new Game());
        }
        return games.get(p);
    }

    public void setGame(UUID u1, UUID u2, Game game) {
        if (u1 == null || u2 == null || game == null) {
            return;
        }
        if (u1.compareTo(u2) < 0) {
            UUID temp = u1;
            u1 = u2;
            u2 = temp;
        }
        Pair<UUID, UUID> p = new Pair<UUID, UUID>(u1, u2);
        games.put(p, game);
    }
}

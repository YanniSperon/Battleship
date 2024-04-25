import Data.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class Server {
    private final TheServer server;
    private final Consumer<Serializable> serverLogCallback;
    public final DataManager dataManager;
    private final HashMap<UUID, ClientThread> clients;
    public final User serverUser;
    public final Group globalChat;


    Server(Consumer<Serializable> call) {
        clients = new HashMap<UUID, ClientThread>();
        serverLogCallback = call;
        server = new TheServer();
        server.start();
        dataManager = new DataManager();
        UUID serverUserID = dataManager.createNewUser();
        serverUser = dataManager.users.get(serverUserID);
        UUID globalChatID = dataManager.createNewGroup();
        globalChat = dataManager.groups.get(globalChatID);
        globalChat.creator = serverUserID;
        serverUser.username = "Server";
        globalChat.name = "Global";
        clients.put(serverUserID, new ClientThread());
    }

    private String getLogClientDescriptor(UUID id) {
        if (dataManager.isValidUser(id)) {
            User u = dataManager.users.get(id);
            if (u.username != null) {
                return "Username:\"" + u.username + "\"";
            } else {
                return "UserID:\"" + u.uuid.toString() + "\"";
            }
        } else {
            return "Invalid User";
        }
    }

    private String getLogGroupDescriptor(UUID id) {
        if (dataManager.isValidGroup(id)) {
            Group g = dataManager.groups.get(id);
            if (g.name != null) {
                return "Group:\"" + g.name + "\"";
            } else {
                return "GroupID:\"" + g.uuid.toString() + "\"";
            }
        } else {
            return "Invalid Group";
        }
    }

    private void executeInvalidRequest(UUID id, Packet p) {
        serverLogCallback.accept("Received invalid request from " + getLogClientDescriptor(id));
        // Reply with an invalid request
        Payload payload = new Payload(Payload.Type.INVALID_OPERATION);
        Packet reply = new Packet(payload);
        clients.get(id).sendPacket(reply);
    }

    private void executeLoginAttempt(UUID id, Packet p) {
        System.out.println("Executing login attempt");
        serverLogCallback.accept("Received login request from " + getLogClientDescriptor(id));
        LoginAttempt request = (LoginAttempt) p.data;
        OperationResult replyPayload = new OperationResult();
        replyPayload.status = !dataManager.containsUsername(request.username);
        clients.get(id).sendPacket(new Packet(replyPayload));
        if (replyPayload.status) {
            dataManager.users.get(id).username = request.username;
            sendUpdatedUserList();
            sendUpdatedGroupList();
            serverLogCallback.accept(getLogClientDescriptor(id) + " login successful with username " + request.username);

            Message joinMessage = new Message();
            joinMessage.content = dataManager.users.get(id).username + " has joined the server";
            joinMessage.sender = serverUser.uuid;
            dataManager.getGroupChat(globalChat.uuid).messages.add(joinMessage);

            UpdateGroupChat updateGlobalChatPayload = new UpdateGroupChat();
            updateGlobalChatPayload.groupID = globalChat.uuid;
            updateGlobalChatPayload.chat = dataManager.getGroupChat(globalChat.uuid);
            updateGroupMembers(globalChat.uuid, new Packet(updateGlobalChatPayload));
        }
    }

    private void executeGroupCreate(UUID id, Packet p) {
        serverLogCallback.accept("Received group create request from " + getLogClientDescriptor(id));
        // Read the request and construct the reply
        GroupCreate request = (GroupCreate) p.data;
        OperationResult replyPayload = new OperationResult();
        // Only allow a user to be created if it is from a valid user and the name isn't taken
        replyPayload.status = (!dataManager.containsGroupName(request.groupName)) && dataManager.isValidUser(request.creatorID);
        // Tell the user whether creation was allowed
        Packet reply = new Packet(replyPayload);
        clients.get(id).sendPacket(reply);
        // If creation allowed
        if (replyPayload.status) {
            // Create group
            UUID newGroupID = dataManager.createNewGroup();
            Group newGroup = dataManager.groups.get(newGroupID);
            newGroup.name = request.groupName;
            newGroup.creator = request.creatorID;
            newGroup.users = request.members;
            newGroup.allowInvites = request.allowInvites;
            newGroup.isPrivate = request.isPrivate;
            // Tell all clients to update their lists of groups
            sendUpdatedGroupList();

            Message m = new Message();
            m.sender = serverUser.uuid;
            m.content = dataManager.users.get(id).username + " created the group";
            dataManager.getGroupChat(newGroupID).messages.add(m);
            UpdateGroupChat followupPayload = new UpdateGroupChat();
            followupPayload.groupID = newGroupID;
            followupPayload.chat = dataManager.getGroupChat(newGroupID);
            updateGroupMembers(newGroupID, new Packet(followupPayload));
            serverLogCallback.accept(getLogClientDescriptor(id) + " creating group " + getLogGroupDescriptor(newGroupID) + " successful");
        }
    }

    private void executeGroupMessage(UUID id, Packet p) {
        // Read the request
        GroupMessage request = (GroupMessage) p.data;
        UUID receiver = request.receivingGroup;
        // If the message is not empty
        if (!request.message.content.isEmpty()) {
            // If the user and group exist
            if (dataManager.isValidUser(id) && dataManager.isValidGroup(receiver)) {
                // If the user is in the group
                if (!dataManager.groups.get(receiver).isPrivate || dataManager.groups.get(receiver).containsUser(id)) {
                    // Add the message to the group chat
                    dataManager.getGroupChat(receiver).messages.add(request.message);
                    // Tell all other members of the group to update the chat
                    UpdateGroupChat followupPayload = new UpdateGroupChat();
                    followupPayload.groupID = receiver;
                    followupPayload.chat = dataManager.getGroupChat(receiver);
                    updateGroupMembers(receiver, new Packet(followupPayload));
                    serverLogCallback.accept(getLogClientDescriptor(id) + " sent message to group chat " + getLogGroupDescriptor(receiver));
                }
            }
        }
    }

    private void executeGroupLeave(UUID id, Packet p) {
        // Read the request
        GroupLeave request = (GroupLeave) p.data;
        UUID groupToLeave = request.groupID;

        // If the user and group exist
        if (dataManager.isValidUser(id) && dataManager.isValidGroup(groupToLeave)) {
            if (dataManager.leaveGroup(id, groupToLeave)) {
                // If the group still exists after leaving, tell the other members
                if (dataManager.isValidGroup(groupToLeave)) {
                    // User left the group, send a message about it
                    Message m = new Message();
                    m.sender = serverUser.uuid;
                    m.content = dataManager.users.get(id).username + " has left the group";
                    dataManager.getGroupChat(groupToLeave).messages.add(m);
                    // Tell all other members of the group to update the chat
                    UpdateGroupChat followupPayload = new UpdateGroupChat();
                    followupPayload.groupID = groupToLeave;
                    followupPayload.chat = dataManager.getGroupChat(groupToLeave);
                    updateGroupMembers(groupToLeave, new Packet(followupPayload));
                    serverLogCallback.accept(getLogClientDescriptor(id) + " left the group chat " + getLogGroupDescriptor(groupToLeave));
                }
            }
            // Whether the group exists or not, update everyone's list of the groups
            sendUpdatedGroupList();
        }
    }

    private void executeGroupKick(UUID id, Packet p) {
        GroupKick request = (GroupKick) p.data;
        if (dataManager.isValidGroup(request.groupID) && dataManager.isValidUser(request.kickedMemberID) && dataManager.isValidUser(id)) {
            // Make sure they are not attempting to kick themselves, also make sure they have permission to kick members
            if (!id.equals(request.kickedMemberID) && dataManager.groups.get(request.groupID).canUserKickMembers(id)) {
                if (dataManager.leaveGroup(request.kickedMemberID, request.groupID)) {
                    // User kicked from the group, send a message about it
                    Message m = new Message();
                    m.sender = serverUser.uuid;
                    m.content = dataManager.users.get(request.kickedMemberID).username + " was removed from the group";
                    dataManager.getGroupChat(request.groupID).messages.add(m);
                    // Tell all other members of the group to update the chat
                    UpdateGroupChat followupPayload = new UpdateGroupChat();
                    followupPayload.groupID = request.groupID;
                    followupPayload.chat = dataManager.getGroupChat(request.groupID);
                    updateGroupMembers(request.groupID, new Packet(followupPayload));
                    serverLogCallback.accept(getLogClientDescriptor(id) + " was kicked from the group chat " + getLogGroupDescriptor(request.groupID));
                }
                sendUpdatedGroupList();
            }
        }
    }

    private void executeGroupAdd(UUID id, Packet p) {
        GroupAdd request = (GroupAdd) p.data;
        if (dataManager.isValidGroup(request.groupID)) {
            Group g = dataManager.groups.get(request.groupID);
            if (g.canUserAddMembers(request.groupID)) {
                StringBuilder addedUsers = new StringBuilder("(");
                for (UUID entry : request.membersToAdd) {
                    dataManager.addToGroup(entry, g.uuid);
                    addedUsers.append(getLogClientDescriptor(entry));
                    addedUsers.append(", ");
                }
                addedUsers.append(')');
                sendUpdatedGroupList();
                UpdateGroupChat followupPayload = new UpdateGroupChat();
                followupPayload.groupID = request.groupID;
                followupPayload.chat = dataManager.getGroupChat(request.groupID);
                updateGroupMembers(request.groupID, new Packet(followupPayload));
                serverLogCallback.accept(addedUsers.toString() + " were added to the group chat " + getLogGroupDescriptor(request.groupID));
            }
        }
    }

    private void executeGroupDelete(UUID id, Packet p) {
        GroupAdd request = (GroupAdd) p.data;
        if (dataManager.isValidGroup(request.groupID)) {
            Group g = dataManager.groups.get(request.groupID);
            if (g.canUserDeleteGroup(request.groupID)) {
                dataManager.removeGroup(request.groupID);
                sendUpdatedGroupList();
                serverLogCallback.accept(getLogClientDescriptor(id) + " deleted the group chat " + getLogGroupDescriptor(request.groupID));
            }
        }
    }

    private void executeGroupSettings(UUID id, Packet p) {
        GroupSettings request = (GroupSettings) p.data;
        if (dataManager.isValidGroup(request.groupID) && dataManager.isValidUser(id)) {
            Group g = dataManager.groups.get(request.groupID);
            if (g.canUserChangeSettings(id)) {
                g.allowInvites = request.allowInvites;
                g.isPrivate = request.isPrivate;
                Message m = new Message();
                m.sender = serverUser.uuid;
                m.content = dataManager.users.get(id).username + " changed the group settings";
                dataManager.getGroupChat(request.groupID).messages.add(m);
                // Tell all other members of the group to update the chat
                UpdateGroupChat followupPayload = new UpdateGroupChat();
                followupPayload.groupID = request.groupID;
                followupPayload.chat = dataManager.getGroupChat(request.groupID);
                updateGroupMembers(request.groupID, new Packet(followupPayload));
                serverLogCallback.accept(getLogClientDescriptor(id) + " changed the settings of the group " + getLogGroupDescriptor(request.groupID));
                sendUpdatedGroupList();
            }
        }
    }

    private void executeDirectMessage(UUID id, Packet p) {
        // Read the request
        DirectMessage request = (DirectMessage) p.data;
        UUID receiver = request.receiver;
        request.message.sender = id;

        // If the message is not empty
        if (!request.message.content.isEmpty()) {
            // If both the users exist, and they are two different users
            if (dataManager.isValidUser(id) && dataManager.isValidUser(request.receiver) && !request.receiver.equals(id)) {
                User u1 = dataManager.users.get(id);
                User u2 = dataManager.users.get(receiver);
                // Make sure one person doesn't have the other blocked
                if (u1.canCommunicateWith(u2) && u2.canCommunicateWith(u1)) {
                    // Add the message to the dm
                    Chat dm = dataManager.getDM(id, request.receiver);
                    dm.messages.add(request.message);
                    // Tell all other members of the group to update the chat
                    UpdateDirectMessage followupPayload = new UpdateDirectMessage();
                    followupPayload.user1ID = id;
                    followupPayload.user2ID = receiver;
                    followupPayload.chat = dm;
                    updateUser(id, new Packet(followupPayload));
                    updateUser(receiver, new Packet(followupPayload));
                    serverLogCallback.accept(getLogClientDescriptor(id) + " sent message to " + getLogClientDescriptor(receiver));
                }
            }
        }
    }

    private void executeBlockUser(UUID id, Packet p) {
        BlockUser request = (BlockUser) p.data;
        if (dataManager.isValidUser(id) && dataManager.isValidUser(request.userToBlock)) {
            if (request.shouldBlock) {
                dataManager.users.get(id).blockUser(request.userToBlock);
                serverLogCallback.accept(getLogClientDescriptor(id) + " blocked " + getLogClientDescriptor(request.userToBlock));
            } else {
                dataManager.users.get(id).unblockUser(request.userToBlock);
                serverLogCallback.accept(getLogClientDescriptor(id) + " unblocked " + getLogClientDescriptor(request.userToBlock));
            }
            sendUpdatedUserList();
        }
    }

    public void executeCommand(UUID id, Packet p) {
        serverLogCallback.accept("client: " + id + " sent: " + p.toString());
        synchronized (dataManager) {
            switch (p.data.type) {
                case LOGIN_ATTEMPT: {
                    executeLoginAttempt(id, p);
                    break;
                }
                case GROUP_CREATE: {
                    executeGroupCreate(id, p);
                    break;
                }
                case GROUP_MESSAGE: {
                    executeGroupMessage(id, p);
                    break;
                }
                case GROUP_LEAVE: {
                    executeGroupLeave(id, p);
                    break;
                }
                case GROUP_KICK: {
                    executeGroupKick(id, p);
                    break;
                }
                case GROUP_ADD: {
                    executeGroupAdd(id, p);
                    break;
                }
                case GROUP_DELETE: {
                    executeGroupDelete(id, p);
                    break;
                }
                case GROUP_SETTINGS: {
                    executeGroupSettings(id, p);
                    break;
                }
                case DIRECT_MESSAGE: {
                    executeDirectMessage(id, p);
                    break;
                }
                case BLOCK_USER: {
                    executeBlockUser(id, p);
                    break;
                }
                default: {
                    executeInvalidRequest(id, p);
                    break;
                }
            }
        }
    }

    public class TheServer extends Thread {

        public void run() {

            try (ServerSocket mySocket = new ServerSocket(5555);) {
                System.out.println("Server is waiting for a client!");
                serverLogCallback.accept("server initialized");

                while (true) {
                    UUID newUserUUID = null;
                    ClientThread c = null;
                    Socket s = mySocket.accept();
                    synchronized (dataManager) {
                        synchronized (clients) {
                            newUserUUID = dataManager.createNewUser();
                            c = new ClientThread(s, newUserUUID);
                            clients.put(newUserUUID, c);
                        }
                    }
                    c.start();
                    serverLogCallback.accept("client has connected to server, given id " + newUserUUID);
                }
            }//end of try
            catch (Exception e) {
                serverLogCallback.accept("Server socket did not launch");
            }
        }//end of while
    }

    public void updateUser(UUID u, Packet p) {
        if (dataManager.isValidUser(u)) {
            clients.get(u).sendPacket(p);
        }
    }

    public void updateGroupMembers(UUID g, Packet p) {
        if (dataManager.isValidGroup(g)) {
            Group group = dataManager.groups.get(g);
            if (group.isPrivate) {
                clients.get(group.creator).sendPacket(p);
                for (UUID id : group.users) {
                    clients.get(id).sendPacket(p);
                }
            } else {
                updateClients(p);
            }
        }
    }

    public void updateClients(Packet p) {
        synchronized (clients) {
            clients.forEach((key, value) -> {
                value.sendPacket(p);
            });
        }
    }

    public void sendUpdatedUserList() {
        UpdateUsers d = new UpdateUsers();
        synchronized (dataManager) {
            d.users = dataManager.users;
        }
        Packet p = new Packet(d);
        updateClients(p);
    }

    public void sendUpdatedGroupList() {
        UpdateGroups d = new UpdateGroups();
        synchronized (dataManager) {
            d.groups = dataManager.groups;
        }
        Packet p = new Packet(d);
        updateClients(p);
    }

    public void sendUpdatedGroupChats(UUID id) {
        synchronized (dataManager) {
            dataManager.groupChats.forEach((k, v) -> {
                Group g = dataManager.groups.get(k);
                if (g != null) {
                    if (!g.isPrivate || g.users.contains(id)) {
                        UpdateGroupChat ugc = new UpdateGroupChat();
                        ugc.groupID = k;
                        ugc.chat = v;
                        updateUser(id, new Packet(ugc));
                    }
                }
            });
        }
    }

    public void removeClient(UUID id) {
        System.out.println("Executing disconnect");

        synchronized (clients) {
            clients.remove(id);
        }

        serverLogCallback.accept(getLogClientDescriptor(id) + " has disconnected from server");
        if (dataManager.users.get(id).username != null) {
            Message leaveMessage = new Message();
            leaveMessage.content = dataManager.users.get(id).username + " has left the server";
            leaveMessage.sender = serverUser.uuid;
            dataManager.getGroupChat(globalChat.uuid).messages.add(leaveMessage);

            dataManager.groupChats.forEach((k, v) -> {
                for (Message m : v.messages) {
                    if (m.sender != null && m.sender.equals(id)) {
                        m.content = dataManager.users.get(id).username + "(disconnected): " + m.content;
                        m.sender = serverUser.uuid;
                    }
                }
            });

            synchronized (dataManager) {
                dataManager.removeUser(id);
            }

            UpdateGroupChat updateGlobalChatPayload = new UpdateGroupChat();
            updateGlobalChatPayload.groupID = globalChat.uuid;
            updateGlobalChatPayload.chat = dataManager.getGroupChat(globalChat.uuid);
            updateGroupMembers(globalChat.uuid, new Packet(updateGlobalChatPayload));
        }

        sendUpdatedUserList();
        sendUpdatedGroupList();
    }


    class ClientThread extends Thread {
        Socket connection;
        ObjectInputStream in;
        ObjectOutputStream out;
        UUID uuid;
        boolean isValid;

        ClientThread() {
            isValid = false;
        }

        ClientThread(Socket s, UUID uuid) {
            this.connection = s;
            this.uuid = uuid;
            this.isValid = true;
        }

        public void sendPacket(Packet p) {
            if (isValid) {
                synchronized (this.out) {
                    try {
                        this.out.reset();
                        this.out.writeObject(p);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void run() {
            if (isValid) {
                try {
                    in = new ObjectInputStream(connection.getInputStream());
                    out = new ObjectOutputStream(connection.getOutputStream());
                    connection.setTcpNoDelay(true);
                } catch (Exception e) {
                    System.out.println("Streams not open");
                }

                Connected connectedPayload = new Connected();
                connectedPayload.userID = this.uuid;
                Packet connectedPacket = new Packet(connectedPayload);
                sendPacket(connectedPacket);

                sendUpdatedUserList();
                sendUpdatedGroupList();
                sendUpdatedGroupChats(this.uuid);

                while (true) {
                    try {
                        Packet data = (Packet) in.readObject();
                        executeCommand(uuid, data);
                    } catch (Exception e) {
                        e.printStackTrace();
                        serverLogCallback.accept("Oops, something wrong with the socket from client: " + getLogClientDescriptor(this.uuid) + "... closing down!");
                        removeClient(this.uuid);
                        break;
                    }
                }
            }
        }//end of run


    }//end of client thread
}


	
	

	

import Data.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.UUID;
import java.util.function.Consumer;


public class Client extends Thread {


    Socket socketClient;

    ObjectOutputStream out;
    ObjectInputStream in;

    private final Consumer<Serializable> UIUpdateCallback;

    public UUID uuid;
    public final DataManager dataManager = new DataManager();
    public Data.Payload.Type lastOperation = Payload.Type.LOGIN_ATTEMPT;

    Client(Consumer<Serializable> call) {

        UIUpdateCallback = call;
    }

    private void executeUpdateUsers(UUID id, Packet p) {
        UpdateUsers d = (UpdateUsers) p.data;
        dataManager.users.clear();
        dataManager.users.putAll(d.users);
        UIUpdateCallback.accept(new GUICommand(GUICommand.Type.REFRESH));
    }

    private void executeUpdateGroups(UUID id, Packet p) {
        UpdateGroups d = (UpdateGroups) p.data;
        dataManager.groups.clear();
        dataManager.groups.putAll(d.groups);
        UIUpdateCallback.accept(new GUICommand(GUICommand.Type.REFRESH));
    }

    private void executeOperationResult(UUID id, Packet p) {
        OperationResult d = (OperationResult) p.data;
        if (d.status) {
            if (lastOperation == Payload.Type.LOGIN_ATTEMPT) {
                UIUpdateCallback.accept(new GUICommand(GUICommand.Type.LOGIN_SUCCESS));
            }
            else if (lastOperation == Payload.Type.GROUP_CREATE) {
                UIUpdateCallback.accept(new GUICommand(GUICommand.Type.GROUP_CREATE_SUCCESS));
            }
        } else {
            if (lastOperation == Payload.Type.LOGIN_ATTEMPT) {
                UIUpdateCallback.accept(new GUICommand(GUICommand.Type.LOGIN_ERROR));
            }
            else if (lastOperation == Payload.Type.GROUP_CREATE) {
                UIUpdateCallback.accept(new GUICommand(GUICommand.Type.GROUP_CREATE_ERROR));
            }
        }
    }

    private void executeUpdateGroupChat(UUID id, Packet p) {
        UpdateGroupChat d = (UpdateGroupChat) p.data;
        for (Data.Message m : d.chat.messages) {
            System.out.println(m.content);
        }
        dataManager.setGroupChat(d.groupID, d.chat);
        UIUpdateCallback.accept(new GUICommand(GUICommand.Type.REFRESH));
    }

    private void executeUpdateDirectMessage(UUID id, Packet p) {
        UpdateDirectMessage d = (UpdateDirectMessage) p.data;
        dataManager.setDirectMessage(d.user1ID, d.user2ID, d.chat);
        UIUpdateCallback.accept(new GUICommand(GUICommand.Type.REFRESH));
    }

    public void executeCommand(UUID id, Packet p) {
        if (id == null) {
            if (p.data.type == Payload.Type.CONNECTED) {
                Connected d = (Connected) p.data;
                this.uuid = d.userID;
                System.out.println("Client given UUID " + this.uuid);
            } else {
                System.out.println("Received non-connected message when id is null");
            }
            return;
        }
        synchronized (dataManager) {
            switch (p.data.type) {
                case UPDATE_USERS: {
                    executeUpdateUsers(id, p);
                    break;
                }
                case UPDATE_GROUPS: {
                    executeUpdateGroups(id, p);
                    break;
                }
                case OPERATION_RESULT: {
                    executeOperationResult(id, p);
                    break;
                }
                case UPDATE_GROUP_CHAT: {
                    executeUpdateGroupChat(id, p);
                    break;
                }
                case UPDATE_DIRECT_MESSAGE: {
                    executeUpdateDirectMessage(id, p);
                }
                default:
                    break;
            }
        }
    }

    public void run() {
        try {
            socketClient = new Socket("127.0.0.1", 5555);
            out = new ObjectOutputStream(socketClient.getOutputStream());
            in = new ObjectInputStream(socketClient.getInputStream());
            socketClient.setTcpNoDelay(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Client connected to server");

        while (true) {
            try {
                Packet p = (Packet) in.readObject();
                executeCommand(uuid, p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void send(Packet data) {
        try {
            synchronized (this.out) {
                this.out.reset();
                this.out.writeObject(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

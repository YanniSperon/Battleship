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
    public Boolean isSearchingForGame = false;
    public String privateGameJoinCode = null;
    public final DataManager dataManager = new DataManager();

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

    private void executeLoginResult(UUID id, Packet p) {
        LoginResult d = (LoginResult) p.data;
        if (d.status) {
            UIUpdateCallback.accept(new GUICommand(GUICommand.Type.LOGIN_SUCCESS));
        } else {
            UIUpdateCallback.accept(new GUICommand(GUICommand.Type.LOGIN_ERROR));
        }
    }

    private void executeGroupCreateResult(UUID id, Packet p) {
        GroupCreateResult d = (GroupCreateResult) p.data;
        if (d.status) {
            UIUpdateCallback.accept(new GUICommand(GUICommand.Type.GROUP_CREATE_SUCCESS));
        } else {
            UIUpdateCallback.accept(new GUICommand(GUICommand.Type.GROUP_CREATE_ERROR));
        }
    }

    private void executeUpdateGroupChat(UUID id, Packet p) {
        UpdateGroupChat d = (UpdateGroupChat) p.data;
        dataManager.setGroupChat(d.groupID, d.chat);
        UIUpdateCallback.accept(new GUICommand(GUICommand.Type.REFRESH));
    }

    private void executeUpdateDirectMessage(UUID id, Packet p) {
        UpdateDirectMessage d = (UpdateDirectMessage) p.data;
        dataManager.setDirectMessage(d.user1ID, d.user2ID, d.chat);
        UIUpdateCallback.accept(new GUICommand(GUICommand.Type.REFRESH));
    }

    private void executeGameFound(UUID id, Packet p) {
        GameFound d = (GameFound) p.data;
        GUIClient.currentActiveGame = d.user1.equals(id) ? d.user2 : d.user1;
        UIUpdateCallback.accept(new GUICommand(GUICommand.Type.GAME_FOUND));
    }

    private void executeUpdateGame(UUID id, Packet p) {
        UpdateGame d = (UpdateGame) p.data;
        dataManager.setGame(d.game.player1, d.game.player2, d.game);
        UIUpdateCallback.accept(new GUICommand(GUICommand.Type.REFRESH));
    }

    private void executeStartPrivateGameResult(UUID id, Packet p) {
        StartPrivateGameResult d = (StartPrivateGameResult) p.data;
        privateGameJoinCode = d.joinableID;
        UIUpdateCallback.accept(new GUICommand(GUICommand.Type.PRIVATE_STARTED));
    }

    private void executeJoinPrivateGameResult(UUID id, Packet p) {
        JoinPrivateGameResult d = (JoinPrivateGameResult) p.data;
        if (d.success) {
            GUIClient.currentActiveGame = d.otherUser;
            UIUpdateCallback.accept(new GUICommand(GUICommand.Type.JOIN_PRIVATE_SUCCESS));
        } else {
            UIUpdateCallback.accept(new GUICommand(GUICommand.Type.JOIN_PRIVATE_ERROR));
        }
    }

    private void executePlacePieceResult(UUID id, Packet p) {
        PlacePieceResult d = (PlacePieceResult) p.data;
        if (d.status) {
            UIUpdateCallback.accept(new GUICommand(GUICommand.Type.PLACE_PIECE_SUCCESS));
        } else {
            UIUpdateCallback.accept(new GUICommand(GUICommand.Type.PLACE_PIECE_ERROR));
        }
    }

    private void executeMoveResult(UUID id, Packet p) {
        MoveResult d = (MoveResult) p.data;
        if (d.status) {
            if (d.didHit) {
                UIUpdateCallback.accept(new GUICommand(GUICommand.Type.MOVE_SUCCESS_HIT));
            } else {
                UIUpdateCallback.accept(new GUICommand(GUICommand.Type.MOVE_SUCCESS_MISS));
            }
        } else {
            UIUpdateCallback.accept(new GUICommand(GUICommand.Type.MOVE_FAIL));
        }
    }

    private void executeTurnSwitch(UUID id, Packet p) {
        TurnSwitch d = (TurnSwitch) p.data;
        Game g = dataManager.getGame(d.p1, d.p2);
        synchronized (g.turn) {
            g.turn = d.newTurn;
        }
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
                case LOGIN_RESULT: {
                    executeLoginResult(id, p);
                    break;
                }
                case UPDATE_GROUP_CHAT: {
                    executeUpdateGroupChat(id, p);
                    break;
                }
                case UPDATE_DIRECT_MESSAGE: {
                    executeUpdateDirectMessage(id, p);
                    break;
                }
                case GROUP_CREATE_RESULT: {
                    executeGroupCreateResult(id, p);
                    break;
                }
                case UPDATE_GAME: {
                    executeUpdateGame(id, p);
                    break;
                }
                case GAME_FOUND: {
                    executeGameFound(id, p);
                    break;
                }
                case START_PRIVATE_GAME_RESULT: {
                    executeStartPrivateGameResult(id, p);
                    break;
                }
                case JOIN_PRIVATE_GAME_RESULT: {
                    executeJoinPrivateGameResult(id, p);
                    break;
                }
                case PLACE_PIECE_RESULT: {
                    executePlacePieceResult(id, p);
                    break;
                }
                case MOVE_RESULT: {
                    executeMoveResult(id, p);
                    break;
                }
                case TURN_SWITCH: {
                    executeTurnSwitch(id, p);
                    break;
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
                System.exit(1);
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

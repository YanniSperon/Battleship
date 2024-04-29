import Data.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Consumer;

public class Server {
    private final TheServer server;
    private final Consumer<Serializable> serverLogCallback;
    public final DataManager dataManager;
    private final HashMap<UUID, ClientThread> clients;
    public final User serverUser;
    public final Group globalChat;
    private final LinkedList<UUID> gameQueue;
    private final HashMap<String, Game> pendingPrivateGames;


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
        gameQueue = new LinkedList<UUID>();
        pendingPrivateGames = new HashMap<String, Game>();
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
        LoginResult replyPayload = new LoginResult();
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
        GroupCreateResult replyPayload = new GroupCreateResult();
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

    private void executeFindGame(UUID id, Packet p) {
        FindGame request = (FindGame) p.data;
        synchronized (gameQueue) {
            if (request.shouldFindGame) {
                if (request.vsAI) {
                    UUID firstID = id;
                    UUID secondID = serverUser.uuid;

                    Game newGame = dataManager.createGame(firstID, secondID);

                    GameFound replyPayload = new GameFound();
                    replyPayload.user1 = firstID;
                    replyPayload.user2 = secondID;
                    Packet reply = new Packet(replyPayload);
                    clients.get(firstID).sendPacket(reply);
                    clients.get(secondID).sendPacket(reply);

                    UpdateGame updateGamePayload = new UpdateGame();
                    updateGamePayload.user1 = firstID;
                    updateGamePayload.user2 = secondID;
                    updateGamePayload.game = newGame;
                    Packet updateGamePacket = new Packet(updateGamePayload);
                    updateGameMembers(newGame, updateGamePacket);

                    serverLogCallback.accept(getLogClientDescriptor(firstID) + " looking for game vs AI, starting AI game");
                } else {
                    if (gameQueue.contains(id)) {
                        // Ignore request, player already in queue
                    } else {
                        gameQueue.add(id);
                        if (gameQueue.size() >= 2) {
                            // At least two players are in queue, take the two at the front
                            UUID firstID = gameQueue.pollLast();
                            UUID secondID = gameQueue.pollLast();

                            Game newGame = dataManager.createGame(firstID, secondID);

                            GameFound replyPayload = new GameFound();
                            replyPayload.user1 = firstID;
                            replyPayload.user2 = secondID;
                            Packet reply = new Packet(replyPayload);
                            clients.get(firstID).sendPacket(reply);
                            clients.get(secondID).sendPacket(reply);

                            UpdateGame updateGamePayload = new UpdateGame();
                            updateGamePayload.user1 = firstID;
                            updateGamePayload.user2 = secondID;
                            updateGamePayload.game = newGame;
                            Packet updateGamePacket = new Packet(updateGamePayload);
                            updateGameMembers(newGame, updateGamePacket);

                            serverLogCallback.accept(getLogClientDescriptor(firstID) + " looking for game, found with " + getLogClientDescriptor(secondID));
                        } else {
                            serverLogCallback.accept(getLogClientDescriptor(id) + " looking for game");
                        }
                    }
                }
            } else {
                gameQueue.remove(id);
            }
        }
    }

    private void executeLeaveGame(UUID id, Packet p) {
        LeaveGame request = (LeaveGame) p.data;
        Game g = dataManager.leaveGame(id, request.otherUser, id);
        if (g != null) {
            UpdateGame gameEndingPayload = new UpdateGame();
            gameEndingPayload.game = g;
            gameEndingPayload.user1 = g.player1;
            gameEndingPayload.user2 = g.player2;

            updateGameMembers(g, new Packet(gameEndingPayload));

            // Update user list since xp should be awarded
            sendUpdatedUserList();

            serverLogCallback.accept(getLogClientDescriptor(id) + " left an active game, automatic loss");
        }
    }

    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom rnd = new SecureRandom();
    private static final int CODE_SIZE = 8;

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_SIZE);
        for (int i = 0; i < CODE_SIZE; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }

    private void executeStartPrivateGame(UUID id, Packet p) {
        StartPrivateGame request = (StartPrivateGame) p.data;

        String code = generateRandomCode();
        Game g = new Game();
        g.player1 = id;
        synchronized (pendingPrivateGames) {
            pendingPrivateGames.put(code, g);
        }

        StartPrivateGameResult resultPayload = new StartPrivateGameResult();
        resultPayload.joinableID = code;
        clients.get(id).sendPacket(new Packet(resultPayload));

        serverLogCallback.accept(getLogClientDescriptor(id) + " started a private game with code \"" + code + "\"");
    }

    private boolean isSpaceTaken(ArrayList<Piece> pieces, Coordinate c) {
        if (c == null || c.X < 0 || c.X >= 10 || c.Y < 0 || c.Y >= 10) {
            return true;
        }
        for (Piece p : pieces) {
            for (Coordinate otherCoord : p.getPositions()) {
                if (otherCoord.equals(c)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidPieceLocation(ArrayList<Piece> otherPieces, Piece p) {
        for (Coordinate c : p.getPositions()) {
            System.out.println("Position " + c);
            if (isSpaceTaken(otherPieces, c) || c.X < 0 || c.X > 9 || c.Y < 0 || c.Y > 9) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidPieceType(ArrayList<Piece> pieces, Piece p) {
        if (p.type == Piece.PieceType.NONE) {
            return false;
        }
        System.out.println("Existing pieces: ");
        for (Piece otherPiece : pieces) {
            System.out.println("  " + otherPiece);
            if (otherPiece.type == p.type) {
                return false;
            }
        }
        return true;
    }

    private boolean canExist(ArrayList<Piece> otherPieces, Piece p) {
        return isValidPieceType(otherPieces, p) && isValidPieceLocation(otherPieces, p);
    }

    private void executePlacePiece(UUID id, Packet p) {
        PlacePiece request = (PlacePiece) p.data;
        Game g = dataManager.getGame(request.opponent, id);
        System.out.println(request.piece);
        boolean wasSuccessful = false;
        if (g.player1.equals(id)) {
            System.out.println("We are player 1");
            // We are player 1
            if (g.player1Pieces.size() < 5) {
                System.out.println("Pieces size < 5");
                if (canExist(g.player1Pieces, request.piece)) {
                    System.out.println("Can exist");
                    // Valid
                    g.player1Pieces.add(request.piece);
                    PlacePieceResult res = new PlacePieceResult();
                    res.opponent = request.opponent;
                    res.status = true;
                    clients.get(id).sendPacket(new Packet(res));
                    serverLogCallback.accept(getLogClientDescriptor(id) + " placed a piece in their game with " + getLogClientDescriptor(g.player2));

                    wasSuccessful = true;
                }
            }
            if (g.player1Pieces.size() == 5 && g.player2.equals(serverUser.uuid)) {
                // Place AI pieces, AI will always be player 2 if it is an AI game
                generateAIPositions(g);
                wasSuccessful = true;
            }
        } else if (g.player2.equals(id)) {
            System.out.println("We are player 2");
            // We are player 2
            if (g.player2Pieces.size() < 5) {
                System.out.println("Pieces size < 5");
                if (canExist(g.player2Pieces, request.piece)) {
                    System.out.println("Can exist");
                    // Valid
                    g.player2Pieces.add(request.piece);
                    PlacePieceResult res = new PlacePieceResult();
                    res.opponent = request.opponent;
                    res.status = true;
                    clients.get(id).sendPacket(new Packet(res));
                    serverLogCallback.accept(getLogClientDescriptor(id) + " placed a piece in their game with " + getLogClientDescriptor(g.player1));

                    wasSuccessful = true;
                }
            }
        }

        if (!wasSuccessful) {
            // If we did not return above, the placement was invalid, indicate to user
            PlacePieceResult res = new PlacePieceResult();
            res.opponent = request.opponent;
            res.status = false;
            clients.get(id).sendPacket(new Packet(res));
            serverLogCallback.accept(getLogClientDescriptor(id) + " attempted to place a piece, but it failed");
        } else {
            UpdateGame updateGamePayload = new UpdateGame();
            updateGamePayload.game = g;
            updateGameMembers(g, new Packet(updateGamePayload));

            TurnSwitch setTurnPayload = new TurnSwitch();
            setTurnPayload.p1 = g.player1;
            setTurnPayload.p2 = g.player2;
            setTurnPayload.newTurn = Game.Player.PLAYER1;
            updateGameMembers(g, new Packet(setTurnPayload));
        }
    }

    // Sets piece.front
    private void placeAtRandomPosition(ArrayList<Piece> otherPieces, Piece p) {
        Random r = new Random();
        boolean isValid = false;
        while (!isValid) {
            p.front = new Coordinate(r.nextInt(10), r.nextInt(10));
            isValid = isValidPieceLocation(otherPieces, p);
        }
    }

    private void generateAIPositions(Game g) {
        System.out.println("Generating AI board positions");
        g.player2Pieces.clear();
        Random r = new Random();
        {
            Piece p1 = new Piece();
            p1.orientation = r.nextBoolean() ? Piece.PieceOrientation.DOWN : Piece.PieceOrientation.RIGHT;
            p1.type = Piece.PieceType.CARRIER;
            placeAtRandomPosition(g.player2Pieces, p1);
            g.player2Pieces.add(p1);
        }
        {
            Piece p1 = new Piece();
            p1.orientation = r.nextBoolean() ? Piece.PieceOrientation.DOWN : Piece.PieceOrientation.RIGHT;
            p1.type = Piece.PieceType.BATTLESHIP;
            placeAtRandomPosition(g.player2Pieces, p1);
            g.player2Pieces.add(p1);
        }
        {
            Piece p1 = new Piece();
            p1.orientation = r.nextBoolean() ? Piece.PieceOrientation.DOWN : Piece.PieceOrientation.RIGHT;
            p1.type = Piece.PieceType.CRUISER;
            placeAtRandomPosition(g.player2Pieces, p1);
            g.player2Pieces.add(p1);
        }
        {
            Piece p1 = new Piece();
            p1.orientation = r.nextBoolean() ? Piece.PieceOrientation.DOWN : Piece.PieceOrientation.RIGHT;
            p1.type = Piece.PieceType.SUBMARINE;
            placeAtRandomPosition(g.player2Pieces, p1);
            g.player2Pieces.add(p1);
        }
        {
            Piece p1 = new Piece();
            p1.orientation = r.nextBoolean() ? Piece.PieceOrientation.DOWN : Piece.PieceOrientation.RIGHT;
            p1.type = Piece.PieceType.DESTROYER;
            placeAtRandomPosition(g.player2Pieces, p1);
            g.player2Pieces.add(p1);
        }
        System.out.println("Finished generating AI board positions");
        System.out.println("AI size: " + g.player2Pieces.size());
    }

    private boolean isValidMove(ArrayList<Move> otherMoves, Move m) {
        for (Move otherMove : otherMoves) {
            if (otherMove.position.equals(m.position)) {
                return false;
            }
        }

        return true;
    }

    private Move getNextAIMove(ArrayList<Move> alreadyMadeMoves) {
        Random r = new Random();
        Move newMove = new Move(serverUser.uuid);
        newMove.position = new Coordinate(r.nextInt(10), r.nextInt(10));
        while (!isValidMove(alreadyMadeMoves, newMove)) {
            newMove.position = new Coordinate(r.nextInt(10), r.nextInt(10));
        }
        return newMove;
    }

    private void executeMakeMove(UUID id, Packet p) {
        MakeMove request = (MakeMove) p.data;
        Game g = dataManager.getGame(id, request.otherUser);
        if (g.player1Pieces.size() == 5 && g.player2Pieces.size() == 5 && g.winner == Game.Player.NONE) {
            // Only allow moves here since this means both players have placed their pieces down
            MoveResult res = new MoveResult();
            UUID sendingPlayer = null;
            boolean isP1 = false;
            boolean isVSAI = false;
            res.didHit = false;
            if (id.equals(g.player1)) {
                res.status = isValidMove(g.player1Moves, request.move);
                sendingPlayer = g.player1;
                isP1 = true;
                isVSAI = g.player2.equals(serverUser.uuid);
                res.opponent = g.player2;
                if (isSpaceTaken(g.player2Pieces, request.move.position)) {
                    res.didHit = true;
                }
                g.turn = Game.Player.PLAYER2;
                request.move.isHit = res.didHit;
                g.player1Moves.add(request.move);
            } else if (id.equals(g.player2)) {
                res.status = isValidMove(g.player2Moves, request.move);
                sendingPlayer = g.player2;
                res.opponent = g.player1;
                if (isSpaceTaken(g.player1Pieces, request.move.position)) {
                    res.didHit = true;
                }
                request.move.isHit = res.didHit;
                g.turn = Game.Player.PLAYER1;
                g.player2Moves.add(request.move);
            } else {
                serverLogCallback.accept(getLogClientDescriptor(id) + " does not belong to game, but tried to make move!");
                return;
            }

            serverLogCallback.accept(getLogClientDescriptor(id) + " attempted to make a move " + request.move);
            clients.get(id).sendPacket(new Packet(res));

            if (res.status && !isVSAI) {
                TurnSwitch setTurnPayload = new TurnSwitch();
                setTurnPayload.p1 = g.player1;
                setTurnPayload.p2 = g.player2;
                setTurnPayload.newTurn = g.turn;
                updateGameMembers(g, new Packet(setTurnPayload));
            }

            if (res.didHit) {
                serverLogCallback.accept(getLogClientDescriptor(id) + " hit with their move");
                g.updateGameOver(isP1 ? Game.Player.PLAYER1 : Game.Player.PLAYER2);
                if (g.winner != Game.Player.NONE) {
                    UpdateGame gameEndingPayload = new UpdateGame();
                    gameEndingPayload.game = g;
                    gameEndingPayload.user1 = g.player1;
                    gameEndingPayload.user2 = g.player2;
                    updateGameMembers(g, new Packet(gameEndingPayload));

                    dataManager.users.get(sendingPlayer).xp += 50;
                    sendUpdatedUserList();

                    serverLogCallback.accept(getLogClientDescriptor(id) + " won the game against " + getLogClientDescriptor(res.opponent) + "!");
                    return;
                }
            } else {
                serverLogCallback.accept(getLogClientDescriptor(id) + " missed with their move");
            }

            if (isVSAI) {
                System.out.println("Making AI move");
                // Make AI move
                Move newMove = getNextAIMove(g.player2Moves);
                g.turn = Game.Player.PLAYER1;
                if (isSpaceTaken(g.player1Pieces, newMove.position)) {
                    newMove.isHit = true;
                }
                g.player2Moves.add(newMove);
                if (newMove.isHit) {
                    g.updateGameOver(Game.Player.PLAYER2);
                    if (g.winner != Game.Player.NONE) {
                        UpdateGame gameEndingPayload = new UpdateGame();
                        gameEndingPayload.game = g;
                        gameEndingPayload.user1 = g.player1;
                        gameEndingPayload.user2 = g.player2;
                        updateGameMembers(g, new Packet(gameEndingPayload));

                        dataManager.users.get(serverUser.uuid).xp += 50;
                        sendUpdatedUserList();

                        serverLogCallback.accept(getLogClientDescriptor(id) + " won the game against " + getLogClientDescriptor(res.opponent) + "!");
                        return;
                    }
                }
            }

            UpdateGame gameStatusChangedPayload = new UpdateGame();
            gameStatusChangedPayload.game = g;
            gameStatusChangedPayload.user1 = g.player1;
            gameStatusChangedPayload.user2 = g.player2;
            updateGameMembers(g, new Packet(gameStatusChangedPayload));
        }
        MoveResult res = new MoveResult();
        res.status = false;
        res.didHit = false;
        res.opponent = null;
        clients.get(id).sendPacket(new Packet(res));
    }

    private void executeJoinPrivateGame(UUID id, Packet p) {
        JoinPrivateGame request = (JoinPrivateGame) p.data;
        if (pendingPrivateGames.containsKey(request.code)) {
            Game g = null;
            synchronized (pendingPrivateGames) {
                g = pendingPrivateGames.remove(request.code);
            }
            g.player2 = id;
            dataManager.setGame(g.player1, g.player2, g);

            JoinPrivateGameResult resultPayload = new JoinPrivateGameResult();
            resultPayload.otherUser = g.player1;
            resultPayload.success = true;
            clients.get(id).sendPacket(new Packet(resultPayload));

            UpdateGame replyPayloadForBoth = new UpdateGame();
            replyPayloadForBoth.user1 = g.player1;
            replyPayloadForBoth.user2 = g.player2;
            replyPayloadForBoth.game = g;
            Packet replyPacketForBoth = new Packet(replyPayloadForBoth);

            updateGameMembers(g, replyPacketForBoth);

            serverLogCallback.accept(getLogClientDescriptor(id) + " joined private game with code \"" + request.code + "\"");
        } else {
            JoinPrivateGameResult resultPayload = new JoinPrivateGameResult();
            resultPayload.otherUser = null;
            resultPayload.success = false;
            clients.get(id).sendPacket(new Packet(resultPayload));

            serverLogCallback.accept(getLogClientDescriptor(id) + " attempted to join a private game with code \"" + request.code + "\" but no game was found");
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
                case FIND_GAME: {
                    executeFindGame(id, p);
                    break;
                }
                case LEAVE_GAME: {
                    executeLeaveGame(id, p);
                    break;
                }
                case START_PRIVATE_GAME: {
                    executeStartPrivateGame(id, p);
                    break;
                }
                case PLACE_PIECE: {
                    executePlacePiece(id, p);
                    break;
                }
                case MAKE_MOVE: {
                    executeMakeMove(id, p);
                    break;
                }
                case JOIN_PRIVATE_GAME: {
                    executeJoinPrivateGame(id, p);
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

    public void updateGameMembers(Game g, Packet p) {
        Server.ClientThread t1 = clients.get(g.player1);
        if (t1 != null) {
            t1.sendPacket(p);
        }
        Server.ClientThread t2 = clients.get(g.player2);
        if (t2 != null) {
            t2.sendPacket(p);
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

        synchronized (gameQueue) {
            gameQueue.remove(id);
        }

        synchronized (clients) {
            clients.remove(id);
        }

        serverLogCallback.accept(getLogClientDescriptor(id) + " has disconnected from server");
        if (dataManager.users.get(id).username != null) {
            Message leaveMessage = new Message();
            leaveMessage.content = dataManager.users.get(id).username + " has left the server";
            leaveMessage.sender = serverUser.uuid;
            dataManager.getGroupChat(globalChat.uuid).messages.add(leaveMessage);

            dataManager.games.forEach((k, v) -> {
                boolean isPlayer1 = v.player1 != null && v.player1.equals(id);
                if (isPlayer1 || (v.player2 != null && v.player2.equals(id))) {
                    serverLogCallback.accept("Player left game, making other player winner");
                    System.out.println("Other player is " + (isPlayer1 ? v.player2 : v.player1));
                    UpdateGame gameEndingPayload = new UpdateGame();
                    gameEndingPayload.game = v;
                    gameEndingPayload.game.winner = isPlayer1 ? Game.Player.PLAYER2 : Game.Player.PLAYER1;
                    gameEndingPayload.game.turn = Game.Player.NONE;
                    UUID winnerID = isPlayer1 ? v.player2 : v.player1;
                    if (clients.containsKey(winnerID)) {
                        System.out.println("Sending packet to other player");
                        System.out.println(gameEndingPayload.game);
                        clients.get(winnerID).sendPacket(new Packet(gameEndingPayload));
                        if (dataManager.users.containsKey(winnerID)) {
                            dataManager.users.get(winnerID).xp += 50;
                        }
                    }

                    sendUpdatedUserList();
                }
            });

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


	
	

	

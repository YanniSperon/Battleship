package Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class Game implements Serializable {
    public ArrayList<Move> moves = new ArrayList<Move>();
    public UUID player1 = null;
    public UUID player2 = null;
    public ArrayList<Piece> user1Pieces = new ArrayList<Piece>();
    public ArrayList<Piece> user2Pieces = new ArrayList<Piece>();
    public enum Player {
        PLAYER1, PLAYER2, NONE
    }
    public Player winner = Player.NONE;
    public Player turn = Player.NONE;

    public Game() {
    }
}

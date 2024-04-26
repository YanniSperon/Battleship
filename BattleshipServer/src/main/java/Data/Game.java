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
    public enum GameEndReason {
        LEFT_GAME, WINNER, NONE
    }
    public Player winner = Player.NONE;
    public Player turn = Player.PLAYER1;
    public GameEndReason gameEndReason = GameEndReason.NONE;

    public Game() {
    }

    @Override
    public String toString() {
        return "Game(P1: \"" + player1.toString() + "\" P2: \"" + player2.toString() + "\" winner: \"" + winner.toString() + "\")";
    }
}

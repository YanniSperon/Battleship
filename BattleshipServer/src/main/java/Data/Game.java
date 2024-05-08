package Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class Game implements Serializable {
    public ArrayList<Move> player1Moves = new ArrayList<Move>();
    public ArrayList<Move> player2Moves = new ArrayList<Move>();
    public UUID player1 = null;
    public UUID player2 = null;
    public ArrayList<Piece> player1Pieces = new ArrayList<Piece>();
    public ArrayList<Piece> player2Pieces = new ArrayList<Piece>();

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

    public boolean hasMadeMove(Player p, Coordinate c) {
        if (p == Player.PLAYER1) {
            for (Move m : player1Moves) {
                if (m.position.equals(c)) {
                    return true;
                }
            }
        } else if (p == Player.PLAYER2) {
            for (Move m : player2Moves) {
                if (m.position.equals(c)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void updateGameOver(Player potentiallyWinningPlayer) {
        if (potentiallyWinningPlayer == Player.PLAYER2) {
            ArrayList<Coordinate> allP2Moves = new ArrayList<Coordinate>();
            for (Move m : player2Moves) {
                allP2Moves.add(m.position);
            }
            for (Piece p : player1Pieces) {
                for (Coordinate c : p.getPositions()) {
                    if (!allP2Moves.contains(c)) {
                        return;
                    }
                }
            }
            this.gameEndReason = GameEndReason.WINNER;
            this.winner = Player.PLAYER2;
        } else {
            ArrayList<Coordinate> allP1Moves = new ArrayList<Coordinate>();
            for (Move m : player1Moves) {
                allP1Moves.add(m.position);
            }
            for (Piece p : player2Pieces) {
                for (Coordinate c : p.getPositions()) {
                    if (!allP1Moves.contains(c)) {
                        return;
                    }
                }
            }
            this.gameEndReason = GameEndReason.WINNER;
            this.winner = Player.PLAYER1;
        }
        this.turn = Player.NONE;
    }

    // Returns a game instance which has elements that only include changes from one to the next
    public Game getDelta(Game olderGame) {
        Game deltaGame = new Game();
        for (Piece p : this.player1Pieces) {
            if (!olderGame.player1Pieces.contains(p)) {
                deltaGame.player1Pieces.add(p);
            }
        }
        for (Move m : this.player1Moves) {
            if (!olderGame.player1Moves.contains(m)) {
                deltaGame.player1Moves.add(m);
            }
        }
        for (Piece p : this.player2Pieces) {
            if (!olderGame.player2Pieces.contains(p)) {
                deltaGame.player2Pieces.add(p);
            }
        }
        for (Move m : this.player2Moves) {
            if (!olderGame.player2Moves.contains(m)) {
                deltaGame.player2Moves.add(m);
            }
        }
        return deltaGame;
    }
}

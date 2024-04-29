package Data;

import java.util.UUID;

public class TurnSwitch extends Payload {
    public Game.Player newTurn = Game.Player.PLAYER1;
    public UUID p1 = null;
    public UUID p2 = null;

    public TurnSwitch() {
        super(Type.TURN_SWITCH);
    }
}

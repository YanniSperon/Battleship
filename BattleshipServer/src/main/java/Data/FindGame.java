package Data;

public class FindGame extends Payload {
    // Cancelable by sending another payload with this set to false
    public boolean shouldFindGame = true;
    public boolean vsAI = false;

    public FindGame() {
        super(Type.FIND_GAME);
    }
}

import javafx.scene.Scene;

public class GUIView {
    public Scene scene;
    public CustomController controller;

    public GUIView(Scene s, CustomController c) {
        this.scene = s;
        this.controller = c;
    }
}
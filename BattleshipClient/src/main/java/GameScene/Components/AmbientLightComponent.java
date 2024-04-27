package GameScene.Components;

import javafx.scene.AmbientLight;

public class AmbientLightComponent extends Component {
    public AmbientLight light = null;

    public AmbientLightComponent() {
        this.type = ComponentType.AMBIENT_LIGHT;
        light = new AmbientLight();
    }

    @Override
    public void onAdded() {
        this.gameObject.childrenHolder.getChildren().add(light);
    }
}

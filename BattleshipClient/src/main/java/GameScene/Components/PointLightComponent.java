package GameScene.Components;

import javafx.scene.PointLight;

public class PointLightComponent extends Component {
    public PointLight light = null;

    public PointLightComponent() {
        this.type = ComponentType.POINT_LIGHT;
        light = new PointLight();
    }

    @Override
    public void onAdded() {
        this.gameObject.childrenHolder.getChildren().add(light);
    }
}

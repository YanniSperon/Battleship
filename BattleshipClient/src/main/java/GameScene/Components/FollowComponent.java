package GameScene.Components;

import GameScene.GameObject;
import javafx.geometry.Point3D;
import javafx.scene.Node;

public class FollowComponent extends Component {

    public Node objectToFollow = null;

    public FollowComponent() {
        this.type = ComponentType.FOLLOW;
    }

    @Override
    public void onRenderUpdate(double deltaTime) {
        if (objectToFollow != null) {
            Point3D posToGoTo = objectToFollow.getLocalToSceneTransform().transform(new Point3D(objectToFollow.getTranslateX(), objectToFollow.getTranslateY(), objectToFollow.getTranslateZ()));
            this.gameObject.setTranslation(posToGoTo);
        }
    }
}

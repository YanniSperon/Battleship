package GameScene.Components;

import Assets.MaterialManager;
import Assets.Mesh3D;
import Assets.MeshManager;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

public class Mesh3DComponent extends MovableComponent {
    public Mesh3D mesh3D = null;
    public MeshView meshView = null;

    public Mesh3DComponent() {
        this.type = ComponentType.MESH3D;
    }

    @Override
    public void onAdded() {
        this.meshView = new MeshView(mesh3D.mesh);
        meshView.setCullFace(CullFace.BACK);
        meshView.setUserData(this.gameObject);
        gameObject.childrenHolder.getChildren().add(this.meshView);
    }

    @Override
    public boolean onKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
            if (keyEvent.getCode() == KeyCode.F) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Bounds getBounds() {
        return meshView.getBoundsInLocal();
    }

    public MeshView selector = null;

    @Override
    public void onSelected() {
        super.onSelected();
        if (canSelect) {
            if (selector == null) {
                selector = new MeshView(MeshManager.load("Selector.obj").mesh);
                selector.setMaterial(MaterialManager.load("Selector.mat"));
                selector.setScaleX(meshView.boundsInLocalProperty().getValue().getWidth() * 0.6);
                selector.setScaleY(meshView.boundsInLocalProperty().getValue().getHeight() * 0.6);
                selector.setScaleZ(meshView.boundsInLocalProperty().getValue().getDepth() * 0.6);
                selector.setTranslateX(meshView.boundsInLocalProperty().getValue().getCenterX());
                selector.setTranslateY(meshView.boundsInLocalProperty().getValue().getCenterY());
                selector.setTranslateZ(meshView.boundsInLocalProperty().getValue().getCenterZ());
                selector.setMouseTransparent(true);
            }
            gameObject.childrenHolder.getChildren().add(selector);
        }
        if (onPressedCallback != null) {
            onPressedCallback.accept(gameObject.id);
        }
    }

    @Override
    public void onDeselected() {
        if (canSelect) {
            super.onDeselected();
            if (selector != null) {
                gameObject.childrenHolder.getChildren().remove(selector);
            }
        }
    }

    @Override
    protected ArrayList<Node> getPickableComponents() {
        ArrayList<Node> pickables = new ArrayList<Node>();
        pickables.add(meshView);
        return pickables;
    }
}

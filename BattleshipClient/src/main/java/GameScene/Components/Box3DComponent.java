package GameScene.Components;

import Assets.MaterialManager;
import Assets.MeshManager;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;

import java.util.ArrayList;

public class Box3DComponent extends MovableComponent {
    public Box box = null;

    public Box3DComponent() {
        this.type = ComponentType.BOX3D;

        this.box = new Box(1.0, 1.0, 1.0);
        box.setCullFace(CullFace.BACK);
    }

    @Override
    public void onAdded() {
        gameObject.childrenHolder.getChildren().add(this.box);
        box.setUserData(this.gameObject);
    }

    @Override
    public Bounds getBounds() {
        return box.getBoundsInLocal();
    }

    public MeshView selector = null;

    @Override
    public void onSelected() {
        super.onSelected();
        if (selector == null) {
            selector = new MeshView(MeshManager.load("Selector.obj").mesh);
            selector.setMaterial(MaterialManager.load("Selector.mat"));
            selector.setScaleX(box.boundsInLocalProperty().getValue().getWidth() * 0.60);
            selector.setScaleY(box.boundsInLocalProperty().getValue().getHeight() * 0.60);
            selector.setScaleZ(box.boundsInLocalProperty().getValue().getDepth() * 0.60);
            selector.setMouseTransparent(true);
        }
        gameObject.childrenHolder.getChildren().add(selector);
    }

    @Override
    public void onDeselected() {
        super.onDeselected();
        if (selector != null) {
            gameObject.childrenHolder.getChildren().remove(selector);
        }
    }

    @Override
    protected ArrayList<Node> getPickableComponents() {
        ArrayList<Node> pickables = new ArrayList<Node>();
        pickables.add(box);
        return pickables;
    }
}

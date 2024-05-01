package GameScene.Components;

import Assets.MaterialManager;
import Assets.MeshManager;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Material;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;

import java.util.Random;

public class SpawnerComponent extends Component {
    public Mesh meshToSpawn;
    public Material materialToApply;
    public Group g;
    public Point3D min;
    public Point3D max;
    // Only used if random
    public int count = 1;

    public boolean isRandom = false;
    public int stepsX = 1;
    public int stepsY = 1;
    // Not implemented yet
    public int stepsZ = 1;
    public Rotate rotation = null;

    public SpawnerComponent()
    {
        this.type = ComponentType.SPAWNER;
    }

    @Override
    public void onAdded() {
        g = new Group();
        gameObject.childrenHolder.getChildren().add(g);
        if (isRandom) {
            Random r = new Random();
            Point3D trueMin = new Point3D(Math.min(min.getX(), max.getX()), Math.min(min.getY(), max.getY()), Math.min(min.getZ(), max.getZ()));
            Point3D trueMax = new Point3D(Math.max(min.getX(), max.getX()), Math.max(min.getY(), max.getY()), Math.max(min.getZ(), max.getZ()));
            for (int i = 0; i < count; ++i) {
                MeshView m = new MeshView(meshToSpawn);
                m.setMaterial(materialToApply);
                m.setTranslateX(trueMin.getX() + (trueMax.getX() - trueMin.getX()) * r.nextDouble());
                m.setTranslateY(trueMin.getY() + (trueMax.getY() - trueMin.getY()) * r.nextDouble());
                m.setTranslateZ(trueMin.getZ() + (trueMax.getZ() - trueMin.getZ()) * r.nextDouble());
                g.getChildren().add(m);
            }
        } else {
            Point3D delta = max.subtract(min);
            Point3D totalDeltaAcrossColumns = new Point3D(0.0, 0.0, delta.getZ());
            Point3D totalDeltaAcrossRows = new Point3D(delta.getX(), delta.getY(), 0.0);

            Point3D deltaPerRow = totalDeltaAcrossRows.multiply(1.0 / (stepsY - 1.0));
            Point3D deltaPerColumn = totalDeltaAcrossColumns.multiply(1.0 / (stepsX - 1.0));

            for (int y = 0; y < stepsY; ++y) {
                Point3D thisRow = min.add(deltaPerRow.multiply(y));
                for (int x = 0; x < stepsX; ++x) {
                    Point3D newLoc = thisRow.add(deltaPerColumn.multiply(x));
                    MeshView m = new MeshView(meshToSpawn);
                    m.setMaterial(materialToApply);
                    m.setTranslateX(newLoc.getX());
                    m.setTranslateY(newLoc.getY());
                    m.setTranslateZ(newLoc.getZ());
                    m.setMouseTransparent(true);
                    g.getChildren().add(m);
                }
            }
        }
    }
}

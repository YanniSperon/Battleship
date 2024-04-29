package GameScene.Components;

import Assets.AnimatedMesh3D;
import Assets.Mesh3D;
import javafx.animation.Interpolator;
import javafx.scene.shape.Mesh;

public class AnimationControllerComponent extends Component {
    private double playbackSpeed = 1.0;
    public Interpolator interpolator = Interpolator.EASE_BOTH;
    private double currentAnimationTime = 0.0;
    private double secondsBetweenFrames = 1.0;
    private boolean shouldLoop = false;
    private boolean isPlaying = false;
    private boolean shouldDisappearAfterPlaying = false;

    public AnimationControllerComponent() {
        this.type = ComponentType.ANIMATION_CONTROLLER;
    }

    @Override
    public void onRenderUpdate(double deltaTime) {
        if (isPlaying) {
            Component c = gameObject.getComponentOfType(ComponentType.MESH3D);
            if (c != null) {
                Mesh3DComponent m3dc = (Mesh3DComponent) c;
                if (m3dc.mesh3D.type == Mesh3D.Type.ANIMATED) {
                    AnimatedMesh3D am = (AnimatedMesh3D) m3dc.mesh3D;
                    if (interpolator != null) {
                        am.setAnimationPosition(interpolator.interpolate(0.0, am.getLargestAnimationPosition(), currentAnimationTime / am.getLargestAnimationPosition()));
                    }
                    currentAnimationTime += (deltaTime / secondsBetweenFrames) * playbackSpeed;
                    if (currentAnimationTime >= am.getLargestAnimationPosition()) {
                        currentAnimationTime = 0.0;
                        if (shouldDisappearAfterPlaying) {
                            m3dc.meshView.setVisible(false);
                        }
                        isPlaying = shouldLoop;
                    }
                }
            }
        }
    }

    public void startPlaying() {
        currentAnimationTime = 0.0;
        isPlaying = true;
        Component c = gameObject.getComponentOfType(ComponentType.MESH3D);
        if (c != null) {
            Mesh3DComponent m3dc = (Mesh3DComponent) c;
            if (m3dc.mesh3D.type == Mesh3D.Type.ANIMATED) {
                m3dc.meshView.setVisible(true);
            }
        }
    }

    public void setShouldLoop(boolean shouldLoop) {
        this.shouldLoop = shouldLoop;
    }

    public void setPlaybackSpeed(double playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
    }

    public void setTimeBetweenFrames(double secondsBetweenFrames) {
        this.secondsBetweenFrames = secondsBetweenFrames;
    }
}

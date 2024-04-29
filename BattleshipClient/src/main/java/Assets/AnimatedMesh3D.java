package Assets;

import javafx.application.Platform;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

import java.util.ArrayList;

public class AnimatedMesh3D extends Mesh3D {
    private final ArrayList<PrimitiveFloatArrayList> frames;
    private PrimitiveFloatArrayList outputFrame;
    private double currMeshPos;
    private final Object isUpdatingMutex;
    private boolean isUpdating;

    public AnimatedMesh3D() {
        super(null);
        frames = new ArrayList<PrimitiveFloatArrayList>();
        currMeshPos = -1.0;
        type = Type.ANIMATED;
        isUpdatingMutex = new Object();
        isUpdating = false;
    }

    public void setInitialMesh(TriangleMesh m) {
        this.mesh = m;
        m.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
    }

    public void addFrame(PrimitiveFloatArrayList newFrame) {
        if (frames.isEmpty()) {
            outputFrame = new PrimitiveFloatArrayList(newFrame.size);
        }
        frames.add(newFrame);
    }

    public void setAnimationPosition(double pos) {
        boolean shouldRun = false;
        synchronized (isUpdatingMutex) {
            shouldRun = !isUpdating;
        }
        if (shouldRun) {
            synchronized (isUpdatingMutex) {
                isUpdating = true;
            }
            pos = Math.max(0.0, Math.min(pos, (double) frames.size()));
            if (pos != currMeshPos) {
                currMeshPos = pos;
                int indexOfCurrFrameLeft = Math.min((int) pos, frames.size() - 2);
                int indexOfCurrFrameRight = indexOfCurrFrameLeft + 1;

                double t = pos - indexOfCurrFrameLeft;

                if (t == 0.0) {
                    mesh.getPoints().setAll(frames.get(indexOfCurrFrameLeft).data);
                    synchronized (isUpdatingMutex) {
                        isUpdating = false;
                    }
                } else {
                    Thread taskThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            float tf = (float) t;
                            float oneMinusTF = 1.0f - tf;
                            float[] res = outputFrame.data;
                            float[] left = frames.get(indexOfCurrFrameLeft).data;
                            float[] right = frames.get(indexOfCurrFrameRight).data;

                            for (int i = 0; i < res.length; ++i) {
                                res[i] = (oneMinusTF * left[i]) + (tf * right[i]);
                            }

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    mesh.getPoints().setAll(outputFrame.data);
                                    synchronized (isUpdatingMutex) {
                                        isUpdating = false;
                                    }
                                }
                            });
                        }
                    });
                    taskThread.start();
                }
            } else {
                synchronized (isUpdatingMutex) {
                    isUpdating = false;
                }
            }
        }
    }

    public double getLargestAnimationPosition() {
        return ((double) frames.size() - 1);
    }
}

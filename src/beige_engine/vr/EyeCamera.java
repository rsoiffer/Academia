package beige_engine.vr;

import beige_engine.graphics.Camera;
import beige_engine.util.math.Transformation;
import org.joml.Matrix4d;

import static beige_engine.vr.OpenVRUtils.*;
import static beige_engine.vr.Vive.vrCoords;

public class EyeCamera extends Camera {

    static Transformation headPoseRaw = Transformation.IDENTITY;

    private final Matrix4d eyeToHeadTransform, eyeProjectionMatrix;

    public EyeCamera(boolean leftEye) {
        eyeToHeadTransform = getEyeToHeadTransform(leftEye);
        eyeProjectionMatrix = getEyeProjectionMatrix(leftEye);
    }

    private static Transformation headPoseInternal() {
        return vrCoords().mul(headPoseRaw);
    }

    public static Transformation headPose() {
        return Vive.footTransform.get().mul(headPoseRaw.conjugate(new Transformation(Vive.COORD_CHANGE)));
    }

    public static void waitUpdatePos() {
        headPoseRaw = new Transformation(waitGetPoses());
    }

    @Override
    public Matrix4d projectionMatrix() {
        return new Matrix4d(eyeProjectionMatrix);
    }

    @Override
    public Matrix4d viewMatrix() {
        return new Matrix4d(eyeToHeadTransform)
                .mul(headPoseInternal().matrix().invert());
    }
}

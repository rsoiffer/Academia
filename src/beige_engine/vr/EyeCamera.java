package beige_engine.vr;

import beige_engine.graphics.Camera;
import org.joml.Matrix4d;
import beige_engine.util.math.Transformation;
import static beige_engine.vr.OpenVRUtils.getEyeProjectionMatrix;
import static beige_engine.vr.OpenVRUtils.getEyeToHeadTransform;
import static beige_engine.vr.OpenVRUtils.waitGetPoses;
import static beige_engine.vr.Vive.vrCoords;

public class EyeCamera extends Camera {

    static Transformation headPoseRaw = Transformation.IDENTITY;

    private final Matrix4d eyeToHeadTransform, eyeProjectionMatrix;

    public EyeCamera(boolean leftEye) {
        eyeToHeadTransform = getEyeToHeadTransform(leftEye);
        eyeProjectionMatrix = getEyeProjectionMatrix(leftEye);
    }

    public static Transformation headPose() {
        return vrCoords().mul(headPoseRaw);
    }

    @Override
    public Matrix4d projectionMatrix() {
        return new Matrix4d(eyeProjectionMatrix);
    }

    @Override
    public Matrix4d viewMatrix() {
        return new Matrix4d(eyeToHeadTransform)
                .mul(headPose().matrix().invert());
    }

    public static void waitUpdatePos() {
        headPoseRaw = new Transformation(waitGetPoses());
    }
}

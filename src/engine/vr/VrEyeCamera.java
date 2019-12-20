package engine.vr;

import engine.graphics.Camera;
import engine.util.math.Transformation;
import static engine.vr.VrCore.vrCoords;
import static engine.vr.VrUtils.*;
import org.joml.Matrix4d;

public class VrEyeCamera extends Camera {

    static Transformation headPoseRaw = Transformation.IDENTITY;

    private final Matrix4d eyeToHeadTransform, eyeProjectionMatrix;

    public VrEyeCamera(boolean leftEye) {
        eyeToHeadTransform = getEyeToHeadTransform(leftEye);
        eyeProjectionMatrix = getEyeProjectionMatrix(leftEye);
    }

    private static Transformation headPoseInternal() {
        return vrCoords().mul(headPoseRaw);
    }

    public static Transformation headPose() {
        return VrCore.footTransform.get().mul(headPoseRaw.conjugate(new Transformation(VrCore.COORD_CHANGE)));
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

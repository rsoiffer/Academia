package beige_engine.vr;

import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Matrix4x3d;
import org.lwjgl.openvr.*;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

abstract class OpenVRUtils {

    static void checkError(int ecode) {
        if (ecode != 0) {
            throw new RuntimeException("VR Error (code " + ecode + ")");
        }
    }

    //    static Matrix4d getDeviceToAbsoluteTrackingPose() {
//        try (MemoryStack stack = MemoryStack.stackPush()) {
//            ByteBuffer bb = stack.malloc(128); // value unknown, 128 is a safe upper bound
//            TrackedDevicePose.Buffer tdp = new TrackedDevicePose.Buffer(bb);
//            VRSystem.VRSystem_GetDeviceToAbsoluteTrackingPose(
//                    VR.ETrackingUniverseOrigin_TrackingUniverseStanding, 0, tdp);
//            return read4x3Matrix(tdp.mDeviceToAbsoluteTracking().m()).invert();
//        }
//    }
    static Matrix4d getEyeProjectionMatrix(boolean leftEye) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer bb = stack.malloc(64);
            VRSystem.VRSystem_GetProjectionMatrix(leftEye ? VR.EVREye_Eye_Left : VR.EVREye_Eye_Right, .2f, 2000, new HmdMatrix44(bb));
            return read4x4Matrix(bb.asFloatBuffer());
        }
    }

    static Matrix4d getEyeToHeadTransform(boolean leftEye) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer bb = stack.malloc(48);
            VRSystem.VRSystem_GetEyeToHeadTransform(leftEye ? VR.EVREye_Eye_Left : VR.EVREye_Eye_Right, new HmdMatrix34(bb));
            return read4x3Matrix(bb.asFloatBuffer()).invert();
        }
    }

    static Matrix4d read4x4Matrix(FloatBuffer b) {
        return new Matrix4d(new Matrix4f(b)).transpose();
    }

    static Matrix4d read4x3Matrix(FloatBuffer b) {
        return new Matrix4d(new Matrix4x3d(
                b.get(0), b.get(4), b.get(8),
                b.get(1), b.get(5), b.get(9),
                b.get(2), b.get(6), b.get(10),
                b.get(3), b.get(7), b.get(11)
        ));
    }

    static Matrix4d waitGetPoses() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer bb1 = stack.malloc(128); // value unknown, 128 is a safe upper bound
            TrackedDevicePose.Buffer tdp1 = new TrackedDevicePose.Buffer(bb1);
            ByteBuffer bb2 = stack.malloc(128); // value unknown, 128 is a safe upper bound
            TrackedDevicePose.Buffer tdp2 = new TrackedDevicePose.Buffer(bb2);
            int ecode = VRCompositor.VRCompositor_WaitGetPoses(tdp1, tdp2);
            checkError(ecode);
            return read4x3Matrix(tdp1.mDeviceToAbsoluteTracking().m());
        }
    }
}

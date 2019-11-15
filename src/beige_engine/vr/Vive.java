package beige_engine.vr;

import beige_engine.graphics.opengl.Framebuffer;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import org.joml.Matrix4d;
import org.lwjgl.openvr.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.function.Supplier;

import static beige_engine.vr.OpenVRUtils.checkError;

public class Vive {

    public static final int MENU = 1, GRIP = 2, TRACKPAD = 32, TRIGGER = 33;

    static final Matrix4d COORD_CHANGE = new Matrix4d(
            0, 0, -1, 0,
            -1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 0, 1).invert();
    public static Supplier<Transformation> footTransform = () -> Transformation.IDENTITY;

    public static boolean running;

    public static ViveController RIGHT, LEFT;
    public static Framebuffer leftEye, rightEye;

    public static void main(String[] args) {
        init();
        System.out.println("Initialized sucessfully!");
        System.out.println("Recommended size is " + getRecommendedRenderTargetSize());
        for (int i = 0; i < 100; i++) {
            EyeCamera.waitUpdatePos();
            System.out.println("Current HMD position in VR coords:");
            System.out.println(EyeCamera.headPoseRaw.position());
            System.out.println("Current HMD position in game coords:");
            System.out.println(EyeCamera.headPose().position());
            System.out.println();

            LEFT.update();
            System.out.println("Current LEFT controller position in VR coords:");
            System.out.println(LEFT.poseRaw().position());
            System.out.println("Current LEFT controller position in game coords:");
            System.out.println(LEFT.pose().position());
            System.out.println();

            System.out.println("Current LEFT controller direction in VR coords:");
            System.out.println(LEFT.poseRaw().applyRotation(new Vec3d(1, 0, 0)));
            System.out.println("Current LEFT controller direction in game coords:");
            System.out.println(LEFT.pose().applyRotation(new Vec3d(1, 0, 0)));
            System.out.println();

            System.out.println("Current LEFT controller upwards in VR coords:");
            System.out.println(LEFT.poseRaw().applyRotation(new Vec3d(0, 0, 1)));
            System.out.println("Current LEFT controller upwards in game coords:");
            System.out.println(LEFT.pose().applyRotation(new Vec3d(0, 0, 1)));
            System.out.println();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
        shutdown();
    }

    public static boolean canStart() {
        return VR.VR_IsRuntimeInstalled() && VR.VR_IsHmdPresent();
    }

    public static Vec2d getRecommendedRenderTargetSize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            VRSystem.VRSystem_GetRecommendedRenderTargetSize(w, h);
            return new Vec2d(w.get(), h.get());
        }
    }

    public static void init() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer peError = stack.mallocInt(1);
            int token = VR.VR_InitInternal(peError, VR.EVRApplicationType_VRApplication_Scene);
            checkError(peError.get());
            OpenVR.create(token);
        }
        for (int i = 0; i < 64; i++) {
            if (VRSystem.VRSystem_IsTrackedDeviceConnected(i)) {
                int deviceClass = VRSystem.VRSystem_GetTrackedDeviceClass(i);
                if (deviceClass == VR.ETrackedDeviceClass_TrackedDeviceClass_Controller) {
                    ViveController vc = new ViveController(i);
                    vc.update();
                    if (VRSystem.VRSystem_GetControllerRoleForTrackedDeviceIndex(i) == 1) {
                        LEFT = vc;
                    } else {
                        RIGHT = vc;
                    }
                }
            }
        }
        running = true;
    }

    public static void resetRightLeft() {
        Vec3d rightPos = RIGHT.pose().position();
        Vec3d leftPos = LEFT.pose().position();
        if (rightPos.y > leftPos.y) {
            ViveController temp = RIGHT;
            RIGHT = LEFT;
            LEFT = temp;
        }
    }

    public static void resetSeatedZeroPose() {
        VRSystem.VRSystem_ResetSeatedZeroPose();
    }

    public static void shutdown() {
        VR.VR_ShutdownInternal();
    }

    public static void submit(boolean leftEye, beige_engine.graphics.opengl.Texture t) {
        Texture pTexture = Texture.create();
        pTexture.handle(t.id);
        pTexture.eType(VR.ETextureType_TextureType_OpenGL);
        pTexture.eColorSpace(VR.EColorSpace_ColorSpace_Auto);
        VRTextureBounds pBounds = null;
        int ecode = VRCompositor.VRCompositor_Submit(leftEye ? VR.EVREye_Eye_Left : VR.EVREye_Eye_Right,
                pTexture, pBounds, 0);
        checkError(ecode);
    }

    public static void update() {
        RIGHT.update();
        LEFT.update();
    }

    public static Transformation vrCoords() {
        return footTransform.get().mul(new Transformation(COORD_CHANGE));
    }
}

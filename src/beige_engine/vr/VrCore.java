package beige_engine.vr;

import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec2d;
import static beige_engine.vr.VrUtils.checkError;
import java.nio.IntBuffer;
import java.util.function.Supplier;
import org.joml.Matrix4d;
import org.lwjgl.openvr.*;
import org.lwjgl.system.MemoryStack;

public class VrCore {

    /**
     * The ID numbers corresponding to buttons on the controller
     */
    public static final int MENU = 1, GRIP = 2, TRACKPAD = 32, TRIGGER = 33;

    /**
     * The coordinate transformation from OpenVR space (y up) to game object space (z up)
     */
    static final Matrix4d COORD_CHANGE = new Matrix4d(
            0, 0, -1, 0,
            -1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 0, 1).invert();
    /**
     * The location in the world of the foot of the VR character
     */
    public static Supplier<Transformation> footTransform = () -> Transformation.IDENTITY;

    /**
     * The left and right VR controllers (or null if the controller is disconnected)
     */
    public static VrController RIGHT, LEFT;

    /**
     * Checks if we can start the VR runtime
     *
     * @return If we can start the VR runtime
     */
    public static boolean canStart() {
        return VR.VR_IsRuntimeInstalled() && VR.VR_IsHmdPresent();
    }

    /**
     * Gets the size of the display for one eye in the VR headset
     *
     * @return The size of the display, in pixels
     */
    public static Vec2d getRecommendedRenderTargetSize() {
        try ( MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            VRSystem.VRSystem_GetRecommendedRenderTargetSize(w, h);
            return new Vec2d(w.get(), h.get());
        }
    }

    /**
     * Initializes the VR runtime
     */
    public static void init() {
        try ( MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer peError = stack.mallocInt(1);
            int token = VR.VR_InitInternal(peError, VR.EVRApplicationType_VRApplication_Scene);
            checkError(peError.get());
            OpenVR.create(token);
        }
        initControllers();
    }

    /**
     * Attempts to connect to the VR controllers
     */
    public static void initControllers() {
        for (int i = 0; i < 64; i++) {
            if (VRSystem.VRSystem_IsTrackedDeviceConnected(i)) {
                int deviceClass = VRSystem.VRSystem_GetTrackedDeviceClass(i);
                if (deviceClass == VR.ETrackedDeviceClass_TrackedDeviceClass_Controller) {
                    VrController vc = new VrController(i);
                    vc.update();
                    if (VRSystem.VRSystem_GetControllerRoleForTrackedDeviceIndex(i) == 1) {
                        LEFT = vc;
                    } else {
                        RIGHT = vc;
                    }
                }
            }
        }
    }

    public static void resetSeatedZeroPose() {
        VRSystem.VRSystem_ResetSeatedZeroPose();
    }

    public static void shutdown() {
        VR.VR_ShutdownInternal();
    }

    public static void submit(boolean leftEye, beige_engine.graphics.opengl.Texture t) {
        var pTexture = Texture.create();
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

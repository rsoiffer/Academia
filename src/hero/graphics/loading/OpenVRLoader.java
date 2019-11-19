package hero.graphics.loading;

import beige_engine.graphics.opengl.Texture;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.ViveController;
import hero.graphics.ModelNode;
import hero.graphics.materials.DiffuseMaterial;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openvr.*;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.system.MemoryStack.stackPush;

public class OpenVRLoader {

    public final ModelNode rootNode;

    public OpenVRLoader(ViveController vc) {
        RenderModel rm;
        try (MemoryStack stack = stackPush()) {
            PointerBuffer pb = stack.callocPointer(1);
            String renderModelName = vc.getPropertyString(VR.ETrackedDeviceProperty_Prop_RenderModelName_String);
            while (VRRenderModels.VRRenderModels_LoadRenderModel_Async(renderModelName, pb) == 100) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            int ecode = VRRenderModels.VRRenderModels_LoadRenderModel_Async(renderModelName, pb);
            if (ecode != 0) {
                throw new RuntimeException("Could not load OpenVR render model: "
                        + VRRenderModels.VRRenderModels_GetRenderModelErrorNameFromEnum(ecode));
            }
            rm = RenderModel.create(pb.get());
        }
        RenderModelTextureMap rmtm;
        try (MemoryStack stack = stackPush()) {
            PointerBuffer pb = stack.callocPointer(1);
            while (VRRenderModels.VRRenderModels_LoadTexture_Async(rm.diffuseTextureId(), pb) == 100) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            int ecode = VRRenderModels.VRRenderModels_LoadTexture_Async(rm.diffuseTextureId(), pb);
            if (ecode != 0) {
                throw new RuntimeException("Could not load OpenVR render model diffuse texture: "
                        + VRRenderModels.VRRenderModels_GetRenderModelErrorNameFromEnum(ecode));
            }
            rmtm = RenderModelTextureMap.create(pb.get());
        }

        Quaternion rotator = Quaternion.fromXYAxes(new Vec3d(0, -1, 0), new Vec3d(0, 0, 1));
        var RMB = new RawMeshBuilder();
        rm.rVertexData().forEach(rmv -> RMB.addVertex(
                rotator.applyTo(toVec3d(rmv.vPosition())),
                new Vec2d(rmv.rfTextureCoord(0), rmv.rfTextureCoord(1)),
                rotator.applyTo(toVec3d(rmv.vNormal())),
                new Vec3d(0, 0, 0),
                new Vec3d(0, 0, 0)));

        var sb = rm.IndexData();
        while (sb.hasRemaining()) {
            RMB.addIndices((int) sb.get());
        }

        var diffuseTexture = new Texture(GL_TEXTURE_2D);
        diffuseTexture.setParameter(GL_TEXTURE_MAX_LEVEL, 4);
        diffuseTexture.uploadData(rmtm.unWidth(), rmtm.unHeight(), rmtm.rubTextureMapData(4 * rmtm.unWidth() * rmtm.unHeight()));
        var diffuseMat = new DiffuseMaterial();
        diffuseMat.texture = diffuseTexture;

        rootNode = new ModelNode(diffuseMat.buildRenderable(RMB.toRawMesh()));
    }

    private static Vec3d toVec3d(HmdVector3 v) {
        return new Vec3d(v.v(0), v.v(1), v.v(2));
    }
}

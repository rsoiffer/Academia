package hero.graphics.models;

import beige_engine.util.math.Quaternion;
import hero.graphics.models.Vertex.VertexPBR;
import beige_engine.graphics.opengl.BufferObject;
import beige_engine.graphics.opengl.Shader;
import beige_engine.graphics.opengl.Texture;
import beige_engine.graphics.opengl.VertexArrayObject;

import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.PointerBuffer;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11C.glDrawElements;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.opengl.GL15C.GL_ELEMENT_ARRAY_BUFFER;
import org.lwjgl.openvr.HmdVector3;
import org.lwjgl.openvr.RenderModel;
import org.lwjgl.openvr.RenderModelTextureMap;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRRenderModels;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.ViveController;

public class OpenVRModel implements Model {

    private static final Shader DIFFUSE_SHADER = Shader.load("geometry_pass_diffuse");

    public final ViveController vc;
    public final Texture diffuseTexture;

    private final int num;
    private final VertexArrayObject vao;
    private final BufferObject ebo;

    public OpenVRModel(ViveController vc) {
        this.vc = vc;

        List<VertexPBR> vertices = new ArrayList();
        List<Integer> indices = new ArrayList();

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
        rm.rVertexData().forEach(rmv -> vertices.add(new VertexPBR(
                rotator.applyTo(toVec3d(rmv.vPosition())), // convert inches to meters
                new Vec2d(rmv.rfTextureCoord(0), rmv.rfTextureCoord(1)),
                rotator.applyTo(toVec3d(rmv.vNormal())),
                new Vec3d(0, 0, 0),
                new Vec3d(0, 0, 0))));

        var sb = rm.IndexData();
        while (sb.hasRemaining()) {
            indices.add((int) sb.get());
        }

        num = indices.size();
        vao = Vertex.createVAO(vertices, new int[]{3, 2, 3, 3, 3});
        ebo = new BufferObject(GL_ELEMENT_ARRAY_BUFFER, indices.stream().mapToInt(i -> i).toArray());

        diffuseTexture = new Texture(GL_TEXTURE_2D);
        diffuseTexture.setParameter(GL_TEXTURE_MAX_LEVEL, 4);
        diffuseTexture.uploadData(rmtm.unWidth(), rmtm.unHeight(), rmtm.rubTextureMapData(4 * rmtm.unWidth() * rmtm.unHeight()));
    }

    @Override
    public void render() {
        vao.bind();
        glDrawElements(GL_TRIANGLES, num, GL_UNSIGNED_INT, 0);
    }

    private static Vec3d toVec3d(HmdVector3 v) {
        return new Vec3d(v.v(0), v.v(1), v.v(2));
    }
}

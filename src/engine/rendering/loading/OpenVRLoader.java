package engine.rendering.loading;

import engine.graphics.opengl.Texture;
import engine.util.math.Quaternion;
import engine.util.math.Vec2d;
import engine.util.math.Vec3d;
import engine.vr.VrController;
import engine.rendering.ModelNode;
import static engine.rendering.loading.ConversionUtils.*;
import engine.rendering.materials.DiffuseMaterial;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;

public class OpenVRLoader {

    public final ModelNode rootNode;

    public OpenVRLoader(VrController vc) {
        var rm = loadRenderModel(vc);
        var rmtm = loadRenderModelTextureMap(rm.diffuseTextureId());

        var rotator = Quaternion.fromXYAxes(new Vec3d(0, -1, 0), new Vec3d(0, 0, 1));
        var RMB = new RawMeshBuilder();
        rm.rVertexData().forEach(rmv -> RMB.addVertex(
                rotator.applyTo(toVec3d(rmv.vPosition())),
                new Vec2d(rmv.rfTextureCoord(0), rmv.rfTextureCoord(1)),
                rotator.applyTo(toVec3d(rmv.vNormal())),
                new Vec3d(0, 0, 0),
                new Vec3d(0, 0, 0)));

        var sb = rm.IndexData();
        while (sb.hasRemaining()) {
            RMB.addIndices(sb.get());
        }

        var diffuseTexture = new Texture(GL_TEXTURE_2D);
        diffuseTexture.setParameter(GL_TEXTURE_MAX_LEVEL, 4);
        diffuseTexture.uploadData(rmtm.unWidth(), rmtm.unHeight(), rmtm.rubTextureMapData(4 * rmtm.unWidth() * rmtm.unHeight()));
        var diffuseMat = new DiffuseMaterial();
        diffuseMat.texture = diffuseTexture;

        rootNode = new ModelNode(diffuseMat.buildRenderable(RMB.toMesh()));
    }
}

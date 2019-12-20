package engine.rendering.passes;

import engine.graphics.Camera;
import engine.graphics.opengl.Framebuffer;
import engine.graphics.opengl.GLState;
import engine.graphics.opengl.Shader;
import engine.graphics.opengl.Texture;
import engine.util.Resources;
import engine.util.math.Transformation;
import engine.util.math.Vec2d;
import engine.rendering.ModelComponent;
import engine.rendering.passes.RenderPipeline.RenderPass;

import static engine.graphics.Color.BLACK;
import static engine.graphics.opengl.GLObject.bindAll;
import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_NEAREST;
import static org.lwjgl.opengl.GL11C.GL_RGB;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.*;

public class GeometryPass implements RenderPass {

    public static final Shader SHADER_COLOR = Resources.loadShader("geometry_pass_color");
    public static final Shader SHADER_DIFFUSE = Resources.loadShader("geometry_pass_diffuse");
    public static final Shader SHADER_PBR = Resources.loadShader("geometry_pass_pbr");

    static {
        SHADER_PBR.setUniform("albedoMap", 0);
        SHADER_PBR.setUniform("normalMap", 1);
        SHADER_PBR.setUniform("metallicMap", 2);
        SHADER_PBR.setUniform("roughnessMap", 3);
        SHADER_PBR.setUniform("aoMap", 4);
        SHADER_PBR.setUniform("heightMap", 5);
        SHADER_PBR.setUniform("alphaMap", 6);
        SHADER_PBR.setUniform("emissiveMap", 7);
    }

    final Framebuffer gBuffer;
    private final Texture gPosition, gNormal, gAlbedo, gMRA, gEmissive;
    public Camera camera;

    public GeometryPass(Vec2d framebufferSize) {
        if (framebufferSize == null) {
            gBuffer = new Framebuffer();
        } else {
            gBuffer = new Framebuffer(framebufferSize);
        }
        gBuffer.bind();
        gPosition = gBuffer.attachTexture(GL_RGB32F, GL_RGB, GL_FLOAT, GL_NEAREST, GL_COLOR_ATTACHMENT0);
        gNormal = gBuffer.attachTexture(GL_RGB16F, GL_RGB, GL_FLOAT, GL_NEAREST, GL_COLOR_ATTACHMENT1);
        gAlbedo = gBuffer.attachTexture(GL_RGB16F, GL_RGB, GL_FLOAT, GL_NEAREST, GL_COLOR_ATTACHMENT2);
        gMRA = gBuffer.attachTexture(GL_RGB16F, GL_RGB, GL_FLOAT, GL_NEAREST, GL_COLOR_ATTACHMENT3);
        gEmissive = gBuffer.attachTexture(GL_RGB16F, GL_RGB, GL_FLOAT, GL_NEAREST, GL_COLOR_ATTACHMENT4);
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2, GL_COLOR_ATTACHMENT3, GL_COLOR_ATTACHMENT4});
        gBuffer.attachDepthRenderbuffer();
        GLState.bindFramebuffer(null);

        gPosition.num = 0;
        gNormal.num = 1;
        gAlbedo.num = 2;
        gMRA.num = 3;
        gEmissive.num = 4;
    }

    public static void updateShaderUniforms() {
        SHADER_COLOR.setMVP(Transformation.IDENTITY);
        SHADER_DIFFUSE.setMVP(Transformation.IDENTITY);
        SHADER_PBR.setMVP(Transformation.IDENTITY);
        SHADER_PBR.setUniform("camPos", Camera.current.getPos());
    }

    public void bindGBuffer() {
        bindAll(gPosition, gNormal, gAlbedo, gMRA, gEmissive);
    }

    @Override
    public void run() {
        Camera.current = camera;
        GLState.enable(GL_DEPTH_TEST);
        GLState.disable(GL_BLEND);
        gBuffer.clear(BLACK);
        updateShaderUniforms();
        ModelComponent.allNodes().forEach(n -> n.render(Transformation.IDENTITY, 0));
        GLState.bindFramebuffer(null);
    }
}

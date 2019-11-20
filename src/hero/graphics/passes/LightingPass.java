package hero.graphics.passes;

import beige_engine.engine.Settings;
import beige_engine.graphics.Camera;
import beige_engine.graphics.Color;
import beige_engine.graphics.opengl.Framebuffer;
import beige_engine.graphics.opengl.GLState;
import beige_engine.graphics.opengl.Shader;
import beige_engine.graphics.opengl.Texture;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import hero.game.ModelBehavior;
import hero.graphics.passes.RenderPipeline.RenderPass;

import java.util.List;

import static beige_engine.graphics.opengl.Framebuffer.FRAMEBUFFER_VAO;
import static beige_engine.graphics.opengl.GLObject.bindAll;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.*;

public class LightingPass implements RenderPass {

    private static final Shader SHADER_LIGHTING = Shader.load("lighting_pass");
    public static final Shader SHADER_EMISSIVE_FLAT = Shader.load("emissive_pass_flat");
    private static final Texture BRDF_LUT = Texture.load("brdf_lut.png");

    static {
        SHADER_LIGHTING.setUniform("gPosition", 0);
        SHADER_LIGHTING.setUniform("gNormal", 1);
        SHADER_LIGHTING.setUniform("gAlbedo", 2);
        SHADER_LIGHTING.setUniform("gMRA", 3);
        SHADER_LIGHTING.setUniform("gEmissive", 4);
        SHADER_LIGHTING.setUniform("brdfLUT", 5);
        for (int i = 0; i < 5; i++) {
            SHADER_LIGHTING.setUniform("shadowMap[" + i + "]", 6 + i);
        }
        BRDF_LUT.num = 5;
    }

    private final GeometryPass gp;
    private final List<ShadowPass> spList;
    private Framebuffer framebuffer, rawLight, preBloom, bloomBuf1, bloomBuf2;
    private Texture tex1, tex2, rawLightTex;
    public Camera camera;
    public Color skyColor;
    public Vec3d sunColor, sunDirection;

    public LightingPass(Vec2d framebufferSize, GeometryPass gp, List<ShadowPass> spList) {
        this.gp = gp;
        this.spList = spList;
        if (framebufferSize == null) {
            framebufferSize = new Vec2d(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT);
        }

        framebuffer = new Framebuffer(framebufferSize);
        framebuffer.attachColorBuffer();
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0});
        framebuffer.attachDepthRenderbuffer();

        rawLight = new Framebuffer(framebufferSize);
        rawLightTex = rawLight.attachTexture(GL_RGB16F, GL_RGB, GL_FLOAT, GL_NEAREST, GL_COLOR_ATTACHMENT0);
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0});
//        rawLight.attachDepthRenderbuffer();
        glBindRenderbuffer(GL_RENDERBUFFER, gp.gBuffer.rboDepth);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, gp.gBuffer.rboDepth);

        preBloom = new Framebuffer(framebufferSize);
        tex1 = preBloom.attachTexture(GL_RGB16F, GL_RGB, GL_FLOAT, GL_NEAREST, GL_COLOR_ATTACHMENT0);
        tex2 = preBloom.attachTexture(GL_RGB16F, GL_RGB, GL_FLOAT, GL_NEAREST, GL_COLOR_ATTACHMENT1);
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1});
        preBloom.attachDepthRenderbuffer();

        bloomBuf1 = new Framebuffer(framebufferSize.div(8));
        bloomBuf1.attachColorBuffer();
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0});
        bloomBuf1.attachDepthRenderbuffer();

        bloomBuf2 = new Framebuffer(framebufferSize.div(8));
        bloomBuf2.attachColorBuffer();
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0});
        bloomBuf2.attachDepthRenderbuffer();

        GLState.bindFramebuffer(null);
        GLState.bindShader(null);
        for (int i = 0; i < spList.size(); i++) {
            spList.get(i).bindShadowMap(6 + i);
            glActiveTexture(GL_TEXTURE0 + 6 + i);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }

    public Texture colorBuffer() {
        return framebuffer.colorBuffer;
    }

    @Override
    public void run() {
        rawLight.clearColor(skyColor);
        GLState.disable(GL_DEPTH_TEST);

        GLState.bindShader(null);
        for (int i = 0; i < spList.size(); i++) {
            spList.get(i).bindShadowMap(6 + i);
        }
        SHADER_LIGHTING.setUniform("sunColor", sunColor);
        SHADER_LIGHTING.setUniform("sunDirection", sunDirection);
        for (int i = 0; i < spList.size(); i++) {
            SHADER_LIGHTING.setUniform("lightSpaceMatrix[" + i + "]", spList.get(i).getLightSpaceMatrix());
            SHADER_LIGHTING.setUniform("cascadeEndClipSpace[" + i + "]", (float) spList.get(i).zMax);
        }
        SHADER_LIGHTING.setUniform("projectionViewMatrix", camera.projectionMatrix().mul(camera.viewMatrix()));
        SHADER_LIGHTING.setUniform("camPos", camera.getPos());
        gp.bindGBuffer();
        bindAll(BRDF_LUT, FRAMEBUFFER_VAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

        GLState.bindShader(null);
        for (int i = 0; i < spList.size(); i++) {
            GLState.bindTexture(null, 6 + i);
        }

        // New code!

        GLState.enable(GL_BLEND, GL_DEPTH_TEST);
        Camera.current = camera;
        glBlendFunc(GL_ONE, GL_ONE);
        SHADER_EMISSIVE_FLAT.setMVP(Transformation.IDENTITY);
        ModelBehavior.allNodes().forEach(n -> n.render(Transformation.IDENTITY, 2));
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        GLState.disable(GL_DEPTH_TEST);
        var camera = new Camera.Camera2d();
        camera.lowerLeft = new Vec2d(-1, -1);
        Camera.current = camera;

        var sprite = Shader.load("sprite");
        sprite.setMVP(Transformation.IDENTITY);
        sprite.setUniform("color", Color.WHITE);
        var bloom = Shader.load("bloom");
        bloom.setMVP(Transformation.IDENTITY);
        var hdr = Shader.load("hdr");
        hdr.setMVP(Transformation.IDENTITY);

        preBloom.clear(Color.BLACK);
        preBloom.drawToSelf(rawLightTex, hdr);
        rawLight.clear(skyColor);

        bloom.setUniform("horizontal", false);
        bloomBuf1.drawToSelf(tex2, bloom);
        bloom.setUniform("horizontal", true);
        bloomBuf2.drawToSelf(bloomBuf1.colorBuffer, bloom);
        for (int i = 0; i < 3; i++) {
            bloom.setUniform("horizontal", false);
            bloomBuf1.drawToSelf(bloomBuf2.colorBuffer, bloom);
            bloom.setUniform("horizontal", true);
            bloomBuf2.drawToSelf(bloomBuf1.colorBuffer, bloom);
        }

        framebuffer.clear(Color.BLACK);
        framebuffer.drawToSelf(tex1, sprite);

        glBlendFunc(GL_ONE, GL_ONE);
        framebuffer.drawToSelf(bloomBuf2.colorBuffer, sprite);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        GLState.bindFramebuffer(null);
    }
}

package hero.graphics.passes;

import beige_engine.core.AbstractSystem;
import beige_engine.graphics.Camera;
import beige_engine.graphics.Color;
import beige_engine.graphics.opengl.GLState;
import beige_engine.graphics.sprites.Sprite;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.VrCore;
import beige_engine.vr.VrEyeCamera;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glFlush;

public class RenderPipeline extends AbstractSystem {

    public static RenderPass currentPass;
    private final List<GeometryPass> gpList = new ArrayList();
    private final List<ShadowPass> spList = new ArrayList();
    private final List<LightingPass> lpList = new ArrayList();
    public Color skyColor = new Color(.3, .6, .9, 1);
    public Vec3d sunColor = new Vec3d(10, 9, 8).mul(.25);
    public Vec3d sunDirection = new Vec3d(.3, -.15, 1);
    public boolean isVR;
    private List<Camera> cameras;
    private List<Vec2d> framebufferSizes;

    public RenderPipeline() {
        cameras = isVR ? Arrays.asList(new VrEyeCamera(true), new VrEyeCamera(false)) : Arrays.asList(Camera.camera3d);
        framebufferSizes = isVR ? Arrays.asList(VrCore.getRecommendedRenderTargetSize(), VrCore.getRecommendedRenderTargetSize()) : Arrays.asList((Vec2d) null);

        for (int i = 0; i < 5; i++) {
            ShadowPass sp = new ShadowPass();
            sp.cameras = cameras;
            sp.zMin = i == 0 ? -1 : (1 - Math.pow(.2, i + 1));
            sp.zMax = 1 - Math.pow(.2, i + 2);
            sp.sunDirection = sunDirection;
            spList.add(sp);
        }

        for (int i = 0; i < cameras.size(); i++) {
            GeometryPass gp = new GeometryPass(framebufferSizes.get(i));
            gp.camera = cameras.get(i);
            gpList.add(gp);

            LightingPass lp = new LightingPass(framebufferSizes.get(i), gp, spList);
            lp.camera = cameras.get(i);
            lp.skyColor = skyColor;
            lp.sunColor = sunColor;
            lp.sunDirection = sunDirection;
            lpList.add(lp);
        }
    }

    public void setSunColor(Vec3d sunColor) {
        this.sunColor = sunColor;
        for (var lp : lpList) {
            lp.sunColor = sunColor;
        }
    }

    public void setSunDirection(Vec3d sunDirection) {
        this.sunDirection = sunDirection;
        for (var lp : lpList) {
            lp.sunDirection = sunDirection;
        }
        for (var sp : spList) {
            sp.sunDirection = sunDirection;
        }
    }

    public void setSkyColor(Color skyColor) {
        this.skyColor = skyColor;
        for (var lp : lpList) {
            lp.skyColor = skyColor;
        }
    }

    @Override
    public void onStep() {
        if (isVR) {
            VrEyeCamera.waitUpdatePos();
        }
        gpList.forEach(RenderPass::doPass);
        spList.forEach(RenderPass::doPass);
        lpList.forEach(RenderPass::doPass);
        if (isVR) {
            VrCore.submit(true, lpList.get(0).colorBuffer());
            VrCore.submit(false, lpList.get(1).colorBuffer());
            glFlush();
        }

        Camera.current = Camera.camera2d;
        GLState.disable(GL_DEPTH_TEST);
        GLState.bindFramebuffer(null);
        if (isVR) {
            Sprite.drawTexture(lpList.get(0).colorBuffer(), Transformation.create(new Vec2d(.25, .5), new Vec2d(.48, 0), new Vec2d(0, .96)), Color.WHITE);
            Sprite.drawTexture(lpList.get(1).colorBuffer(), Transformation.create(new Vec2d(.75, .5), new Vec2d(.48, 0), new Vec2d(0, .96)), Color.WHITE);
        } else {
            Sprite.drawTexture(lpList.get(0).colorBuffer(), Transformation.create(new Vec2d(.5, .5), 0, 1), Color.WHITE);
        }
    }

    public interface RenderPass extends Runnable {

        default void doPass() {
            currentPass = this;
            run();
        }
    }
}

package hero.game.particles;

import beige_engine.graphics.Color;
import beige_engine.graphics.opengl.Texture;
import beige_engine.util.math.MathUtils;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Vec3d;
import static hero.game.controllers.IceCaster.iceModel;
import hero.graphics.materials.ColorMaterial;
import hero.graphics.materials.EmissiveMaterial;
import hero.graphics.materials.EmissiveTexMaterial;
import hero.graphics.utils.SDF;
import static hero.graphics.utils.SDF.sphere;
import hero.physics.shapes.AABB;
import java.util.Arrays;
import java.util.Random;

public abstract class ParticleTypes {

    private static final Random random = new Random();

    public static final ParticleEmitter FIRE, SMOKE, ICE;

    static {
        FIRE = new ParticleEmitter();
        FIRE.material = new EmissiveMaterial(new Vec3d(5, .5, .2));
        FIRE.archetype = p -> {
            p.rotation = Quaternion.fromAngleAxis(new Vec3d(0, 0, Math.random() * 2 * Math.PI));
            p.scale = () -> new Vec3d(1, 1, 1).mul(1 / (1 + 4 * p.time));
            p.fadeTime = .1;
        };
        FIRE.create();

        SMOKE = new ParticleEmitter();
        var smokeMaterial = new ColorMaterial();
        smokeMaterial.color = new Vec3d(.4, .4, .4);
        smokeMaterial.hasShadows = false;
        SMOKE.material = smokeMaterial;
        SMOKE.archetype = p -> {
            p.rotation = Quaternion.fromAngleAxis(new Vec3d(0, 0, Math.random() * 2 * Math.PI));
            p.scale = () -> new Vec3d(1, 1, 1).mul(1 / (1 + .4 * p.time));
            p.acceleration = new Vec3d(0, 0, -5);
            p.friction = 1;
            p.fadeTime = .5;
        };
        SMOKE.create();

        ICE = new ParticleEmitter();
        var iceMaterial = new EmissiveTexMaterial();
        iceMaterial.tex = Texture.load("ball.png");
        iceMaterial.color = new Color(.4, .6, 1).multRGB(.005);
        ICE.material = iceMaterial;
        ICE.archetype = p -> {
            p.rotation = Quaternion.fromAngleAxis(new Vec3d(0, 0, Math.random() * 2 * Math.PI));
//            p.rotation = Quaternion.fromXYAxes(MathUtils.randomInSphere(random), MathUtils.randomInSphere(random));
            p.scale = () -> new Vec3d(1, 1, 1).mul(1 / (1 + 4 * p.time));
//            p.billboard = false;
            p.acceleration = new Vec3d(0, 0, -5);
            p.fadeTime = .2;
        };
        ICE.create();
    }

    public static void explosion(Vec3d position, Vec3d velocity, int numParticles) {
        for (int i = 0; i < numParticles; i++) {
            var p = FIRE.addParticle();
            p.position = position;
            p.velocity = velocity.add(MathUtils.randomInSphere(random).mul(10 + Math.random() * 10));
            p.startupTime = .1;
        }
        for (int i = 0; i < numParticles / 4; i++) {
            var p = SMOKE.addParticle();
            p.position = position;
            p.velocity = velocity.add(MathUtils.randomInSphere(random).mul(10 + Math.random() * 10));
            p.startupTime = .1;
        }

        if (numParticles >= 10) {
            SDF shape2 = sphere(position, 4);
            AABB bounds2 = AABB.boundingBox(Arrays.asList(position.sub(5), position.add(5)));
            iceModel.intersectionSDF(shape2.invert(), bounds2);
        }
    }
}

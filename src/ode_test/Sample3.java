package ode_test;

import org.ode4j.ode.*;
import org.ode4j.ode.internal.DxBody;
import org.ode4j.ode.internal.DxMass;
import org.ode4j.ode.internal.DxWorld;

import static org.ode4j.ode.internal.DxBody.dBodyCreate;
import static org.ode4j.ode.internal.DxWorld.dWorldCreate;
import static org.ode4j.ode.internal.OdeInit.dCloseODE;
import static org.ode4j.ode.internal.OdeInit.dInitODE;

public class Sample3 {
    public static DxWorld world;
    public static DxBody ball;
    public static final double radius = .2, mass = 1;

    public static void main(String[] args) {
        double x0 = 0, y0 = 0, z0 = 1;
        DxMass m = new DxMass();

        dInitODE();
        world = dWorldCreate();
        world.setGravity(0, 0, -.001);

        ball = dBodyCreate(world);
        m.setZero();
        m.setSphereTotal(mass, radius);
        ball.setMass(m);
        ball.setPosition(x0, y0, z0);

        for (int i = 0; i < 1000; i++) {
            simLoop(0);
        }

        world.destroy();
        dCloseODE();
    }

    public static void simLoop(int pause) {
        world.step(.05);
        // dsSetColor(1, 0, 0);
        var pos = ball.getPosition();
        var r = ball.getRotation();
        // dsDrawSphere(pos, r, radius);
        System.out.println(pos + " " + r);
    }
}

package hero.physics;

import static hero.physics.PhysicsManager.STEP_SIZE;
import java.lang.reflect.Field;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DDoubleBallJoint;
import static org.ode4j.ode.DJoint.PARAM_N.dParamCFM1;
import static org.ode4j.ode.DJoint.PARAM_N.dParamERP1;
import static org.ode4j.ode.OdeHelper.createDBallJoint;
import org.ode4j.ode.internal.joints.DxJointDBall;

public class SpringJoint {

    private static final Field TARGET_DISTANCE;

    static {
        try {
            TARGET_DISTANCE = DxJointDBall.class.getDeclaredField("targetDistance");
            TARGET_DISTANCE.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    public double kp = 1000;
    public double kd = 100;
    public double prefLength = 0;

    private final DDoubleBallJoint joint;

    public SpringJoint(PhysicsManager manager) {
        joint = createDBallJoint(manager.world);

    }

    public void attach(DBody b1, DBody b2) {
        joint.attach(b1, b2);
    }

    private double getCFM() {
        return 1 / (STEP_SIZE * kp + kd);
    }

    private double getERP() {
        return STEP_SIZE * kp / (STEP_SIZE * kp + kd);
    }

    public void updateParams() {
        joint.setParam(dParamCFM1, getCFM());
        joint.setParam(dParamERP1, getERP());
        try {
            TARGET_DISTANCE.setDouble(joint, prefLength);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}

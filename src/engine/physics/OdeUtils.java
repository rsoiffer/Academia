package engine.physics;

import engine.util.math.MathUtils;
import engine.util.math.Quaternion;
import engine.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import org.ode4j.math.DQuaternion;
import org.ode4j.math.DQuaternionC;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DContact;
import org.ode4j.ode.DContactBuffer;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.internal.DxGeom;
import static org.ode4j.ode.internal.DxGeom.dCollide;

public abstract class OdeUtils {

    public static int MAX_CONTACTS = 15;

    private static final Random RANDOM = new Random();

    public static List<DContact> collide(DGeom g1, DGeom g2) {
        return collide(g1, g2, null);
    }

    public static List<DContact> collide(DGeom g1, DGeom g2, Consumer<DContact> c) {
        var contact = new DContactBuffer(MAX_CONTACTS);
        if (c != null) {
            for (int i = 0; i < MAX_CONTACTS; i++) {
                c.accept(contact.get(i));
            }
        }
        int numc = dCollide((DxGeom) g1, (DxGeom) g2, MAX_CONTACTS, contact.getGeomBuffer(), 0);
        var r = new ArrayList<DContact>(numc);
        for (int i = 0; i < numc; i++) {
            r.add(contact.get(i));
        }
        return r;
    }

    public static DVector3 randomFrictionDir(DVector3 normal) {
        var n = toVec3d(normal);
        var f = MathUtils.randomInSphere(RANDOM).projectAgainst(n).normalize();
        return toDVector3(f);
    }

    public static Vec3d toVec3d(DVector3C v) {
        return new Vec3d(v.get0(), v.get1(), v.get2());
    }

    public static DVector3 toDVector3(Vec3d v) {
        return new DVector3(v.x, v.y, v.z);
    }

    public static Quaternion toQuaternion(DQuaternionC quat) {
        return new Quaternion(quat.get0(), quat.get1(), quat.get2(), quat.get3());
    }

    public static DQuaternion toDQuaternion(Quaternion quat) {
        return new DQuaternion(quat.a, quat.b, quat.c, quat.d);
    }
}

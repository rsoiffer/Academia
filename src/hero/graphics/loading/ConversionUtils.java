package hero.graphics.loading;

import engine.util.math.Vec3d;
import engine.vr.VrController;
import java.nio.IntBuffer;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.joml.Matrix4d;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import static org.lwjgl.assimp.Assimp.*;
import org.lwjgl.openvr.HmdVector3;
import org.lwjgl.openvr.RenderModel;
import org.lwjgl.openvr.RenderModelTextureMap;
import org.lwjgl.openvr.VR;
import static org.lwjgl.openvr.VRRenderModels.*;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

public abstract class ConversionUtils {

    public static Optional<Vec3d> loadMaterialColor(AIMaterial aiMaterial, String key) {
        AIColor4D aiColor = AIColor4D.create();
        return aiGetMaterialColor(aiMaterial, key, aiTextureType_NONE, 0, aiColor) == 0
                ? Optional.of(new Vec3d(aiColor.r(), aiColor.g(), aiColor.b())) : Optional.empty();
    }

    public static Optional<Double> loadMaterialFloat(AIMaterial aiMaterial, String key) {
        float[] res = new float[1];
        return aiGetMaterialFloatArray(aiMaterial, key, aiTextureType_NONE, 0, res, new int[]{1}) == 0
                ? Optional.of((double) res[0]) : Optional.empty();
    }

    public static Optional<String> loadMaterialTexturePath(AIMaterial aiMaterial, int type) {
        AIString aiPath = AIString.calloc();
        var ecode = aiGetMaterialTexture(aiMaterial, type, 0, aiPath,
                (IntBuffer) null, null, null, null, null, null);
        return ecode == 0 ? Optional.of(aiPath.dataString()) : Optional.empty();
    }

    public static RenderModel loadRenderModel(VrController vc) {
        try ( MemoryStack stack = stackPush()) {
            var pb = stack.callocPointer(1);
            var renderModelName = vc.getPropertyString(VR.ETrackedDeviceProperty_Prop_RenderModelName_String);
            int ecode = repeatWhile(() -> VRRenderModels_LoadRenderModel_Async(renderModelName, pb), 100);
            if (ecode != 0) {
                throw new RuntimeException("Could not load OpenVR render model: "
                        + VRRenderModels_GetRenderModelErrorNameFromEnum(ecode));
            }
            return RenderModel.create(pb.get());
        }
    }

    public static RenderModelTextureMap loadRenderModelTextureMap(int diffuseTextureId) {
        try ( MemoryStack stack = stackPush()) {
            var pb = stack.callocPointer(1);
            int ecode = repeatWhile(() -> VRRenderModels_LoadTexture_Async(diffuseTextureId, pb), 100);
            if (ecode != 0) {
                throw new RuntimeException("Could not load OpenVR render model diffuse texture: "
                        + VRRenderModels_GetRenderModelErrorNameFromEnum(ecode));
            }
            return RenderModelTextureMap.create(pb.get());
        }
    }

    public static <T> T repeatWhile(Supplier<T> s, T t) {
        while (true) {
            var r = s.get();
            if (!Objects.equals(r, t)) {
                return r;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
    }

    public static Stream<Integer> streamBuf(IntBuffer buf) {
        return Stream.generate(buf::get).limit(buf.remaining());
    }

    public static Stream<Long> streamBuf(PointerBuffer buf) {
        return Stream.generate(buf::get).limit(buf.remaining());
    }

    public static <T extends Struct> Stream<T> streamBuf(StructBuffer<T, ?> buf) {
        return Stream.generate(buf::get).limit(buf.remaining());
    }

    public static Stream<Float> streamVec2d(AIVector3D v) {
        return Stream.of(v.x(), v.y());
    }

    public static Stream<Float> streamVec3d(AIVector3D v) {
        return Stream.of(v.x(), v.y(), v.z());
    }

    public static float[] toFloatArray(Collection<Float> c) {
        var r = new float[c.size()];
        int pos = 0;
        for (var f : c) {
            r[pos++] = f;
        }
        return r;
    }

    public static float[] toFloatArray(Stream<Float> s) {
        return toFloatArray(s.collect(Collectors.toList()));
    }

    public static List<Float> toFloatList(float[] a) {
        var r = new ArrayList<Float>(a.length);
        for (var f : a) {
            r.add(f);
        }
        return r;
    }

    public static int[] toIntArray(Collection<Integer> c) {
        return toIntArray(c.stream());
    }

    public static int[] toIntArray(Stream<Integer> s) {
        return s.mapToInt(i -> i).toArray();
    }

    public static Matrix4d toMatrix4d(AIMatrix4x4 m) {
        return new Matrix4d(
                m.a1(), m.a2(), m.a3(), m.a4(),
                m.b1(), m.b2(), m.b3(), m.b4(),
                m.c1(), m.c2(), m.c3(), m.c4(),
                m.d1(), m.d2(), m.d3(), m.d4()).transpose();
    }

    public static Vec3d toVec3d(HmdVector3 v) {
        return new Vec3d(v.v(0), v.v(1), v.v(2));
    }
}

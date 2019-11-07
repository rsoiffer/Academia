package beige_engine.network;

import beige_engine.graphics.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
import beige_engine.util.Log;
import beige_engine.util.math.IntVectorN;
import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import beige_engine.util.math.VectorN;

public abstract class NetworkUtils {

    //Starting and connecting to servers
    public static final int PORT = 51243;

    public static Connection connect(String ip) {
        if (ip.equals("")) {
            ip = "localhost";
        }
        try {
            Connection c = new Connection(new Socket(ip, PORT));
            System.out.println("Connected to server");
            c.onClose(() -> System.out.println("Disconnected from server"));
            return c;
        } catch (IOException ex) {
            System.out.println("Could not connect to server: " + ip);
            return null;
        }
    }

    public static Connection connectManual() {
        System.out.println("Enter the ip address to connect to:");
        Connection c = connect(new Scanner(System.in).nextLine());
        if (c == null) {
            throw new RuntimeException("Failed to connect to server");
        }
        return c;
    }

    public static Thread server(Consumer<Connection> onConnect) {
        return new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT);
                Log.print("Server started on port " + PORT);
                while (true) {
                    Connection conn = new Connection(serverSocket.accept());
                    //Going too fast somtimes means the message handlers haven't registered
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException ex) {
                        Log.error(ex);
                    }
                    onConnect.accept(conn);
                }
            } catch (IOException ex) {
                Log.error(ex);
            }
        });
    }

    //Readers and writers
    static final Map<Class, Reader<Connection, Object>> READERS = new HashMap();
    static final Map<Class, Writer<Connection, Object>> WRITERS = new HashMap();
    
    static final Map<Class, Class> ALIASES = new HashMap();

    public static void initialize() {
        registerBasicType(Boolean.class, DataInputStream::readBoolean, DataOutputStream::writeBoolean);
        registerBasicType(Byte.class, DataInputStream::readByte, (o, b) -> o.writeByte(b));
        registerBasicType(Float.class, DataInputStream::readFloat, DataOutputStream::writeFloat);
        registerBasicType(Double.class, DataInputStream::readDouble, DataOutputStream::writeDouble);
        registerBasicType(Integer.class, DataInputStream::readInt, DataOutputStream::writeInt);
        registerBasicType(Long.class, DataInputStream::readLong, DataOutputStream::writeLong);
        registerBasicType(String.class, i -> i.readUTF(), DataOutputStream::writeUTF);

        registerType(Color.class, c -> new Color(c.read(Double.class), c.read(Double.class), c.read(Double.class), c.read(Double.class)), (c, o) -> c.write(o.r, o.g, o.b, o.a));
        registerType(Vec2d.class, c -> new Vec2d(c.read(Double.class), c.read(Double.class)), (c, v) -> c.write(v.x, v.y));
        registerType(Vec3d.class, c -> new Vec3d(c.read(Double.class), c.read(Double.class), c.read(Double.class)), (c, v) -> c.write(v.x, v.y, v.z));

        registerArrayType(Integer.class);
        registerArrayType(Double.class);
        registerType(IntVectorN.class, c -> {
            Integer[] vals = c.read(Integer[].class);
            int[] inVals = new int[vals.length];
            for (int i = 0; i < vals.length; i++) {
                inVals[i] = vals[i];
            }
            return IntVectorN.of(inVals);
        }, (c, v) -> {
            int[] vec = v.asArray();
            Integer[] send = new Integer[vec.length];
            for (int i = 0; i < vec.length; i++) {
                send[i] = (Integer) vec[i];
            }
            c.write(packSingle(send));
        });
        registerType(VectorN.class, c -> {
            Double[] vals = c.read(Double[].class);
            double[] inVals = new double[vals.length];
            for (int i = 0; i < vals.length; i++) {
                inVals[i] = vals[i];
            }
            return VectorN.of(inVals);
        }, (c, v) -> {
            double[] vec = v.asArray();
            Double[] send = new Double[vec.length];
            for (int i = 0; i < vec.length; i++) {
                send[i] = (Double) vec[i];
            }
            c.write(packSingle(send));
        });
    }

    private static <T> void registerBasicType(Class<T> c, Reader<DataInputStream, T> reader, Writer<DataOutputStream, T> writer) {
        registerType(c, conn -> reader.read(conn.input), (conn, t) -> writer.write(conn.output, t));
    }

    public static <T> void registerType(Class<T> c, Reader<Connection, T> reader, Writer<Connection, T> writer) {
        if (!READERS.containsKey(c)) {
            READERS.put(c, (Reader) reader);
            WRITERS.put(c, (Writer) writer);
        }
    }

    public static <T> void registerArrayType(Class<T> c) {
        if (!READERS.containsKey(c)) {
            throw new RuntimeException("Cannot register an array type for a type which is not registered: " + c);
        }
        Class arrayClass = Array.newInstance(c, 0).getClass();
        if (!READERS.containsKey(arrayClass)) {
            READERS.put(arrayClass, conn -> {
                int len = conn.read(Integer.class);
                T[] array = (T[]) Array.newInstance(c, len);
                for (int i = 0; i < len; i++) {
                    array[i] = conn.read(c);
                }
                return array;
            });
            WRITERS.put(arrayClass, (conn, a) -> {
                Object[] array = (Object[]) a;
                conn.write(array.length);
                for (Object o : array) {
                    if (o == null) {
                        throw new RuntimeException("Null is not serializable.");
                    }
                    conn.write(o);
                }
            });
        }
    }

    public static <T, S> void registerMapType(Class<T> kc, Class<S> vc) {
        registerArrayType(kc);
        registerArrayType(vc);
        Map<T, S> s = new HashMap();
        Class mapClass = s.getClass();
        if (!READERS.containsKey(mapClass)) {
            READERS.put(mapClass, conn -> {
                T[] keys = (T[]) conn.read(Array.newInstance(kc, 0).getClass());
                S[] values = (S[]) conn.read(Array.newInstance(vc, 0).getClass());
                if (keys.length != values.length) {
                    throw new RuntimeException("Key and value lengths do not match.");
                }
                Map<T, S> map = new HashMap();
                for (int i = 0; i < keys.length; i++) {
                    map.put(keys[i], values[i]);
                }
                return map;
            });
            WRITERS.put(mapClass, (conn, m) -> {
                Map<T, S> map = (Map<T, S>) m;
                Object[] rawKeys = map.keySet().toArray();
                T[] keys = (T[]) Array.newInstance(kc, rawKeys.length);
                for (int i = 0; i < rawKeys.length; i++) {
                    keys[i] = (T) rawKeys[i];
                }
                S[] values = (S[]) Array.newInstance(vc, keys.length);
                for (int i = 0; i < keys.length; i++) {
                    if (keys[i] == null) {
                        throw new RuntimeException("Null in keys is not serializable.");
                    }
                    values[i] = map.get(keys[i]);
                    if (values[i] == null) {
                        throw new RuntimeException("Null in values is not serializable.");
                    }
                }
                conn.write(keys, values);
            });
        }
    }
    
    public static <T extends S, S> void registerAlias(Class<T> c, Class<S> alias){
        ALIASES.put(c, alias);
    }
    
    public static Class getAlias(Class c){
        return ALIASES.get(c);
    }

    @FunctionalInterface
    public static interface Reader<T, R> {

        public R read(T t) throws IOException;
    }

    @FunctionalInterface
    public static interface Writer<T, R> {

        public void write(T t, R r) throws IOException;
    }

    /**
     * Packages the objects into an array.
     *
     * @param o The objects to pack.
     * @return The array containing all of the objects.
     */
    public static Object[] pack(Object... o) {
        return o;
    }

    /**
     * Packages the single object into an array.
     *
     * @param o The object to pack.
     * @return The array containing the object.
     */
    public static Object[] packSingle(Object o) {
        Object[] ret = new Object[1];
        ret[0] = o;
        return ret;
    }
    
    public static <T, S> Class<HashMap<T, S>> getHashMapType(Class<T> kc, Class<S> vc){
        Map<T, S> s = new HashMap();
        return (Class<HashMap<T, S>>) s.getClass();
    }
}

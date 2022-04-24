package ServerCore;

import arc.struct.Seq;
import mindustry.gen.Building;

import java.nio.ByteBuffer;
import java.util.Base64;

public class JLib {
    private static final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    public static final Base64.Encoder B64Encoder = Base64.getEncoder();
    public static final Base64.Decoder B64Decoder = Base64.getDecoder();

    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        final byte[] out = buffer.array();
        buffer.clear();
        return out;
    }

    public static long bytesToLong(byte[] bytes) {
        buffer.put(bytes, 0, bytes.length);
        buffer.flip(); //need flip
        long out = buffer.getLong();
        buffer.clear();
        return out;
    }

    public static String longToB64(long l) {
        return B64Encoder.encodeToString(longToBytes(l));
    }

    public static Long b64ToLong(String s) {
        return bytesToLong(B64Decoder.decode(s));
    }

    /**
     * Returns a Seq of all the blocks found next to a block in check Seq, recursively
     * @param type what type of building you are looking for in check Seq
     * @param out the final Seq containing all the buildings found
     * @param check a list of all the buildings to be checked
     */
    public static <T extends Building> Seq<T> getLinked(Class<T> type, Seq<T> out, Seq<? extends Building> check) {
        final Seq<T> a = new Seq<>();
        for (var b : check) {
            b.updateProximity();
            for (Building c : b.proximity) {
                if (type.isInstance(c)) {
                    T d = type.cast(c);
                    if (!out.contains(d) && !a.contains(d)) {
                        a.add(d);
                    }
                }
            }
        }
        if (a.isEmpty()) {
            return out;
        } else {
            out.addAll(a);
            return getLinked(type, out, a);
        }

    }

    public static <T extends Building> Seq<Building> getLinkedBuild(Class<T> type, Seq<Building> out, Seq<? extends Building> check) {
        final Seq<Building> a = new Seq<>();
        for (var b : check) {
            b.updateProximity();
            for (Building c : b.proximity) {
                if (type.isInstance(c)) {
                    if (!out.contains(c) && !a.contains(c)) {
                        a.add(c);
                    }
                }
            }
        }
        if (a.isEmpty()) {
            return out;
        } else {
            out.addAll(a);
            return getLinkedBuild(type, out, a);
        }
    }

    public static class TimedLogging {
        public long last;

        public TimedLogging() {
            last = System.currentTimeMillis();
        }

        public void log(String reason) {
            long now = System.currentTimeMillis();
            System.out.println(reason + " (" + (now - last + 1) + "ms)");
            last = now;
        }
    }
}

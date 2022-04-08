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

/**
 * Encode any bytes to Utf16 chars
 */
public class BaseUtf16 {
    private static final int[][] BitsRange  = {{0x0000, 0x0fff}, {0x1000, 0x5fff}, {0x6000, 0x7fff}};
    private static final int[][] Utf16Range = {{0x3400, 0x43ff}, {0x4e00, 0x9dff}, {0xb000, 0xcfff}};

    public static char[] encode(byte[] bytes) {
        validate(bytes != null && bytes.length != 0, "bytes is null or empty");

        int bitLen = bytes.length * 8;
        int left = bitLen % 15;
        int utf16Len = (left == 0) ? bitLen / 15 : bitLen / 15 + 1;

        char[] result = new char[utf16Len + (left == 0 ? 0 : 1)];
        int start = 0;
        int i = 0;
        while (i < utf16Len) {
            int n = start >>> 3;
            int sm = start & 7;

            int bs = (bytes[n] & 0xFF) << (sm + 24);
            if (n + 1 < bytes.length) bs += (bytes[n+1] & 0xFF) << (sm + 16);
            if (n + 2 < bytes.length) bs += (bytes[n+2] & 0xFF) << (sm + 8);

            int c = bs >>> 17;
            int bi = getIndex(BitsRange, c);
            int uc = c - BitsRange[bi][0] + Utf16Range[bi][0];
            result[i] = (char) (uc);

            start += 15;
            i += 1;
        }

        if (left != 0) {
            result[i] = (char)('a' + 15 -left);
        }

        return result;
    }

    public static byte[] decode(char[] chars) {
        validate(chars != null && chars.length != 0, "chars is null or empty");
        int len = chars.length;
        int lastChar = chars[len - 1];
        int padding = (lastChar > 'a' && lastChar < 'a' + 16) ? lastChar - 'a' : 0;

        int utf16Len = (padding == 0) ? len : len - 1;
        int bitLen = utf16Len * 15 - padding;

        int resultLen = (bitLen + 4) / 8;
        byte[] result = new byte[resultLen];
        java.util.Arrays.fill(result, (byte) 0);

        int i = 0;
        int s = 0;
        while (i < utf16Len) {
            char c = chars[i];
            int bi = getIndex(Utf16Range, c);
            int ori = (c - Utf16Range[bi][0] + BitsRange[bi][0]) << 1;
            int n = s >>> 3;
            int ni = s & 7;

            result[n] = (byte) (result[n] | (ori >>> (8 + ni)));
            if (n + 1 < resultLen) result[n + 1] = (byte)((ori << (8 - ni)) >>> 8);
            if (ni > 1 && n + 2 < resultLen) result[n + 2] = (byte) ((ori << ( 16 - ni)) >>> 8);

            s += 15;
            i += 1;
        }
        return result;
    }

    private static void validate(boolean condition, String message, Object... args) {
        if (!condition) {
            String errMsg = (args == null || args.length == 0) ? message : String.format(message, args);
            throw new IllegalArgumentException(errMsg);
        }
    }

    private static int getIndex(int[][] array, int c) {
        for (int i = 0; i < array.length; ++i) {
            if (c >= array[i][0] && c <= array[i][1]) {
                return i;
            }
        }
        return -1;
    }
}

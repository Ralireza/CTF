package okhttp3.internal.http2;

import java.io.IOException;
import okhttp3.internal.Util;
import okio.ByteString;

public final class Http2 {
    static final String[] BINARY = new String[256];
    static final ByteString CONNECTION_PREFACE = ByteString.encodeUtf8("PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n");
    static final String[] FLAGS = new String[64];
    static final byte FLAG_ACK = 1;
    static final byte FLAG_COMPRESSED = 32;
    static final byte FLAG_END_HEADERS = 4;
    static final byte FLAG_END_PUSH_PROMISE = 4;
    static final byte FLAG_END_STREAM = 1;
    static final byte FLAG_NONE = 0;
    static final byte FLAG_PADDED = 8;
    static final byte FLAG_PRIORITY = 32;
    private static final String[] FRAME_NAMES = {"DATA", "HEADERS", "PRIORITY", "RST_STREAM", "SETTINGS", "PUSH_PROMISE", "PING", "GOAWAY", "WINDOW_UPDATE", "CONTINUATION"};
    static final int INITIAL_MAX_FRAME_SIZE = 16384;
    static final byte TYPE_CONTINUATION = 9;
    static final byte TYPE_DATA = 0;
    static final byte TYPE_GOAWAY = 7;
    static final byte TYPE_HEADERS = 1;
    static final byte TYPE_PING = 6;
    static final byte TYPE_PRIORITY = 2;
    static final byte TYPE_PUSH_PROMISE = 5;
    static final byte TYPE_RST_STREAM = 3;
    static final byte TYPE_SETTINGS = 4;
    static final byte TYPE_WINDOW_UPDATE = 8;

    static {
        String str;
        int[] iArr;
        int i = 0;
        int i2 = 0;
        while (true) {
            String[] strArr = BINARY;
            if (i2 >= strArr.length) {
                break;
            }
            strArr[i2] = Util.format("%8s", Integer.toBinaryString(i2)).replace(' ', '0');
            i2++;
        }
        String[] strArr2 = FLAGS;
        strArr2[0] = "";
        strArr2[1] = "END_STREAM";
        int[] iArr2 = {1};
        strArr2[8] = "PADDED";
        int length = iArr2.length;
        int i3 = 0;
        while (true) {
            str = "|PADDED";
            if (i3 >= length) {
                break;
            }
            int i4 = iArr2[i3];
            String[] strArr3 = FLAGS;
            int i5 = i4 | 8;
            StringBuilder sb = new StringBuilder();
            sb.append(FLAGS[i4]);
            sb.append(str);
            strArr3[i5] = sb.toString();
            i3++;
        }
        String[] strArr4 = FLAGS;
        strArr4[4] = "END_HEADERS";
        strArr4[32] = "PRIORITY";
        strArr4[36] = "END_HEADERS|PRIORITY";
        for (int i6 : new int[]{4, 32, 36}) {
            for (int i7 : iArr2) {
                String[] strArr5 = FLAGS;
                int i8 = i7 | i6;
                StringBuilder sb2 = new StringBuilder();
                sb2.append(FLAGS[i7]);
                sb2.append('|');
                sb2.append(FLAGS[i6]);
                strArr5[i8] = sb2.toString();
                String[] strArr6 = FLAGS;
                int i9 = i8 | 8;
                StringBuilder sb3 = new StringBuilder();
                sb3.append(FLAGS[i7]);
                sb3.append('|');
                sb3.append(FLAGS[i6]);
                sb3.append(str);
                strArr6[i9] = sb3.toString();
            }
        }
        while (true) {
            String[] strArr7 = FLAGS;
            if (i < strArr7.length) {
                if (strArr7[i] == null) {
                    strArr7[i] = BINARY[i];
                }
                i++;
            } else {
                return;
            }
        }
    }

    private Http2() {
    }

    static IllegalArgumentException illegalArgument(String str, Object... objArr) {
        throw new IllegalArgumentException(Util.format(str, objArr));
    }

    static IOException ioException(String str, Object... objArr) throws IOException {
        throw new IOException(Util.format(str, objArr));
    }

    static String frameLog(boolean z, int i, int i2, byte b, byte b2) {
        String[] strArr = FRAME_NAMES;
        String format = b < strArr.length ? strArr[b] : Util.format("0x%02x", Byte.valueOf(b));
        String formatFlags = formatFlags(b, b2);
        Object[] objArr = new Object[5];
        objArr[0] = z ? "<<" : ">>";
        objArr[1] = Integer.valueOf(i);
        objArr[2] = Integer.valueOf(i2);
        objArr[3] = format;
        objArr[4] = formatFlags;
        return Util.format("%s 0x%08x %5d %-13s %s", objArr);
    }

    static String formatFlags(byte b, byte b2) {
        if (b2 == 0) {
            return "";
        }
        if (!(b == 2 || b == 3)) {
            if (b == 4 || b == 6) {
                return b2 == 1 ? "ACK" : BINARY[b2];
            } else if (!(b == 7 || b == 8)) {
                String[] strArr = FLAGS;
                String str = b2 < strArr.length ? strArr[b2] : BINARY[b2];
                if (b == 5 && (b2 & 4) != 0) {
                    return str.replace("HEADERS", "PUSH_PROMISE");
                }
                if (b != 0 || (b2 & 32) == 0) {
                    return str;
                }
                return str.replace("PRIORITY", "COMPRESSED");
            }
        }
        return BINARY[b2];
    }
}

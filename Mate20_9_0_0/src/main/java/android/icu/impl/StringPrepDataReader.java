package android.icu.impl;

import android.icu.impl.ICUBinary.Authenticate;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;

public final class StringPrepDataReader implements Authenticate {
    private static final int DATA_FORMAT_ID = 1397772880;
    private static final byte[] DATA_FORMAT_VERSION = new byte[]{(byte) 3, (byte) 2, (byte) 5, (byte) 2};
    private static final boolean debug = ICUDebug.enabled("NormalizerDataReader");
    private ByteBuffer byteBuffer;
    private int unicodeVersion;

    public StringPrepDataReader(ByteBuffer bytes) throws IOException {
        PrintStream printStream;
        StringBuilder stringBuilder;
        if (debug) {
            printStream = System.out;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Bytes in buffer ");
            stringBuilder.append(bytes.remaining());
            printStream.println(stringBuilder.toString());
        }
        this.byteBuffer = bytes;
        this.unicodeVersion = ICUBinary.readHeader(this.byteBuffer, DATA_FORMAT_ID, this);
        if (debug) {
            printStream = System.out;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Bytes left in byteBuffer ");
            stringBuilder.append(this.byteBuffer.remaining());
            printStream.println(stringBuilder.toString());
        }
    }

    public char[] read(int length) throws IOException {
        return ICUBinary.getChars(this.byteBuffer, length, 0);
    }

    public boolean isDataVersionAcceptable(byte[] version) {
        return version[0] == DATA_FORMAT_VERSION[0] && version[2] == DATA_FORMAT_VERSION[2] && version[3] == DATA_FORMAT_VERSION[3];
    }

    public int[] readIndexes(int length) throws IOException {
        int[] indexes = new int[length];
        for (int i = 0; i < length; i++) {
            indexes[i] = this.byteBuffer.getInt();
        }
        return indexes;
    }

    public byte[] getUnicodeVersion() {
        return ICUBinary.getVersionByteArrayFromCompactInt(this.unicodeVersion);
    }
}

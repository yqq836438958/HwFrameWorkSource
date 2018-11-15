package org.apache.http.entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

@Deprecated
public class StringEntity extends AbstractHttpEntity implements Cloneable {
    protected final byte[] content;

    public StringEntity(String s, String charset) throws UnsupportedEncodingException {
        if (s != null) {
            if (charset == null) {
                charset = "ISO-8859-1";
            }
            this.content = s.getBytes(charset);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("text/plain; charset=");
            stringBuilder.append(charset);
            setContentType(stringBuilder.toString());
            return;
        }
        throw new IllegalArgumentException("Source string may not be null");
    }

    public StringEntity(String s) throws UnsupportedEncodingException {
        this(s, null);
    }

    public boolean isRepeatable() {
        return true;
    }

    public long getContentLength() {
        return (long) this.content.length;
    }

    public InputStream getContent() throws IOException {
        return new ByteArrayInputStream(this.content);
    }

    public void writeTo(OutputStream outstream) throws IOException {
        if (outstream != null) {
            outstream.write(this.content);
            outstream.flush();
            return;
        }
        throw new IllegalArgumentException("Output stream may not be null");
    }

    public boolean isStreaming() {
        return false;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
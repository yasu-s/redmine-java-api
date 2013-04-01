package com.taskadapter.redmineapi;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream, which tracks a remaining size and does not allow reader to
 * advance after a specified bound.
 * 
 */
final class LimitedInputStream extends FilterInputStream {

    long limit;

    LimitedInputStream(InputStream peer, long limit) throws IOException {
        super(peer);
        if (limit < 0)
            throw new IllegalArgumentException(
                    "Limit can't be negative but is " + limit);
        this.limit = limit;
        ensureEOF();
    }

    @Override
    public int read() throws IOException {
        if (limit == 0)
            return -1;
        final int res = in.read();
        if (res < 0)
            throw new IOException("Unexpected end of file, expected at least "
                    + limit + " bytes at read position");
        limit -= 1;
        ensureEOF();
        return res;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        if (limit == 0)
            return -1;
        final int readed = in.read(b, off, (int) Math.min(len, limit));
        if (readed < 0)
            throw new IOException("Unexpected end of file, expected at least "
                    + limit + " bytes at read position");
        limit -= readed;
        ensureEOF();
        return readed;
    }
    
    @Override
    public long skip(long n) throws IOException {
        if (limit == 0)
            return 0;
        final long skipped = in.skip(Math.min(limit, n));
        if (skipped < 0)
            throw new IOException("Skipped negative amount " + skipped);
        limit -= skipped;
        ensureEOF();
        return skipped;
    }
    
    @Override
    public int available() throws IOException {
        return (int) Math.min(in.available(), limit);
    }
    
    @Override
    public synchronized void mark(int readlimit) {
        // not supported.
    }
    
    @Override
    public synchronized void reset() throws IOException {
        // not supported.
    }
    
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Ensures an EOF at a proper position.
     * 
     * @throws IOException
     *             if stream fails to read or have an incorrect length.
     */
    private void ensureEOF() throws IOException {
        if (limit == 0)
            if (in.read() >= 0)
                throw new IOException(
                        "Base input stream is longer, than allowed");
    }
}

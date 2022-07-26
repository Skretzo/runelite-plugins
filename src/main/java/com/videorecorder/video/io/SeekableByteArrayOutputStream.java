/*
 * @(#)SeekableByteArrayOutputStream.java
 * 
 * Copyright © 2010-2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package com.videorecorder.video.io;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * {@code SeekableByteArrayOutputStream}.
 *
 * @author Werner Randelshofer
 * @version $Id: SeekableByteArrayOutputStream.java 299 2013-01-03 07:40:18Z werner $
 */
public class SeekableByteArrayOutputStream extends ByteArrayOutputStream {

    /**
     * The current stream position.
     */
    private int pos;

    /**
     * Creates a new byte array output stream. The buffer capacity is
     * initially 32 bytes, though its size increases if necessary.
     */
    public SeekableByteArrayOutputStream() {
	this(32);
    }

    /**
     * Creates a new byte array output stream, with a buffer capacity of
     * the specified size, in bytes.
     *
     * @param   size   the initial size.
     * @exception  IllegalArgumentException if size is negative.
     */
    public SeekableByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
	    buf = new byte[size];
    }

    /**
     * Creates a new byte array output stream, which reuses the supplied buffer.
     */
    public SeekableByteArrayOutputStream(byte[] buf) {
	this.buf = buf;
    }

    /**
     * Writes the specified byte to this byte array output stream.
     *
     * @param   b   the byte to be written.
     */
    @Override
    public synchronized void write(int b) {
	    int newcount = Math.max(pos + 1, count);
	    if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
	    }
	    buf[pos++] = (byte)b;
	    count = newcount;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this byte array output stream.
     *
     * @param   b     the data.
     * @param   off   the start offset in the data.
     * @param   len   the number of bytes to write.
     */
    @Override
    public synchronized void write(byte b[], int off, int len) {
	    if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) > b.length) || ((off + len) < 0)) {
	        throw new IndexOutOfBoundsException();
	    } else if (len == 0) {
	        return;
	    }
        int newcount = Math.max(pos+len,count);
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        System.arraycopy(b, off, buf, pos, len);
        pos+=len;
        count = newcount;
    }

    /**
     * Resets the <code>count</code> field of this byte array output
     * stream to zero, so that all currently accumulated output in the
     * output stream is discarded. The output stream can be used again,
     * reusing the already allocated buffer space.
     */
    @Override
    public synchronized void reset() {
	    count = 0;
        pos=0;
    }

    /** Returns the underlying byte buffer. */
    public byte[] getBuffer() {
        return buf;
    }
}

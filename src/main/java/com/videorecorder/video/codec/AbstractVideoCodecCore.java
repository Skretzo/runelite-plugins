/*
 * @(#)AbstractVideoCodecCore.java
 * 
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package com.videorecorder.video.codec;

import java.io.IOException;
import javax.imageio.stream.ImageOutputStream;

/**
 * {@code AbstractVideoCodecCore}.
 *
 * @author Werner Randelshofer
 * @version $Id: AbstractVideoCodecCore.java 299 2013-01-03 07:40:18Z werner $
 */
public class AbstractVideoCodecCore {

    private byte[] byteBuf = new byte[4];

    protected void writeInt24LE(ImageOutputStream out, int v) throws IOException {
        byteBuf[0] = (byte) (v >>> 0);
        byteBuf[1] = (byte) (v >>> 8);
        byteBuf[2] = (byte) (v >>> 16);
        out.write(byteBuf, 0, 3);
    }

    protected void writeInts24LE(ImageOutputStream out, int[] i, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
        }

        for (int j = 0; j < len; j++) {
            writeInt24LE(out, i[off + j]);
        }
    }
}

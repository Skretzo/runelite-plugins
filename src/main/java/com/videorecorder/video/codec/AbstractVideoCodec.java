/*
 * @(#)AbstractVideoCodec.java
 * 
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package com.videorecorder.video.codec;

import com.videorecorder.video.format.Format;
import com.videorecorder.video.nio.Buffer;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import static com.videorecorder.video.format.VideoFormatKeys.HeightKey;
import static com.videorecorder.video.format.VideoFormatKeys.WidthKey;

/**
 * {@code AbstractVideoCodec}.
 *
 * @author Werner Randelshofer
 * @version $Id: AbstractVideoCodec.java 299 2013-01-03 07:40:18Z werner $
 */
public abstract class AbstractVideoCodec extends AbstractCodec {

    private BufferedImage imgConverter;

    public AbstractVideoCodec(Format[] supportedInputFormats) {
        super(supportedInputFormats);
    }

    /** Gets 8-bit indexed pixels from a buffer. Returns null if conversion failed. */
    protected byte[] getIndexed8(Buffer buf) {
        if (buf.data instanceof byte[]) {
            return (byte[]) buf.data;
        }
        if (buf.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) buf.data;
            if (image.getRaster().getDataBuffer() instanceof DataBufferByte) {
                return ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            }
        }
        return null;
    }

    /** Gets 15-bit RGB pixels from a buffer. Returns null if conversion failed. */
    protected short[] getRGB15(Buffer buf) {
        if (buf.data instanceof int[]) {
            return (short[]) buf.data;
        }
        if (buf.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) buf.data;
            if (image.getColorModel() instanceof DirectColorModel) {
                DirectColorModel dcm = (DirectColorModel) image.getColorModel();
                if (image.getRaster().getDataBuffer() instanceof DataBufferShort) {
                    // FIXME - Implement additional checks
                    return ((DataBufferShort) image.getRaster().getDataBuffer()).getData();
                } else if (image.getRaster().getDataBuffer() instanceof DataBufferUShort) {
                    // FIXME - Implement additional checks
                    return ((DataBufferUShort) image.getRaster().getDataBuffer()).getData();
                }
            }
            if (imgConverter == null) {
                int width = outputFormat.get(WidthKey);
                int height = outputFormat.get(HeightKey);
                imgConverter = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_555_RGB);
            }
            Graphics2D g = imgConverter.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            return ((DataBufferUShort) imgConverter.getRaster().getDataBuffer()).getData();
        }
        return null;
    }

    /** Gets 24-bit RGB pixels from a buffer. Returns null if conversion failed. */
    protected int[] getRGB24(Buffer buf) {
        if (buf.data instanceof int[]) {
            return (int[]) buf.data;
        }
        if (buf.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) buf.data;
            if (image.getColorModel() instanceof DirectColorModel) {
                DirectColorModel dcm = (DirectColorModel) image.getColorModel();
                if (dcm.getBlueMask() == 0xff && dcm.getGreenMask() == 0xff00 && dcm.getRedMask() == 0xff0000) {
                    if (image.getRaster().getDataBuffer() instanceof DataBufferInt) {
                        return ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                    }
                }
            }
            return image.getRGB(0, 0, //
                    outputFormat.get(WidthKey), outputFormat.get(HeightKey), //
                    null, 0, outputFormat.get(WidthKey));
        }
        return null;
    }

    /** Gets 32-bit ARGB pixels from a buffer. Returns null if conversion failed. */
    protected int[] getARGB32(Buffer buf) {
        if (buf.data instanceof int[]) {
            return (int[]) buf.data;
        }
        if (buf.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) buf.data;
            if (image.getColorModel() instanceof DirectColorModel) {
                DirectColorModel dcm = (DirectColorModel) image.getColorModel();
                if (dcm.getBlueMask() == 0xff && dcm.getGreenMask() == 0xff00 && dcm.getRedMask() == 0xff0000) {
                    if (image.getRaster().getDataBuffer() instanceof DataBufferInt) {
                        return ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                    }
                }
            }
            return image.getRGB(0, 0, //
                    outputFormat.get(WidthKey), outputFormat.get(HeightKey), //
                    null, 0, outputFormat.get(WidthKey));
        }
        return null;
    }
}

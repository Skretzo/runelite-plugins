/*
 * @(#)VideoFormatKeys.java  
 * 
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package com.videorecorder.video;

import com.videorecorder.video.math.Rational;

/**
 * Defines common format keys for video media.
 *
 * @author Werner Randelshofer
 * @version $Id: VideoFormatKeys.java 299 2013-01-03 07:40:18Z werner $
 */
public class VideoFormatKeys extends FormatKeys {
    // Standard video ENCODING strings for use with FormatKey.Encoding.
    public static final String ENCODING_BUFFERED_IMAGE = "image";

    /** Microsoft Device Independent Bitmap (DIB) format. */
    public static final String ENCODING_AVI_DIB = "DIB ";

    /** Microsoft Run Length format. */
    public static final String ENCODING_AVI_RLE = "RLE ";

    /** Techsmith Screen Capture format. */
    public static final String ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE = "tscc";
    public static final String COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE = "TechSmith Screen Capture";

    /** The WidthKey of a video frame. */
    public final static FormatKey<Integer> WidthKey = new FormatKey<>("dimX","width", Integer.class);

    /** The HeightKey of a video frame. */
    public final static FormatKey<Integer> HeightKey = new FormatKey<>("dimY","height", Integer.class);

    /** The number of bits per pixel. */
    public final static FormatKey<Integer> DepthKey = new FormatKey<>("dimZ","depth", Integer.class);

    /** The data class. */
    public final static FormatKey<Class> DataClassKey = new FormatKey<>("dataClass", Class.class);

    /** The compressor name. */
    public final static FormatKey<String> CompressorNameKey = new FormatKey<>("compressorName", "compressorName", String.class, true);

    /** The compressor name. */
    public final static FormatKey<Integer> CompressionLevelKey = new FormatKey<>("compressionLevel", "compressionLevel", Integer.class);

    /** The pixel aspect ratio WidthKey : HeightKey; */
    public final static FormatKey<Rational> PixelAspectRatioKey = new FormatKey<>("pixelAspectRatio", Rational.class);

    /** Whether the frame rate must be fixed. False means variable frame rate. */
    public final static FormatKey<Boolean> FixedFrameRateKey = new FormatKey<>("fixedFrameRate", Boolean.class);

    /** Whether the video is interlaced. */
    public final static FormatKey<Boolean> InterlaceKey = new FormatKey<>("interlace", Boolean.class);

    /** Encoding quality. Value between 0 and 1. */
    public final static FormatKey<Float> QualityKey = new FormatKey<>("quality", Float.class);
}

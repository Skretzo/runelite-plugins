/*
 * @(#)FormatKeys.java 
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
 * Defines common {@code FormatKey}'s.
 *
 * @author Werner Randelshofer
 * @version $Id: FormatKeys.java 299 2013-01-03 07:40:18Z werner $
 */
public class FormatKeys {
     public enum MediaType {
        AUDIO,
        VIDEO,
        FILE
    }

    /**
     * The media MediaTypeKey.
     */
    public final static FormatKey<MediaType> MediaTypeKey = new FormatKey<>("mediaType", MediaType.class);

    /**
     * The EncodingKey.
     */
    public final static FormatKey<String> EncodingKey = new FormatKey<>("encoding", String.class);
    
    public final static String MIME_AVI = "video/avi";
    public final static String MIME_QUICKTIME = "video/quicktime";
    public final static String MIME_JAVA = "Java";

    /**
     * The mime type.
     */
    public final static FormatKey<String> MimeTypeKey = new FormatKey<>("mimeType", String.class);

    /** 
     * The number of frames per second.
     */
    public final static FormatKey<Rational> FrameRateKey = new FormatKey<>("frameRate", Rational.class);

    /** 
     * The interval between key frames.
     * If this value is not specified, most codecs will use {@code FrameRateKey}
     * as a hint and try to produce one key frame per second.
     */
    public final static FormatKey<Integer> KeyFrameIntervalKey = new FormatKey<>("keyFrameInterval", Integer.class);
}

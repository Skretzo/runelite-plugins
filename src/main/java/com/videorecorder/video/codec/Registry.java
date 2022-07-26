/*
 * @(#)Registry.java  
 * 
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package com.videorecorder.video.codec;

import com.videorecorder.video.avi.AVIWriter;
import com.videorecorder.video.format.Format;
import com.videorecorder.video.format.FormatKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import static com.videorecorder.video.format.FormatKey.EncodingKey;
import static com.videorecorder.video.format.FormatKey.MIME_AVI;
import static com.videorecorder.video.format.FormatKey.MIME_JAVA;
import static com.videorecorder.video.format.FormatKey.MIME_QUICKTIME;
import static com.videorecorder.video.format.FormatKey.MediaTypeKey;
import static com.videorecorder.video.format.FormatKey.MimeTypeKey;
import static com.videorecorder.video.format.VideoFormatKeys.COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE;
import static com.videorecorder.video.format.VideoFormatKeys.CompressorNameKey;
import static com.videorecorder.video.format.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static com.videorecorder.video.format.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;

/**
 * The {@code Registry} for audio and video codecs.
 *
 * @author Werner Randelshofer
 * @version $Id: Registry.java 299 2013-01-03 07:40:18Z werner $
 */
public class Registry {

    private HashMap<String, LinkedList<RegistryEntry>> codecMap;
    private HashMap<String, LinkedList<RegistryEntry>> writerMap;

    private static class RegistryEntry {

        Format inputFormat;
        Format outputFormat;
        String className;

        public RegistryEntry(Format inputFormat, Format outputFormat, String className) {
            this.inputFormat = inputFormat;
            this.outputFormat = outputFormat;
            this.className = className;
        }
    }

    private static Registry instance;

    public static Registry getInstance() {
        if (instance == null) {
            instance = new Registry();
            instance.init();
        }
        return instance;
    }

    /**
     * Initializes the registry.
     */
    protected void init() {
        codecMap = new HashMap<>();
        writerMap = new HashMap<>();

        putBidiCodec(
            new Format(MediaTypeKey, FormatKey.MediaType.VIDEO, MimeTypeKey, MIME_AVI, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE),
            new Format(MediaTypeKey, FormatKey.MediaType.VIDEO, MimeTypeKey, MIME_JAVA, EncodingKey, ENCODING_BUFFERED_IMAGE),
                TechSmithCodec.class.getName());

        putBidiCodec(
            new Format(MediaTypeKey, FormatKey.MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE),
            new Format(MediaTypeKey, FormatKey.MediaType.VIDEO, MimeTypeKey, MIME_JAVA, EncodingKey, ENCODING_BUFFERED_IMAGE),
                TechSmithCodec.class.getName());

        putWriter(new Format(MediaTypeKey, FormatKey.MediaType.FILE, MimeTypeKey, MIME_AVI), AVIWriter.class.getName());
    }

    /**
     *
     * @param inputFormat Must have {@code MediaTypeKey}, {@code EncodingKey}, {@code MimeTypeKey}.
     * @param outputFormat Must have {@code MediaTypeKey}, {@code EncodingKey}, {@code MimeTypeKey}.
     * @param codecClass The string codec class.
     */
    public void putBidiCodec(Format inputFormat, Format outputFormat, String codecClass) {
        putCodec(inputFormat, outputFormat, codecClass);
        putCodec(outputFormat, inputFormat, codecClass);
    }

    /**
     * Puts a codec into the registry.
     *
     * @param inputFormat The input format. Must not be null.
     * @param outputFormat The output format. Must not be null.
     * @param codecClass The codec class name. Must not be null.
     */
    public void putCodec(Format inputFormat, Format outputFormat, String codecClass) {
        RegistryEntry entry = new RegistryEntry(inputFormat, outputFormat, codecClass);
        addCodecEntry(inputFormat.get(EncodingKey), entry);
        addCodecEntry(outputFormat.get(EncodingKey), entry);
    }

    private void addCodecEntry(String key, RegistryEntry entry) {
        LinkedList<RegistryEntry> list = codecMap.get(key);
        if (list == null) {
            list = new LinkedList<>();
            codecMap.put(key, list);
        }
        list.add(entry);
    }

    /**
     * Gets all codecs which can transcode from the specified input format to
     * the specified output format.
     *
     * @param inputFormat The input format.
     * @param outputFormat The output format.
     * @return An array of codec class names. If no codec was found, an empty
     * array is returned.
     */
    public String[] getCodecClasses(Format inputFormat, Format outputFormat) {
        HashSet<String> classNames = new HashSet<>();
        HashSet<RegistryEntry> entries = new HashSet<>();
        if (inputFormat != null) {
            LinkedList<RegistryEntry> re;
            if (inputFormat.get(EncodingKey) == null) {
                re = new LinkedList<>();
                for (Map.Entry<String, LinkedList<RegistryEntry>> i : codecMap.entrySet()) {
                    for (RegistryEntry j : i.getValue()) {
                        if (inputFormat.matches(j.inputFormat)) {
                            re.add(j);
                        }
                    }
                }
            } else {
                re = codecMap.get(inputFormat.get(EncodingKey));
            }
            if (re != null) {
                entries.addAll(re);
            }
        }
        if (outputFormat != null) {
            LinkedList<RegistryEntry> re;
            if (outputFormat.get(EncodingKey) == null) {
                re = new LinkedList<>();
                for (Map.Entry<String, LinkedList<RegistryEntry>> i : codecMap.entrySet()) {
                    for (RegistryEntry j : i.getValue()) {
                        if (outputFormat.matches(j.outputFormat)) {
                            re.add(j);
                        }
                    }
                }
            } else {
                re = codecMap.get(outputFormat.get(EncodingKey));
            }
            if (re != null) {
                entries.addAll(re);
            }
        }
        for (RegistryEntry e : entries) {
            if ((inputFormat == null || e.inputFormat == null || inputFormat.matches(e.inputFormat))
                    && (outputFormat == null || e.outputFormat == null || outputFormat.matches(e.outputFormat))) {
                classNames.add(e.className);
            }
        }
        return classNames.toArray(new String[classNames.size()]);
    }

    /**
     * Gets the first codec which can encode the specified foramt.
     *
     * @param outputFormat The output format.
     * @return A codec. Returns null if no codec was found.
     */
    public Codec getEncoder(Format outputFormat) {
        return getCodec(null, outputFormat);
    }

    /**
     * Gets a codec which can transcode from the specified input format to the
     * specified output format.
     *
     * @param inputFormat The input format.
     * @param outputFormat The output format.
     * @return A codec or null.
     */
    public Codec getCodec(Format inputFormat, Format outputFormat) {
        String[] clazz = getCodecClasses(inputFormat, outputFormat);
        for (int i = 0; i < clazz.length; i++) {
            try {
                Codec codec = ((Codec) Class.forName(clazz[i]).newInstance());
                codec.setInputFormat(inputFormat);
                if (outputFormat != null) {
                    codec.setOutputFormat(outputFormat);
                }
                return codec;
            } catch (Exception ex) {
                System.err.println("Monte Registry. Codec class not found: " + clazz[i]);
                unregisterCodec(clazz[i]);
            }
        }
        return null;
    }

    /**
     * Puts a writer into the registry.
     *
     * @param fileFormat The file format, e.g."video/avi", "video/quicktime".
     * Use "Java" for formats which are not tied to a file format. Must not be
     * null.
     * @param writerClass The writer class name. Must not be null.
     */
    public void putWriter(Format fileFormat, String writerClass) {
        RegistryEntry entry = new RegistryEntry(fileFormat, null, writerClass);
        String key = fileFormat.get(MimeTypeKey);
        LinkedList<RegistryEntry> list = writerMap.get(key);
        if (list == null) {
            list = new LinkedList<>();
            writerMap.put(key, list);
        }
        list.add(entry);
    }

    public void unregisterCodec(String codecClass) {
        for (Map.Entry<String, LinkedList<RegistryEntry>> i:codecMap.entrySet()) {
            LinkedList<RegistryEntry> ll=i.getValue();
            for (Iterator<RegistryEntry> j = ll.iterator(); j.hasNext();) {
                RegistryEntry e = j.next();
                if (e.className.equals(codecClass)) {
                    j.remove();
                }
            }
        }
    }
}

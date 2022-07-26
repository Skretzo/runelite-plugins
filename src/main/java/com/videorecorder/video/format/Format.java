/*
 * @(#)Format.java  
 * 
 * Copyright (c) 2011-2012 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance onlyWith the
 * license agreement you entered into onlyWith Werner Randelshofer.
 * For details see accompanying license terms.
 */
package com.videorecorder.video.format;

import java.util.HashMap;
import java.util.Map;

/**
 * Specifies the format of a media, for example of audio and video.
 *
 * @author Werner Randelshofer
 * @version $Id: Format.java 299 2013-01-03 07:40:18Z werner $
 */
public class Format {

    /**
     * Holds the properties of the format.
     */
    private HashMap<FormatKey, Object> properties;

    /**
     * Creates a new format onlyWith the specified properties.
     */
    public Format(Map<FormatKey, Object> properties) {
        this(properties, true);
    }

    /**
     * Creates a new format onlyWith the specified properties.
     */
    private Format(Map<FormatKey, Object> properties, boolean copy) {
        if (copy || ! (properties instanceof HashMap)) {
            for (Map.Entry<FormatKey, Object> e : properties.entrySet()) {
                if (!e.getKey().isAssignable(e.getValue())) {
                    throw new ClassCastException(e.getValue() + " must be of type " + e.getKey().getValueClass());
                }
            }
            this.properties = new HashMap<>(properties);
        } else {
            this.properties = (HashMap<FormatKey, Object>) properties;
        }
    }

    /**
     * Creates a new format onlyWith the specified properties. The properties
     * must be given as key value pairs.
     */
    public Format(Object... p) {
        this.properties = new HashMap<>();
        for (int i = 0; i < p.length; i += 2) {
            FormatKey key = (FormatKey) p[i];
            if (!key.isAssignable(p[i + 1])) {
                throw new ClassCastException(key + ": " + p[i + 1] + " must be of type " + key.getValueClass());
            }
            this.properties.put(key, p[i + 1]);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(FormatKey<T> key) {
        return (T) properties.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(FormatKey<T> key, T defaultValue) {
        return (properties.containsKey(key)) ? (T) properties.get(key) : defaultValue;
    }

    public boolean containsKey(FormatKey key) {
        return properties.containsKey(key);
    }

    /**
     * Returns true if that format matches this format. That is iff all
     * properties defined in both format objects are identical. Properties which
     * are only defined in one of the format objects are not considered.
     *
     * @param that Another format.
     * @return True if the other format matches this format.
     */
    public boolean matches(Format that) {
        for (Map.Entry<FormatKey, Object> e : properties.entrySet()) {
            if (!e.getKey().isComment()) {
                if (that.properties.containsKey(e.getKey())) {
                    Object a = e.getValue();
                    Object b = that.properties.get(e.getKey());
                    if (a != b && a == null || !a.equals(b)) {
                        return false;
                    }

                }
            }
        }
        return true;
    }

    /**
     * Creates a new format which contains all properties from this format and
     * additional properties from that format. <p> If a property is specified in
     * both formats, then the property value from this format is used. It
     * overwrites that format. <p> If one of the format has more properties than
     * the other, then the new format is more specific than this format.
     *
     * @param that The format to be appended.
     * @return That format with properties overwritten by this format.
     */
    public Format append(Format that) {
        HashMap<FormatKey, Object> m = new HashMap<>(this.properties);
        for (Map.Entry<FormatKey, Object> e : that.properties.entrySet()) {
            if (!m.containsKey(e.getKey())) {
                m.put(e.getKey(), e.getValue());
            }
        }
        return new Format(m, false);
    }

    /**
     * Creates a new format which contains all specified properties and 
     * additional properties from this format. 
     * <p> If a property is specified in both formats, then the property value
     * from this format is used. It overwrites that format. 
     * <p> If one of the format has more properties than the other, then the new
     * format is more specific than this format.
     *
     * @param p The properties must be given as key value pairs.
     * @return That format with properties overwritten by this format.
     */
    public Format prepend(Object... p) {
        HashMap<FormatKey, Object> m = new HashMap<>();
        for (int i = 0; i < p.length; i += 2) {
            FormatKey key = (FormatKey) p[i];
            if (!key.isAssignable(p[i + 1])) {
                throw new ClassCastException(key + ": " + p[i + 1] + " must be of type " + key.getValueClass());
            }
            m.put(key, p[i + 1]);
        }
        for (Map.Entry<FormatKey, Object> e : this.properties.entrySet()) {
            if (!m.containsKey(e.getKey())) {
                m.put(e.getKey(), e.getValue());
            }
        }
        return new Format(m, false);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("Format{");
        boolean isFirst = true;
        for (Map.Entry<FormatKey, Object> e : properties.entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                buf.append(',');
            }
            buf.append(e.getKey().toString());
            buf.append(':');
            appendStuffedString(e.getValue(), buf);
        }
        buf.append('}');
        return buf.toString();
    }

    /**
     * This method is used by #toString.
     */
    private static void appendStuffedString(Object value, StringBuilder stuffed) {
        if (value == null) {
            stuffed.append("null");
        }
        value = value.toString();
        if (value instanceof String) {
            for (char ch : ((String) value).toCharArray()) {
                if (ch >= ' ') {
                    stuffed.append(ch);
                } else {
                    String hex = Integer.toHexString(ch);
                    stuffed.append("\\u");
                    for (int i = hex.length(); i < 4; i++) {
                        stuffed.append('0');
                    }
                    stuffed.append(hex);
                }
            }
        }
    }
}

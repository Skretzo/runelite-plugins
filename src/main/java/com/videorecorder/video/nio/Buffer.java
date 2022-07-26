/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.videorecorder.video.nio;

import com.videorecorder.video.format.Format;
import java.util.EnumSet;

/**
 * A {@code Buffer} carries media data from one media processing unit to another.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-03-12 Created.
 */
public class Buffer {

    /** A flag mask that describes the boolean attributes for this buffer.
     */
    public EnumSet<BufferFlag> flags = EnumSet.noneOf(BufferFlag.class);

    /** The track number.
     */
    public int track;

    /** Header information, such as RTP header for this chunk. */
    public Object header;

    /** The media data. */
    public Object data;

    /** The data offset. This field is only used if {@code data} is an array. */
    public int offset;

    /** The data length. This field is only used if {@code data} is an array. */
    public int length;

    /** Duration of a sample in seconds.
     * Multiply this with {@code sampleCount} to get the buffer duration. 
     */
    public int sampleDuration;

    /** The time stamp of this buffer in seconds. */
    public int timeStamp;

    /** The format of the data in this buffer. */
    public Format format;

    /** The number of samples in the data field. */
    public int sampleCount = 1;
    
    /** Sequence number of the buffer. This can be used for debugging. */
    public long sequenceNumber;

    /** Sets all variables of this buffer to that buffer except for {@code data},
     * {@code offset}, {@code length} and {@code header}.
     */
    public void setMetaTo(Buffer that) {
        this.flags = EnumSet.copyOf(that.flags);
        // this.data = that.data;
        // this.offset = that.offset;
        // this.length = that.length;
        // this.header = that.header;
        this.track = that.track;
        this.sampleDuration = that.sampleDuration;
        this.timeStamp = that.timeStamp;
        this.format = that.format;
        this.sampleCount = that.sampleCount;
        this.format = that.format;
        this.sequenceNumber = that.sequenceNumber;
    }

    /** Returns true if the specified flag is set. */
    public boolean isFlag(BufferFlag flag) {
        return flags.contains(flag);
    }

    /** Convenience method for setting a flag. */
    public void setFlag(BufferFlag flag) {
        setFlag(flag, true);
    }

    /** Convenience method for clearing a flag. */
    public void clearFlag(BufferFlag flag) {
        setFlag(flag, false);
    }

    /** Sets or clears the specified flag. */
    public void setFlag(BufferFlag flag, boolean value) {
        if (value) {
            flags.add(flag);
        } else {
            flags.remove(flag);
        }
    }
}

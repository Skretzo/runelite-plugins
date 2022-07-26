/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.videorecorder.video.codec;

import com.videorecorder.video.format.Format;
import com.videorecorder.video.nio.Buffer;

/**
 * A {@code Codec} processes a {@code Buffer} and stores the result in another
 * {@code Buffer}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-03-12 Created.
 */
public interface Codec {

    /** The codec successfully converted the input to output. */
    int CODEC_OK = 0;

    /** The codec could not handle the input. */
    int CODEC_FAILED = 1;

    /** Lists all of the input formats that this codec accepts. */
    Format[] getInputFormats();

    /** Lists all of the output formats that this codec can generate
     * with the provided input format. If the input format is null, returns
     * all supported output formats.
     */
    Format[] getOutputFormats(Format input);

    /** Sets the input format.
     * Returns the format that was actually set. This is the closest format
     * that the Codec supports. Returns null if the specified format is not
     * supported and no reasonable match could be found.
     */
    Format setInputFormat(Format input);

    Format getInputFormat();

    /** Sets the output format.
     * Returns the format that was actually set. This is the closest format
     * that the Codec supports. Returns null if the specified format is not
     * supported and no reasonable match could be found.
     */
    Format setOutputFormat(Format output);

    Format getOutputFormat();

    /** Performs the media processing defined by this codec. 
     * <p>
     * Copies the data from the input buffer into the output buffer.
     * 
     * @return A combination of processing flags.
     */
    int process(Buffer in, Buffer out);

    /** Returns a human readable name of the codec. */
    String getName();

    /** Resets the state of the codec. */
    void reset();
}

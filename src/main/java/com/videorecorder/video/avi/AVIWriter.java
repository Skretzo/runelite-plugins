/*
 * @(#)AVIWriter.java
 *
 * Copyright (c) 2011-2012 Werner Randelshofer, Goldau, Switzerland. All
 * rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer. For details see
 * accompanying license terms.
 */
package com.videorecorder.video.avi;

import com.videorecorder.video.codec.Codec;
import com.videorecorder.video.codec.Registry;
import com.videorecorder.video.codec.TechSmithCodec;
import com.videorecorder.video.format.Format;
import com.videorecorder.video.format.FormatKey;
import com.videorecorder.video.io.ByteArrayImageOutputStream;
import com.videorecorder.video.nio.Buffer;
import com.videorecorder.video.nio.BufferFlag;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import static com.videorecorder.video.format.FormatKey.EncodingKey;
import static com.videorecorder.video.format.FormatKey.FrameRateKey;
import static com.videorecorder.video.format.FormatKey.KeyFrameIntervalKey;
import static com.videorecorder.video.format.FormatKey.MIME_AVI;
import static com.videorecorder.video.format.FormatKey.MediaTypeKey;
import static com.videorecorder.video.format.FormatKey.MimeTypeKey;
import static com.videorecorder.video.format.VideoFormatKeys.CompressionLevelKey;
import static com.videorecorder.video.format.VideoFormatKeys.DataClassKey;
import static com.videorecorder.video.format.VideoFormatKeys.DepthKey;
import static com.videorecorder.video.format.VideoFormatKeys.ENCODING_AVI_DIB;
import static com.videorecorder.video.format.VideoFormatKeys.ENCODING_AVI_RLE;
import static com.videorecorder.video.format.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static com.videorecorder.video.format.VideoFormatKeys.FixedFrameRateKey;
import static com.videorecorder.video.format.VideoFormatKeys.HeightKey;
import static com.videorecorder.video.format.VideoFormatKeys.QualityKey;
import static com.videorecorder.video.format.VideoFormatKeys.WidthKey;
import static com.videorecorder.video.nio.BufferFlag.DISCARD;
import static com.videorecorder.video.nio.BufferFlag.KEYFRAME;

/**
 * Provides low-level support for writing already encoded audio and video
 * samples into an AVI 1.0 file. <p> The length of an AVI 1.0 file is limited to
 * 1 GB. This class supports lengths of up to 4 GB, but such files may not work
 * on all players. <p> For detailed information about the AVI 1.0 file format
 * see:<br> <a
 * href="http://msdn.microsoft.com/en-us/library/ms779636.aspx">msdn.microsoft.com
 * AVI RIFF</a><br> <a
 * href="http://www.microsoft.com/whdc/archive/fourcc.mspx">www.microsoft.com
 * FOURCC for Video Compression</a><br> <a
 * href="http://www.saettler.com/RIFFMCI/riffmci.html">www.saettler.com
 * RIFF</a><br>
 *
 * @author Werner Randelshofer
 * @version $Id: AVIWriter.java 306 2013-01-04 16:19:29Z werner $
 */
public class AVIWriter {

    /**
     * Chunk IDs.
     */
    protected final static int RIFF_ID =0x52494646;// "RIFF"
    protected final static int AVI_ID = 0x41564920;// "AVI "
    protected final static int LIST_ID = 0x4c495354;// "LIST"
    protected final static int MOVI_ID = 0x6d6f7669;// "movi"
    protected final static int HDRL_ID = 0x6864726c;// "hdrl"
    protected final static int AVIH_ID = 0x61766968;// "avih"
    protected final static int STRL_ID = 0x7374726c;// "strl"
    protected final static int STRH_ID = 0x73747268;// "strh"
    protected final static int STRN_ID = 0x7374726e;// "strn"
    protected final static int STRF_ID = 0x73747266;// "strf"
    protected final static int IDX1_ID = 0x69647831;// "idx1"
    protected final static int PC_ID = 0x00007063;// "??pc"
    protected final static int DB_ID = 0x00006462;// "??db"
    protected final static int DC_ID = 0x00006463;// "??dc"

    /**
     * Indicates this video stream contains palette changes. This flag warns the
     * playback software that it will need to animate the palette.
     */
    public final static int STRH_FLAG_VIDEO_PALETTE_CHANGES = 0x00010000;

    /**
     * Underlying output stream.
     */
    protected ImageOutputStream out;

    /**
     * The offset in the underlying ImageOutputStream. Normally this is 0 unless
     * the underlying stream already contained data when it was passed to the
     * constructor.
     */
    protected long streamOffset;

    /**
     * The states of the movie output stream.
     */
    protected enum States {

        STARTED, FINISHED, CLOSED
    }

    /**
     * The current state of the movie output stream.
     */
    protected States state = States.FINISHED;

    /**
     * This chunk holds the whole AVI content.
     */
    protected CompositeChunk aviChunk;

    /**
     * This chunk holds the movie frames.
     */
    protected CompositeChunk moviChunk;

    /**
     * This chunk holds the AVI Main Header.
     */
    protected FixedSizeDataChunk avihChunk;

    ArrayList<Sample> idx1 = new ArrayList<>();

    /**
     * Creates a new AVI writer.
     *
     * @param file the output file
     */
    public AVIWriter(File file) throws IOException {
        if (file.exists()) {
            file.delete();
        }
        this.out = new FileImageOutputStream(file);
        out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        this.streamOffset = 0;
    }

    /**
     * Adds a track.
     *
     * @param vf The format of the track.
     * @return The track number.
     */
    public int addVideoTrack(Format vf) throws IOException {
        if (!vf.containsKey(EncodingKey)) {
            throw new IllegalArgumentException("EncodingKey missing in " + vf);
        }
        if (!vf.containsKey(FrameRateKey)) {
            throw new IllegalArgumentException("FrameRateKey missing in " + vf);
        }
        if (!vf.containsKey(WidthKey)) {
            throw new IllegalArgumentException("WidthKey missing in " + vf);
        }
        if (!vf.containsKey(HeightKey)) {
            throw new IllegalArgumentException("HeightKey missing in " + vf);
        }
        if (!vf.containsKey(DepthKey)) {
            throw new IllegalArgumentException("DepthKey missing in " + vf);
        }
        int tr = addVideoTrack(
            vf.get(EncodingKey),
            1,
            vf.get(FrameRateKey),
            vf.get(WidthKey),
            vf.get(HeightKey),
            vf.get(DepthKey),
            vf.get(KeyFrameIntervalKey, vf.get(FrameRateKey)),
            vf.get(CompressionLevelKey, TechSmithCodec.DEFAULT_COMPRESSION_LEVEL));
        setCompressionQuality(tr, vf.get(QualityKey, 1.0f));
        return tr;
    }

    /**
     * Adds a video track.
     *
     * @param fccHandler The 4-character code of the format.
     * @param scale The denominator of the sample rate.
     * @param rate The numerator of the sample rate.
     * @param width The width of a video image. Must be greater than 0.
     * @param height The height of a video image. Must be greater than 0.
     * @param depth The number of bits per pixel. Must be greater than 0.
     * @param syncInterval Interval for sync-samples. 0 = automatic. 1 = all frames
     * are keyframes. Values larger than 1 specify that for every n-th frame is
     * a keyframe.
     *
     * @return Returns the track index.
     *
     * @throws IllegalArgumentException if the width or the height is smaller
     * than 1.
     */
    public int addVideoTrack(String fccHandler, int scale, int rate, int width, int height, int depth, int syncInterval, int compressionLevel) throws IOException {
        ensureFinished();

        if (fccHandler == null || fccHandler.length() != 4) {
            throw new IllegalArgumentException("fccHandler must be 4 characters long:" + fccHandler);
        }

        VideoTrack vt = new VideoTrack(
            tracks.size(),
            typeToInt(fccHandler),
            new Format(MediaTypeKey, FormatKey.MediaType.VIDEO,
                MimeTypeKey, MIME_AVI,
                EncodingKey, fccHandler,
                DataClassKey, byte[].class,
                WidthKey, width,
                HeightKey, height,
                DepthKey, depth,
                FixedFrameRateKey, true,
                FrameRateKey, rate,
                KeyFrameIntervalKey, syncInterval,
                CompressionLevelKey, compressionLevel));
        vt.scale = scale;
        vt.rate = rate;
        vt.syncInterval = syncInterval;
        vt.frameLeft = 0;
        vt.frameTop = 0;
        vt.frameRight = width;
        vt.frameBottom = height;
        vt.bitCount = depth;
        vt.planes = 1; // must be 1

        if (depth == 4) {
            byte[] gray = new byte[16];
            for (int i = 0; i < gray.length; i++) {
                gray[i] = (byte) ((i << 4) | i);
            }
            vt.palette = new IndexColorModel(4, 16, gray, gray, gray);
        } else if (depth == 8) {
            byte[] gray = new byte[256];
            for (int i = 0; i < gray.length; i++) {
                gray[i] = (byte) i;
            }
            vt.palette = new IndexColorModel(8, 256, gray, gray, gray);
        }

        tracks.add(vt);
        return tracks.size() - 1;
    }

    /**
     * Encodes the provided image and writes its sample data into the specified
     * track.
     *
     * @param track The track index.
     * @param image The image of the video frame.
     *
     * @throws IllegalArgumentException if the dimension of the frame
     * does not match the dimension of the video.
     * @throws UnsupportedOperationException if the {@code MovieWriter} does not
     * have a built-in encoder for this video format.
     * @throws IOException if writing the sample data failed.
     */
    public void write(int track, BufferedImage image) throws IOException {
        if (isClosed()) {
            return;
        }

        ensureStarted();

        VideoTrack vt = tracks.get(track);
        if (vt.codec == null) {
            createCodec(track);
        }
        if (vt.codec == null) {
            throw new UnsupportedOperationException("No codec for this format: " + vt.format);
        }

        // The dimension of the image must match the dimension of the video track
        Format fmt = vt.format;
        if (fmt.get(WidthKey) != image.getWidth() || fmt.get(HeightKey) != image.getHeight()) {
            throw new IllegalArgumentException("Dimensions of image[" + vt.samples.size()
                    + "] (width=" + image.getWidth() + ", height=" + image.getHeight()
                    + ") differs from video format of track: " + fmt);
        }

        // Encode pixel data
        {
            if (vt.outputBuffer == null) {
                vt.outputBuffer = new Buffer();
            }

            boolean isKeyframe = vt.syncInterval == 0 ? false : vt.samples.size() % vt.syncInterval == 0;

            Buffer inputBuffer = new Buffer();
            inputBuffer.flags = (isKeyframe) ? EnumSet.of(KEYFRAME) : EnumSet.noneOf(BufferFlag.class);
            inputBuffer.data = image;
            vt.codec.process(inputBuffer, vt.outputBuffer);
            if (vt.outputBuffer.flags.contains(DISCARD)) {
                return;
            }

            // Encode palette data
            isKeyframe = vt.outputBuffer.flags.contains(KEYFRAME);
            boolean paletteChange = writePalette(track, image, isKeyframe);
            writeSample(track, (byte[]) vt.outputBuffer.data, vt.outputBuffer.offset, vt.outputBuffer.length, isKeyframe && !paletteChange);
        }
    }

    private boolean writePalette(int track, BufferedImage image, boolean isKeyframe) throws IOException {
        if ((image.getColorModel() instanceof IndexColorModel)) {
            return writePalette(track, (IndexColorModel) image.getColorModel(), isKeyframe);
        }
        return false;
    }

    private boolean writePalette(int track, IndexColorModel imgPalette, boolean isKeyframe) throws IOException {
        ensureStarted();

        VideoTrack vt = tracks.get(track);
        int imgDepth = vt.bitCount;
        ByteArrayImageOutputStream tmp = null;
        boolean paletteChange = false;
        switch (imgDepth) {
            case 4: {
                int[] imgRGBs = new int[16];
                imgPalette.getRGBs(imgRGBs);
                int[] previousRGBs = new int[16];
                if (vt.previousPalette == null) {
                    vt.previousPalette = vt.palette;
                }
                vt.previousPalette.getRGBs(previousRGBs);
                if (isKeyframe || !Arrays.equals(imgRGBs, previousRGBs)) {
                    paletteChange = true;
                    vt.previousPalette = imgPalette;
                    int first = 0;
                    int last = imgPalette.getMapSize() - 1;
                    tmp = new ByteArrayImageOutputStream(ByteOrder.LITTLE_ENDIAN);
                    tmp.writeByte(first);//bFirstEntry
                    tmp.writeByte(last - first + 1);//bNumEntries
                    tmp.writeShort(0);//wFlags

                    for (int i = first; i <= last; i++) {
                        tmp.writeByte((imgRGBs[i] >>> 16) & 0xff); // red
                        tmp.writeByte((imgRGBs[i] >>> 8) & 0xff); // green
                        tmp.writeByte(imgRGBs[i] & 0xff); // blue
                        tmp.writeByte(0); // reserved*/
                    }

                }
                break;
            }
            case 8: {
                int[] imgRGBs = new int[256];
                imgPalette.getRGBs(imgRGBs);
                int[] previousRGBs = new int[256];
                if (vt.previousPalette != null) {
                    vt.previousPalette.getRGBs(previousRGBs);
                }
                if (isKeyframe || !Arrays.equals(imgRGBs, previousRGBs)) {
                    paletteChange = true;
                    vt.previousPalette = imgPalette;
                    int first = 0;
                    int last = imgPalette.getMapSize() - 1;
                    tmp = new ByteArrayImageOutputStream(ByteOrder.LITTLE_ENDIAN);
                    tmp.writeByte(first); // bFirstEntry
                    tmp.writeByte(last - first + 1); // bNumEntries
                    tmp.writeShort(0); // wFlags
                    for (int i = first; i <= last; i++) {
                        tmp.writeByte((imgRGBs[i] >>> 16) & 0xff); // red
                        tmp.writeByte((imgRGBs[i] >>> 8) & 0xff); // green
                        tmp.writeByte(imgRGBs[i] & 0xff); // blue
                        tmp.writeByte(0); // reserved*/
                    }
                }

                break;
            }
        }
        if (tmp != null) {
            tmp.close();
            writePalette(track, tmp.toByteArray(), 0, (int) tmp.length(), isKeyframe);
        }
        return paletteChange;
    }

    /**
     * Sets the global color palette.
     */
    public void setPalette(int track, ColorModel palette) {
        if (palette instanceof IndexColorModel) {
            tracks.get(track).palette = (IndexColorModel) palette;
        }
    }

    private Codec createCodec(Format fmt) {
        return Registry.getInstance().getEncoder(fmt.prepend(MimeTypeKey, MIME_AVI));
    }

    private void createCodec(int track) {
        VideoTrack tr = tracks.get(track);
        Format fmt = tr.format;
        tr.codec = createCodec(fmt);
        if (tr.codec != null) {
            if (fmt.get(MediaTypeKey) == FormatKey.MediaType.VIDEO) {
                tr.codec.setInputFormat(fmt.prepend(
                        EncodingKey, ENCODING_BUFFERED_IMAGE,
                        DataClassKey, BufferedImage.class));
                if (null == tr.codec.setOutputFormat(
                        fmt.prepend(FixedFrameRateKey, true,
                                QualityKey, getCompressionQuality(track),
                                MimeTypeKey, MIME_AVI,
                                DataClassKey, byte[].class))) {
                    throw new UnsupportedOperationException("Track " + tr + " codec does not support format " + fmt + ". codec=" + tr.codec);
                }
            } else {
                tr.codec.setInputFormat(null);
                if (null == tr.codec.setOutputFormat(
                        fmt.prepend(FixedFrameRateKey, true,
                                QualityKey, getCompressionQuality(track),
                                MimeTypeKey, MIME_AVI,
                                DataClassKey, byte[].class))) {
                    throw new UnsupportedOperationException("Track " + tr + " codec " + tr.codec + " does not support format. " + fmt);
                }
            }
        }
    }

    /**
     * Gets the dimension of a track.
     */
    public Dimension getVideoDimension(int track) {
        VideoTrack vt = tracks.get(track);
        Format fmt = vt.format;
        return new Dimension(fmt.get(WidthKey), fmt.get(HeightKey));
    }

    /**
     * Sets the compression quality of a track. <p> A value of 0 stands for
     * "high compression is important" a value of 1 for "high image quality is
     * important". <p> Changing this value affects the encoding of video frames
     * which are subsequently written into the track. Frames which have already
     * been written are not changed. <p> This value has no effect on videos
     * encoded with lossless encoders such as the PNG format. <p> The default
     * value is 0.97.
     *
     * @param newValue the new video quality
     */
    public void setCompressionQuality(int track, float newValue) {
        VideoTrack vt = tracks.get(track);
        vt.videoQuality = newValue;
    }

    /**
     * Returns the compression quality of a track.
     *
     * @return compression quality
     */
    public float getCompressionQuality(int track) {
        return tracks.get(track).videoQuality;
    }

    /**
     * Sets the state of the QuickTimeOutputStream to started. <p> If the state
     * is changed by this method, the prolog is written.
     */
    protected void ensureStarted() throws IOException {
        if (state != States.STARTED) {
            writeProlog();
            state = States.STARTED;
        }
    }

    /**
     * Sets the state of the QuickTimeOutpuStream to finished. <p> If the state
     * is changed by this method, the prolog is written.
     */
    protected void ensureFinished() {
        if (state != States.FINISHED) {
            throw new IllegalStateException("Writer is in illegal state for this operation.");
        }
    }

    /**
     * Writes an already encoded palette change into the specified track. <p> If
     * a track contains palette changes, then all key frames must be immediately
     * preceeded by a palette change chunk which also is a key frame. If a key
     * frame is not preceeded by a key frame palette change chunk, it will be
     * downgraded to a delta frame.
     *
     * @throws IllegalArgumentException if the track is not a video track.
     */
    public void writePalette(int track, byte[] data, int off, int len, boolean isKeyframe) throws IOException {
        VideoTrack vt = tracks.get(track);
        if (!isKeyframe && vt.samples.isEmpty()) {
            throw new IllegalStateException("The first sample in a track must be a keyframe.");
        }

        vt.flags |= STRH_FLAG_VIDEO_PALETTE_CHANGES;

        DataChunk paletteChangeChunk = new DataChunk(vt.twoCC | PC_ID);
        long offset = getRelativeStreamPosition();
        ImageOutputStream pOut = paletteChangeChunk.getOutputStream();
        pOut.write(data, off, len);
        moviChunk.add(paletteChangeChunk);
        paletteChangeChunk.finish();
        long length = getRelativeStreamPosition() - offset;
        Sample s = new Sample(paletteChangeChunk.chunkType, 0, offset, length, isKeyframe);
        vt.addSample(s);
        idx1.add(s);
        //tr.length+=0;  Length is not affected by this chunk!
        offset = getRelativeStreamPosition();
    }

    /**
     * Writes an already encoded sample from a byte array into a track. <p> This
     * method does not inspect the contents of the samples. The contents has to
     * match the format and dimensions of the media in this track. <p> If a
     * track contains palette changes, then all key frames must be immediately
     * preceeded by a palette change chunk. If a key frame is not preceeded by a
     * palette change chunk, it will be downgraded to a delta frame.
     *
     * @param track The track index.
     * @param data The encoded sample data.
     * @param off The startTime offset in the data.
     * @param len The number of bytes to write.
     * @param isKeyframe Whether the sample is a sync sample (keyframe).
     *
     * @throws IllegalArgumentException if the duration is less than 1.
     * @throws IOException if writing the sample data failed.
     */
    public void writeSample(int track, byte[] data, int off, int len, boolean isKeyframe) throws IOException {
        ensureStarted();
        VideoTrack tr = tracks.get(track);

        // The first sample in a track is always a key frame
        if (!isKeyframe && tr.samples.isEmpty()) {
            throw new IllegalStateException("The first sample in a track must be a keyframe.\nTrack="+track+", "+tr.format);
        }

        // If a stream has palette changes, then only palette change samples can
        // be marked as keyframe.
        if (isKeyframe && 0 != (tr.flags & STRH_FLAG_VIDEO_PALETTE_CHANGES)) {
            throw new IllegalStateException("Only palette changes can be marked as keyframe.\nTrack="+track+", "+tr.format);
        }

        DataChunk dc = new DataChunk(tr.getSampleChunkFourCC(), len);
        moviChunk.add(dc);
        ImageOutputStream mdatOut = dc.getOutputStream();
        long offset = getRelativeStreamPosition();
        mdatOut.write(data, off, len);
        long length = getRelativeStreamPosition() - offset;
        dc.finish();
        Sample s = new Sample(dc.chunkType, 1, offset, length, isKeyframe);
        tr.addSample(s);
        idx1.add(s);
        if (getRelativeStreamPosition() > 1L << 32) {
            throw new IOException("AVI file is larger than 4 GB");
        }
    }

    /**
     * Indicates whether the track is closed or finished.
     */
    public boolean isClosed() {
        return state == States.CLOSED;
    }

    /**
     * Closes the stream.
     *
     * @exception IOException if an I/O error has occurred
     */
    public void close() throws IOException {
        if (state == States.STARTED) {
            finish();
        }
        if (state != States.CLOSED) {
            out.close();
            state = States.CLOSED;
        }
    }

    /**
     * Finishes writing the contents of the AVI output stream without closing
     * the underlying stream. Use this method when applying multiple filters in
     * succession to the same output stream.
     *
     * @exception IllegalStateException if the dimension of the video track has
     * not been specified or determined yet.
     * @exception IOException if an I/O exception has occurred
     */
    public void finish() throws IOException {
        ensureOpen();
        if (state != States.FINISHED) {
            moviChunk.finish();
            writeEpilog();
            state = States.FINISHED;
        }
    }

    /**
     * Check to make sure that this stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (state == States.CLOSED) {
            throw new IOException("Stream closed");
        }
    }

    private void writeProlog() throws IOException {
        // The file has the following structure:
        //
        // .RIFF AVI
        // ..avih (AVI Header Chunk)
        // ..LIST strl (for each track)
        // ...strh (Stream Header Chunk)
        // ...strf (Stream Format Chunk)
        // ...**** (Extra Stream Header Chunks)
        // ...strn (Stream Name Chunk)
        // ..LIST movi
        // ...00dc (Compressed video data chunk in Track 00, repeated for each frame)
        // ..idx1 (List of video data chunks and their location in the file)

        // The RIFF AVI Chunk holds the complete movie
        aviChunk = new CompositeChunk(RIFF_ID, AVI_ID);
        CompositeChunk hdrlChunk = new CompositeChunk(LIST_ID, HDRL_ID);

        // Write empty AVI Main Header Chunk - we fill the data in later
        aviChunk.add(hdrlChunk);
        avihChunk = new FixedSizeDataChunk(AVIH_ID, 56);
        avihChunk.seekToEndOfChunk();
        hdrlChunk.add(avihChunk);

        // Write empty AVI Stream Header Chunk - we fill the data in later
        for (VideoTrack tr : tracks) {

            CompositeChunk strlChunk = new CompositeChunk(LIST_ID, STRL_ID);
            hdrlChunk.add(strlChunk);

            tr.strhChunk = new FixedSizeDataChunk(STRH_ID, 56);
            tr.strhChunk.seekToEndOfChunk();
            strlChunk.add(tr.strhChunk);

            tr.strfChunk = new FixedSizeDataChunk(STRF_ID, tr.getSTRFChunkSize());
            tr.strfChunk.seekToEndOfChunk();
            strlChunk.add(tr.strfChunk);

            if (tr.name != null) {
                byte[] data = (tr.name + "\u0000").getBytes("ASCII");
                DataChunk d = new DataChunk(STRN_ID,
                        data.length);
                ImageOutputStream dout = d.getOutputStream();
                dout.write(data);
                d.finish();
                strlChunk.add(d);
            }
        }

        moviChunk = new CompositeChunk(LIST_ID, MOVI_ID);
        aviChunk.add(moviChunk);
    }

    private void writeEpilog() throws IOException {

        ImageOutputStream d;

        /* Create Idx1 Chunk and write data
         * -------------
         typedef struct _avioldindex {
         FOURCC  fcc;
         DWORD   cb;
         struct _avioldindex_entry {
         DWORD   dwChunkId;
         DWORD   flags;
         DWORD   dwOffset;
         DWORD   dwSize;
         } aIndex[];
         } AVIOLDINDEX;
         */
        {
            DataChunk idx1Chunk = new DataChunk(IDX1_ID);
            aviChunk.add(idx1Chunk);
            d = idx1Chunk.getOutputStream();
            long moviListOffset = moviChunk.offset + 8 + 8;

            {
                for (Sample s : idx1) {
                    d.setByteOrder(ByteOrder.BIG_ENDIAN);
                    d.writeInt(s.chunkType); // dwChunkId
                    d.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                    // Specifies a FOURCC that identifies a stream in the AVI file. The
                    // FOURCC must have the form 'xxyy' where xx is the stream number and yy
                    // is a two-character code that identifies the contents of the stream:
                    //
                    // Two-character code   Description
                    //  db                  Uncompressed video frame
                    //  dc                  Compressed video frame
                    //  header                  Palette change
                    //  wb                  Audio data

                    d.writeInt(((s.chunkType & 0xffff) == PC_ID ? 0x100 : 0x0)//
                            | (s.isKeyframe ? 0x10 : 0x0)); // flags
                    // Specifies a bitwise combination of zero or more of the following
                    // flags:
                    //
                    // Value    Name            Description
                    // 0x10     AVIIF_KEYFRAME  The data chunk is a key frame.
                    // 0x1      AVIIF_LIST      The data chunk is a 'rec ' list.
                    // 0x100    AVIIF_NO_TIME   The data chunk does not affect the timing of the
                    //                          stream. For example, this flag should be set for
                    //                          palette changes.

                    d.writeInt((int) (s.offset - moviListOffset)); // dwOffset
                    // Specifies the location of the data chunk in the file. The value
                    // should be specified as an offset, in bytes, from the startTime of the
                    // 'movi' list; however, in some AVI files it is given as an offset from
                    // the startTime of the file.

                    d.writeInt((int) (s.length)); // dwSize
                    // Specifies the size of the data chunk, in bytes.
                }

            }

            idx1Chunk.finish();
        }

        /* Write Data into AVI Main Header Chunk
         * -------------
         * The AVIMAINHEADER structure defines global information in an AVI file.
         * see http://msdn.microsoft.com/en-us/library/ms779632(VS.85).aspx
         typedef struct _avimainheader {
         FOURCC fcc;
         DWORD  cb;
         DWORD  dwMicroSecPerFrame;
         DWORD  dwMaxBytesPerSec;
         DWORD  dwPaddingGranularity;
         DWORD  flags;
         DWORD  dwTotalFrames;
         DWORD  initialFrames;
         DWORD  dwStreams;
         DWORD  dwSuggestedBufferSize;
         DWORD  dwWidth;
         DWORD  dwHeight;
         DWORD  dwReserved[4];
         } AVIMAINHEADER; */
        {
            avihChunk.seekToStartOfData();
            d = avihChunk.getOutputStream();

            // compute largest buffer size
            long largestBufferSize = 0;
            long duration = 0;
            for (VideoTrack tr : tracks) {
                long trackDuration = 0;
                for (Sample s : tr.samples) {
                    trackDuration += s.duration;
                }
                duration = Math.max(duration, trackDuration);
                for (Sample s : tr.samples) {
                    if (s.length > largestBufferSize) {
                        largestBufferSize = s.length;
                    }
                }
            }

            VideoTrack tt = tracks.get(0);

            d.writeInt((int) ((1000000L * tt.scale) / tt.rate)); // dwMicroSecPerFrame
            // Specifies the number of microseconds between frames.
            // This value indicates the overall timing for the file.

            d.writeInt((int)largestBufferSize); // dwMaxBytesPerSec
            // Specifies the approximate maximum data rate of the file.
            // This value indicates the number of bytes per second the system
            // must handle to present an AVI sequence as specified by the other
            // parameters contained in the main header and stream header chunks.

            d.writeInt(0); // dwPaddingGranularity
            // Specifies the alignment for data, in bytes. Pad the data to multiples
            // of this value.

            d.writeInt(0x10|0x100|0x800); // flags 
            // Contains a bitwise combination of zero or more of the following
            // flags:
            //
            // Value   Name         Description
            // 0x10    AVIF_HASINDEX Indicates the AVI file has an index.
            // 0x20    AVIF_MUSTUSEINDEX Indicates that application should use the
            //                      index, rather than the physical ordering of the
            //                      chunks in the file, to determine the order of
            //                      presentation of the data. For example, this flag
            //                      could be used to create a list of frames for
            //                      editing.
            // 0x100   AVIF_ISINTERLEAVED Indicates the AVI file is interleaved.
            // 0x800   AVIF_TRUST_CK_TYPE ???  
            // 0x1000  AVIF_WASCAPTUREFILE Indicates the AVI file is a specially
            //                      allocated file used for capturing real-time
            //                      video. Applications should warn the user before
            //                      writing over a file with this flag set because
            //                      the user probably defragmented this file.
            // 0x20000 AVIF_COPYRIGHTED Indicates the AVI file contains copyrighted
            //                      data and software. When this flag is used,
            //                      software should not permit the data to be
            //                      duplicated.

            /*long dwTotalFrames = 0;
             for (Track t : tracks) {
             dwTotalFrames += t.samples.size();
             }*/
            d.writeInt(tt.samples.size()); // dwTotalFrames
            // Specifies the total number of frames of data in the file.

            d.writeInt(0); // initialFrames
            // Specifies the initial frame for interleaved files. Noninterleaved
            // files should specify zero. If you are creating interleaved files,
            // specify the number of frames in the file prior to the initial frame
            // of the AVI sequence in this member.
            // To give the audio driver enough audio to work with, the audio data in
            // an interleaved file must be skewed from the video data. Typically,
            // the audio data should be moved forward enough frames to allow
            // approximately 0.75 seconds of audio data to be preloaded. The
            // dwInitialRecords member should be set to the number of frames the
            // audio is skewed. Also set the same value for the initialFrames
            // member of the AVISTREAMHEADER structure in the audio stream header

            d.writeInt(tracks.size()); // dwStreams
            // Specifies the number of streams in the file. For example, a file with
            // audio and video has two streams.

            d.writeInt((int) largestBufferSize); // dwSuggestedBufferSize
            // Specifies the suggested buffer size for reading the file. Generally,
            // this size should be large enough to contain the largest chunk in the
            // file. If set to zero, or if it is too small, the playback software
            // will have to reallocate memory during playback, which will reduce
            // performance. For an interleaved file, the buffer size should be large
            // enough to read an entire record, and not just a chunk.
            {
                int width = 0, height = 0;
                for (VideoTrack tr : tracks) {
                    width = Math.max(width, Math.max(tr.frameLeft, tr.frameRight));
                    height = Math.max(height, Math.max(tr.frameTop, tr.frameBottom));
                }
                d.writeInt(width); // dwWidth
                // Specifies the width of the AVI file in pixels.

                d.writeInt(height); // dwHeight
                // Specifies the height of the AVI file in pixels.
            }
            d.writeInt(0); // dwReserved[0]
            d.writeInt(0); // dwReserved[1]
            d.writeInt(0); // dwReserved[2]
            d.writeInt(0); // dwReserved[3]
            // Reserved. Set this array to zero.
        }

        for (VideoTrack vt : tracks) {
            /* Write Data into AVI Stream Header Chunk
             * -------------
             * The AVISTREAMHEADER structure contains information about one stream
             * in an AVI file.
             * see http://msdn.microsoft.com/en-us/library/ms779638(VS.85).aspx
             typedef struct _avistreamheader {
             FOURCC fcc;
             DWORD  cb;
             FOURCC fccType;
             FOURCC fccHandler;
             DWORD  flags;
             WORD   priority;
             WORD   language;
             DWORD  initialFrames;
             DWORD  scale;
             DWORD  rate;
             DWORD  startTime;
             DWORD  dwLength;
             DWORD  dwSuggestedBufferSize;
             DWORD  quality;
             DWORD  dwSampleSize;
             struct {
             short int left;
             short int top;
             short int right;
             short int bottom;
             }  rcFrame;
             } AVISTREAMHEADER;
             */
            vt.strhChunk.seekToStartOfData();
            d = vt.strhChunk.getOutputStream();
            d.setByteOrder(ByteOrder.BIG_ENDIAN);
            d.writeInt(typeToInt(VideoTrack.FOURCC));
            d.writeInt(vt.fccHandler); // fccHandler: specifies the codec
            d.setByteOrder(ByteOrder.LITTLE_ENDIAN);

            d.writeInt(vt.flags);
            // Contains any flags for the data stream. The bits in the high-order
            // word of these flags are specific to the type of data contained in the
            // stream. The following standard flags are defined:
            //
            // Value    Name        Description
            //          AVISF_DISABLED 0x00000001 Indicates this stream should not
            //                      be enabled by default.
            //          AVISF_VIDEO_PALCHANGES 0x00010000
            //                      Indicates this video stream contains
            //                      palette changes. This flag warns the playback
            //                      software that it will need to animate the
            //                      palette.

            d.writeShort(vt.priority); // priority: highest priority denotes default stream
            d.writeShort(vt.language); // language: language code (?)
            d.writeInt((int) vt.initialFrames); // initialFrames: how far audio data is ahead of the video frames
            d.writeInt((int) vt.scale); // scale: time scale
            d.writeInt((int) vt.rate); // rate: sample rate in scale units
            d.writeInt((int) vt.startTime); // startTime: starting time of stream
            d.writeInt((int) vt.length); // dwLength: length of stream ! WRONG

            long dwSuggestedBufferSize = 0;
            long dwSampleSize = -1; // => -1 indicates unknown
            for (Sample s : vt.samples) {
                if (s.length > dwSuggestedBufferSize) {
                    dwSuggestedBufferSize = s.length;
                }
                if (dwSampleSize == -1) {
                    dwSampleSize = s.length;
                } else if (dwSampleSize != s.length) {
                    dwSampleSize = 0;
                }
            }
            if (dwSampleSize == -1) {
                dwSampleSize = 0;
            }

            d.writeInt((int) dwSuggestedBufferSize); // dwSuggestedBufferSize
            // Specifies how large a buffer should be used to read this stream.
            // Typically, this contains a value corresponding to the largest chunk
            // present in the stream. Using the correct buffer size makes playback
            // more efficient. Use zero if you do not know the correct buffer size.

            d.writeInt(vt.quality); // quality
            // Specifies an indicator of the quality of the data in the stream.
            // Quality is represented as a number between 0 and 10,000.
            // For compressed data, this typically represents the value of the
            // quality parameter passed to the compression software. If set to –1,
            // drivers use the default quality value.

            d.writeInt((int) dwSampleSize); // dwSampleSize
            // Specifies the size of a single sample of data. This is set to zero
            // if the samples can vary in size. If this number is nonzero, then
            // multiple samples of data can be grouped into a single chunk within
            // the file. If it is zero, each sample of data (such as a video frame)
            // must be in a separate chunk. For video streams, this number is
            // typically zero, although it can be nonzero if all video frames are
            // the same size. For audio streams, this number should be the same as
            // the blockAlign member of the WAVEFORMATEX structure describing the
            // audio.

            d.writeShort(vt.frameLeft); // rcFrame.left
            d.writeShort(vt.frameTop); // rcFrame.top
            d.writeShort(vt.frameRight); // rcFrame.right
            d.writeShort(vt.frameBottom); // rcFrame.bottom
            // Specifies the destination rectangle for a text or video stream within
            // the movie rectangle specified by the dwWidth and dwHeight members of
            // the AVI main header structure. The rcFrame member is typically used
            // in support of multiple video streams. Set this rectangle to the
            // coordinates corresponding to the movie rectangle to update the whole
            // movie rectangle. Units for this member are pixels. The upper-left
            // corner of the destination rectangle is relative to the upper-left
            // corner of the movie rectangle.

            Format vf = vt.format;

            /* Write BITMAPINFOHEADR Data into AVI Stream Format Chunk
             /* -------------
             * see http://msdn.microsoft.com/en-us/library/ms779712(VS.85).aspx
             typedef struct tagBITMAPINFOHEADER {
             DWORD  biSize;
             LONG   width;
             LONG   height;
             WORD   planes;
             WORD   bitCount;
             DWORD  compression;
             DWORD  sizeImage;
             LONG   xPelsPerMeter;
             LONG   yPelsPerMeter;
             DWORD  clrUsed;
             DWORD  clrImportant;
             } BITMAPINFOHEADER;
             */
            vt.strfChunk.seekToStartOfData();
            d = vt.strfChunk.getOutputStream();
            d.writeInt(40); // biSize: number of bytes required by the structure.
            d.writeInt(vf.get(WidthKey)); // width
            d.writeInt(vf.get(HeightKey)); // height
            d.writeShort(1); // planes
            d.writeShort(vf.get(DepthKey)); // bitCount

            String enc = vf.get(EncodingKey);
            if (enc.equals(ENCODING_AVI_DIB)) {
                d.writeInt(0); // compression - BI_RGB for uncompressed RGB
            } else if (enc.equals(ENCODING_AVI_RLE)) {
                if (vf.get(DepthKey) == 8) {
                    d.writeInt(1); // compression - BI_RLE8
                } else if (vf.get(DepthKey) == 4) {
                    d.writeInt(2); // compression - BI_RLE4
                } else {
                    throw new UnsupportedOperationException("RLE only supports 4-bit and 8-bit images");
                }
            } else {
                d.setByteOrder(ByteOrder.BIG_ENDIAN);
                d.writeInt(typeToInt(vt.format.get(EncodingKey))); // compression
                d.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            }

            if (enc.equals(ENCODING_AVI_DIB)) {
                d.writeInt(0); // sizeImage
            } else {
                if (vf.get(DepthKey) == 4) {
                    d.writeInt(vf.get(WidthKey) * vf.get(HeightKey) / 2); // sizeImage
                } else {
                    int bytesPerPixel = Math.max(1, vf.get(DepthKey) / 8);
                    d.writeInt(vf.get(WidthKey) * vf.get(HeightKey) * bytesPerPixel); // sizeImage
                }
            }

            d.writeInt(0); // xPelsPerMeter
            d.writeInt(0); // yPelsPerMeter

            d.writeInt(vt.palette == null ? 0 : vt.palette.getMapSize()); // clrUsed

            d.writeInt(0); // clrImportant

            if (vt.palette != null) {
                for (int i = 0, n = vt.palette.getMapSize(); i < n; ++i) {
                    /*
                     * typedef struct tagRGBQUAD {
                     BYTE rgbBlue;
                     BYTE rgbGreen;
                     BYTE rgbRed;
                     BYTE rgbReserved; // This member is reserved and must be zero.
                     } RGBQUAD;
                     */
                    d.write(vt.palette.getBlue(i));
                    d.write(vt.palette.getGreen(i));
                    d.write(vt.palette.getRed(i));
                    d.write(0);
                }
            }

        }

        // -----------------
        aviChunk.finish();
    }

    /**
     * The list of tracks in the file.
     */
    protected ArrayList<VideoTrack> tracks = new ArrayList<>();

    /**
     * AVI stores media data in sample chunks. A sample chunk may contain one or
     * more media samples. A media sample is a single element in a sequence of
     * time-ordered data.
     */
    protected static class Sample {

        int chunkType;

        /**
         * Offset of the sample chunk relative to the startTime of the AVI file.
         */
        long offset;

        /**
         * Data length of the sample chunk.
         */
        long length;

        /**
         * The number of media samples in the sample chunk.
         */
        int duration;

        /**
         * Whether the sample is a sync-sample.
         */
        boolean isKeyframe;

        long timeStamp;

        /**
         * Creates a new sample.
         *
         * @param duration The number of media samples contained in the sample
         * chunk.
         * @param offset The offset in the AVI stream.
         * @param length The length in the AVI stream.
         */
        public Sample(int chunkId, int duration, long offset, long length, boolean isSync) {
            this.chunkType = chunkId;
            this.duration = duration;
            this.offset = offset;
            this.length = length;
            this.isKeyframe = isSync;
        }
    }

    /**
     * Represents a video track in an AVI file. <p> The format of a video track
     * is defined in a "strf" chunk, which contains a {@code BITMAPINFOHEADER}
     * struct.
     *
     * </pre> //---------------------- // AVI Bitmap Info Header //
     * ---------------------- typedef struct { BYTE blue; BYTE green; BYTE red;
     * BYTE reserved; } RGBQUAD;
     *
     * // Values for this enum taken from: //
     * http://www.fourcc.org/index.php?http%3A//www.fourcc.org/rgb.php enum {
     * BI_RGB = 0x00000000, RGB = 0x32424752, // Alias for BI_RGB BI_RLE8 =
     * 0x01000000, RLE8 = 0x38454C52, // Alias for BI_RLE8 BI_RLE4 = 0x00000002,
     * RLE4 = 0x34454C52, // Alias for BI_RLE4 BI_BITFIELDS = 0x00000003, raw =
     * 0x32776173, RGBA = 0x41424752, RGBT = 0x54424752, cvid = "cvid" }
     * bitmapCompression;
     *
     * typedef struct { DWORD structSize; // Specifies the number of bytes
     * required by the structure. LONG width; // Specifies the width of the
     * bitmap. // - For RGB formats, the width is specified in pixels. // - The
     * same is true for YUV formats if the bitdepth is an even power // of 2. //
     * - For YUV formats where the bitdepth is not an even power of 2, //
     * however, the width is specified in bytes. // Decoders and video sources
     * should propose formats where "width" is // the width of the image. If the
     * video renderer is using DirectDraw, it // modifies the format so that
     * "width" equals the stride of the surface, // and the "target" member of
     * the VIDEOINFOHEADER or VIDEOINFOHEADER2 // structure specifies the image
     * width. Then it proposes the modified // format using IPin::QueryAccept.
     * // For RGB and even-power-of-2 YUV formats, if the video renderer does //
     * not specify the stride, then round the width up to the nearst DWORD //
     * boundary to find the stride. LONG height; // Specifies the height of the
     * bitmap, in pixels. // - For uncompressed RGB bitmaps, if "height" is
     * positive, the bitmap // is a bottom-up DIB with the origin at the lower
     * left corner. If // "height" is negative, the bitmap is a top-down DIB
     * with the origin // at the upper left corner. // - For YUV bitmaps, the
     * bitmap is always top-down, regardless of the // sign of "height".
     * Decoders should offer YUV formats with postive // "height", but for
     * backward compatibility they should accept YUV // formats with either
     * positive or negative "height". // - For compressed formats, height must
     * be positive, regardless of // image orientation. WORD planes; //
     * Specifies the number of planes for the target device. This value must //
     * be set to 1. WORD bitCount; // Specifies the number of bits per pixel.
     * //DWORD enum bitmapCompression compression; FOURCC enum bitmapCompression
     * compression; // If the bitmap is compressed, this member is a FOURCC the
     * specifies // the compression. // Value Description // BI_RLE8 A
     * run-length encoded (RLE) format for bitmaps with 8 // bpp. The
     * compression format is a 2-byte format // consisting of a count byte
     * followed by a byte containing a color index. For more information, see
     * Bitmap Compression. // BI_RLE4 An RLE format for bitmaps with 4 bpp. The
     * compression // format is a 2-byte format consisting of a count byte //
     * followed by two word-length color indexes. For more // information, see
     * Bitmap Compression. // BI_JPEG Windows 98/Me, Windows 2000/XP: Indicates
     * that the // image is a JPEG image. // BI_PNG Windows 98/Me, Windows
     * 2000/XP: Indicates that the // image is a PNG image. // For uncompressed
     * formats, the following values are possible: // Value Description //
     * BI_RGB Uncompressed RGB. // BI_BITFIELDS Specifies that the bitmap is not
     * compressed and that // the color table consists of three DWORD color
     * masks // that specify the red, green, and blue components, //
     * respectively, of each pixel. This is valid when used // with 16- and
     * 32-bpp bitmaps. DWORD imageSizeInBytes; // Specifies the size, in bytes,
     * of the image. This can be set to 0 for // uncompressed RGB bitmaps. LONG
     * xPelsPerMeter; // Specifies the horizontal resolution, in pixels per
     * meter, of the // target device for the bitmap. LONG yPelsPerMeter; //
     * Specifies the vertical resolution, in pixels per meter, of the target //
     * device for the bitmap. DWORD numberOfColorsUsed; // Specifies the number
     * of color indices in the color table that are // actually used by the
     * bitmap DWORD numberOfColorsImportant; // Specifies the number of color
     * indices that are considered important // for displaying the bitmap. If
     * this value is zero, all colors are // important. RGBQUAD colors[]; // If
     * the bitmap is 8-bpp or less, the bitmap uses a color table, which //
     * immediately follows the BITMAPINFOHEADER. The color table consists of //
     * an array of RGBQUAD values. The size of the array is given by the //
     * "clrUsed" member. If "clrUsed" is zero, the array contains the // maximum
     * number of colors for the given bitdepth; that is, // 2^"bitCount" colors.
     * } BITMAPINFOHEADER;
     * </pre>
     */
    protected class VideoTrack {

        /**
         * The media format.
         */
        protected Format format;

        /**
         * List of samples.
         */
        protected ArrayList<Sample> samples;

        /**
         * Interval between sync samples (keyframes). 0 = automatic. 1 = write
         * all samples as sync samples. n = sync every n-th sample.
         */
        protected int syncInterval = 30;

        /**
         * The twoCC code is used for the ids of the chunks which hold the data
         * samples.
         */
        protected int twoCC;

        /**
         * {@code FOURCC} specifies the type of the data contained in the stream.
         * The following standard AVI values for video and audio are defined.
         *
         * FOURCC Description
         * 'auds'	Audio stream
         * 'mids'	MIDI stream
         * 'txts'	Text stream
         * 'vids'	Video stream
         */
        protected static final String FOURCC = "vids";

        /**
         * Optionally, contains a FOURCC that identifies a specific data
         * handler. The data handler is the preferred handler for the stream.
         * For audio and video streams, this specifies the codec for decoding
         * the stream.
         */
        protected int fccHandler;

        /**
         * Contains any flags for the data stream. The bits in the high-order
         * word of these flags are specific to the type of data contained in the
         * stream. The following standard flags are defined.
         *
         * Value	Description
         *
         * AVISF_DISABLED	0x00000001 Indicates this stream should not be enabled
         * by default.
         *
         * AVISF_VIDEO_PALCHANGES 0x00010000 Indicates this video stream
         * contains palette changes. This flag warns the playback software that
         * it will need to animate the palette.
         */
        protected int flags;

        /**
         * Specifies priority of a stream type. For example, in a file with
         * multiple audio streams, the one with the highest priority might be
         * the default stream.
         */
        protected int priority = 0;

        /**
         * Language tag.
         */
        protected int language = 0;

        /**
         * Specifies how far audio data is skewed ahead of the video frames in
         * interleaved files. Typically, this is about 0.75 seconds. If you are
         * creating interleaved files, specify the number of frames in the file
         * prior to the initial frame of the AVI sequence in this member. For
         * more information, see the remarks for the initialFrames member of the
         * AVIMAINHEADER structure.
         */
        protected long initialFrames = 0;

        /**
         * Used with rate to specify the time scale that this stream will use.
         * Dividing rate by scale gives the number of samples per second. For
         * video streams, this is the frame rate. For audio streams, this rate
         * corresponds to the time needed to play blockAlign bytes of audio,
         * which for PCM audio is the just the sample rate.
         */
        protected long scale = 1;

        /**
         * The rate of the media in scale units.
         */
        protected long rate = 30;

        /**
         * Specifies the starting time for this stream. The units are defined by
         * the rate and scale members in the main file header. Usually, this is
         * zero, but it can specify a delay time for a stream that does not
         * startTime concurrently with the file.
         */
        protected long startTime = 0;

        /**
         * Specifies the length of this stream. The units are defined by the
         * rate and scale members of the stream's header.
         */
        protected long length;

        /**
         * Specifies an indicator of the quality of the data in the stream.
         * Quality is represented as a number between 0 and 10,000. For
         * compressed data, this typically represents the value of the quality
         * parameter passed to the compression software. If set to –1, drivers
         * use the default quality value.
         */
        protected int quality = -1;

        /**
         * Specifies the destination rectangle for a text or video stream within
         * the movie rectangle specified by the dwWidth and dwHeight members of
         * the AVI main header structure. The rcFrame member is typically used
         * in support of multiple video streams. Set this rectangle to the
         * coordinates corresponding to the movie rectangle to update the whole
         * movie rectangle. Units for this member are pixels. The upper-left
         * corner of the destination rectangle is relative to the upper-left
         * corner of the movie rectangle.
         */
        int frameLeft;
        int frameTop;
        int frameRight;
        int frameBottom;

        /**
         * This chunk holds the AVI Stream Header.
         */
        protected FixedSizeDataChunk strhChunk;

        /**
         * This chunk holds the AVI Stream Format Header.
         */
        protected FixedSizeDataChunk strfChunk;

        /**
         * The optional name of the track.
         */
        protected String name;

        /**
         * The codec.
         */
        protected Codec codec;

        /**
         * The output buffer is used to store the output of the codec.
         */
        protected Buffer outputBuffer;

        /**
         * The video compression quality.
         */
        protected float videoQuality = 0.97f;

        /**
         * Index color model for RAW_RGB4 and RAW_RGB8 formats.
         */
        protected IndexColorModel palette;

        protected IndexColorModel previousPalette;

        /**
         * Specifies the number of planes for the target device. This value must
         * be set to 1.
         */
        int planes;

        /**
         * Specifies the number of bits per pixel (bpp). For uncompressed
         * formats, this value is the average number of bits per pixel. For
         * compressed formats, this value is the implied bit depth of the
         * uncompressed image, after the image has been decoded.
         */
        int bitCount;

        private int sampleChunkFourCC;

        public VideoTrack(int trackIndex, int fourCC, Format videoFormat) {
            twoCC = (('0'+trackIndex/10)<<24) | (('0'+trackIndex%10)<<16);

            this.fccHandler = fourCC;
            this.samples = new ArrayList<>();
            // this.extraHeaders = new ArrayList<>();
            this.format = videoFormat;
            sampleChunkFourCC = videoFormat != null && videoFormat.get(EncodingKey).equals(ENCODING_AVI_DIB) ? twoCC | DB_ID : twoCC | DC_ID;
        }

        public long getSTRFChunkSize() {
            return palette == null ? 40 : 40 + palette.getMapSize() * 4;

        }

        public int getSampleChunkFourCC() {
            return sampleChunkFourCC;
        }

        public void addSample(Sample s) {
            if (!samples.isEmpty()) {
                s.timeStamp = samples.get(samples.size() - 1).timeStamp + samples.get(samples.size() - 1).duration;
            }
            samples.add(s);
            length++;
        }
    }

    /**
     * Chunk base class.
     */
    protected abstract class Chunk {

        /**
         * The chunkType of the chunk. A String with the length of 4 characters.
         */
        protected int chunkType;

        /**
         * The offset of the chunk relative to the startTime of the
         * ImageOutputStream.
         */
        protected long offset;

        /**
         * Creates a new Chunk at the current position of the ImageOutputStream.
         *
         * @param chunkType The chunkType of the chunk. A string with a length
         * of 4 characters.
         */
        public Chunk(int chunkType) throws IOException {
            this.chunkType = chunkType;
            offset = getRelativeStreamPosition();
        }

        /**
         * Writes the chunk to the ImageOutputStream and disposes it.
         */
        public abstract void finish() throws IOException;

        /**
         * Returns the size of the chunk including the size of the chunk header.
         *
         * @return The size of the chunk.
         */
        public abstract long size();
    }

    /**
     * A CompositeChunk contains an ordered list of Chunks.
     */
    protected class CompositeChunk extends Chunk {

        /**
         * The type of the composite. A String with the length of 4 characters.
         */
        protected int compositeType;
        protected LinkedList<Chunk> children;
        protected boolean finished;

        /**
         * Creates a new CompositeChunk at the current position of the
         * ImageOutputStream.
         *
         * @param compositeType The type of the composite.
         * @param chunkType The type of the chunk.
         */
        public CompositeChunk(int compositeType, int chunkType) throws IOException {
            super(chunkType);
            this.compositeType = compositeType;
            out.writeLong(0); // make room for the chunk header
            out.writeInt(0); // make room for the chunk header
            children = new LinkedList<>();
        }

        public void add(Chunk child) throws IOException {
            if (children.size() > 0) {
                children.getLast().finish();
            }
            children.add(child);
        }

        /**
         * Writes the chunk and all its children to the ImageOutputStream and
         * disposes of all resources held by the chunk.
         *
         * @throws java.io.IOException when CompositeChunk is too large.
         */
        @Override
        public void finish() throws IOException {
            if (!finished) {
                if (size() > 0xffffffffL) {
                    throw new IOException("CompositeChunk \"" + chunkType + "\" is too large: " + size());
                }

                long pointer = getRelativeStreamPosition();
                seekRelative(offset);

                out.setByteOrder(ByteOrder.BIG_ENDIAN);
                out.writeInt(compositeType);
                out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                out.writeInt((int) (size() - 8));
                out.setByteOrder(ByteOrder.BIG_ENDIAN);
                out.writeInt(chunkType);
                out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                for (Chunk child : children) {
                    child.finish();
                }
                seekRelative(pointer);
                if (size() % 2 == 1) {
                    out.writeByte(0); // write pad byte
                }
                finished = true;
            }
        }

        @Override
        public long size() {
            long length = 12;
            for (Chunk child : children) {
                length += child.size() + child.size() % 2;
            }
            return length;
        }
    }

    /**
     * Data Chunk.
     */
    protected class DataChunk extends Chunk {

        protected boolean finished;
        private long finishedSize;

        /**
         * Creates a new DataChunk at the current position of the
         * ImageOutputStream.
         *
         * @param name The name of the chunk.
         */
        public DataChunk(int name) throws IOException {
            this(name, -1);
        }

        /**
         * Creates a new DataChunk at the current position of the
         * ImageOutputStream.
         *
         * @param name The name of the chunk.
         * @param dataSize The size of the chunk data, or -1 if not known.
         */
        public DataChunk(int name, long dataSize) throws IOException {
            super(name);
            out.setByteOrder(ByteOrder.BIG_ENDIAN);
            out.writeInt(chunkType);
            out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            out.writeInt((int) Math.max(0, dataSize));
            finishedSize = dataSize == -1 ? -1 : dataSize + 8;
        }

        public ImageOutputStream getOutputStream() {
            if (finished) {
                throw new IllegalStateException("DataChunk is finished");
            }
            return out;
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                if (finishedSize == -1) {
                    finishedSize = size();

                    if (finishedSize > 0xffffffffL) {
                        throw new IOException("DataChunk \"" + chunkType + "\" is too large: " + size());
                    }

                    seekRelative(offset + 4);
                    out.writeInt((int) (finishedSize - 8));
                    seekRelative(offset + finishedSize);
                } else {
                    if (size() != finishedSize) {
                        throw new IOException("DataChunk \"" + chunkType + "\" actual size differs from given size: actual size:" + size() + " given size:" + finishedSize);
                    }
                }
                if (size() % 2 == 1) {
                    out.writeByte(0); // write pad byte
                }

                finished = true;
            }
        }

        @Override
        public long size() {
            if (finished) {
                return finishedSize;
            }

            try {
                return out.getStreamPosition() - offset;
            } catch (IOException ex) {
                InternalError ie = new InternalError("IOException");
                ie.initCause(ex);
                throw ie;
            }
        }
    }

    /**
     * A DataChunk with a fixed size.
     */
    protected class FixedSizeDataChunk extends Chunk {

        protected boolean finished;
        protected long fixedSize;

        /**
         * Creates a new DataChunk at the current position of the
         * ImageOutputStream.
         *
         * @param chunkType The chunkType of the chunk.
         */
        public FixedSizeDataChunk(int chunkType, long fixedSize) throws IOException {
            super(chunkType);
            this.fixedSize = fixedSize;
            out.setByteOrder(ByteOrder.BIG_ENDIAN);
            out.writeInt(chunkType);
            out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            out.writeInt((int) fixedSize);

            // Fill fixed size with nulls
            byte[] buf = new byte[(int) Math.min(512, fixedSize)];
            long written = 0;
            while (written < fixedSize) {
                out.write(buf, 0, (int) Math.min(buf.length, fixedSize - written));
                written += Math.min(buf.length, fixedSize - written);
            }
            if (fixedSize % 2 == 1) {
                out.writeByte(0); // write pad byte
            }
            seekToStartOfData();
        }

        public ImageOutputStream getOutputStream() {
            return out;
        }

        public void seekToStartOfData() throws IOException {
            seekRelative(offset + 8);

        }

        public void seekToEndOfChunk() throws IOException {
            seekRelative(offset + 8 + fixedSize + fixedSize % 2);
        }

        @Override
        public void finish() {
            if (!finished) {
                finished = true;
            }
        }

        @Override
        public long size() {
            return 8 + fixedSize;
        }
    }

    /**
     * Gets the position relative to the beginning of the QuickTime stream. <p>
     * Usually this value is equal to the stream position of the underlying
     * ImageOutputStream, but can be larger if the underlying stream already
     * contained data.
     *
     * @return The relative stream position.
     * @throws IOException for the ImageOutputStream
     */
    protected long getRelativeStreamPosition() throws IOException {
        return out.getStreamPosition() - streamOffset;
    }

    /**
     * Seeks relative to the beginning of the AVI stream. <p> Usually this equal
     * to seeking in the underlying ImageOutputStream, but can be different if
     * the underlying stream already contained data.
     */
    protected void seekRelative(long newPosition) throws IOException {
        out.seek(newPosition + streamOffset);
    }

    protected static int typeToInt(String str) {
        return ((str.charAt(0) & 0xff) << 24) | ((str.charAt(1) & 0xff) << 16) | ((str.charAt(2) & 0xff) << 8) | (str.charAt(3) & 0xff);
    }
}

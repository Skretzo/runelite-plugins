/*
 * @(#)TechSmithCodec.java 
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
import com.videorecorder.video.format.FormatKey;
import com.videorecorder.video.io.SeekableByteArrayOutputStream;
import com.videorecorder.video.nio.Buffer;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import static com.videorecorder.video.format.FormatKey.EncodingKey;
import static com.videorecorder.video.format.FormatKey.FrameRateKey;
import static com.videorecorder.video.format.FormatKey.KeyFrameIntervalKey;
import static com.videorecorder.video.format.FormatKey.MIME_AVI;
import static com.videorecorder.video.format.FormatKey.MIME_QUICKTIME;
import static com.videorecorder.video.format.FormatKey.MediaTypeKey;
import static com.videorecorder.video.format.FormatKey.MimeTypeKey;
import static com.videorecorder.video.format.VideoFormatKeys.COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE;
import static com.videorecorder.video.format.VideoFormatKeys.CompressionLevelKey;
import static com.videorecorder.video.format.VideoFormatKeys.CompressorNameKey;
import static com.videorecorder.video.format.VideoFormatKeys.DataClassKey;
import static com.videorecorder.video.format.VideoFormatKeys.DepthKey;
import static com.videorecorder.video.format.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static com.videorecorder.video.format.VideoFormatKeys.FixedFrameRateKey;
import static com.videorecorder.video.format.VideoFormatKeys.HeightKey;
import static com.videorecorder.video.format.VideoFormatKeys.WidthKey;
import static com.videorecorder.video.nio.BufferFlag.DISCARD;
import static com.videorecorder.video.nio.BufferFlag.KEYFRAME;
import static com.videorecorder.video.nio.BufferFlag.SAME_DATA;

/**
 * {@code TechSmithCodec} (tscc) encodes a BufferedImage as a byte[] array. <p>
 * The TechSmith codec works with AVI and QuickTime. <p> This codec supports
 * encoding from a {@code BufferedImage} into the file format, and decoding from
 * the file format to a {@code BufferedImage}. <p> <p> This codec does not
 * encode the color palette of an image. This must be done separately. <p>
 * Supported input formats: <ul> {@code Format} with
 * {@code BufferedImage.class}, any width, any height, depth=8,16 or 24. </ul>
 * Supported output formats: <ul> {@code Format} with {@code byte[].class}, same
 * width and height as input format, depth=8,16 or 24. </ul> The codec supports
 * lossless delta- and key-frame encoding of images with 8, 16 or 24 bits per
 * pixel. <p> Compression of a frame is performed in two steps: In the first,
 * step a frame is compressed line by line from bottom to top. In the second
 * step the resulting data is compressed again using zlib compression. <p> Apart
 * from the second compression step and the support for 16- and 24-bit data,
 * this encoder is identical to the RunLengthCodec. <p> Each line of a
 * frame is compressed individually. A line consists of two-byte op-codes
 * optionally followed by data. The end of the line is marked with the EOL
 * op-code. <p> The following op-codes are supported: <ul> <li>{@code 0x00 0x00}
 * <br>Marks the end of a line.</li>
 *
 * <li>{@code  0x00 0x01} <br>Marks the end of the bitmap.</li>
 *
 * <li>{@code 0x00 0x02 x y} <br> Marks a delta (skip). {@code x} and {@code y}
 * indicate the horizontal and vertical offset from the current position.
 * {@code x} and {@code y} are unsigned 8-bit values.</li>
 *
 * <li>{@code 0x00 n pixel{n} 0x00?} <br> Marks a literal run. {@code n} gives
 * the number of 8-, 16- or 24-bit pixels that follow. {@code n} must be between
 * 3 and 255. If n is odd and 8-bit pixels are used, a pad byte with the value
 * 0x00 must be added. </li> <li>{@code n pixel} <br> Marks a repetition.
 * {@code n} gives the number of times the given pixel is repeated. {@code n}
 * must be between 1 and 255. </li> </ul> Example:
 * <pre>
 * Compressed data         Expanded data
 *
 * 03 04                   04 04 04
 * 05 06                   06 06 06 06 06
 * 00 03 45 56 67 00       45 56 67
 * 02 78                   78 78
 * 00 02 05 01             Move 5 right and 1 down
 * 02 78                   78 78
 * 00 00                   End of line
 * 09 1E                   1E 1E 1E 1E 1E 1E 1E 1E 1E
 * 00 01                   End of RLE bitmap
 * </pre>
 *
 * References:<br/> <a
 * href="http://wiki.multimedia.cx/index.php?title=TechSmith_Screen_Capture_Codec"
 * >http://wiki.multimedia.cx/index.php?title=TechSmith_Screen_Capture_Codec</a><br>
 *
 *
 * @author Werner Randelshofer
 * @version $Id: TechSmithCodec.java 299 2013-01-03 07:40:18Z werner $
 */
public class TechSmithCodec extends AbstractVideoCodec {

    public static final Integer DEFAULT_COMPRESSION_LEVEL = 6;

    private TechSmithCodecCore state;
    private Object previousPixels;
    private int frameCounter;

    public TechSmithCodec() {
        super(
            new Format[] {
                new Format(MediaTypeKey, FormatKey.MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 8),
                new Format(MediaTypeKey, FormatKey.MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 16),
                new Format(MediaTypeKey, FormatKey.MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 24),
                new Format(MediaTypeKey, FormatKey.MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 8),
                new Format(MediaTypeKey, FormatKey.MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 16),
                new Format(MediaTypeKey, FormatKey.MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 24),
            });
        name = COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE;
    }

    @Override
    public void reset() {
        state = null;
        frameCounter = 0;
    }

    @Override
    public int process(Buffer in, Buffer out) {
        if (state == null) {
            state = new TechSmithCodecCore();
        }
        if (in.isFlag(DISCARD)) {
            out.setMetaTo(in);
            return CODEC_OK;
        }
        
        if (outputFormat.get(EncodingKey).equals(ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE)) {
            return encode(in, out);
        } else {
            return CODEC_OK;
        }
    }

    public int encode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }

        SeekableByteArrayOutputStream tmp;
        if (out.data instanceof byte[]) {
            tmp = new SeekableByteArrayOutputStream((byte[]) out.data);
        } else {
            tmp = new SeekableByteArrayOutputStream();
        }

        boolean isKeyframe = frameCounter == 0 ||
            frameCounter % outputFormat.get(KeyFrameIntervalKey, outputFormat.get(FrameRateKey)) == 0;
        out.setFlag(KEYFRAME, isKeyframe);
        out.clearFlag(SAME_DATA);
        frameCounter++;

        // Handle sub-image
        Rectangle r;
        int scanlineStride;
        if (in.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) in.data;
            WritableRaster raster = image.getRaster();
            scanlineStride = raster.getSampleModel().getWidth();
            r = raster.getBounds();
            r.x -= raster.getSampleModelTranslateX();
            r.y -= raster.getSampleModelTranslateY();
            out.header = image.getColorModel();
        } else {
            r = new Rectangle(0, 0, outputFormat.get(WidthKey), outputFormat.get(HeightKey));
            scanlineStride = outputFormat.get(WidthKey);
            out.header = null;
        }
        int offset = r.x + r.y * scanlineStride;

        try {
            switch (outputFormat.get(DepthKey)) {
                case 8: {
                    byte[] pixels = getIndexed8(in);
                    if (pixels == null) {
                        out.setFlag(DISCARD);
                        return CODEC_OK;
                    }

                    if (isKeyframe) {
                        state.encodeKey8(tmp, pixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey),
                            offset, scanlineStride, outputFormat.get(CompressionLevelKey, DEFAULT_COMPRESSION_LEVEL));
                    } else {
                        if (in.isFlag(SAME_DATA)) {
                            state.encodeSameDelta8(tmp);
                        } else {
                            state.encodeDelta8(tmp, pixels, (byte[]) previousPixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey),
                                offset, scanlineStride, outputFormat.get(CompressionLevelKey, DEFAULT_COMPRESSION_LEVEL));
                        }
                        out.clearFlag(KEYFRAME);
                    }
                    if (previousPixels == null) {
                        previousPixels = pixels.clone();
                    } else {
                        System.arraycopy(pixels, 0, (byte[]) previousPixels, 0, pixels.length);
                    }
                    break;
                }
                case 16: {
                    short[] pixels = getRGB15(in); // 16-bit TSCC is actually just 15-bit
                    if (pixels == null) {
                        out.setFlag(DISCARD);
                        return CODEC_OK;
                    }

                    if (isKeyframe) {
                        state.encodeKey16(tmp, pixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey),
                            offset, scanlineStride, outputFormat.get(CompressionLevelKey, DEFAULT_COMPRESSION_LEVEL));
                    } else {
                        if (in.isFlag(SAME_DATA)) {
                            state.encodeSameDelta16(tmp);
                        } else {
                            state.encodeDelta16(tmp, pixels, (short[]) previousPixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey),
                                offset, scanlineStride, outputFormat.get(CompressionLevelKey, DEFAULT_COMPRESSION_LEVEL));
                        }
                    }
                    if (previousPixels == null) {
                        previousPixels = pixels.clone();
                    } else {
                        System.arraycopy(pixels, 0, (short[]) previousPixels, 0, pixels.length);
                    }
                    break;
                }
                case 24: {
                    int[] pixels = getRGB24(in);
                    if (pixels == null) {
                        out.setFlag(DISCARD);
                        return CODEC_OK;
                    }

                    if (isKeyframe) {
                        state.encodeKey24(tmp, pixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey),
                            offset, scanlineStride, outputFormat.get(CompressionLevelKey, DEFAULT_COMPRESSION_LEVEL));
                        out.setFlag(KEYFRAME);
                    } else {
                        if (in.isFlag(SAME_DATA)) {
                            state.encodeSameDelta24(tmp);
                        } else {
                            state.encodeDelta24(tmp, pixels, (int[]) previousPixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey),
                                offset, scanlineStride, outputFormat.get(CompressionLevelKey, DEFAULT_COMPRESSION_LEVEL));
                        }
                        out.clearFlag(KEYFRAME);
                    }
                    if (previousPixels == null) {
                        previousPixels = pixels.clone();
                    } else {
                        System.arraycopy(pixels, 0, (int[]) previousPixels, 0, pixels.length);
                    }
                    break;
                }
                default: {
                    out.setFlag(DISCARD);
                    return CODEC_FAILED;
                }
            }

            out.format = outputFormat;
            out.data = tmp.getBuffer();
            out.offset = 0;
            out.sampleCount = 1;
            out.length = tmp.size();
            return CODEC_OK;
        } catch (IOException ex) {
            ex.printStackTrace();
            out.setFlag(DISCARD);
            return CODEC_OK;
        }
    }
}

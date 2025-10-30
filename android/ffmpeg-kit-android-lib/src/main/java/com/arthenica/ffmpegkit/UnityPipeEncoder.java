package com.arthenica.ffmpegkit;

import android.content.Context;
import android.util.LongSparseArray;

/**
 * JNI-friendly bridge for Unity to encode during recording.
 * Wraps {@link AsyncFrameEncoder} with simple static methods.
 */
public final class UnityPipeEncoder {

    private static final LongSparseArray<AsyncFrameEncoder> ENCODERS = new LongSparseArray<>();
    private static long NEXT_ID = 1L;

    private UnityPipeEncoder() {
    }

    public static synchronized long create(final Context context,
                                           final int width,
                                           final int height,
                                           final int fps,
                                           final String outputPath,
                                           final String inputFormat, // "RAW_RGB24" or "JPEG"
                                           final String[] extraEncoderArgs,
                                           final int queueCapacity) {
        final AsyncFrameEncoder.InputFormat fmt =
            "JPEG".equalsIgnoreCase(inputFormat) ? AsyncFrameEncoder.InputFormat.JPEG : AsyncFrameEncoder.InputFormat.RAW_RGB24;
        final AsyncFrameEncoder encoder = new AsyncFrameEncoder(
            context,
            width,
            height,
            fps,
            outputPath,
            fmt,
            extraEncoderArgs,
            queueCapacity
        );
        final long id = NEXT_ID++;
        ENCODERS.put(id, encoder);
        return id;
    }

    public static synchronized void start(final long id) {
        final AsyncFrameEncoder enc = ENCODERS.get(id);
        if (enc != null) enc.start();
    }

    public static boolean offerFrame(final long id, final byte[] frame, final int timeoutMs) {
        final AsyncFrameEncoder enc = ENCODERS.get(id);
        return enc != null && enc.offerFrame(frame, timeoutMs);
    }

    public static synchronized void stop(final long id) {
        final AsyncFrameEncoder enc = ENCODERS.get(id);
        if (enc != null) enc.stop();
    }

    public static synchronized void release(final long id) {
        final AsyncFrameEncoder enc = ENCODERS.get(id);
        if (enc != null) {
            enc.stop();
            ENCODERS.remove(id);
        }
    }
}



package com.arthenica.ffmpegkit;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Streams frames to FFmpeg via a named pipe and encodes asynchronously while recording.
 */
public final class AsyncFrameEncoder {

    public enum InputFormat {
        RAW_RGB24,
        JPEG
    }

    private static final String TAG = "AsyncFrameEncoder";

    private final Context context;
    private final int width;
    private final int height;
    private final int fps;
    private final String outputPath;
    private final InputFormat inputFormat;
    private final String[] extraEncoderArgs;
    private final int queueCapacity;

    private volatile boolean started;
    private volatile boolean stopped;
    private String pipePath;
    private FFmpegSession session;
    private FileOutputStream pipeOutputStream;
    private Thread writerThread;
    private BlockingQueue<byte[]> frameQueue;

    public AsyncFrameEncoder(final Context context,
                             final int width,
                             final int height,
                             final int fps,
                             final String outputPath,
                             final InputFormat inputFormat,
                             final String[] extraEncoderArgs,
                             final int queueCapacity) {
        this.context = Objects.requireNonNull(context);
        this.width = width;
        this.height = height;
        this.fps = fps;
        this.outputPath = Objects.requireNonNull(outputPath);
        this.inputFormat = Objects.requireNonNull(inputFormat);
        this.extraEncoderArgs = extraEncoderArgs == null ? new String[0] : Arrays.copyOf(extraEncoderArgs, extraEncoderArgs.length);
        this.queueCapacity = Math.max(2, queueCapacity);
    }

    public synchronized void start() {
        if (started) return;
        started = true;

        pipePath = FFmpegKitConfig.registerNewFFmpegPipe(context);
        if (pipePath == null) {
            throw new IllegalStateException("Failed to create FFmpeg pipe");
        }

        frameQueue = new ArrayBlockingQueue<>(queueCapacity);

        final String[] args = buildCommandArgs(pipePath, outputPath);

        session = FFmpegKit.executeWithArgumentsAsync(args, new FFmpegSessionCompleteCallback() {
            @Override
            public void apply(FFmpegSession completedSession) {
                Log.d(TAG, "FFmpeg session completed: " + completedSession.getReturnCode());
                cleanupPipe();
            }
        });

        try {
            pipeOutputStream = new FileOutputStream(new File(pipePath));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open pipe for writing", e);
        }

        writerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                drainQueueToPipe();
            }
        }, "AsyncFrameEncoder-Writer");
        writerThread.start();
    }

    public boolean offerFrame(final byte[] frameData, final long timeoutMillis) {
        if (!started || stopped) return false;
        try {
            return frameQueue.offer(frameData, timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public synchronized void stop() {
        if (!started || stopped) return;
        stopped = true;

        // Signal writer thread to finish by enqueueing a sentinel null
        frameQueue.offer(new byte[0]);

        if (writerThread != null) {
            try {
                writerThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Closing the pipe writer signals EOF to FFmpeg which finalizes the file
        closeQuietly(pipeOutputStream);

        // Session will complete once FFmpeg finalizes output
        // Optionally, users can wait on session if they need synchronous completion
    }

    public FFmpegSession getSession() {
        return session;
    }

    private String[] buildCommandArgs(final String inputPipe, final String output) {
        if (inputFormat == InputFormat.RAW_RGB24) {
            return concat(new String[]{
                "-hide_banner",
                "-f", "rawvideo",
                "-pix_fmt", "rgb24",
                "-s", width + "x" + height,
                "-r", String.valueOf(fps),
                "-i", inputPipe,
                "-an",
                "-c:v", "h264_mediacodec"
            }, extraEncoderArgs, new String[]{output});
        } else {
            return concat(new String[]{
                "-hide_banner",
                // Help FFmpeg demux MJPEG over a pipe reliably
                "-fflags", "+nobuffer",
                "-probesize", "50M",
                "-analyzeduration", "50M",
                "-f", "image2pipe",
                "-vcodec", "mjpeg",
                "-r", String.valueOf(fps),
                "-use_wallclock_as_timestamps", "1",
                "-thread_queue_size", "512",
                "-i", inputPipe,
                "-an",
                "-c:v", "h264_mediacodec"
            }, extraEncoderArgs, new String[]{output});
        }
    }

    private static String[] concat(final String[] head, final String[] middle, final String[] tail) {
        final int total = head.length + middle.length + tail.length;
        final String[] result = new String[total];
        int p = 0;
        System.arraycopy(head, 0, result, p, head.length);
        p += head.length;
        System.arraycopy(middle, 0, result, p, middle.length);
        p += middle.length;
        System.arraycopy(tail, 0, result, p, tail.length);
        return result;
    }

    private void drainQueueToPipe() {
        try {
            while (true) {
                final byte[] data = frameQueue.take();
                if (data.length == 0) break; // sentinel
                pipeOutputStream.write(data);
            }
            pipeOutputStream.flush();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            Log.w(TAG, "Pipe write failed: " + e.getMessage());
        } finally {
            closeQuietly(pipeOutputStream);
        }
    }

    private void cleanupPipe() {
        if (pipePath != null) {
            FFmpegKitConfig.closeFFmpegPipe(pipePath);
            pipePath = null;
        }
    }

    private static void closeQuietly(final FileOutputStream os) {
        if (os == null) return;
        try {
            os.close();
        } catch (IOException ignore) {
        }
    }
}



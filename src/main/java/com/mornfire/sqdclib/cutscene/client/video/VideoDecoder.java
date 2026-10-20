package com.mornfire.sqdclib.cutscene.client.video;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.PictureWithMetadata;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.Transform;
import org.jcodec.scale.Yuv420pToRgb;
import org.jetbrains.annotations.Nullable;

public final class VideoDecoder implements AutoCloseable {
    private static final int QUEUE_CAPACITY = 8;
    private static final long POLL_MILLIS = 8L;
    private static final Transform TRANSFORM = new Yuv420pToRgb();

    public record Frame(NativeImage image, double timestamp) {}

    private final Path tempFile;
    private final BlockingQueue<Frame> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private final Thread worker;
    private volatile boolean running = true;
    private volatile boolean exhausted;
    @Nullable
    private volatile Throwable failure;

    private VideoDecoder(Path tempFile, FrameGrab grab) {
        this.tempFile = tempFile;
        this.worker = new Thread(() -> decodeLoop(grab), "sqdclib-video-decoder");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    public static VideoDecoder open(ResourceLocation videoId) throws Exception {
        ResourceLocation file = videoFile(videoId);
        Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(file);
        Path temp = Files.createTempFile("sqdclib-video", ".mp4");
        try (InputStream stream = resource.open()) {
            Files.copy(stream, temp, StandardCopyOption.REPLACE_EXISTING);
        }
        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(temp.toFile()));
        return new VideoDecoder(temp, grab);
    }

    public static ResourceLocation videoFile(ResourceLocation videoId) {
        String path = videoId.getPath();
        if (path.startsWith("videos/") || path.endsWith(".mp4") || path.endsWith(".webm")) {
            return videoId;
        }
        return ResourceLocation.fromNamespaceAndPath(videoId.getNamespace(), "videos/" + path + ".mp4");
    }

    @Nullable
    public Frame poll(double elapsedSeconds) {
        Frame latest = null;
        Frame head = queue.peek();
        while (head != null && head.timestamp() <= elapsedSeconds) {
            latest = queue.poll();
            head = queue.peek();
        }
        return latest;
    }

    @Nullable
    public Frame take() {
        return queue.poll();
    }

    public boolean failed() {
        return failure != null;
    }

    @Nullable
    public Throwable failure() {
        return failure;
    }

    public boolean finished() {
        return exhausted && queue.isEmpty();
    }

    private void decodeLoop(FrameGrab grab) {
        try {
            while (running) {
                PictureWithMetadata picture = grab.getNativeFrameWithMetadata();
                if (picture == null) {
                    exhausted = true;
                    return;
                }
                Frame frame = new Frame(toNativeImage(picture.getPicture()), picture.getTimestamp());
                while (running) {
                    if (queue.offer(frame, POLL_MILLIS, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                }
                if (!running) {
                    frame.image().close();
                }
            }
        } catch (Throwable throwable) {
            failure = throwable;
            exhausted = true;
        }
    }

    private static NativeImage toNativeImage(Picture source) {
        int width = source.getWidth();
        int height = source.getHeight();
        Picture rgb = Picture.create(width, height, ColorSpace.RGB);
        TRANSFORM.transform(source, rgb);
        byte[] data = rgb.getPlaneData(0);
        NativeImage image = new NativeImage(width, height, true);
        for (int y = 0; y < height; y++) {
            int rowStart = y * width * 3;
            for (int x = 0; x < width; x++) {
                int index = rowStart + x * 3;
                int red = data[index] & 0xFF;
                int green = data[index + 1] & 0xFF;
                int blue = data[index + 2] & 0xFF;
                image.setPixelRGBA(x, y, 0xFF000000 | (blue << 16) | (green << 8) | red);
            }
        }
        return image;
    }

    private static void drain(BlockingQueue<Frame> queue) {
        Frame frame;
        while ((frame = queue.poll()) != null) {
            frame.image().close();
        }
    }

    @Override
    public void close() {
        running = false;
        worker.interrupt();
        drain(queue);
        try {
            Files.deleteIfExists(tempFile);
        } catch (Exception ignored) {
        }
    }

    public static Optional<VideoDecoder> tryOpen(ResourceLocation videoId) {
        try {
            return Optional.of(open(videoId));
        } catch (NoClassDefFoundError missing) {
            ClientVideos.LOGGER.warn("[sqdclib] video support is not available because JCodec is not on the "
                    + "classpath; video '{}' will be skipped", videoId);
            return Optional.empty();
        } catch (Throwable throwable) {
            ClientVideos.LOGGER.warn("[sqdclib] could not open video '{}': {}",
                    videoId, throwable.toString());
            return Optional.empty();
        }
    }
}

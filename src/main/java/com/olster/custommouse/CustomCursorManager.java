package com.olster.custommouse;

import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CustomCursorManager {
    private static final List<Long> cursorHandles = new ArrayList<>();
    private static int animationFrame = 0;
    private static int animationTicks = 0;
    private static List<CursorFrame> frames = List.of();

    private CustomCursorManager() {}

    public static void load(Path path, Window window) throws IOException {
        String name = path.getFileName().toString().toLowerCase();
        List<CursorFrame> loaded;
        if (name.endsWith(".ani")) loaded = readAni(Files.readAllBytes(path));
        else if (name.endsWith(".cur")) loaded = List.of(readCur(Files.readAllBytes(path)));
        else throw new IOException("Only .cur and .ani cursor files are supported");

        if (loaded.isEmpty()) throw new IOException("No cursor frames found");
        clear(window);
        frames = loaded;
        animationFrame = 0;
        animationTicks = 0;
        applyFrame(window);
    }

    public static void tick(Window window) {
        if (frames.size() <= 1) return;
        CursorFrame frame = frames.get(animationFrame);
        animationTicks++;
        if (animationTicks >= Math.max(1, frame.delayTicks)) {
            animationTicks = 0;
            animationFrame = (animationFrame + 1) % frames.size();
            applyFrame(window);
        }
    }

    public static void clear(Window window) {
        if (window != null) GLFW.glfwSetCursor(window.handle(), 0L);
        for (long handle : cursorHandles) if (handle != 0L) GLFW.glfwDestroyCursor(handle);
        cursorHandles.clear();
        frames = List.of();
        animationFrame = 0;
        animationTicks = 0;
    }

    private static void applyFrame(Window window) {
        if (window == null || frames.isEmpty()) return;
        CursorFrame frame = frames.get(animationFrame);
        GLFWImage image = GLFWImage.malloc();
        image.width(frame.image.getWidth());
        image.height(frame.image.getHeight());

        ByteBuffer pixels = ByteBuffer.allocateDirect(frame.image.getWidth() * frame.image.getHeight() * 4);
        for (int y = 0; y < frame.image.getHeight(); y++) {
            for (int x = 0; x < frame.image.getWidth(); x++) {
                int argb = frame.image.getRGB(x, y);
                pixels.put((byte) ((argb >> 16) & 0xFF));
                pixels.put((byte) ((argb >> 8) & 0xFF));
                pixels.put((byte) (argb & 0xFF));
                pixels.put((byte) ((argb >> 24) & 0xFF));
            }
        }
        pixels.flip();
        image.pixels(pixels);
        long cursor = GLFW.glfwCreateCursor(image, frame.hotspotX, frame.hotspotY);
        image.free();
        if (cursor == 0L) return;
        cursorHandles.add(cursor);
        GLFW.glfwSetCursor(window.handle(), cursor);
    }

    private static CursorFrame readCur(byte[] data) throws IOException {
        if (data.length < 22 || u16(data, 0) != 0 || u16(data, 2) != 2 || u16(data, 4) < 1)
            throw new IOException("Invalid CUR file");
        int entry = 6;
        int width = data[entry] & 255; if (width == 0) width = 256;
        int height = data[entry + 1] & 255; if (height == 0) height = 256;
        int hotspotX = u16(data, entry + 4);
        int hotspotY = u16(data, entry + 6);
        int size = i32(data, entry + 8);
        int offset = i32(data, entry + 12);
        if (offset < 0 || size < 0 || offset + size > data.length) throw new IOException("Invalid CUR image");
        return new CursorFrame(decodeDib(data, offset, size, width, height), hotspotX, hotspotY, 1);
    }

    private static BufferedImage decodeDib(byte[] data, int offset, int size, int width, int height) throws IOException {
        byte[] dib = new byte[size];
        System.arraycopy(data, offset, dib, 0, size);
        if (size >= 40 && i32(dib, 0) == 40 && u16(dib, 14) == 32 && i32(dib, 16) == 0 && size >= 40 + width * height * 4) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) {
                int srcY = height - 1 - y;
                for (int x = 0; x < width; x++) {
                    int p = 40 + (srcY * width + x) * 4;
                    int b = dib[p] & 255, g = dib[p + 1] & 255, r = dib[p + 2] & 255, a = dib[p + 3] & 255;
                    image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                }
            }
            return image;
        }
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(dib));
        if (image == null) throw new IOException("Unsupported CUR image format");
        return image;
    }

    private static List<CursorFrame> readAni(byte[] data) throws IOException {
        if (data.length < 12 || data[0] != 'R' || data[1] != 'I' || data[2] != 'F' || data[3] != 'F')
            throw new IOException("Invalid ANI file");
        List<byte[]> images = new ArrayList<>();
        List<Integer> rates = new ArrayList<>();
        int p = 12;
        while (p + 8 <= data.length) {
            String id = new String(data, p, 4, java.nio.charset.StandardCharsets.US_ASCII);
            int len = i32(data, p + 4), start = p + 8;
            if (len < 0 || start + len > data.length) break;
            if (id.equals("icon")) {
                byte[] icon = new byte[len];
                System.arraycopy(data, start, icon, 0, len);
                images.add(icon);
            } else if (id.equals("rate")) {
                for (int q = start; q + 4 <= start + len; q += 4) rates.add(i32(data, q));
            }
            p = start + len + (len & 1);
        }
        List<CursorFrame> result = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) result.add(decodeAniIcon(images.get(i), rates.size() > i ? rates.get(i) : 6));
        return result;
    }

    private static CursorFrame decodeAniIcon(byte[] data, int rate) throws IOException {
        if (data.length >= 22 && u16(data, 0) == 0 && u16(data, 2) == 2) return withRate(readCur(data), rate);
        if (data.length >= 22 && u16(data, 0) == 0 && u16(data, 2) == 1) {
            int count = u16(data, 4);
            if (count < 1 || data.length < 6 + count * 16) throw new IOException("Invalid ANI icon frame");
            int entry = 6;
            int size = i32(data, entry + 8), offset = i32(data, entry + 12);
            int width = data[entry] & 255; if (width == 0) width = 256;
            int height = data[entry + 1] & 255; if (height == 0) height = 256;
            return new CursorFrame(decodeDib(data, offset, size, width, height), width / 2, height / 2, Math.max(1, rate));
        }
        throw new IOException("Unsupported ANI frame format");
    }

    private static CursorFrame withRate(CursorFrame frame, int rate) {
        return new CursorFrame(frame.image, frame.hotspotX, frame.hotspotY, Math.max(1, rate));
    }

    private static int u16(byte[] d, int p) { return (d[p] & 255) | ((d[p + 1] & 255) << 8); }
    private static int i32(byte[] d, int p) { return (d[p] & 255) | ((d[p + 1] & 255) << 8) | ((d[p + 2] & 255) << 16) | (d[p + 3] << 24); }
    private record CursorFrame(BufferedImage image, int hotspotX, int hotspotY, int delayTicks) {}
}

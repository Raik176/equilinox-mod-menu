package de.rhm176;

import de.matthiasmann.twl.utils.PNGDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

public class ModMenuUtil {
    public static Optional<Boolean> getBoolean(String key, ModMetadata metadata) {
        return metadata.containsCustomValue(key)
                ? Optional.of(metadata.getCustomValue(key).getAsBoolean())
                : Optional.empty();
    }

    public static Optional<Boolean> getBoolean(String key, CustomValue.CvObject object) {
        if (object == null) return Optional.of(false);

        return object.containsKey(key) ? Optional.of(object.get(key).getAsBoolean()) : Optional.empty();
    }

    public static Optional<String> getString(String key, ModMetadata metadata) {
        return metadata.containsCustomValue(key)
                ? Optional.of(metadata.getCustomValue(key).getAsString())
                : Optional.empty();
    }

    public static Optional<String> getString(String key, CustomValue.CvObject object) {
        return object.containsKey(key) ? Optional.of(object.get(key).getAsString()) : Optional.empty();
    }

    public static Optional<Set<String>> getStringSet(String key, CustomValue.CvObject object) {
        if (object.containsKey(key)) {
            Set<String> strings = new HashSet<>();
            for (CustomValue value : object.get(key).getAsArray()) {
                strings.add(value.getAsString());
            }
            return Optional.of(strings);
        }
        return Optional.empty();
    }

    public static Optional<Map<String, String>> getStringMap(String key, CustomValue.CvObject object) {
        if (object.containsKey(key)) {
            Map<String, String> strings = new HashMap<>();
            for (Map.Entry<String, CustomValue> entry : object.get(key).getAsObject()) {
                strings.put(entry.getKey(), entry.getValue().getAsString());
            }
            return Optional.of(strings);
        }
        return Optional.empty();
    }

    public static ByteBuffer loadPng(ModContainer container, String file, int size) {
        ByteBuffer buffer;

        Optional<Path> path = container.findPath(file);
        if (path.isEmpty()) {
            return null;
        }

        try (InputStream in = Files.newInputStream(path.get())) {
            PNGDecoder decoder = new PNGDecoder(in);
            int width = decoder.getWidth();
            int height = decoder.getHeight();
            buffer = ByteBuffer.allocateDirect(4 * width * height);
            decoder.decode(buffer, width * 4, PNGDecoder.Format.BGRA);
            buffer.flip();

            if (width == size && height == size) {
                return buffer;
            } else {
                ByteBuffer scaledBuffer = ByteBuffer.allocateDirect(4 * size * size);

                for (int ty = 0; ty < size; ty++) {
                    for (int tx = 0; tx < size; tx++) {
                        int sx = (int) (tx * (double) width / size);
                        int sy = (int) (ty * (double) height / size);

                        sx = Math.max(0, Math.min(sx, width - 1));
                        sy = Math.max(0, Math.min(sy, height - 1));

                        int sourceIndex = (sy * width + sx) * 4;

                        byte b = buffer.get(sourceIndex);
                        byte g = buffer.get(sourceIndex + 1);
                        byte r = buffer.get(sourceIndex + 2);
                        byte a = buffer.get(sourceIndex + 3);

                        scaledBuffer.put(b);
                        scaledBuffer.put(g);
                        scaledBuffer.put(r);
                        scaledBuffer.put(a);
                    }
                }

                scaledBuffer.flip();
                return scaledBuffer;
            }
        } catch (IOException e) {
            ModMenu.LOGGER.warn(
                    "Could not load png {} for mod: {}",
                    file,
                    container.getMetadata().getId(),
                    e);
            return null;
        }
    }
}

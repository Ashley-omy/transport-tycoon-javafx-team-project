package view;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import model.BridgeType;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class BridgeVisuals {
    private static final String BRIDGE1_TEXTURE_PATH = "/textures/bridges/Bridge1.png";
    private static final String BRIDGE2_TEXTURE_PATH = "/textures/bridges/Bridge2.png";
    private static final String BRIDGE3_TEXTURE_PATH = "/textures/bridges/Bridge3.png";
    private static final Image BRIDGE1_TEXTURE = loadTexture(BRIDGE1_TEXTURE_PATH);
    private static final Image BRIDGE2_TEXTURE = loadTexture(BRIDGE2_TEXTURE_PATH);
    private static final Image BRIDGE3_TEXTURE = loadTexture(BRIDGE3_TEXTURE_PATH);

    private BridgeVisuals() {
    }

    public static Color colorFor(BridgeType type) {
        if (type == null) {
            return Color.SADDLEBROWN;
        }
        return switch (type) {
            case TYPE_A -> Color.rgb(117, 74, 43);
            case TYPE_B -> Color.rgb(145, 96, 57);
            case TYPE_C -> Color.rgb(171, 120, 74);
        };
    }

    public static Image textureFor(BridgeType type) {
        if (type == null) {
            return BRIDGE1_TEXTURE;
        }
        return switch (type) {
            case TYPE_A -> BRIDGE1_TEXTURE;
            case TYPE_B -> BRIDGE2_TEXTURE;
            case TYPE_C -> BRIDGE3_TEXTURE;
        };
    }

    private static Image loadTexture(String resourcePath) {
        try (InputStream stream = BridgeVisuals.class.getResourceAsStream(resourcePath)) {
            if (stream != null) {
                return new Image(stream);
            }
        } catch (Exception ignored) {
        }
        return loadTextureFromProjectPath(resourcePath);
    }

    private static Image loadTextureFromProjectPath(String resourcePath) {
        try {
            String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
            Path filePath = Paths.get("src", "main", "resources").resolve(normalized);
            if (!Files.exists(filePath)) {
                return null;
            }
            return new Image(filePath.toUri().toString());
        } catch (Exception ignored) {
            return null;
        }
    }
}

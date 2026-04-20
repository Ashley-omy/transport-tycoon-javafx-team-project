package view;

import javafx.scene.paint.Color;
import model.BridgeType;

public final class BridgeVisuals {
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
}

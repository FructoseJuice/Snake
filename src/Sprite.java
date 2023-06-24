import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

/**
 * Represents every sprite for game use
 */
public abstract class Sprite {
    public abstract Pair<Integer, Integer> getCoordinates();
    public abstract Rectangle getNode();
}

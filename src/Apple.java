import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

import java.nio.channels.Pipe;

/**
 * Represents an apple that the snake can eat
 */
public class Apple extends Sprite{
    Rectangle node;
    Pair<Integer, Integer> coordinates;
    public Apple() {
        node = new Rectangle();

        node.setFill(Color.RED);

        //Set size of rectangle
        node.setWidth(25);
        node.setHeight(25);

        //Set rounded corners
        node.setArcHeight(50);
        node.setArcWidth(50);
    }

    public Rectangle getNode() {
        return node;
    }
    public void setCoordinates(int xCoor, int yCoor) {
        coordinates = new Pair<>(xCoor, yCoor);
    }

    @Override
    public Pair<Integer, Integer> getCoordinates() {
        return coordinates;
    }
}

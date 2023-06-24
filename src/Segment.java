import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

/**
 * Represents each segment of the snake
 */
public class Segment extends Sprite{
    Rectangle segment;
    Pair<Integer, Integer> coordinates;

    public Segment() {
        segment = new Rectangle();

        segment.setFill(Color.GREEN);

        //Set size of rectangle
        segment.setWidth(45);
        segment.setHeight(45);

        //Set rounded corners
        segment.setArcHeight(20);
        segment.setArcWidth(20);
    }

    public Rectangle getNode() {
        return segment;
    }

    public void setCoordinates(int xCoor, int yCoor) {
        coordinates = new Pair<>(xCoor, yCoor);
    }
    @Override
    public Pair<Integer, Integer> getCoordinates() {
        return coordinates;
    }

    public int getX() {
        return coordinates.getKey();
    }

    public int getY() {
        return coordinates.getValue();
    }

    public int getToken() {
        int xCoor = coordinates.getKey();
        int yCoor = coordinates.getValue();

        if (xCoor >= yCoor) {
            return (int) (Math.pow(xCoor, 2) + xCoor + yCoor);
        } else {
            return (int) (xCoor + Math.pow(yCoor, 2));
        }
    }
}

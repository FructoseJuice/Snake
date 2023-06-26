import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents apple to be eaten by snake
 */
public class Main extends Application {
    public static void main(String[] args) {launch(args);}

    //Play field
    GridPane map = new GridPane();

    ArrayList<Pair<Integer, Integer>> possibleSpaces = new ArrayList<>(352);
    //Directions snake can move in
    enum DIR {LEFT, RIGHT, UP, DOWN}

    @Override
    public void start(Stage primaryStage) {
        //Initialize scene elements
        StackPane stack = new StackPane();
        VBox layout = new VBox();
        HBox hud = new HBox();

        /*
        Construct layout
         */

        //Create score counter text
        Font font = Font.loadFont("file:Resources/font/PressStart2P-vaV7.ttf", 30);
        Text scoreCounter = new Text("SCORE: 0");
        scoreCounter.setFont(font);
        scoreCounter.setFill(Color.WHITE);

        //Populate hud with scoreCounter
        hud.getChildren().add(scoreCounter);
        hud.setStyle("-fx-fill: grey");
        hud.setAlignment(Pos.CENTER);

        //Set layout properties
        layout.setSpacing(35);
        layout.setPadding(new Insets(15));

        //Set map properties
        map.setStyle("width: 1000px; height: 727px");
        map.setAlignment(Pos.CENTER);

        /*
        Create map cells on gridPane, set dimensions to 45px
         */

        for ( int row = 0; row < 16; row++ ) {
            RowConstraints newRow = new RowConstraints(45);
            map.getRowConstraints().add(newRow);
        }

        for ( int col = 0; col < 22; col++ ) {
            ColumnConstraints cell = new ColumnConstraints(45);
            map.getColumnConstraints().add(cell);
        }

        //Create play area Outline
        Rectangle outline = new Rectangle();

        outline.setHeight(733);
        outline.setWidth(1006);
        outline.setStyle("-fx-fill: black; -fx-stroke: white; -fx-stroke-width: 3px");

        //Create stack of nodes
        stack.getChildren().addAll(outline, map);
        //Add stack and hud to layout
        layout.getChildren().addAll(hud, stack);

        /*
        Construct scene
         */
        //Set layout and color
        Scene scene = new Scene(layout);
        scene.setFill(Color.BLACK);

        //Set properties, and show stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("Snake");
        primaryStage.setResizable(false);
        primaryStage.setHeight(900);
        primaryStage.setWidth(1150);
        primaryStage.show();

        /*
        Create game variables
         */

        //HashMap representing locations of sprites in play area
        HashMap<Integer, Sprite> spriteLocations = new HashMap<>(352);

        //Linked-List defining the Snake
        LinkedList<Segment> snake = new LinkedList<>();

        Apple apple = new Apple();

        AtomicBoolean left = new AtomicBoolean(false);
        AtomicBoolean right = new AtomicBoolean(false);
        AtomicBoolean up = new AtomicBoolean(false);
        AtomicBoolean down = new AtomicBoolean(false);

        /*
        Initialize variables
         */

        //Populate spriteLocations with null values
        //Populate possibleSpaces
        for ( int y = 0; y < 16; y++ ) {
            for ( int x = 0; x < 22; x++ ) {
                spriteLocations.put( tokenizeCoordinates(x, y), null );

                possibleSpaces.add(new Pair<>(x, y));
            }
        }

        //Initialize snake
        snake.add( new Segment() );
        snake.add( new Segment() );
        snake.add( new Segment() );

        for ( int i = 0; i < 3; i++ ) {
            //Generate starting positions
            snake.get(i).setCoordinates(11, 4 - i);

            //Update sprite locations
            spriteLocations.put( snake.get(i).getToken(), snake.get(i) );

            //Update gridPane/map
            Segment seg = snake.get(i);
            map.add( seg.getNode(), snake.get(i).getX(), snake.get(i).getY() );
        }

        //Generate initial apple
        generateApplePos(apple, spriteLocations, snake);

        //Create listener for user input
        scene.setOnKeyPressed(event -> {
            //Set all inputs to false
            up.set(false);
            down.set(false);
            left.set(false);
            right.set(false);

            //Process keyboard input
            switch ( event.getCode() ) {
                case W, UP -> up.set(true);
                case A, LEFT -> left.set(true);
                case S, DOWN -> down.set(true);
                case D, RIGHT -> right.set(true);
            }
        });

        /*
        Start game
         */
        AnimationTimer animationTimer = new AnimationTimer() {

            //Timing variables
            int timer = 0;
            final short speed = 6;

            //Game variables
            DIR currentDir = DIR.DOWN;
            DIR newDir;
            boolean ateApple = false;
            Pair<Integer, Integer> newSegmentCoors;
            boolean gameOver = false;
            int score = 0;
            final double scoreMultiplier = 1.1;

            @Override
            public void handle(long now) {
                //If game-state should refresh
                if ( timer == speed )
                {
                    //Reset timer
                    timer = 0;

                    //Check keyboard inputs
                    if (up.get()) newDir = DIR.UP;
                    if (down.get()) newDir = DIR.DOWN;
                    if (left.get()) newDir = DIR.LEFT;
                    if (right.get()) newDir = DIR.RIGHT;

                    //Validate new direction
                    if ( validateDirection(currentDir, newDir) ) {
                        //If newDir valid, update current direction
                        currentDir = newDir;
                    }

                    /*
                    Control snake movement
                     */

                    //Add new segment if apple eaten last frame
                    if ( ateApple ) {
                        addSnakeSegment(snake, newSegmentCoors, spriteLocations);
                        ateApple = false;
                    }

                    //Convert direction enum to integer values
                    Pair<Integer, Integer> dirValue = dirToIntegerValue(currentDir);

                    //Retrieve head of snake to begin movement
                    Segment head;
                    head = snake.getFirst();

                    /*
                    Verify if next move is valid
                     */

                    //Check Sprite contained in next cell in movement path
                    Sprite nextCell = checkNextCell(head, dirToIntegerValue(currentDir), spriteLocations);
                    if ( nextCell != null ) {

                        //Check if eating apple
                        if ( nextCell instanceof Apple ) {
                            ateApple = true;

                            //Save coordinates of last segment for new segment
                            newSegmentCoors = snake.getLast().coordinates;
                        } else {
                            //We're either hitting the wall or snake
                            gameOver = true;
                        }
                    }

                    if ( !gameOver )
                    {
                        //Move the snake
                        moveSnake(snake, spriteLocations, dirValue);

                        //if apple eaten move apple and update score
                        if (ateApple) {
                            //Update score counter
                            score = (int) Math.floor((score + 100) * scoreMultiplier);
                            scoreCounter.setText("Score: " + score);

                            //Move apple
                            generateApplePos(apple, spriteLocations, snake);
                        }
                    }
                }

                //iterate timer
                timer++;

                //Halt if game over
                if ( gameOver ) {
                    //Create game over text
                    Text text = new Text("Game Over.");
                    text.setFont(font);
                    text.setFill(Color.WHITE);

                    //Show game over text
                    map.add(text, 8, 7);

                    //Stop animation timer
                    this.stop();
                }
            }
        };
        animationTimer.start();
    }

    /**
     * Randomly chooses a spot in the play field to spawn a new apple
     * @param apple Apple to be spawned
     * @param spriteLocations Location of all sprites in play field
     */
    public void generateApplePos(Apple apple, HashMap<Integer, Sprite> spriteLocations, LinkedList<Segment> snake) {
        int segX;
        int segY;
        int index;
        Pair<Integer, Integer> oldCoors = apple.getCoordinates();
        ArrayList<Pair<Integer, Integer>> emptyCells = cloneArrayList(possibleSpaces);
        Deque<Pair<Integer, Integer>> occupiedCells = new ArrayDeque<>();

        for ( Segment seg : snake ) {

            //Retrieve position of segment
            segX = GridPane.getColumnIndex(seg.getNode());
            segY = GridPane.getRowIndex(seg.getNode());

            //Get index of segment in possibleSpaces ArrayList
            index = (segY*22) + segX;

            //Remove from emptyCells list
            occupiedCells.add(possibleSpaces.get(index));
        }

        while ( !occupiedCells.isEmpty() ) {
            //Remove all occupied cells
            emptyCells.remove(occupiedCells.poll());
        }

        Random rand = new Random();
        Pair<Integer, Integer> coordinates;

        //Generate random index to choose for coordinates
        index = rand.nextInt(emptyCells.size());

        //Get new coordinates for apple
        coordinates = emptyCells.get(index);

        //Add apple to hashMap
        spriteLocations.put(tokenizeCoordinates(coordinates), apple);

        //Set new coordinates for the apple
        apple.setCoordinates(coordinates.getKey(), coordinates.getValue());

        //Remove apple if on gridPane
        map.getChildren().remove(apple.getNode());

        //Place apple back on gridPane
        addSpriteToMap(apple);

        //Align apple
        GridPane.setHalignment(apple.getNode(), HPos.CENTER);
        GridPane.setValignment(apple.getNode(), VPos.CENTER);

        //Ensure new apple spot generates
        if ( oldCoors != null && oldCoors.equals(apple.getCoordinates())) {
            generateApplePos(apple, spriteLocations, snake);
        }
    }

    /**
     * Verifies player is not trying to move head opposite to current direction
     * @param oldDir old direction
     * @param newDir desired direction
     * @return if the user inputted movement is allowed
     */
    public boolean validateDirection(DIR oldDir, DIR newDir) {
        if ( newDir == DIR.UP ) return oldDir != DIR.DOWN;

        if ( newDir == DIR.LEFT ) return oldDir != DIR.RIGHT;

        if ( newDir == DIR.RIGHT ) return oldDir != DIR.LEFT;

        if ( newDir == DIR.DOWN ) return oldDir != DIR.UP;

        return false;
    }

    /**
     * Return the sprite contained at next cell in movement path
     * @param seg snakes head
     * @param dir direction snakes head is moving in
     * @param spriteLocations location of all sprite
     * @return sprite contained in next space, null if there's not a sprite in the next cell
     */
    public Sprite checkNextCell(Segment seg, Pair<Integer, Integer> dir, HashMap<Integer, Sprite> spriteLocations) {

        Pair<Integer, Integer> oldPos = seg.getCoordinates();

        //Get new position
        Pair<Integer, Integer> newPos;
        newPos = new Pair<>( oldPos.getKey() + dir.getKey(), oldPos.getValue() + dir.getValue());

        //Check if hitting edge of play area
        if ( (newPos.getKey() > 21 || newPos.getKey() < 0) || (newPos.getValue() > 15 || newPos.getValue() < 0) ) {
            //Treat the wall as a section of the snake to signify game over
            return new Segment();
        }

        //Return what's contained in target location
        return spriteLocations.get(tokenizeCoordinates(newPos));
    }

    /**
     * Converts a directional enum to a pair of integer values signifying
     * the amount of x and y coordinates to move.
     * @param dir direction enum to convert
     * @return pair of Integers representing x and y movement
     */
    public Pair<Integer, Integer> dirToIntegerValue(DIR dir) {
        if ( dir == DIR.UP ) return new Pair<>(0, -1);

        if ( dir == DIR.DOWN ) return new Pair<>(0, 1);

        if ( dir == DIR.LEFT ) return new Pair<>(-1, 0);

        if ( dir == DIR.RIGHT ) return new Pair<>(1, 0);

        return null;
    }


    /**
     * Move the snake in desired direction
     * @param snake Snake to move
     * @param spriteLocations Location of all sprites in play field
     * @param dir Direction to move represented by a pair of integers
     */
    public void moveSnake(LinkedList<Segment> snake, HashMap<Integer, Sprite> spriteLocations,
                          Pair<Integer, Integer> dir) {
        //Generate new Coordinates
        Pair<Integer, Integer> newHeadCoors = snake.getFirst().coordinates;
        newHeadCoors = new Pair<>( newHeadCoors.getKey() + dir.getKey(), newHeadCoors.getValue() + dir.getValue() );

        Segment tail = snake.getLast();

        //Remove tail from spriteLocations
        spriteLocations.remove(tail.getToken());

        //Remove tail from map
        map.getChildren().remove(tail.getNode());

        //Move tail to start of snake
        snake.remove(tail);
        snake.addFirst(tail);

        //Set new Coordinates
        tail.setCoordinates(newHeadCoors.getKey(), newHeadCoors.getValue());

        //Add back to map
        addSpriteToMap(tail);

        //Add back to spriteLocations
        spriteLocations.put(tail.getToken(), tail);
    }

    /**
     * Adds a new snake segment to the end of the snake
     * @param snake The snake
     * @param coors Coordinates to add the segment atf
     * @param spriteLocations Location of all sprites
     */
    public void addSnakeSegment
            (LinkedList<Segment> snake, Pair<Integer, Integer> coors, HashMap<Integer, Sprite> spriteLocations) {
        Segment seg = new Segment();

        //Add to snake
        snake.add(seg);

        //Set coordinates
        seg.coordinates = coors;

        //Update sprite locations with this new segment
        spriteLocations.put(seg.getToken(), seg);

        //Put segment on gridPane
        map.add(seg.getNode(), coors.getKey(), coors.getValue());
    }

    /**
     * Takes an x and y value, and produces a unique integer for every combination
     * @param xCoor X coordinate
     * @param yCoor Y coordinate
     * @return Token of the two integer values
     */
    public int tokenizeCoordinates(int xCoor, int yCoor) {
        if ( xCoor >= yCoor ) {
            return (int) ( Math.pow(xCoor, 2) + xCoor + yCoor );
        } else {
            return (int) ( xCoor + Math.pow(yCoor, 2) );
        }
    }

    /**
     * Takes a pair of coordinates and produces a unique integer for every combination
     * @param coors Coordinates to tokenize
     * @return Token of the two integer values
     */
    public int tokenizeCoordinates(Pair<Integer, Integer> coors) {
        return tokenizeCoordinates(coors.getKey(), coors.getValue());
    }

    /**
     * Adds sprite to gridPane
     * @param sprite Sprite to add to gridPane
     */
    public void addSpriteToMap(Sprite sprite) {
        //Grab coordinates
        int xCoor = sprite.getCoordinates().getKey();
        int yCoor = sprite.getCoordinates().getValue();

        //Add to gridPane
        map.add(sprite.getNode(), xCoor, yCoor);
    }

    /**
     * Used to clone possible spaces list
     * @param parent possibleSpaces list
     * @return clone of possible spaces
     */
    public ArrayList<Pair<Integer, Integer>> cloneArrayList(ArrayList<Pair<Integer, Integer>> parent) {
        ArrayList<Pair<Integer, Integer>> clone = new ArrayList<>(352);
        Pair<Integer, Integer> newPair;
        int index;

        for ( int i = 0; i < 16; i++ ) {
            for ( int j = 0; j < 22; j++ ) {
                index = (i*22) + j;
                newPair = new Pair<>(parent.get(index).getKey(), parent.get(index).getValue());
                clone.add(newPair);
            }
        }

        return clone;
    }
}

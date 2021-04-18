package BackEnd;

/**
 * Represents a two dimensional point with X and Y integer components.
 * @author Atif Ishaq, Joshua Oladitan
 * @version 1.0
 */

public class Coordinate {

    /*
     * These are variables to represent the x and y coordinate pairs.
     */
    private final int x;
    private final int y;

    /**
     * Constructs a new coordinate.
     * @param x the X coordinate
     * @param y the Y coordinate
     */
    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x Coordinate.
     * @return the x Coordinate.
     */
    public int getX() {
        return this.x;
    }


    /**
     * Gets the y Coordinate.
     * @return the y Coordinate.
     */
    public int getY() {
        return this.y;
    }

    /**
     * Determines whether two coordinates are equal.
     * @param input coordinate to check
     * @return true if the x and y coordinates are equal to this coordinate's components.
     */
    public boolean equals(Coordinate input) {
        return (x == input.getX()) && (y == input.getY());
    }

    /**
     * Returns a string representation of this coordinate.
     * @return A string consisting of the coordinates.
     */
    @Override
    public String toString() {
        return getX() + " " + getY();
    }

    /**
     * Determines whether this coordinate is equal to some object.
     * @param o The object to test equality with.
     * @return true if the object is a coordinate with equal components; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        return (o instanceof Coordinate) &&
                ((Coordinate) o).getX() == this.getX() &&
                ((Coordinate) o).getY() == this.getY();
    }

    /**
     * Shifts a Coordinate by a given amount.
     * This method does not mutate the current Coordinate,
     * but instead returns a new Coordinate object.
     * @param x amount to shift right
     * @param y amount to shift down
     * @return a new Coordinate in shifted location.
     */
    public Coordinate shift(int x, int y) {
        return shift(new Coordinate(x, y));
    }

    /**
     * Shifts a Coordinate by a given amount.
     * This method does not mutate the current Coordinate,
     * but instead returns a new Coordinate object.
     * @param shiftAmount Coordinate representing the amount to shift right and down
     * @return a new Coordinate in shifted location.
     */
    public Coordinate shift(Coordinate shiftAmount) {
        return new Coordinate(shiftAmount.getX() + x, shiftAmount.getY() + y);
    }

    /**
     * Returns the sum of the absolute values of the x and y components.
     * @return {@code abs(x) + abs(y)}
     */
    public int absoluteSum() {
        return Math.abs(x) + Math.abs(y);
    }

    /**
     * Returns the difference between this coordinate and another coordinate.
     * In other words, {@code (this.x - other.x, this.y - other.y)}
     * @param other the other coordinate
     * @return the difference between the two coordinates
     */
    public Coordinate difference(Coordinate other) {
        return new Coordinate(x - other.x, y - other.y);
    }
}

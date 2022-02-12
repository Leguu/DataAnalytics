import io.jenetics.jpx.Length;
import io.jenetics.jpx.Point;
import io.jenetics.jpx.Speed;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Any given continuous length of points
 */
public class Split {
    final List<Point> points;

    public Split(List<Point> points) {
        this.points = points;
    }

    /**
     * Initialise points to all the points between
     * the start instant and end instant in the given list of points.
     *
     * @author Quan
     */
    public Split(List<Point> points, Instant start, Instant end) {
        this.points = new LinkedList<>(); // List type doesn't matter
    }

    /**
     * This distance travelled in this split.
     *
     * @author Jingyi
     */
    public Length distance() {
        return null;
    }

    /**
     * The time taken for this split.
     *
     * @author Jingyi
     */
    public Instant time() {
        return null;
    }

    /**
     * Calculate time spent on pause.
     *
     * @param max the max speed that determines whether someone is paused.
     * @author Brandon
     */
    public Instant timePaused(Speed max) {
        return null;
    }

    /**
     * Calculate the average speed from the total distance and total time taken.
     *
     * @param autopause determines whether paused time is taken into account.
     * @author Brandon
     */
    public Speed speed(boolean autopause) {
        return null;
    }

    public Stream<Point> stream() {
        return this.points.stream();
    }

    /**
     * A string with a few debug information about this split.
     */
    @Override
    public String toString() {
        return "Split{distance=" + distance() +
                ", time=" + time() +
                ", speed=" + speed(false) +
                ", speedAutopause=" + speed(true) +
                "}\n" + this.points;
    }
}

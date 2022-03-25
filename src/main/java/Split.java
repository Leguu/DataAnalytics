import io.jenetics.jpx.Length;
import io.jenetics.jpx.Point;
import io.jenetics.jpx.Speed;
import io.jenetics.jpx.geom.Geoid;

import java.time.Instant;
import java.util.ArrayList;
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
    public Split(List<Point> givenPoints, Instant start, Instant end) {
        this.points = new LinkedList<>();
        var startInSecond = start.getEpochSecond();
        var endInSecond = end.getEpochSecond();
        for (var point : givenPoints) {
            var pointSecond = point.getInstant().get().getEpochSecond();
            if (startInSecond <= pointSecond && pointSecond <= endInSecond) {
                // it's in the range
                this.points.add(point);
            }
        }
    }

    /**
     * This distance travelled in this split.
     *
     * @author Jingyi
     */
    public Length distance() {
        double totalDistance = 0;
        for (int i = 0; i + 1 < points.size(); i++) {
            var start = points.get(i);
            var end = points.get(i + 1);
            var distance = start.distance(end);
            totalDistance += distance.doubleValue();
        }
        return Length.of(totalDistance, Length.Unit.METER);
    }

    /**
     * The time taken for this split.
     *
     * @author Jingyi
     */
    public Instant time() {
        var start = this.points.get(0).getInstant().get().getEpochSecond();
        var end = this.points.get(points.size() - 1).getInstant().get().getEpochSecond();
        return Instant.ofEpochSecond(end - start);
    }

    /**
     * Calculate time spent on pause.
     *
     * @param max the max speed that determines whether someone is paused.
     * @author Brandon
     */
    public Instant timePaused(Speed max) {
        long pausedTime = 0;

        for (int i = 0; i < points.size() - 1; i++) {
            var first = points.get(i);
            var second = points.get(i + 1);
            var time = second.getInstant().get().getEpochSecond() - first.getInstant().get().getEpochSecond();

            var currentSpeed = first.distance(second).longValue() / time;

            if (currentSpeed < max.longValue()) {
                pausedTime += time;
            }
        }

        return Instant.ofEpochSecond(pausedTime);
    }

    /**
     * Calculate the average speed from the total distance and total time taken.
     *
     * @param autopause determines whether paused time is taken into account.
     * @author Brandon
     */
    public Speed speed(boolean autopause) {
        if (autopause) {
            var time = time().getEpochSecond() - timePaused(Speed.of(5, Speed.Unit.KILOMETERS_PER_HOUR)).getEpochSecond();
            return Speed.of(distance().doubleValue() / time, Speed.Unit.METERS_PER_SECOND);
        } else {
            return Speed.of(distance().doubleValue() / time().getEpochSecond(), Speed.Unit.METERS_PER_SECOND);
        }
    }

    public Stream<Point> stream() {
        return this.points.stream();
    }

    @Override
    protected Object clone() {
        return new Split(new ArrayList<>(points));
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

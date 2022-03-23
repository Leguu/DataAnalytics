import io.jenetics.jpx.*;
import io.jenetics.jpx.Speed.Unit;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A workout that contains all points of one workout.
 * When processing, all points will be flattened into one track in the GPX.
 */
public class Workout {
    /**
     * A single Split that contains all points of a workout.
     */
    private final Split points;

    /**
     * Load a workout from a GPX file.
     *
     * @throws IOException if the GPX object can't be read
     */
    public Workout(String filename) throws IOException {
        var gpx = GPX.read(filename);
        var tracks = gpx.tracks();
        this.points = new Split(tracks
                .flatMap(Track::segments)
                .flatMap(TrackSegment::points)
                .collect(Collectors.toList()));
        Logger.getGlobal().info("Successfully loaded workout from file " + filename);
    }

    public Workout(Split points) {
        this.points = points;
    }

    /**
     * Exports this workout to a new GPX.
     */
    public void export(String filename) throws IOException {
        var gpx = GPX.builder()
                .addTrack(track -> track.addSegment(segment -> {
                    for (var point : points.points) {
                        segment.addPoint(WayPoint.of(point));
                    }
                }))
                .build();
        GPX.writer("\t").write(gpx, Path.of(filename));
        Logger.getGlobal().info("Successfully exported workout to file " + filename);
    }

    /**
     * Makes sure that there are no outliers in altitude data.
     * If altitude speed is greater than max, then it is reduced
     * to fit constraints.
     *
     * @param max the upper constraint to vertical speed
     * @return A Workout with the altitude adjusted.
     * @author Tamzid
     */
    public Workout correctAltitude(Speed max) {
        var newSplit = (Split) points.clone();
        for (int i = 0; i < newSplit.points.size() - 1; i++) {
            var current = points.points.get(i);
            var next = points.points.get(i + 1);

            var currentElevation = current.getElevation().get().doubleValue();
            var nextElevation = next.getElevation().get().doubleValue();

            var deltaElevation = nextElevation - currentElevation;
            var deltaTime = next.getInstant().get().getEpochSecond() - next.getInstant().get().getEpochSecond();
            var speed = Math.abs(deltaElevation) / deltaTime;

            if (speed > max.doubleValue()) {
                double boundElevation;
                if (deltaElevation < 0) {
                    boundElevation = current.getElevation().get().doubleValue() - max.doubleValue();
                } else if (deltaElevation > 0) {
                    boundElevation = current.getElevation().get().doubleValue() + max.doubleValue();
                } else {
                    throw new RuntimeException("Change in elevation is equal to zero, but it shouldn't be. Did you set the maximum speed to 0?");
                }
                var newPoint = WayPoint.builder()
                        .lat(next.getLatitude())
                        .lon(next.getLongitude())
                        .ele(boundElevation)
                        .time(next.getInstant().get())
                        .build();
                newSplit.points.set(i + 1, newPoint);
            }
        }
        return new Workout(newSplit);
    }

    /**
     * Split this workout into splits of a given interval.
     * For example, splitting a 10-minute workout into
     * 2 minute splits will give a list of 5 splits.
     *
     * @param interval 2 minute
     * @author Quan
     */
    public List<Split> split(Instant interval) {
        var newSplits = new ArrayList<Split>();
        var totalTimeInSeconds = this.points.time().getEpochSecond();
        var startTimeInSecond = this.points.points.get(0).getInstant().get().getEpochSecond();
        var intervalEpochSecond = interval.getEpochSecond();
        var numberOfNewSplits = (totalTimeInSeconds / intervalEpochSecond) + 1;
        for (int i = 0; i < numberOfNewSplits; i++) {
            var splitStartTimeInSecond = startTimeInSecond + i * intervalEpochSecond;
            var splitEndTimeInSecond = startTimeInSecond + (i + 1) * intervalEpochSecond;
            newSplits.add(new Split(
                    this.points.points,
                    Instant.ofEpochSecond(splitStartTimeInSecond),
                    Instant.ofEpochSecond(splitEndTimeInSecond))
            );
        }
        return newSplits;
    }

    /**
     * Give the top speed for splits of a certain interval.
     *
     * @param autopause whether to account for the user standing still
     * @param interval  the length of splits
     * @author Jingyi
     */
    public Speed topSpeed(Instant interval, boolean autopause) {
        var splits = split(interval);
        double max = 0;
        for (var split : splits) {
            var speed = split.speed(autopause).doubleValue();
            if (speed > max)
                max = speed;
        }
        return Speed.of(max, Unit.METERS_PER_SECOND);
    }

    public Split getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return points.toString();
    }
}

import io.jenetics.jpx.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
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
        return null;
    }

    /**
     * Split this workout into splits of a given interval.
     * For example, splitting a 10-minute workout into
     * 2 minute splits will give a list of 5 splits.
     *
     * @author Quan
     */
    public List<Split> split(Instant interval) {
        return null;
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
        return null;
    }

    @Override
    public String toString() {
        return points.toString();
    }
}

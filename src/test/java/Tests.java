import io.jenetics.jpx.Point;
import io.jenetics.jpx.Speed;
import io.jenetics.jpx.WayPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Tests {
    private final Workout workout;
    private final Workout paused;
    private final Workout altitude;

    public Tests() {
        // A normal workout where the user moves approximately 157 kilometers in the span of 60 seconds.
        workout = new Workout(new Split(Arrays.asList(
                WayPoint.of(1, 1, 0),
                WayPoint.of(2, 2, 60 * 1000)
        )));
        // A normal workout where the user moves approximately 157 kilometers, but also stands still for 60 seconds.
        paused = new Workout(new Split(Arrays.asList(
                WayPoint.of(1, 1, 0),
                WayPoint.of(1, 1, 60 * 1000),
                WayPoint.of(2, 2, 120 * 1000)
        )));
        // Altitude correction. The user cannot move 100 m/s upwards!
        altitude = new Workout(new Split(Arrays.asList(
                WayPoint.of(1, 1, 0, 0),
                WayPoint.of(1, 1, 100, 60 * 1000),
                WayPoint.of(1, 1, 0, 120 * 1000),
                WayPoint.of(1, 1, 0, 180 * 1000)
        )));
    }

    @Test
    void splits() {
        var splits = altitude.split(Instant.ofEpochSecond(60));
        // Make sure there are 4 splits when dividing by 60 seconds.
        assertEquals(4, splits.size());
    }

    @Test
    void splitConstructor() {
        var split = new Split(altitude.getPoints().points, Instant.ofEpochSecond(1), Instant.ofEpochSecond(61));
        assertEquals(split.points.size(), 1);
        assertEquals(split.points.get(0).getElevation().get().intValue(), 100);
    }

    @Test
    void distance() {
        // In meters
        var totalDistance = workout.getPoints().distance().doubleValue();
        // Checks whether totalDistance is within 157500 +- 1000
        assertEquals(157500, totalDistance, 1000);
    }

    @Test
    void speed() {
        // In m/s
        var speed = workout.getPoints().speed(false).doubleValue();
        assertEquals(157000.0 / 60, speed, 1000.0 / 60);

        // The autopause speed should be exactly the same!
        var speedAutopause = workout.getPoints().speed(true).doubleValue();
        assertEquals(speed, speedAutopause);
    }

    /**
     * Assert that our speed stays the same if we're standing still.
     */
    @Test
    void speedAutopause() {
        var speedAutopause = paused.getPoints().speed(true).doubleValue();
        assertEquals(157000.0 / 60, speedAutopause, 1000.0 / 60);
    }

    @Test
    void time() {
        var time = workout.getPoints().time().toEpochMilli();
        assertEquals(time, 60 * 1000);
    }

    @Test
    void timePaused() {
        var time = paused.getPoints().timePaused(Speed.of(5, Speed.Unit.METERS_PER_SECOND))
                .toEpochMilli();
        // The user should have stayed in the same spot for 60 seconds.
        assertEquals(time, 60 * 1000);
    }

    @Test
    void topSpeed() {
        var topSpeed = workout.topSpeed(Instant.ofEpochMilli(60 * 1000), false)
                .doubleValue();
        assertEquals(157000.0 / 60, topSpeed, 1000);
    }

    @Test
    void altitudeCorrection() {
        var corrected = altitude.correctAltitude(Speed.of(1, Speed.Unit.METERS_PER_SECOND));
        // Since the user moved more than 10 m/s in the second point, we expect the algorithm
        // to constrain the elevation to a reasonable 10 m/s again.
        assertEquals(
                1,
                corrected.getPoints()
                        .points
                        .get(1)
                        .getElevation()
                        .get()
                        .doubleValue(),
                5
        );
    }
}

import java.io.IOException;

public class Driver {
    public static void main(String[] args) throws IOException {
        var workout1 = new Workout("./assets/Workout-2021-06-06-11-57-42.gpx");
        System.out.println(workout1);
    }
}

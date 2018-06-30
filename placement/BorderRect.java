package sim.app.flockers.placement;

import sim.app.flockers.Flocker;
import sim.app.flockers.Flockers;
import sim.util.Double2D;

import java.util.List;

/**
 * Created by danfu on 11/25/17.
 */
public class BorderRect extends Placement {
    private double origin_x;
    private double origin_y;
    private double r;

    // assuming the grid is a square
    public BorderRect(PlacementParams params) {
        this.params = params;
        this.origin_x = params.placementOriginX;
        this.origin_y = params.placementOriginY;
        this.r = params.placementRadius;
    }

    // place the adhoc agents
    public void setup(List<Flocker> boids) {
        int numBirdsPerSide = (int) Math.round(params.numAdhoc / 4);
        int leftoverBirds = params.numAdhoc - numBirdsPerSide * 4;

        int birdsPerSide[] = {numBirdsPerSide, numBirdsPerSide, numBirdsPerSide, numBirdsPerSide};

        for (int i = 0; i < leftoverBirds; i++) {
            birdsPerSide[i] += 1;
        }

        for (int i = 0; i < 4; i++) {
            double birdCount = birdsPerSide[i];
            double inc = 2 * r / (birdCount + 1);
            if (i == 0) {
                for (int x = 1; x <= birdCount; x++) {
                    double x_val = origin_x - r + x * inc;
                    double y_val = origin_y - r;


                    Double2D location = new Double2D(x_val, y_val);
                    addNewAdhocFlocker(location, boids);
                }
            } else if (i == 1) {
                for (int x = 1; x <= birdCount; x++) {
                    double x_val = origin_x - r + x * inc;
                    double y_val = origin_y + r;

                    Double2D location = new Double2D(x_val, y_val);
                    addNewAdhocFlocker(location, boids);
                }
            } else if (i == 2) {
                for (int x = 1; x <= birdCount; x++) {
                    double x_val = origin_x + r;
                    double y_val = origin_y + r - x * inc;


                    Double2D location = new Double2D(x_val, y_val);
                    addNewAdhocFlocker(location, boids);
                }
            } else {
                for (int x = 1; x <= birdCount; x++) {
                    double x_val = origin_x - r;
                    double y_val = origin_y + r - x * inc;

                    Double2D location = new Double2D(x_val, y_val);
                    addNewAdhocFlocker(location, boids);
                }
            }
        }
    }
}

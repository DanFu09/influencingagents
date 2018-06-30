package sim.app.flockers.placement;

import sim.app.flockers.Flocker;
import sim.util.Double2D;

import java.util.List;

/**
 * Created by danfu on 11/25/17.
 */
public class GridCircle extends Placement {
    private double origin_x;
    private double origin_y;
    private double r;

    public GridCircle(PlacementParams params) {
        this.params = params;
        this.origin_x = params.placementOriginX;
        this.origin_y = params.placementOriginY;
        this.r = params.placementRadius;
    }

    // place the adhoc agents
    public void setup(List<Flocker> boids) {

        int alpha = 2;
        int b = (int) Math.round(alpha * Math.sqrt(params.numAdhoc));
        double phi = (Math.sqrt(5) + 1) / 2;
        double r_val = 0;

        for (int i = 0; i < params.numAdhoc; i++) {
            if (i > params.numAdhoc - b) {
                // place on the boundary
                r_val = 1;
            } else {
                r_val = Math.sqrt(i - 1. / 2) / Math.sqrt(params.numAdhoc - (b + 1) / 2);
            }

            double theta = 2 * Math.PI * i / (phi * phi);
            double x_val = origin_x - r_val + r_val * Math.cos(theta) * r;
            double y_val = origin_y - r_val + r_val * Math.sin(theta) * r;

            Double2D location = new Double2D(x_val, y_val);
            addNewAdhocFlocker(location, boids);
        }
    }
}

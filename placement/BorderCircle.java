package sim.app.flockers.placement;

import sim.app.flockers.Flocker;
import sim.app.flockers.Flockers;
import sim.util.Double2D;

import java.util.List;

/**
 * Created by danfu on 11/25/17.
 */
public class BorderCircle extends Placement {
    private double origin_x;
    private double origin_y;
    private double r;

    public BorderCircle(PlacementParams params) {
        this.params = params;
        this.origin_x = params.placementOriginX;
        this.origin_y = params.placementOriginY;
        this.r = params.placementRadius;
    }

    // place the adhoc agents
    public void setup(List<Flocker> boids) {
        double ang = Math.PI * 2 / params.numAdhoc;
        for (int x = 0; x < params.numAdhoc; x++) {
            // distribute the agents randomly within a circle

            double angle = (x + 1) * ang;
            double x_val = Math.cos(angle) * r;
            double y_val = Math.sin(angle) * r;
            double widthRange = origin_x + x_val;
            double heightRange = origin_y + y_val;

            Double2D location = new Double2D(widthRange, heightRange);
            addNewAdhocFlocker(location, boids);
        }
    }
}

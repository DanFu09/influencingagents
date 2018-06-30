package sim.app.flockers.placement;

import sim.app.flockers.Flocker;
import sim.app.flockers.Flockers;
import sim.util.Double2D;

import java.util.List;

/**
 * Created by danfu on 11/25/17.
 */
public class RandomCircle extends Placement {
    private double origin_x;
    private double origin_y;
    private double r;

    public RandomCircle(PlacementParams params) {
        this.params = params;
        this.origin_x = params.placementOriginX;
        this.origin_y = params.placementOriginY;
        this.r = params.placementRadius;
    }

    // place the adhoc agents
    public void setup(List<Flocker> boids) {
        for (int x = 0; x < params.numAdhoc; x++) {
            // distribute the agents randomly within a circle
            double t = params.state.random.nextDouble() * 2.0 * Math.PI;
            double radius = r * Math.sqrt(params.state.random.nextDouble());
            double widthRange = origin_x + radius * Math.cos(t);
            double heightRange = origin_y + radius * Math.sin(t);

            Double2D location = new Double2D(widthRange, heightRange);
            addNewAdhocFlocker(location, boids);
        }
    }
}

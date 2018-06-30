package sim.app.flockers.placement;

import sim.app.flockers.Flocker;
import sim.util.Double2D;

import java.util.List;

/**
 * Created by danfu on 11/25/17.
 */
public class RandomRect extends Placement {
    private double origin_x;
    private double origin_y;
    private double rx;
    private double ry;

    public RandomRect(PlacementParams params) {
        this.params = params;
        this.origin_x = params.placementOriginX;
        this.origin_y = params.placementOriginY;
        this.rx = params.placementRadius;
        this.ry = params.placementRadius;
    }

    // place the adhoc agents
    public void setup(List<Flocker> boids) {
        for (int x = 0; x < params.numAdhoc; x++) {
            // distribute the agents randomly within a rectangle
            double width = origin_x - rx + params.state.random.nextDouble() * rx * 2;
            double height = origin_y - ry + params.state.random.nextDouble() * ry * 2;
            Double2D location = new Double2D(width, height);
            addNewAdhocFlocker(location, boids);
        }
    }
}

package sim.app.flockers.placement;

import sim.app.flockers.Flocker;
import sim.app.flockers.Flockers;
import sim.util.Double2D;

import java.util.List;

/**
 * Created by danfu on 11/25/17.
 */
public class GridRect extends Placement {
    private double origin_x;
    private double origin_y;
    private double r;

    public GridRect(PlacementParams params) {
        this.params = params;
        this.origin_x = params.placementOriginX;
        this.origin_y = params.placementOriginY;
        this.r = params.placementRadius;
    }

    // place the adhoc agents
    public void setup(List<Flocker> boids) {

        // number of agents you have to place per side
        int side = (int) Math.round(Math.sqrt(params.numAdhoc));

        // total number of grid agents
        int square = (int) Math.pow(side, 2);

        double inc = 2 * r / side;
        int leftOver = params.numAdhoc - square;

        for (int i = 0; i < side; i++) {
            for (int j = 0; j < side; j++) {

                double x_val = origin_x - r + inc * i;
                double y_val = origin_y - r + inc * j;

                Double2D location = new Double2D(x_val, y_val);
                addNewAdhocFlocker(location, boids);
            }
        }

        // for the leftover birds
        for (int i = 0; i < leftOver; i++) {
            double w = origin_x - r + params.state.random.nextDouble() * r * 2;
            double h = origin_y - r + params.state.random.nextDouble() * r * 2;

            Double2D location = new Double2D(w, h);
            addNewAdhocFlocker(location, boids);
        }
    }
}

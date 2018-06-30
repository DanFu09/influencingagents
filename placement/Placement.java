package sim.app.flockers.placement;

import sim.app.flockers.behavior.Behavior;
import sim.app.flockers.Flocker;
import sim.util.Double2D;

import java.util.List;

/**************************************************************************
 * PLACEMENT CODE
 *************************************************************************/

public abstract class Placement {
    public enum PlacementFlag {
        RANDOM_CIRCLE,
        GRID_CIRCLE,
        BORDER_CIRCLE,
        RANDOM_RECT,
        GRID_RECT,
        BORDER_RECT,
        K_MEANS,
        UNKNOWN
    }

    public PlacementParams params;
    public void addNewAdhocFlocker(Double2D location, List<Flocker> boids) {
        Flocker flocker = new Flocker(location);
        flocker.goalDirection = params.goalDirection;
        params.flockers.setObjectLocation(flocker, location);
        flocker.flockers = params.flockers;
        flocker.adhoc = true;
        flocker.localBehavior = params.localBehavior;
        flocker.globalBehavior = params.globalBehavior;
        if (flocker.globalBehavior == Behavior.GlobalBehavior.CIRCLE ||
            flocker.globalBehavior == Behavior.GlobalBehavior.POLYGON) {
            flocker.origin = new Double2D(params.placementOriginX, params.placementOriginY);
            flocker.center_origin = new Double2D(params.placementOriginX, params.placementOriginY);
        }
        if (flocker.globalBehavior == Behavior.GlobalBehavior.POLYGON) {
            flocker.polygonSides = params.polygonSides;
            flocker.polygonRadius = params.placementRadius;
        }
        if (flocker.globalBehavior == Behavior.GlobalBehavior.MULTILARGE ||
                flocker.globalBehavior == Behavior.GlobalBehavior.MULTILARGEFIXED ||
                flocker.globalBehavior == Behavior.GlobalBehavior.MULTILARGELOCAL) {
            flocker.multiLargeParams = params.multiLargeParams;
            flocker.geneticParams = params.multiLargeParams.behavior1Genetics;
        }
        if (flocker.globalBehavior == Behavior.GlobalBehavior.MULTIHERD) {
            flocker.multiHerdParams = new Behavior.MultiHerdParams(params.multiHerdParams);
            flocker.origin = new Double2D(params.placementOriginX, params.placementOriginY);
            flocker.center_origin = new Double2D(params.placementOriginX, params.placementOriginY);
            flocker.polygonRadius = params.placementRadius;
        }
        if (flocker.localBehavior == Behavior.LocalBehavior.GENETIC) {
            flocker.geneticParams = params.geneticParams;
        }

        flocker.theFlock = params.state;
        boids.add(flocker);
    }
    public abstract void setup(List<Flocker> boids);

}

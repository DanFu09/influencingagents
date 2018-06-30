package sim.app.flockers.placement;

import sim.app.flockers.behavior.Behavior;
import sim.app.flockers.Flockers;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 * Params to pass to a Placement
 */
public class PlacementParams {

    public Continuous2D flockers;
    public Flockers state;
    public int numAdhoc;
    public Double2D goalDirection;
    public Behavior.LocalBehavior localBehavior;
    public Behavior.GlobalBehavior globalBehavior;
    public double placementOriginX;
    public double placementOriginY;
    public int polygonSides;
    public double placementRadius;
    public Behavior.MultiLargeParams multiLargeParams;
    public Behavior.MultiHerdParams multiHerdParams;
    public Behavior.GeneticLocalBehaviorParams geneticParams;

    public PlacementParams(Continuous2D flockers,
                           Flockers state,
                           int numAdhoc,
                           Double2D goalDirection,
                           Behavior.LocalBehavior localBehavior,
                           Behavior.GlobalBehavior globalBehavior,
                           double placementOriginX,
                           double placementOriginY,
                           int polygonSides,
                           double placementRadius,
                           Behavior.MultiLargeParams multiLargeParams,
                           Behavior.MultiHerdParams multiHerdParams,
                           Behavior.GeneticLocalBehaviorParams geneticParams) {
        this.flockers = flockers;
        this.state = state;
        this.numAdhoc = numAdhoc;
        this.goalDirection = goalDirection;
        this.localBehavior = localBehavior;
        this.globalBehavior = globalBehavior;
        this.placementOriginX = placementOriginX;
        this.placementOriginY = placementOriginY;
        this.polygonSides = polygonSides;
        this.placementRadius = placementRadius;
        this.multiLargeParams = multiLargeParams;
        this.multiHerdParams = multiHerdParams;
        this.geneticParams = geneticParams;
    }
}

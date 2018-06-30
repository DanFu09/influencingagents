package sim.app.flockers.behavior;

import sim.util.Double2D;

import java.util.List;

public interface GeneticLocalBehavior {

    /**
     * Calculates a new direction from a list of neighbors, my direction, and the goal direction.
     *
     * @param neighbors An unsorted list of neighbor boids; positions are relative, but directions are absolute.
     * @param myDirection The direction this influencing agent is facing.
     * @param goal The goal direction.
     * @param neighborhoodSize The neighborhood size of this influencing agent.
     *
     * @return New direction to travel in.
     */
    Double2D calcNewDirection(List<Behavior.NeighborBoid> neighbors, Double2D myDirection, Double2D goal,
                              double neighborhoodSize);

}

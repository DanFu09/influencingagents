package sim.app.flockers.behavior;

import sim.util.Double2D;

/**
 * Class to store behaviors and behavior params
 */
public class Behavior {

    public enum GlobalBehavior {
        FACE,
        RANDOM,
        CIRCLE,
        POLYGON,
        MULTILARGE,
        MULTILARGEFIXED,
        MULTILARGELOCAL,
        MULTIHERD,
        UNKNOWN
    }

    public enum LocalBehavior {
        FACE_EAST,
        OFFSET_MOMENTUM,
        ONE_STEP_LOOKAHEAD,
        TWO_STEP_LOOKAHEAD,
        COORDINATED,
        MIN_AVG_DIR_NEIGH,
        MIN_AVG_DIR_FLOCK,
        COUZIN,
        GENETIC,
        UNKNOWN
    }

    public static class MultiLargeParams {
        public int switchingPoint;
        public LocalBehavior behavior1;
        public LocalBehavior behavior2;
        public Double2D direction;
        public int stageNum;
        public GeneticLocalBehaviorParams behavior1Genetics;
        public GeneticLocalBehaviorParams behavior2Genetics;

        public MultiLargeParams() {}
    }

    public static class MultiHerdParams {
        public GlobalBehavior initialBehavior;
        public int initialBehaviorSides;
        public LocalBehavior followBehavior;
        public double stopFollowingPoint;
        public GlobalBehavior finalBehavior;
        public double finalBehaviorRadius;
        public int finalBehaviorSides;
        public int stageNum;

        public MultiHerdParams() {}

        public MultiHerdParams(MultiHerdParams other) {
            this.initialBehavior = other.initialBehavior;
            this.initialBehaviorSides = other.initialBehaviorSides;
            this.followBehavior = other.followBehavior;
            this.stopFollowingPoint = other.stopFollowingPoint;
            this.finalBehavior = other.finalBehavior;
            this.finalBehaviorRadius = other.finalBehaviorRadius;
            this.finalBehaviorSides = other.finalBehaviorSides;
            this.stageNum = other.stageNum;
        }
    }

    public static class GeneticLocalBehaviorParams {
        public String genomeFile;
        public GeneticLocalBehavior localBehavior;

        public GeneticLocalBehaviorParams() {}
    }

    /** A helper class to contain information about neighbor boids **/
    public static class NeighborBoid {
        public Double2D relativePosition; // position of neighbor boid relative to me
        public Double2D direction; // direction of neighbor boid

        public NeighborBoid(Double2D relativePosition, Double2D direction) {
            this.relativePosition = relativePosition;
            this.direction = direction;
        }
    }
}

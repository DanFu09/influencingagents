package sim.app.flockers;

import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;

import java.util.*;

public class Metrics {
    /**************************************************************************
     * METRICS CODE
     *************************************************************************/

    // Get the squared distances between all boids
    public static double[][] calcSquaredDistances(ArrayList<Flocker> boids, Continuous2D flockers, int numFlockers,
                                           boolean toroidal) {
        double[][] distances = new double[numFlockers][numFlockers];

        for (int i = 0; i < numFlockers; i++) {
            for (int j = i; j < numFlockers; j++) {
                if (j == i) {
                    distances[i][j] = distances[j][i] = 0;
                } else {
                    Double2D d1 = boids.get(i).loc;
                    Double2D d2 = boids.get(j).loc;
                    double dist = toroidal ?
                        flockers.tds(d1, d2) :
                        ((d1.x - d2.x) * (d1.x - d2.x) + (d1.y - d2.y) * (d1.y - d2.y));
                    distances[i][j] = distances[j][i] = dist;
                }
            }
        }

        return distances;
    }

    // Get the angles in degrees between all boids
    public static double[][] calcAngles(ArrayList<Flocker> boids, int numFlockers) {
        double[][] angles = new double[numFlockers][numFlockers];

        for (int i = 0; i < numFlockers; i++) {
            for (int j = i; j < numFlockers; j++) {
                if (j == i) {
                    angles[i][j] = angles[j][i] = 0;
                } else {
                    Double2D v1 = boids.get(i).lastd;
                    Double2D v2 = boids.get(j).lastd;
                    angles[i][j] = angles[j][i] = Helpers.calcAngle(v1, v2);
                }
            }
        }

        return angles;
    }

    // Are two boids with this distance and angle difference connected?
    // Only true if they influence each other and are within five degrees of each other
    // sqDistance is squared distance, angleDiff is in radians
    public static boolean flockConnected(double neighborhood, double sqDistance, double angleDiff) {
        return sqDistance <= neighborhood * neighborhood && Math.abs(angleDiff) < Math.PI * 5 / 180;
    }

    // Return a list of all flocks - number of total agents, and number of adhoc agents
    private static List<Int2D> flockHelper(Flockers theFlock) {
        int[] flockNum = theFlock.calcFlocks();

        ArrayList<Int2D> flockList = new ArrayList<Int2D>();
        Set<Integer> flocksSeen = new HashSet<Integer>();
        for (int i = 0; i < theFlock.numFlockers; i++) {
            int num = flockNum[i];
            if (!flocksSeen.contains(num)) {
                flocksSeen.add(num);
                int flockSize = 0;
                int numAdhoc = 0;
                for (int j = i; j < theFlock.numFlockers; j++) {
                    if (flockNum[j] == num) {
                        flockSize++;
                        if (theFlock.boids.get(j).isAdhoc()) {
                            numAdhoc++;
                        }
                    }
                }
                flockList.add(new Int2D(flockSize, numAdhoc));
            }
        }

        return flockList;
    }

    // Get the number of agents in this agent's local flock, but only including agents within senseRadius
    // The local flock is defined as all the agents that are path connected to each other and are within
    // five degrees of each other
    public static int localFlockSize(Continuous2D flockers, Flockers theFlock, Flocker flocker,
                                     double neighborhoodRadius, double senseRadius, boolean includeAdhoc) {
        Bag neighbors = flockers.getNeighborsExactlyWithinDistance(flocker.loc, senseRadius, theFlock.toroidal);
        ArrayList<Flocker> boids = new ArrayList<>();
        for (Object f : neighbors) {
            boids.add((Flocker) f);
        }
        int numFlockers = boids.size();
        double[][] sqDistances = calcSquaredDistances(boids, flockers, numFlockers, theFlock.toroidal);
        double[][] angles = calcAngles(boids, numFlockers);

        int[] flockNum = new int[numFlockers];
        for (int i = 0; i < numFlockers; i++) {
            flockNum[i] = 0;
        }
        Stack<Integer> flockerStack = new Stack<Integer>();
        int flockerFlockNum = 0;

        // Go through all the birds, assigning them to groups based on membership, keeping track of
        // groups that are larger than minSize
        int numGroups = 0; // number of groups total
        for (int i = 0; i < numFlockers; i++) {
            if (flockNum[i] != 0) { // seen already
                continue;
            } else {
                numGroups++; // found new group
                flockerStack.add(i);
                // add every boid that can be touched to this group
                while (!flockerStack.isEmpty()) {
                    int curBoid = flockerStack.pop();
                    if (flockNum[curBoid] != 0) {
                        continue;
                    }
                    flockNum[curBoid] = numGroups;
                    if (flocker == boids.get(curBoid)) {
                        flockerFlockNum = numGroups;
                    }
                    for (int j = 0; j < numFlockers; j++) {
                        // only add new boids to a group if they are flock connected
                        if (flockNum[j] == 0 &&
                                Metrics.flockConnected(neighborhoodRadius, sqDistances[curBoid][j],
                                        angles[curBoid][j])) {
                            flockerStack.push(j);
                        }
                    }
                }
            }
        }

        int localFlockSize = 0;
        int localFlockNumAdhoc = 0;
        // Get the size of flocker's local flock
        for (int i = 0; i < numFlockers; i++) {
            if (flockNum[i] == flockerFlockNum) {
                localFlockSize++;
                if (boids.get(i).isAdhoc()) {
                    localFlockNumAdhoc++;
                }
            }
        }

        return localFlockSize - (includeAdhoc ? 0 : localFlockNumAdhoc);
    }

    // Get the number of flocks that have at least minSize members
    // A flock is defined as a group of boids that have a chain of influence between any two members,
    //   and where all the boids are within two degrees of each other
    // Only return flocks of size between minSize and maxSize, inclusive
    // If adhocRequired is true, only count flocks that have an ad hoc agent
    public static int numFlocks(Flockers theFlock, int minSize, int maxSize, boolean adhocRequired) {
        List<Int2D> flockList = flockHelper(theFlock);
        Iterator<Int2D> listIterator = flockList.iterator();
        int numFlocks = 0;
        while (listIterator.hasNext()) {
            Int2D curFlock = listIterator.next();
            int flockSize = curFlock.x;
            int numAdhoc = curFlock.y;
            if (flockSize >= minSize && flockSize <= maxSize && numAdhoc != flockSize &&
                (!adhocRequired || numAdhoc > 0)) {
                numFlocks++;
            }
        }
        return numFlocks;
    }

    public static int numLoners(Flockers theFlock) {
        return numFlocks(theFlock, 1, 1, false);
    }

    // Count the number of boids in flocks of size between minSize and maxSize, inclusive
    // A flock is defined as a group of boids that have a chain of influence between any two members,
    //   and where all the boids are within two degrees of each other
    // If includeAdhoc is true, include ad hoc agents in the count
    // If adhocRequired is true, only count flocks that have an ad hoc agent
    public static int numBoidsInFlocks(Flockers theFlock, int minSize, int maxSize, boolean includeAdhoc,
                                boolean adhocRequired) {
        List<Int2D> flockList = flockHelper(theFlock);
        Iterator<Int2D> listIterator = flockList.iterator();
        int numBoids = 0;
        while (listIterator.hasNext()) {
            Int2D curFlock = listIterator.next();
            int flockSize = curFlock.x;
            int numAdhoc = curFlock.y;
            if (flockSize >= minSize && flockSize <= maxSize && (!adhocRequired || numAdhoc > 0)) {
                numBoids += includeAdhoc ? flockSize : flockSize - numAdhoc;
            }
        }
        return numBoids;
    }

    // Calculate the number of boids facing the same direction, not including
    // ad hoc agents
    public static int maxNumSameDirection(ArrayList<Flocker> boids, int numFlockers) {
        HashMap<Integer, Integer> dirs = new HashMap<Integer, Integer>();

        for (int i = 0; i < numFlockers; i++) {
            if (!boids.get(i).isAdhoc()) {
                double angle = boids.get(i).lastd.angle() / Math.PI * 180;
                if (angle < 0) {
                    angle += 360;
                }
                int angleFloored = ((int) angle) % 360;
                if (!dirs.containsKey(angleFloored)) {
                    dirs.put(angleFloored, 1);
                } else {
                    dirs.put(angleFloored, dirs.get(angleFloored) + 1);
                }
            }
        }

        Integer[] vals = new Integer[0];

        vals = dirs.values().toArray(vals);

        if (vals.length == 0) {
            return 0;
        }
        Arrays.sort(vals);

        return (vals[vals.length - 1]);
    }

    // Calculate the number of boids on screen
    public static int numOnScreen(ArrayList<Flocker> boids, double height, int numFlockers, double width) {
        int alive = 0;
        for (int i = 0; i < numFlockers; i++) {
            Double2D loc = boids.get(i).loc;
            if (loc.x <= width && loc.x >= 0 && loc.y <= height && loc.y >= 0 &&
                !boids.get(i).isAdhoc()) {
                alive += 1;
            }
        }
        return alive;
    }

    // The ad hoc agents form an implicit bounding box, which can be a proxy for an "area they control" in some cases
    // Calculates the number of boids in the bounding box
    public static int numBoidsInAdhocBoundingBox(ArrayList<Flocker> boids, double neighborhood, int numAdhoc,
                                          int numFlockers, boolean includeAdhoc) {
        Double2D max = new Double2D(-1*Double.MAX_VALUE, -1*Double.MAX_VALUE);
        Double2D min = new Double2D(Double.MAX_VALUE, Double.MAX_VALUE);
        for (int i = numFlockers - numAdhoc; i < numFlockers; i++) {
            Flocker boid = boids.get(i);
            if (!boid.isAdhoc()) {
                continue;
            }
            Double2D loc = boid.loc;
            max = new Double2D(Math.max(max.x, loc.x), Math.max(max.y, loc.y));
            min = new Double2D(Math.min(min.x, loc.x), Math.min(min.y, loc.y));
        }

        double margin = 5 * neighborhood;
        max.add(new Double2D(margin, margin));
        min.subtract(new Double2D(margin, margin));

        int numInBoundingBox = 0;
        for (int i = 0; i < numFlockers; i++) {
            Flocker boid = boids.get(i);
            boolean inBox = boid.loc.x < max.x && boid.loc.y < max.y &&
                boid.loc.x > min.x && boid.loc.y > min.y;
            if (inBox && (!boid.isAdhoc() || includeAdhoc)) {
                numInBoundingBox++;
            }
        }

        return numInBoundingBox;
    }

    public static int numBoidsFacingGoalDirection(ArrayList<Flocker> boids, Double2D goalDirection, boolean includeAdhoc) {
        int numFacingGoalDirection = 0;
        double goalAngle = goalDirection.angle() / Math.PI * 360;
        if (goalAngle < 0) {
            goalAngle += 360;
        }
        int goalAngleFloored = ((int) goalAngle) % 360;
        for (int i = 0; i < boids.size(); i++) {
            if (boids.get(i).isAdhoc() && !includeAdhoc) {
                continue;
            }
            double angle = boids.get(i).lastd.angle() / Math.PI * 180;
            if (angle < 0) {
                angle += 360;
            }
            int angleFloored = ((int) angle) % 360;

            if ((angleFloored + 1) % 360 == goalAngleFloored ||
                    (angleFloored - 1) % 360 == goalAngleFloored ||
                    angleFloored == goalAngleFloored) {
                numFacingGoalDirection++;
            }
        }
        return numFacingGoalDirection;
    }

    public static double averageAngleDiffFromGoal(ArrayList<Flocker> boids, Double2D goalDirection, boolean includeAdhoc) {
        double angleDifferenceSum = 0;
        int numBoids = 0;
        for (int i = 0; i < boids.size(); i++) {
            if (boids.get(i).isAdhoc() && !includeAdhoc) {
                continue;
            }
            angleDifferenceSum += Helpers.calcAngle(boids.get(i).lastd, goalDirection) * 180 / Math.PI;
            numBoids++;
        }
        return (numBoids > 0) ? angleDifferenceSum / numBoids : 0;
    }
}

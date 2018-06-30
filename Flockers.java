/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.flockers;
import sim.app.flockers.behavior.Behavior;
import sim.app.flockers.behavior.BehaviorParsing;
import sim.app.flockers.placement.*;
import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;
import java.util.*;
import java.lang.*;


public class Flockers extends SimState implements Steppable {
    private static final long serialVersionUID = 1;

    public Continuous2D flockers;

    /**************************************************************************
     * SETTINGS
     *************************************************************************/

    public double width = 150;
    public double height = 150;
    public int numFlockers = 200;
    public double cohesion = 0.0;
    public double avoidance = 0.0;
    public double randomness = 0.0;
    public double consistency = 1.0;
    public double momentum = 1.0;
    public double deadFlockerProbability = 0.0;
    public double neighborhood = 10;
    public double jump = 0.7;  // how far do we move in a timestep?
    public boolean toroidal = true;
    public int printDelta = 100; // how often to print out an update?
    public int numSteps = 0;
    public int untilNumSameDirection = 0; // run until this many are facing the same direction
    public int untilNumInFlocksWithAdhoc = 0; // run until this many are in flocks with ad hoc agents
    public long curStep;
    public long seed;
    public double radius = 10;
    public double placement_origin_x = width / 2;
    public double placement_origin_y = height / 2;
    public double placement_radius = width / 2;
    public Double2D goalDirection = new Double2D(1, 0);

    // for the coordinated approach, true if two boids are in a pair
    // coordinatedPairs[i][j] is true iff boids.get(i) is in a pair with boids.get(j)
    public boolean[][] coordinatedPairs;
    public ArrayList<Flocker> boids;

    // for the approaches that need to recalculate the flock every time
    public int[] flocks;

    // ad hoc agent parameters
    public int numAdhoc = 0;

    public Placement.PlacementFlag placementFlag = Placement.PlacementFlag.RANDOM_RECT;
    private Placement placement;

    public Behavior.GlobalBehavior globalBehavior = Behavior.GlobalBehavior.FACE;
    public Behavior.LocalBehavior localBehavior = Behavior.LocalBehavior.FACE_EAST;
    public int polygonSides = 10;
    public Behavior.MultiLargeParams multiLargeParams = null;
    public Behavior.MultiHerdParams multiHerdParams = null;
    public Behavior.GeneticLocalBehaviorParams geneticParams = null;

    public enum ExperimentFlag {
        DEFAULT,
        HERD,
        LARGE,
        SMALL
    }
    public ExperimentFlag experimentFlag = ExperimentFlag.DEFAULT;
    private Experiment experiment = new DefaultExperiment(this);
    private ExperimentParams experimentParams = new ExperimentParams();

    // output flags
    public boolean outputNumFlocks = true;
    public boolean outputNumFlocksWithAdhoc = false;
    public boolean outputNumBoidsWithAdhoc = false;
    public boolean outputNumLoners = true;
    public boolean outputNumSameDir = true;
    public boolean outputNumOnScreen = false;
    public boolean outputNumBoidsInBoundingBox = false;
    public boolean outputNumFacingGoalDirection = false;
    public boolean outputAverageAngleDiffFromGoal = false;

    /**************************************************************************
     * GETTERS/SETTERS FOR SETTINGS
     *************************************************************************/

    public double getCohesion() { return cohesion; }
    public void setCohesion(double val) { if (val >= 0.0) cohesion = val; }
    public double getAvoidance() { return avoidance; }
    public void setAvoidance(double val) { if (val >= 0.0) avoidance = val; }
    public double getRandomness() { return randomness; }
    public void setRandomness(double val) { if (val >= 0.0) randomness = val; }
    public double getConsistency() { return consistency; }
    public void setConsistency(double val) { if (val >= 0.0) consistency = val; }
    public double getMomentum() { return momentum; }
    public void setMomentum(double val) { if (val >= 0.0) momentum = val; }
    public int getNumFlockers() { return numFlockers; }
    public void setNumFlockers(int val) { if (val >= 1) numFlockers = val; }
    public double getWidth() { return width; }
    public void setWidth(double val) { if (val > 0) width = val; }
    public double getHeight() { return height; }
    public void setHeight(double val) { if (val > 0) height = val; }
    public double getNeighborhood() { return neighborhood; }
    public void setNeighborhood(double val) { if (val > 0) neighborhood = val; }
    public double getDeadFlockerProbability() { return deadFlockerProbability; }
    public void setDeadFlockerProbability(double val)
    { if (val >= 0.0 && val <= 1.0) deadFlockerProbability = val; }
    public boolean getToroidal() { return toroidal; }
    public void setToroidal(boolean val) { toroidal = val; }
    public double getRadius() { return radius; }
    public void setRadius(double val) {radius = val; }
    public int getNumAdhoc() { return numAdhoc; }
    public void setNumAdhoc(int val) {numAdhoc = val; }
    public int getPrintDelta() { return printDelta; }
    public void setPrintDelta(int val) {printDelta = val; }

    public double getPlacementOriginX() { return placement_origin_x; }
    public void setPlacementOriginX(double val) {placement_origin_x = val; }
    public double getPlacementOriginY() { return placement_origin_y; }
    public void setPlacementOriginY(double val) {placement_origin_y = val; }
    public double getPlacementRadius() { return placement_radius; }
    public void setPlacementRadius(double val) {placement_radius = val; }

    public boolean getOutputNumFlocks() { return outputNumFlocks; }
    public void setOutputNumFlocks(boolean val) { outputNumFlocks = val; }
    public boolean getOutputNumFlocksWithAdhoc() { return outputNumFlocksWithAdhoc; }
    public void setOutputNumFlocksWithAdhoc(boolean val) { outputNumFlocksWithAdhoc = val; }
    public boolean getOutputNumBoidsWithAdhoc() { return outputNumBoidsWithAdhoc; }
    public void setOutputNumBoidsWithAdhoc(boolean val) { outputNumBoidsWithAdhoc = val; }
    public boolean getOutputNumLoners() { return outputNumLoners; }
    public void setOutputNumLoners(boolean val) { outputNumLoners = val; }
    public boolean getOutputNumSameDir() { return outputNumSameDir; }
    public void setOutputNumSameDir(boolean val) { outputNumSameDir = val; }
    public boolean getOutputNumOnScreen() { return outputNumOnScreen; }
    public void setOutputNumOnScreen(boolean val) { outputNumOnScreen = val; }
    public boolean getOutputNumBoidsInBoundingBox() { return outputNumBoidsInBoundingBox; }
    public void setOutputNumBoidsInBoundingBox(boolean val) { outputNumBoidsInBoundingBox = val; }
    public boolean getOutputNumFacingGoalDirection() { return outputNumFacingGoalDirection; }
    public void setOutputNumFacingGoalDirection(boolean val) { outputNumFacingGoalDirection = val; }
    public boolean getOutputAverageAngleDiffFromGoal() { return outputAverageAngleDiffFromGoal; }
    public void setOutputAverageAngleDiffFromGoal(boolean val) { outputAverageAngleDiffFromGoal = val; }

    public String getExperiment() {
        switch (experimentFlag) {
            case DEFAULT: return "DEFAULT";
            case LARGE: return "LARGE";
            case HERD: return "HERD";
            case SMALL: return smallToString(experimentParams);
            default: return "Unknown";
        }
    }
    public void setExperiment(String exp) {
        if (exp.equals("DEFAULT")) {
            experimentFlag = ExperimentFlag.DEFAULT;
        } else if (exp.equals("LARGE")) {
            experimentFlag = ExperimentFlag.LARGE;
        } else if (exp.equals("HERD")) {
            experimentFlag = ExperimentFlag.HERD;
        } else if (parseSmallExperiment(exp) != null) {
            experimentFlag = ExperimentFlag.SMALL;
            experimentParams = parseSmallExperiment(exp);
        }
    }
    public static ExperimentParams parseSmallExperiment(String exp) {
        if (!exp.startsWith("SMALL")) {
            return null;
        } else {
            ExperimentParams params = new ExperimentParams();
            if (exp.endsWith("EAST")) {
                params.initialDirection = new Double2D(1, 0);
            } else if (exp.endsWith("SOUTH")) {
                params.initialDirection = new Double2D(0, 1);
            } else if (exp.endsWith("NORTH")) {
                params.initialDirection = new Double2D(0, -1);
            } else if (exp.endsWith("WEST")) {
                params.initialDirection = new Double2D(-1, 0);
            } else {
                params = null;
            }
            return params;
        }
    }
    public static String smallToString(ExperimentParams params) {
        if (params.initialDirection == null) {
            return null;
        } else if (params.initialDirection.equals(new Double2D(1, 0))) {
            return "SMALL-EAST";
        } else if (params.initialDirection.equals(new Double2D(0, 1))) {
            return "SMALL-SOUTH";
        } else if (params.initialDirection.equals(new Double2D(0, -1))) {
            return "SMALL-NORTH";
        } else if (params.initialDirection.equals(new Double2D(-1, 0))) {
            return "SMALL-WEST";
        } else {
            return null;
        }
    }

    public String getPlacement() {
        return PlacementParsing.getPlacement(placementFlag);
    }
    public void setPlacement(String exp) {
        Placement.PlacementFlag newFlag = PlacementParsing.readPlacement(exp);
        if (newFlag != Placement.PlacementFlag.UNKNOWN) {
            placementFlag = newFlag;
        }
    }

    public String getGlobalBehavior() {
        return BehaviorParsing.globalBehaviorToString(globalBehavior, polygonSides,
                multiLargeParams, multiHerdParams);
    }
    public void setGlobalBehavior(String exp) {
        Behavior.GlobalBehavior parsedBehavior = BehaviorParsing.stringToGlobalBehavior(exp);
        if (parsedBehavior != Behavior.GlobalBehavior.UNKNOWN) {
            globalBehavior = parsedBehavior;
            if (globalBehavior == Behavior.GlobalBehavior.POLYGON) {
                polygonSides = BehaviorParsing.getPolygonSides(exp);
            } else if (globalBehavior == Behavior.GlobalBehavior.MULTILARGE) {
                multiLargeParams = BehaviorParsing.parseMultiLarge(exp);
            } else if (globalBehavior == Behavior.GlobalBehavior.MULTILARGEFIXED) {
                multiLargeParams = BehaviorParsing.parseMultiLargeFixed(exp);
            } else if (globalBehavior == Behavior.GlobalBehavior.MULTILARGELOCAL) {
                multiLargeParams = BehaviorParsing.parseMultiLargeLocal(exp);
            } else if (globalBehavior == Behavior.GlobalBehavior.MULTIHERD) {
                multiHerdParams = BehaviorParsing.parseMultiHerd(exp);
            }
        }
    }

    public String getLocalBehavior() {
        return BehaviorParsing.localBehaviorToString(localBehavior, geneticParams);
    }
    public void setLocalBehavior(String exp) {
        Behavior.LocalBehavior parsedBehavior = BehaviorParsing.stringToLocalBehavior(exp);
        if (parsedBehavior != Behavior.LocalBehavior.UNKNOWN) {
            localBehavior = parsedBehavior;
            if (localBehavior == Behavior.LocalBehavior.GENETIC) {
                geneticParams = BehaviorParsing.parseGenetic(exp);
            }
        }
    }

    /**************************************************************************
     * PUBLIC METHODS
     *************************************************************************/

    public Double2D[] getLocations() {
        if (flockers == null) return new Double2D[0];
        Bag b = flockers.getAllObjects();
        if (b == null) return new Double2D[0];
        Double2D[] locs = new Double2D[b.numObjs];
        for(int i = 0; i < b.numObjs; i++)
            locs[i] = flockers.getObjectLocation(b.objs[i]);
        return locs;
    }

    public Double2D[] getInvertedLocations() {
        if (flockers == null) return new Double2D[0];
        Bag b = flockers.getAllObjects();
        if (b == null) return new Double2D[0];
        Double2D[] locs = new Double2D[b.numObjs];
        for(int i = 0; i < b.numObjs; i++)
        {
            locs[i] = flockers.getObjectLocation(b.objs[i]);
            locs[i] = new Double2D(locs[i].y, locs[i].x);
        }
        return locs;
    }

    // For the coordinated approach, get boid's partner, or null if there is none
    public Flocker getCoordinatedPartner(Flocker boid) {
        for (int i = 0; i < numFlockers; i++) {
            if (boids.get(i) == boid) {
                for (int j = 0; j < numFlockers; j++) {
                    if (coordinatedPairs[i][j]) {
                        return boids.get(j);
                    }
                }
            }
        }

        return null;
    }

    public ArrayList<Flocker> getFlocks(Flocker boid) {
        int boidNum = -1;
        for (int i = 0; i < numFlockers; i++) {
            if (boid == boids.get(i)) {
                boidNum = i;
                break;
            }
        }

        if (boidNum == -1) {
            return new ArrayList<Flocker>();
        }

        ArrayList<Flocker> flock = new ArrayList<Flocker>();
        int flockNum = flocks[boidNum];
        for (int i = 0; i < numFlockers; i++) {
            if (flocks[i] == flockNum) {
                flock.add(boids.get(i));
            }
        }

        return flock;
    }

    /**************************************************************************
     * CLASS SETUP
     *************************************************************************/

    public static boolean keyExists(String key, String[] args) {
        for(int x=0;x<args.length;x++)
            if (args[x].equalsIgnoreCase(key))
                return true;
        return false;
    }

    public static String argumentForKey(String key, String[] args) {
        for(int x=0;x<args.length-1;x++)  // if a key has an argument, it can't be the last string
            if (args[x].equalsIgnoreCase(key))
                return args[x + 1];
        return null;
    }

    /** Creates a Flockers simulation with the given random number seed. */
    public Flockers(long seed) {
        super(seed);
        this.seed = seed;
    }

    public Flockers(long seed, String[] args) {
        super(seed);
        this.seed = seed;

        String for_s = argumentForKey("-for", args);
        if (for_s != null) { numSteps = Integer.parseInt(for_s); }

        String until_s = argumentForKey("-until", args);
        if (until_s != null) { untilNumSameDirection = Integer.parseInt(until_s); }

        String untilControl_s = argumentForKey("-untilControl", args);
        if (untilControl_s != null) { untilNumInFlocksWithAdhoc = Integer.parseInt(untilControl_s); }

        String width_s = argumentForKey("-width", args);
        if (width_s != null) width = Double.parseDouble(width_s);

        String height_s = argumentForKey("-height", args);
        if (height_s != null) height = Double.parseDouble(height_s);

        String numFlockers_s = argumentForKey("-numFlockers", args);
        if (numFlockers_s != null) numFlockers = Integer.parseInt(numFlockers_s);

        String cohesion_s = argumentForKey("-cohesion", args);
        if (cohesion_s != null) cohesion = Double.parseDouble(cohesion_s);

        String avoidance_s = argumentForKey("-avoidance", args);
        if (avoidance_s != null) avoidance = Double.parseDouble(avoidance_s);

        String randomness_s = argumentForKey("-randomness", args);
        if (randomness_s != null) randomness = Double.parseDouble(randomness_s);

        String consistency_s = argumentForKey("-consistency", args);
        if (consistency_s != null) consistency = Double.parseDouble(consistency_s);

        String momentum_s = argumentForKey("-momentum", args);
        if (momentum_s != null) momentum = Double.parseDouble(momentum_s);

        String neighborhood_s = argumentForKey("-neighborhood", args);
        if (neighborhood_s != null) neighborhood = Double.parseDouble(neighborhood_s);

        String toroidal_s = argumentForKey("-toroidal", args);
        if (toroidal_s != null) toroidal = Boolean.parseBoolean(toroidal_s);

        String prints_s = argumentForKey("-prints", args);
        if (prints_s != null) printDelta = Integer.parseInt(prints_s);

        String radius_s = argumentForKey("-radius", args);
        if (radius_s != null) radius = Double.parseDouble(radius_s);

        String placementX_s = argumentForKey("-placementX", args);
        if (placementX_s != null) placement_origin_x = Double.parseDouble(placementX_s);

        String placementRadius_s = argumentForKey("-placementRadius", args);
        if (placementRadius_s != null) placement_radius = Double.parseDouble(placementRadius_s);

        String placementY_s = argumentForKey("-placementY", args);
        if (placementY_s != null) placement_origin_y = Double.parseDouble(placementY_s);

        String placement_s = argumentForKey("-placement", args);
        if (placement_s != null) { setPlacement(placement_s); }

        String localBehavior_s = argumentForKey("-localBehavior", args);
        if (localBehavior_s != null) { setLocalBehavior(localBehavior_s); }

        String globalBehavior_s = argumentForKey("-globalBehavior", args);
        if (globalBehavior_s != null) { setGlobalBehavior(globalBehavior_s); }

        String numAdhoc_s = argumentForKey("-numAdhoc", args);
        if (numAdhoc_s != null) numAdhoc = Integer.parseInt(numAdhoc_s);

        String experiment_s = argumentForKey("-experiment", args);
        if (experiment_s != null) { setExperiment(experiment_s); }

        String outputFlocks_s = argumentForKey("-outputNumFlocks", args);
        if (outputFlocks_s != null) outputNumFlocks = Boolean.parseBoolean(outputFlocks_s);

        String outputFlocksWithAdhoc_s = argumentForKey("-outputNumFlocksWithAdhoc", args);
        if (outputFlocksWithAdhoc_s != null) outputNumFlocksWithAdhoc = Boolean.parseBoolean(outputFlocksWithAdhoc_s);

        String outputBoidsWithAdhoc_s = argumentForKey("-outputNumBoidsWithAdhoc", args);
        if (outputBoidsWithAdhoc_s != null) outputNumBoidsWithAdhoc = Boolean.parseBoolean(outputBoidsWithAdhoc_s);

        String outputLoner_s = argumentForKey("-outputNumLoners", args);
        if (outputLoner_s != null) outputNumLoners = Boolean.parseBoolean(outputLoner_s);

        String outputSameDir_s = argumentForKey("-outputNumSameDir", args);
        if (outputSameDir_s != null) outputNumSameDir = Boolean.parseBoolean(outputSameDir_s);

        String outputNumOnScreen_s = argumentForKey("-outputNumOnScreen", args);
        if (outputNumOnScreen_s != null) outputNumOnScreen = Boolean.parseBoolean(outputNumOnScreen_s);

        String outputBoidsInBoundingBox_s = argumentForKey("-outputNumBoidsInBoundingBox", args);
        if (outputBoidsInBoundingBox_s != null) outputNumBoidsInBoundingBox = Boolean.parseBoolean(outputBoidsInBoundingBox_s);

        String outputNumFacingGoalDirection_s = argumentForKey("-outputNumFacingGoalDirection", args);
        if (outputNumFacingGoalDirection_s != null) outputNumFacingGoalDirection= Boolean.parseBoolean(outputNumFacingGoalDirection_s);

        String outputAverageAngleDiffFromGoal_s = argumentForKey("-outputAverageAngleDiffFromGoal", args);
        if (outputAverageAngleDiffFromGoal_s != null) outputAverageAngleDiffFromGoal = Boolean.parseBoolean(outputAverageAngleDiffFromGoal_s);
    }

    public void start() {
        super.start();

        boids = new ArrayList<Flocker>();

        flockers = new Continuous2D(neighborhood / 1.5, width, height);

        setGlobalBehavior(getGlobalBehavior());
        setLocalBehavior(getLocalBehavior());

        switch (experimentFlag)
        {
            case DEFAULT:
                experiment = new DefaultExperiment(this); break;
            case HERD:
                experiment = new HerdExperiment(this, radius); break;
            case LARGE:
                // vary the number of birds
                experiment = new LargeFieldExperiment(this); break;
            case SMALL:
                experiment = new SmallExperiment(this, radius, experimentParams.initialDirection); break;
            default:
                System.out.println("Error"); break;
        }
        PlacementParams params = new PlacementParams(flockers, this, numAdhoc, goalDirection, localBehavior,
                globalBehavior, placement_origin_x, placement_origin_y, polygonSides, placement_radius,
                multiLargeParams, multiHerdParams, geneticParams);
        switch (placementFlag)
        {
            case RANDOM_RECT:
                placement = new RandomRect(params); break;
            case GRID_RECT:
                placement = new GridRect(params); break;
            case BORDER_RECT:
                placement = new BorderRect(params); break;
            case RANDOM_CIRCLE:
                placement = new RandomCircle(params); break;
            case GRID_CIRCLE:
                placement = new GridCircle(params); break;
            case BORDER_CIRCLE:
                placement = new BorderCircle(params); break;
            case K_MEANS:
                placement = new KMeans(params); break;
            default:
                System.out.println("Error"); break;
        }

        curStep = 0;
        experiment.setup();

        System.out.println("Seed: " + seed);
        System.out.print("Step");
        if (outputNumFlocks) { System.out.print("\tFlocks"); }
        if (outputNumFlocksWithAdhoc) { System.out.print("\tFlocksWithAdhoc"); }
        if (outputNumBoidsWithAdhoc) { System.out.print("\tBoidsWithAdhoc"); }
        if (outputNumLoners) { System.out.print("\tLoners"); }
        if (outputNumSameDir) { System.out.print("\tSameDirection"); }
        if (outputNumOnScreen) { System.out.print("\tNumOnScreen"); }
        if (outputNumBoidsInBoundingBox) { System.out.print("\tInBoundingBox"); }
        if (outputNumFacingGoalDirection) { System.out.print("\tNumFacingGoal"); }
        if (outputAverageAngleDiffFromGoal) { System.out.print("\tAverageAngleDiffFromGoal"); }
        System.out.println();
    }

    public void step(SimState state) {
        curStep++;
        experiment.step();
        if ((curStep % printDelta) == 0) {
            System.out.print(curStep);
            if (outputNumFlocks) { System.out.print("\t" + Metrics.numFlocks(this, 2, Integer.MAX_VALUE, false)); }
            if (outputNumFlocksWithAdhoc) { System.out.print("\t" + Metrics.numFlocks(this, 2, Integer.MAX_VALUE, true)); }
            if (outputNumBoidsWithAdhoc) { System.out.print("\t" + Metrics.numBoidsInFlocks(this, 2, Integer.MAX_VALUE, false, true)); }
            if (outputNumLoners) { System.out.print("\t" + Metrics.numLoners(this)); }
            if (outputNumSameDir) { System.out.print("\t" + Metrics.maxNumSameDirection(boids, numFlockers)); }
            if (outputNumOnScreen) { System.out.print("\t" + Metrics.numOnScreen(boids, height, numFlockers, width)); }
            if (outputNumBoidsInBoundingBox) { System.out.print("\t" + Metrics.numBoidsInAdhocBoundingBox(boids, neighborhood, numAdhoc, numFlockers, false)); }
            if (outputNumFacingGoalDirection) { System.out.print("\t" + Metrics.numBoidsFacingGoalDirection(boids, goalDirection, false)); }
            if (outputAverageAngleDiffFromGoal) { System.out.print("\t" + Metrics.averageAngleDiffFromGoal(boids, goalDirection, false)); }
            System.out.println();

            if (globalBehavior == Behavior.GlobalBehavior.MULTILARGE ||
                    globalBehavior == Behavior.GlobalBehavior.MULTILARGEFIXED ||
                    globalBehavior == Behavior.GlobalBehavior.MULTILARGELOCAL) {
                if (multiLargeParams.stageNum == 0) {
                    int numBoidsWithAdhoc;
                    if (globalBehavior == Behavior.GlobalBehavior.MULTILARGELOCAL) {
                        numBoidsWithAdhoc = 0;
                        for (Flocker f : boids) {
                            if (f.isAdhoc()) {
                                numBoidsWithAdhoc += Metrics.localFlockSize(flockers, this, f, neighborhood,
                                        neighborhood * 2, false);
                            }
                        }
                    } else {
                        numBoidsWithAdhoc = Metrics.numBoidsInFlocks(this, 2, Integer.MAX_VALUE, false, true);
                    }

                    if (numBoidsWithAdhoc >= multiLargeParams.switchingPoint) {
                        multiLargeParams.stageNum = 1;
                        if (globalBehavior != Behavior.GlobalBehavior.MULTILARGEFIXED) {
                            double avgDx = 0;
                            double avgDy = 0;
                            int numBoids = 0;
                            int flockNum[] = calcFlocks();
                            Set<Integer> flocksSeen = new HashSet<Integer>();
                            for (int i = 0; i < numFlockers; i++) {
                                int num = flockNum[i];
                                if (!flocksSeen.contains(num)) {
                                    flocksSeen.add(num);
                                    int flockSize = 0;
                                    double sumDx = 0;
                                    double sumDy = 0;
                                    int numAdhoc = 0;
                                    for (int j = i; j < numFlockers; j++) {
                                        if (flockNum[j] == num) {
                                            flockSize++;
                                            if (boids.get(j).isAdhoc()) {
                                                numAdhoc++;
                                            } else {
                                                sumDx += boids.get(j).lastd.x;
                                                sumDy += boids.get(j).lastd.y;
                                            }
                                        }
                                    }
                                    if (numAdhoc > 0) {
                                        numBoids += (flockSize - numAdhoc);
                                        avgDx += sumDx;
                                        avgDy += sumDy;
                                    }
                                }
                            }
                            multiLargeParams.direction = new Double2D(avgDx / numBoids, avgDy / numBoids);
                            for (Flocker flocker : boids) {
                                if (flocker.isAdhoc()) {
                                    flocker.geneticParams = multiLargeParams.behavior2Genetics;
                                }
                            }
                        } else {
                            multiLargeParams.direction = null;
                        }
                    }
                }
            }
        }
    }

    /**************************************************************************
     * HELPER FUNCTIONS FOR BEHAVIORS
     *************************************************************************/

    private static int calcScore(LinkedList<Int2D> chosenPairs, int[][] numCommonNeighbors) {
        int sum = 0;
        ListIterator<Int2D> iterator = chosenPairs.listIterator();
        while (iterator.hasNext()) {
            Int2D cur = iterator.next();
            sum += numCommonNeighbors[cur.x][cur.y];
        }
        return sum;
    }

    private static LinkedList<Int2D> calcBestPairs(Stack<Int2D> possiblePairs, LinkedList<Int2D> chosenPairs,
        int[][] numCommonNeighbors) {
        if (possiblePairs.size() == 0) {
            return chosenPairs;
        }
        Int2D possibility = possiblePairs.pop();
        // only consider if it doesn't conflict with an existing pair
        boolean valid = true;
        ListIterator<Int2D> chosenIterator = chosenPairs.listIterator();
        while (chosenIterator.hasNext()) {
            Int2D cur = chosenIterator.next();
            if (possibility.x == cur.x || possibility.x == cur.y ||
                possibility.y == cur.x || possibility.y == cur.y) {
                valid = false;
                break;
            }
        }
        // don't add possibility to your choices
        LinkedList<Int2D> resultNoAdd = calcBestPairs(possiblePairs, chosenPairs, numCommonNeighbors);
        LinkedList<Int2D> bestResult = resultNoAdd;
        if (valid) {
            LinkedList<Int2D> chosenWithPossibility = ((LinkedList<Int2D>) chosenPairs.clone());
            chosenWithPossibility.add(possibility);
            LinkedList<Int2D> resultWithAdd =
                calcBestPairs(possiblePairs, chosenWithPossibility, numCommonNeighbors);
            if (calcScore(resultWithAdd, numCommonNeighbors) > calcScore(resultNoAdd, numCommonNeighbors)) {
                bestResult = resultWithAdd;
            }
        }

        possiblePairs.push(possibility);
        return bestResult;
    }

    public int[] calcFlocks() {
        double[][] sqDistances = Metrics.calcSquaredDistances(boids, flockers, numFlockers, toroidal);
        double[][] angles = Metrics.calcAngles(boids, numFlockers);

        int[] flockNum = new int[numFlockers];
        for (int i = 0; i < numFlockers; i++) {
            flockNum[i] = 0;
        }
        Stack<Integer> flockerStack = new Stack<Integer>();

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
                    for (int j = 0; j < numFlockers; j++) {
                        // only add new boids to a group if they are flock connected
                        if (flockNum[j] == 0 &&
                            Metrics.flockConnected(neighborhood, sqDistances[curBoid][j], angles[curBoid][j])) {
                            flockerStack.push(j);
                        }
                    }
                }
            }
        }

        return flockNum;
    }

    /**************************************************************************
     * EXPERIMENT CODE
     *************************************************************************/

    private static class ExperimentParams {
        public Double2D initialDirection;
    }

    private abstract class Experiment {
        public Flockers state;
        public abstract void setup();

        public void step() {
            boolean coordinatedApproach = false;
            for (int x = 0; x < numFlockers; x++) {
                if (boids.get(x).isAdhoc() &&
                    (boids.get(x).localBehavior == Behavior.LocalBehavior.COORDINATED ||
                    boids.get(x).globalBehavior == Behavior.GlobalBehavior.MULTILARGE &&
                    multiLargeParams.behavior2 == Behavior.LocalBehavior.COORDINATED &&
                    multiLargeParams.stageNum == 1 ||
                    boids.get(x).globalBehavior == Behavior.GlobalBehavior.MULTIHERD &&
                    boids.get(x).multiHerdParams.followBehavior == Behavior.LocalBehavior.COORDINATED &&
                    boids.get(x).multiHerdParams.stageNum == 1
                        )) {
                    coordinatedApproach = true;
                    break;
                }
            }

            if (coordinatedApproach) {
                // First, reset coordinated pairs
                coordinatedPairs = new boolean[numFlockers][numFlockers];

                // Get distances between everything
                // Get all pairs
                // Pick the set of pairs that maximizes the number of boids influenced by a pair
                double[][] distances = Metrics.calcSquaredDistances(boids, flockers, numFlockers, toroidal);
                int[][] numCommonNeighbors = new int[numFlockers][numFlockers];
                Stack<Int2D> possiblePairs = new Stack<Int2D>();

                for (int i = 0; i < numFlockers; i++) {
                    for (int j = i + 1; j < numFlockers; j++) {
                        // calculate the common neighbors of adhoc boids i and j
                        if (!boids.get(i).isAdhoc() || !boids.get(j).isAdhoc()) {
                            continue;
                        }

                        int numCommon = 0;
                        for (int k = 0; k < numFlockers; k++) {
                            if (boids.get(i).isAdhoc()) {
                                continue;
                            }
                            // increment only if k is influenced by both i and j
                            if (distances[i][k] <= neighborhood * neighborhood &&
                                distances[j][k] <= neighborhood * neighborhood) {
                                numCommon++;
                            }
                        }

                        numCommonNeighbors[i][j] = numCommonNeighbors[j][i] = numCommon;
                        if (numCommon > 0) {
                            possiblePairs.push(new Int2D(i, j));
                        }
                    }
                }

                // find the set that maximizes the number of common neighbors
                LinkedList<Int2D> bestPairs =
                    calcBestPairs(possiblePairs, new LinkedList<Int2D>(), numCommonNeighbors);

                // save in the public array
                ListIterator<Int2D> iterator = bestPairs.listIterator();
                while (iterator.hasNext()) {
                    Int2D cur = iterator.next();
                    coordinatedPairs[cur.x][cur.y] = true;
                    coordinatedPairs[cur.y][cur.x] = true;
                }
            }

            boolean minAvgDirFlock = false;
            for (int x = 0; x < numFlockers; x++) {
                if (boids.get(x).isAdhoc() &&
                    (boids.get(x).localBehavior == Behavior.LocalBehavior.MIN_AVG_DIR_FLOCK ||
                    boids.get(x).globalBehavior == Behavior.GlobalBehavior.MULTILARGE &&
                    multiLargeParams.behavior2 == Behavior.LocalBehavior.MIN_AVG_DIR_FLOCK &&
                    multiLargeParams.stageNum == 1 ||
                    boids.get(x).globalBehavior == Behavior.GlobalBehavior.MULTIHERD &&
                    boids.get(x).multiHerdParams.followBehavior == Behavior.LocalBehavior.MIN_AVG_DIR_FLOCK &&
                    boids.get(x).multiHerdParams.stageNum == 1
                        )) {
                    minAvgDirFlock = true;
                }
            }

            if (minAvgDirFlock) {
                flocks = calcFlocks();
            }

            for (int x = 0; x < numFlockers; x++) {
                boids.get(x).step(state);
            }
        }
    }

    private class DefaultExperiment extends Experiment
    {
        public DefaultExperiment(Flockers state) { this.state = state; }

        public void setup()
        {
            // make a bunch of flockers and schedule 'em.  A few will be dead
            for(int x = 0; x < numFlockers - numAdhoc; x++)
            {
                Double2D location = new Double2D(random.nextDouble() * width, random.nextDouble() * height);
                Flocker flocker = new Flocker(location);
                if (random.nextBoolean(deadFlockerProbability)) flocker.dead = true;
                flockers.setObjectLocation(flocker, location);
                flocker.flockers = flockers;
                flocker.theFlock = state;
                boids.add(flocker);
            }
            placement.setup(boids);

            schedule.scheduleRepeating(state);
        }
    }

    // An experiment with a large field
    private class LargeFieldExperiment extends Experiment
    {

        public LargeFieldExperiment(Flockers state) { this.state = state; }

        public void setup()
        {
            // make a bunch of flockers and schedule 'em.  A few will be dead
            for(int x = 0; x < numFlockers - numAdhoc; x++)
            {
                Double2D location = new Double2D(random.nextDouble() * width, random.nextDouble() * height);
                Flocker flocker = new Flocker(location);
                flockers.setObjectLocation(flocker, location);
                flocker.flockers = flockers;
                flocker.theFlock = state;
                boids.add(flocker);
            }

            placement.setup(boids);

            schedule.scheduleRepeating(state);

        }
    }

    // An experiment where all the boids start out in a circle in the middle and expand out
    private class HerdExperiment extends Experiment
    {
        private double radius;

        public HerdExperiment(
            Flockers state,
            double radius)
        {
            this.state = state;
            this.radius = radius;
        }

        public void setup()
        {
            // make a bunch of flockers and schedule 'em.  A few will be dead
            for(int x = 0; x < numFlockers - numAdhoc; x++)
            {

                // schedule flock in a circle 
                double t = random.nextDouble() * 2.0* Math.PI;
                double r = radius * Math.sqrt(random.nextDouble());
                double widthRange = width / 2 + r * Math.cos(t);
                double heightRange = height / 2 + r * Math.sin(t);

                Double2D location = new Double2D(widthRange, heightRange);
                Flocker flocker = new Flocker(location);
                flockers.setObjectLocation(flocker, location);
                flocker.flockers = flockers;
                flocker.theFlock = state;
                boids.add(flocker);
            }

            placement.setup(boids);

            schedule.scheduleRepeating(state);

        }
    }

    // Experiments where all the boids start out in a rectangle in the middle, facing one direction
    private class SmallExperiment extends Experiment
    {
        private double radius;
        private Double2D initialDirection;

        public SmallExperiment(
                Flockers state,
                double radius,
                Double2D initialDirection)
        {
            this.state = state;
            this.radius = radius;
            this.initialDirection = initialDirection;
        }

        public void setup()
        {
            /*
            // make a bunch of flockers and schedule 'em.  A few will be dead
            for(int x = 0; x < numFlockers - numAdhoc; x++)
            {

                // schedule flock in a random rectangle
                double widthRange = width / 2 + random.nextDouble() * 2 * radius - radius;
                double heightRange = height / 2 + random.nextDouble() * 2 * radius - radius;

                Double2D location = new Double2D(widthRange, heightRange);
                Flocker flocker = new Flocker(location);
                flockers.setObjectLocation(flocker, location);
                flocker.flockers = flockers;
                flocker.theFlock = state;
                flocker.lastd = initialDirection.normalize().multiply(jump);
                boids.add(flocker);
            }*/

            /*
            // number of agents you have to place per side
            int side = (int) Math.ceil(Math.sqrt(numFlockers - numAdhoc));

            double inc = 2 * radius / (side - 1);
            int numPlaced = 0;

            for (int i = 0; i < side; i++) {
                for (int j = 0; j < side; j++) {
                    if (numPlaced > (numFlockers - numAdhoc)) {
                        break;
                    }
                    if ((side % 2) == 1 && i == side / 2 && j == side / 2) {
                        continue;
                    }

                    double x_val = width / 2 - radius + inc * i;
                    double y_val = width / 2 - radius + inc * j;

                    Double2D location = new Double2D(x_val, y_val);
                    Flocker flocker = new Flocker(location);
                    flockers.setObjectLocation(flocker, location);
                    flocker.flockers = flockers;
                    flocker.theFlock = state;
                    flocker.lastd = initialDirection.normalize().multiply(jump);
                    boids.add(flocker);
                    numPlaced++;
                }
            }*/

            double ang = 2 * Math.PI / (numFlockers - numAdhoc);
            for (int i = 0; i < (numFlockers - numAdhoc); i++) {
                double angle = i * ang;
                double x_val = width / 2 + Math.cos(angle) * radius;
                double y_val = height / 2 + Math.sin(angle) * radius;
                Double2D location = new Double2D(x_val, y_val);
                Flocker flocker = new Flocker(location);
                flockers.setObjectLocation(flocker, location);
                flocker.flockers = flockers;
                flocker.theFlock = state;
                flocker.lastd = initialDirection.normalize().multiply(jump);
                boids.add(flocker);
            }

            placement.setup(boids);
            for (int i = 0; i < numFlockers; i++) {
                Flocker flocker = boids.get(i);
                if (flocker.isAdhoc()) {
                    flocker.lastd = initialDirection.normalize().multiply(jump);
                }
            }

            schedule.scheduleRepeating(state);
        }
    }

    /**************************************************************************
     * MAIN FUNCTION
     *************************************************************************/
    public static void main(String[] args) {
        long seed = 0;

        // Call argumentForKey to get arguments to run
        if (keyExists("-help", args)) {
            System.err.println(
                "Format:            java sim.app.flockers.Flockers \\\n" +
                "                       [-test] \\\n" +
                "                       [-help] \\\n" +
                "                       [-for N] [-until N] [-untilControl N] [-seed S] [-width W] [-height H] \\\n" +
                "                       [-numFlockers N] [-cohesion C] [-avoidance A] [-randomness R] [-consistency C] \\\n" +
                "                       [-momentum M] [-neighborhood N] [-toroidal T] [-prints P] \\\n" +
                "                       [[-experiment E] [-radius R]] \\\n" +
                "                       [[-numAdhoc NA] [-globalBehavior B] [-localBehavior B]  \\\n" +
                "                           [[-placement p] [-placementX] [-placementY] [-placementRadius]] ]\n" +
                "                       [-outputNumFlocks B] [-outputNumFlocksWithAdhoc B] [-outputNumBoidsWithAdhoc B] \\\n" +
                "                           [-outputNumLoners B] [-outputNumSameDir B] [-outputNumOnScreen B] \\\n" +
                "                           [-outputNumBoidsInBoundingBox B] \n" +
                "-help              Shows this message\n" +
                "-test              Run test suite and nothing else\n" +
                "-for N             N > 0: run for N steps; N = 0: run indefinitely\n" +
                "-until N           Run until N boids are facing the same direction\n" +
                "-untilControl N    Run until N boids are in flocks with ad hoc agents\n" +
                "-prints P          Print metrics every P steps, default 100\n" +
                "-outputNum[_]      Boolean, whether to output this metric\n" +
                "-experiment E      E can be one of Default, Large, or Herd\n" +
                "-radius R          Only used with the herd experiment - set the radius of the circle to start in\n" +
                "-numAdhoc NA       Number of adhoc agents\n" +
                "-placement P       One of RANDOM_X, BORDER_X, GRID_X, where X is either RECT or CIRCLE\n" +
                "-behavior B        One of FACE_EAST, OFFSET_MOMENTUM, ONE_STEP_LOOKAHEAD, COORDINATED\n" +
                "\n" +
                "The rest of the arguments are pretty self-explanatory... Toroidal is a bool (true/false),\n" +
                "numFlockers, for, and prints are ints, and everything else is a double."
            );
            System.exit(0);
        }

        if (!keyExists("-seed", args)) {
            seed = System.currentTimeMillis();
        }
        if (keyExists("-test", args)) {
            // test code for calcBestPairs for the coordinated approach
            int[][] numCommonNeighbors = new int[][] {
                {0, 0, 10, 0, 0, 0, 0, 0},
                {0, 10, 0, 0, 0, 0, 0, 0},
                {10, 10, 0, 20, 0, 0, 0, 0},
                {0, 0, 20, 0, 10, 5, 30, 0},
                {0, 0, 0, 10, 0, 0, 0, 0},
                {0, 0, 0, 5, 0, 0, 0, 0},
                {0, 0, 0, 30, 0, 0, 0, 6},
                {0, 0, 0, 0, 0, 0, 6, 0}
            };
            Stack<Int2D> possiblePairs = new Stack<Int2D>();
            possiblePairs.add(new Int2D(0, 2));
            possiblePairs.add(new Int2D(1, 2));
            possiblePairs.add(new Int2D(2, 3));
            possiblePairs.add(new Int2D(2, 6));
            possiblePairs.add(new Int2D(3, 4));
            possiblePairs.add(new Int2D(3, 5));
            possiblePairs.add(new Int2D(3, 6));
            possiblePairs.add(new Int2D(6, 7));

            LinkedList<Int2D> choices =
                calcBestPairs(possiblePairs, new LinkedList<Int2D>(), numCommonNeighbors);
            int sum = calcScore(choices, numCommonNeighbors);
            System.out.println(sum);

            for (int i = 0; i < choices.size(); i++) {
                System.out.println(choices.get(i));
            }

            System.exit(0);
        }

        String seed_s = argumentForKey("-seed", args);
        if (seed_s != null) seed = Integer.parseInt(seed_s);

        Flockers flockers = new Flockers(seed, args);

        flockers.start();
        while (flockers.numSteps <= 0 || flockers.curStep < flockers.numSteps) {
            flockers.step(flockers);
            if (flockers.untilNumSameDirection > 0 &&
                flockers.curStep % flockers.printDelta == 0) {
                if (Metrics.maxNumSameDirection(flockers.boids, flockers.numFlockers) >= flockers.untilNumSameDirection) {
                    break;
                }
            }
            if (flockers.untilNumInFlocksWithAdhoc > 0 &&
                flockers.curStep % flockers.printDelta == 0) {
                if (Metrics.numBoidsInFlocks(flockers, 2, Integer.MAX_VALUE, false, true) >=
                    flockers.untilNumInFlocksWithAdhoc) {
                    break;
                }
            }
        }
        flockers.finish();

        System.exit(0);
    }
}

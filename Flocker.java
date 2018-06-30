/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.flockers;
import sim.app.flockers.behavior.Behavior;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import ec.util.*;
import java.util.*;

public class Flocker implements Steppable, sim.portrayal.Orientable2D {
    private static final long serialVersionUID = 1;

    public Double2D loc = new Double2D(0, 0);
    public Double2D lastd = new Double2D(0, 0);
    public Double2D origin = new Double2D(0, 0);
    public Double2D center_origin = new Double2D(0, 0);
    public Double2D goalDirection = new Double2D(1, 0);
    public Continuous2D flockers;
    public Flockers theFlock;
    public boolean dead = false;
    public boolean adhoc = false;
    public boolean firstStep = true;
    public Behavior.GlobalBehavior globalBehavior = Behavior.GlobalBehavior.FACE;
    public Behavior.LocalBehavior localBehavior = Behavior.LocalBehavior.FACE_EAST;
    // how small angle increments should be for lookhead
    public double angleIncrement = 10.0;
    // for the polygon behavior, how many sides, size of
    public int polygonSides = 10;
    public double polygonRadius = 0;
    public Behavior.MultiLargeParams multiLargeParams = null;
    public Behavior.MultiHerdParams multiHerdParams = null;
    public Behavior.GeneticLocalBehaviorParams geneticParams = null;

    public Flocker(Double2D location) {
        loc = location;
    }

    /**************************************************************************
     * GETTERS/SETTERS
     *************************************************************************/

    public Bag getNeighbors() {
        return flockers.getNeighborsExactlyWithinDistance(loc, theFlock.neighborhood, true);
    }

    public Bag getNeighborsNonToroid() {
        return flockers.getNeighborsExactlyWithinDistance(loc, theFlock.neighborhood, false);
    }

    public double getOrientation() {
        return orientation2D();
    }
    public boolean isDead() {
        return dead;
    }
    public void setDead(boolean val) {
        dead = val;
    }
    public boolean isAdhoc() {
        return adhoc;
    }
    public void setAdhoc(boolean val) {
        adhoc = val;
    }
    public double getAngleIncrement() {
        return angleIncrement;
    }
    public void setAngleIncrement(double val) {
        angleIncrement = val;
    }

    public void setOrientation2D(double val) {
        lastd = new Double2D(Math.cos(val), Math.sin(val));
    }

    public double orientation2D() {
        if (lastd.x == 0 && lastd.y == 0) return 0;
        return Math.atan2(lastd.y, lastd.x);
    }

    /**************************************************************************
     * METHODS FOR CALCULATING NEXT STEPS
     *************************************************************************/

    public Double2D momentum() {
        return lastd;
    }

    public static Double2D consistency(Flocker boid, Bag b, Continuous2D flockers, Flockers theFlock) {
        if (b == null || b.numObjs == 0) return new Double2D(0, 0);

        double x = 0;
        double y = 0;
        int i = 0;
        int count = 0;
        for(i = 0; i < b.numObjs; i++) {
            Flocker other = (Flocker)(b.objs[i]);
            if (!other.dead) {
                double dx = boid.loc.x - other.loc.x;
                double dy = boid.loc.y - other.loc.y;
                if (theFlock.toroidal) {
                    dx = flockers.tdx(boid.loc.x, other.loc.x);
                    dy = flockers.tdy(boid.loc.y, other.loc.y);
                }
                Double2D m = ((Flocker)b.objs[i]).momentum();
                count++;
                x += m.x;
                y += m.y;
            }
        }
        if (count > 0) {
            x /= count;
            y /= count;
        }
        return new Double2D(x, y);
    }

    public static Double2D cohesion(Flocker boid, Bag b, Continuous2D flockers, Flockers theFlock) {
        if (b == null || b.numObjs == 0) return new Double2D(0, 0);

        double x = 0;
        double y = 0;

        int count = 0;
        int i = 0;
        for(i = 0; i < b.numObjs; i++) {
            Flocker other = (Flocker)(b.objs[i]);
            if (!other.dead) {
                double dx = boid.loc.x - other.loc.x;
                double dy = boid.loc.y - other.loc.y;
                if (theFlock.toroidal) {
                    dx = flockers.tdx(boid.loc.x, other.loc.x);
                    dy = flockers.tdy(boid.loc.y, other.loc.y);
                }
                count++;
                x += dx;
                y += dy;
            }
        }
        if (count > 0) {
            x /= count;
            y /= count;
        }
        return new Double2D(-x / 10, -y / 10);
    }

    public static Double2D avoidance(Flocker boid, Bag b, Continuous2D flockers, Flockers theFlock) {
        if (b == null || b.numObjs == 0) return new Double2D(0, 0);
        double x = 0;
        double y = 0;

        int i = 0;
        int count = 0;

        for(i = 0; i < b.numObjs; i++) {
            Flocker other = (Flocker)(b.objs[i]);
            if (other != boid ) {
                double dx = boid.loc.x - other.loc.x;
                double dy = boid.loc.y - other.loc.y;
                if (theFlock.toroidal) {
                    dx = flockers.tdx(boid.loc.x, other.loc.x);
                    dy = flockers.tdy(boid.loc.y, other.loc.y);
                }
                double lensquared = dx * dx + dy * dy;
                count++;
                x += dx / (lensquared * lensquared + 1);
                y += dy / (lensquared * lensquared + 1);
            }
        }
        if (count > 0) {
            x /= count;
            y /= count;
        }
        return new Double2D(400 * x, 400 * y);
    }

    public static Double2D randomness(MersenneTwisterFast r) {
        double x = r.nextDouble() * 2 - 1.0;
        double y = r.nextDouble() * 2 - 1.0;
        double l = Math.sqrt(x * x + y * y);
        return new Double2D(0.05 * x / l, 0.05 * y / l);
    }

    public static Double2D nonAdhocNewDirection(
        Flocker boid, Bag b, Continuous2D flockers, Flockers flock
    ) {
        double dx, dy;

        Double2D avoid = avoidance(boid, b, flock.flockers, flock);
        Double2D cohe = cohesion(boid, b, flock.flockers, flock);
        Double2D rand = randomness(flock.random);
        Double2D cons = consistency(boid, b, flock.flockers, flock);

        dx = flock.cohesion * cohe.x + flock.avoidance * avoid.x + flock.consistency * cons.x +
                    flock.randomness * rand.x + flock.momentum * boid.momentum().x;
        dy = flock.cohesion * cohe.y + flock.avoidance * avoid.y + flock.consistency * cons.y +
                    flock.randomness * rand.y + flock.momentum * boid.momentum().y;

        // with randomness=0, still have the flocking agents move before being influenced
        if (dx == 0 && dy == 0 && flock.randomness == 0) {
            dx = rand.x;
            dy = rand.y;
        }

        return new Double2D(dx, dy);
    }

    public static Double2D normalize(Double2D vec, double size) {
        double dx = vec.x;
        double dy = vec.y;

        // renormalize to the given step size
        double dis = Math.sqrt(dx * dx + dy * dy);
        if (dis > 0) {
            dx = dx / dis * size;
            dy = dy / dis * size;
        }

        return new Double2D(dx, dy);
    }

    public static Double2D average(Flocker agent, Bag neighbors) {
        double avgDx = 0;
        double avgDy = 0;

        int count = 0;

        for (int i = 0; i < neighbors.numObjs; i++) {
            Flocker other = (Flocker)(neighbors.objs[i]);
            if (other != agent) {
                avgDx += other.lastd.x;
                avgDy += other.lastd.y;
                count++;
            }
        }

        if (count != 0) {
            avgDx /= count;
            avgDy /= count;
        }

        return new Double2D(avgDx, avgDy);
    }

    public static void takeStep(Flocker agent, Double2D newVec, Flockers flock) {
        agent.lastd = normalize(newVec, flock.jump);
        if (flock.toroidal)
            agent.loc = new Double2D(
                flock.flockers.stx(agent.loc.x + agent.lastd.x), 
                flock.flockers.sty(agent.loc.y + agent.lastd.y));
        else
            agent.loc = new Double2D(agent.loc.x + agent.lastd.x, agent.loc.y + agent.lastd.y);
        flock.flockers.setObjectLocation(agent, agent.loc);
        
    }


    public Double2D chooseBehavior(Behavior.GlobalBehavior globalBehavior, Behavior.LocalBehavior localBehavior,
                                   Bag neighbors, Double2D goal) {
        Double2D newVec;
        Double2D trueGoal = goal;
        switch (globalBehavior) {
            case FACE:
                trueGoal = goal; break;
            case RANDOM:
                if (neighbors.size() == 1) {
                    if (lastd.equals(new Double2D(0,0)) || lastd.normalize().equals(goal.normalize())) {
                        trueGoal = new Double2D(2 * theFlock.random.nextDouble() - 1,
                                2 * theFlock.random.nextDouble() - 1);
                    } else {
                        trueGoal = lastd;
                    }
                }
                break;
            case CIRCLE:
                trueGoal = calcCircle(neighbors, goal); break;
            case POLYGON:
                trueGoal = calcPolygon(neighbors, goal); break;
            case MULTILARGE:
                return calcMultiLarge(neighbors, goal);
            case MULTILARGEFIXED:
                return calcMultiLarge(neighbors, goal);
            case MULTILARGELOCAL:
                return calcMultiLarge(neighbors, goal);
            case MULTIHERD:
                return calcMultiHerd(neighbors, goal);
            default:
                trueGoal = goal; break;
        }

        switch (localBehavior) {
            case FACE_EAST:
                newVec = calcFaceEast(neighbors, trueGoal); break;
            case OFFSET_MOMENTUM:
                newVec = calcOffsetMomentum(neighbors, trueGoal); break;
            case ONE_STEP_LOOKAHEAD:
                newVec = calcOneStepLookahead(neighbors, trueGoal); break;
            case TWO_STEP_LOOKAHEAD:
                newVec = calcTwoStepLookahead(neighbors, trueGoal); break;
            case COORDINATED:
                newVec = calcCoordinatedKatie(neighbors, trueGoal); break;
            case MIN_AVG_DIR_NEIGH:
                newVec = calcMinAvgDirNeighborhood(neighbors, trueGoal); break;
            case MIN_AVG_DIR_FLOCK:
                newVec = calcMinAvgDirFlock(neighbors, trueGoal); break;
            case COUZIN:
                newVec = calcCouzin(neighbors, trueGoal); break;
            case GENETIC:
                newVec = calcGenetic(neighbors, trueGoal); break;
            default:
                newVec = calcFaceEast(neighbors, trueGoal); break;
        }
        return newVec;
    }

    public void step(SimState state) {
        final Flockers flock = (Flockers)state;
        loc = flock.flockers.getObjectLocation(this);

        if (dead) return;

        Bag b = getNeighbors();
        double dx, dy;
        Double2D newVec;

        if (!adhoc) {
            newVec = nonAdhocNewDirection(this, b, flockers, flock);
        } else {
            newVec = chooseBehavior(globalBehavior, localBehavior, b, goalDirection);
        }

        takeStep(this, newVec, flock);
        this.firstStep = false;
    }

    /**************************************************************************
     * LOCAL BEHAVIORS
     *************************************************************************/

    public Double2D calcFaceEast(Bag neighbors, Double2D goal) {
        return goal;
    }

    public Double2D calcOffsetMomentum(Bag neighbors, Double2D goal) {
        Double2D avg = average(this, neighbors);
        double angle = Helpers.calcAngle(avg, goal);
        if (angle < 90) {
            return goal.subtract(avg);
        } else {
            // the average momentum is more than 90 degrees from the goal
            // simple vector subtraction will actually make us go the opposite direction
            // instead, face the opposite direction of the average, offset slightly by
            // the right direction of the goal
            Double2D candidate1, candidate2;
            if (avg.y == 0) {
                candidate1 = new Double2D(-1*avg.x, 0.1);
                candidate2 = new Double2D(-1*avg.x, -0.1);
            } else if (avg.x == 0) {
                candidate1 = new Double2D(0.1, -1*avg.y);
                candidate2 = new Double2D(-0.1, -1*avg.y);
            } else {
                candidate1 = new Double2D(-1.1*avg.x, -1*avg.y);
                candidate2 = new Double2D(-0.9*avg.x, -1*avg.y);
            }

            return (Helpers.calcAngle(goal, candidate1) <
                Helpers.calcAngle(goal, candidate2)) ? candidate1 : candidate2;
        }
    }

    public Double2D calcOneStepLookahead(Bag neighbors, Double2D goal) {
        double bestAngle = 0;
        Double2D bestVec = goal;
        double bestAngleResult = 360;
        Double2D lastdOrig = lastd;

        if (neighbors.numObjs == 1) {
            return goal;
        }

        // go through the possible angles, choose the right one
        for (double angle = 0; angle < 360; angle += angleIncrement) {
            lastd = normalize(new Double2D(Math.cos(Math.toRadians(angle)),
									Math.sin(Math.toRadians(angle))), theFlock.jump);
            
            Double2D avgOrientation = averageOrientation(neighbors);

            double newAngle = Helpers.calcAngle(goal, avgOrientation);

            if (newAngle < bestAngleResult) {
                bestAngle = angle;
                bestVec = lastd;
                bestAngleResult = newAngle;
            }
        }

        lastd = lastdOrig;

        return bestVec;
    }

    public Double2D calcTwoStepLookahead(Bag neighbors, Double2D goal) {
        double bestAngle = 0;
        Double2D bestVec = goal;
        double bestAngleResult = 360;
        Double2D lastdOrig = lastd;

        if (neighbors.numObjs == 0) {
            return goal;
        }

        // for the first vector, go through all possible angles, choose the right one
        for (double angle = 0; angle < 360; angle += angleIncrement) {
            lastd = normalize(new Double2D(Math.cos(Math.toRadians(angle)), 
									Math.sin(Math.toRadians(angle))), theFlock.jump);
            Double2D angleVec = lastd;

            // calculate the new orientation for the neighbors; set orientations and locations accordingly
            // need to store the old orientations as well
            List<Double2D> oldLocs = new ArrayList<Double2D>();
            List<Double2D> oldLastd = new ArrayList<Double2D>();
            for (int i = 0; i < neighbors.numObjs; i++) {
                Flocker other = (Flocker)(neighbors.objs[i]);
                oldLocs.add(other.loc);
                oldLastd.add(other.lastd);
                if (other != this) {
                    if (other.adhoc) {
                        other.lastd = lastd;
                    } else {
                        other.lastd = nonAdhocNewDirection(other,
                            flockers.getNeighborsExactlyWithinDistance(
                                other.loc, theFlock.neighborhood, theFlock.toroidal),
                            flockers, theFlock);
                    }
                }
                // takeStep(other, other.lastd, theFlock);
            }

            double bestAngleResultStepTwo = 360;

            // now run one step lookahead with a new bag of neighbors
            for (double angleStepTwo = 0; angleStepTwo < 360; angleStepTwo += angleIncrement) {
                double avgDx = 0;
                double avgDy = 0;
                int count = 0;

                lastd = normalize(new Double2D(Math.cos(Math.toRadians(angleStepTwo)),
										Math.sin(Math.toRadians(angleStepTwo))),
								theFlock.jump);

                // calculate the average new orientation of your neighbors
                Double2D avgOrientation = averageOrientation(neighbors); 

                double newAngle = Helpers.calcAngle(goal, avgOrientation);
                if (newAngle < bestAngleResultStepTwo) {
                    bestAngleResultStepTwo = newAngle;
                }
            }

            // if the new average is better than the old average, adopt the new average
            if (bestAngleResultStepTwo < bestAngleResult) {
                bestAngle = angle;
                bestVec = angleVec;
                bestAngleResult = bestAngleResultStepTwo;
            }

            // then reset everything to the way it was before
            for (int i = 0; i < neighbors.numObjs; i++) {
                Flocker boid = (Flocker)(neighbors.objs[i]);
                boid.loc = oldLocs.get(i);
                boid.lastd = oldLastd.get(i);
                theFlock.flockers.setObjectLocation(boid, boid.loc);
            }
        }

        return bestVec;
    }

    public Double2D calcCoordinatedKatie(Bag neighbors, Double2D goal) {
        Flocker partner = theFlock.getCoordinatedPartner(this);
        if (partner == null) {
            return calcOneStepLookahead(neighbors, goal);
        }

        double bestAngle = 0;
        Double2D bestVec = goal;
        double bestAngleResult = 360;
        Double2D lastdOrig = lastd;
        
        Double2D partnerLastdOrig = partner.lastd;
        Bag partnerNeighbors = partner.getNeighbors();

        if (neighbors.numObjs == 0) {
            return goal;
        }

        // go through the possible angles, choose the right one
        for (double angle = 0; angle < 360; angle += angleIncrement) {
            for (double partnerAngle = 0; partnerAngle < 360; partnerAngle += angleIncrement) {
                lastd = normalize(new Double2D(Math.cos(Math.toRadians(angle)),
										Math.sin(Math.toRadians(angle))),
								theFlock.jump);
                partner.lastd = normalize(new Double2D(
                    Math.cos(Math.toRadians(partnerAngle)),
					Math.sin(Math.toRadians(partnerAngle))), theFlock.jump);
                
                double myAvgDx = 0;
                double myAvgDy = 0;
                int myCount = 0;

                double partnerAvgDx = 0;
                double partnerAvgDy = 0;
                int partnerCount = 0;

                // calculate the average new orientation of your neighbors
                for (int i = 0; i < neighbors.numObjs; i++) {
                    Flocker other = (Flocker)(neighbors.objs[i]);
                    if (other != this && other != partner) {
                        Double2D newVec;
                        if (other.adhoc) {
                            newVec = lastd;
                        } else {
                            newVec = nonAdhocNewDirection(other,
                                flockers.getNeighborsExactlyWithinDistance(
                                    other.loc, theFlock.neighborhood, theFlock.toroidal),
                                flockers, theFlock);
                        }
                        myAvgDx += newVec.x;
                        myAvgDy += newVec.y;
                        myCount++;
                    }
                }
                if (myCount != 0) {
                    myAvgDx /= myCount;
                    myAvgDy /= myCount;
                }
                // calculate the average orientation of your partner's neighbors
                for (int i = 0; i < partnerNeighbors.numObjs; i++) {
                    // do not double count
                    for (int j = 0; j < neighbors.numObjs; j++) {
                        if (partnerNeighbors.objs[i] == neighbors.objs[j]) {
                            continue;
                        }
                    }
                    Flocker other = (Flocker)(neighbors.objs[i]);
                    if (other != this && other != partner) {
                        Double2D newVec;
                        if (other.adhoc) {
                            newVec = lastd;
                        } else {
                            newVec = nonAdhocNewDirection(other,
                                flockers.getNeighborsExactlyWithinDistance(
                                    other.loc, theFlock.neighborhood, theFlock.toroidal),
                                flockers, theFlock);
                        }
                        partnerAvgDx += newVec.x;
                        partnerAvgDy += newVec.y;
                        partnerCount++;
                    }
                }
                if (partnerCount != 0) {
                    partnerAvgDx /= partnerCount;
                    partnerAvgDy /= partnerCount;
                }

                double newAngle = Helpers.calcAngle(goal,
                    new Double2D((myAvgDx + partnerAvgDx)/2, (myAvgDy + partnerAvgDy)/2));
                if (newAngle < bestAngleResult) {
                    bestAngle = angle;
                    bestVec = lastd;
                    bestAngleResult = newAngle;
                }
            }
        }

        lastd = lastdOrig;
        partner.lastd = partnerLastdOrig;

        return bestVec;
    }

    public Double2D calcGenetic(Bag neighbors, Double2D goal) {
        List<Behavior.NeighborBoid> processedNeighbors = new ArrayList<>();
        for (int i = 0; i < neighbors.numObjs; i++) {
            Flocker other = (Flocker)(neighbors.objs[i]);
            if (other != this) {
                double dx = other.loc.x - loc.x;
                double dy = other.loc.y - loc.y;
                if (theFlock.toroidal) {
                    dx = flockers.tdx(other.loc.x, loc.x);
                    dy = flockers.tdy(other.loc.y, loc.y);
                }
                processedNeighbors.add(new Behavior.NeighborBoid(new Double2D(dx, dy), other.lastd));
                /*System.out.println(String.format("dx: %f, dy: %f, lastdx: %f, lastdy: %f",
                        dx, dy, other.lastd.x, other.lastd.y));*/
            }
        }

        return geneticParams.localBehavior.calcNewDirection(processedNeighbors, lastd, goal, theFlock.neighborhood);
    }

    /**************************************************************************
     * GLOBAL BEHAVIORS
     *************************************************************************/

    public Double2D calcCircle(Bag neighbors, Double2D goal) {
        double radius = loc.subtract(origin).length();
        double angleFromOrigin = loc.subtract(origin).angle();
        if (angleFromOrigin < 0) {
            angleFromOrigin += 2 * Math.PI;
        }
        double newAngleFromOrigin = angleFromOrigin + theFlock.jump / radius;
        Double2D goalPos = new Double2D(Math.cos(newAngleFromOrigin) * radius,
            Math.sin(newAngleFromOrigin) * radius).add(origin);
        Double2D bestVec = goalPos.subtract(loc);

        return bestVec;
    }

    public Double2D averageOrientation(Bag neighbors) {
        int count = 0;
        double avgDx = 0;
        double avgDy = 0;

        for (int i = 0; i < neighbors.numObjs; i++) {
            Flocker other = (Flocker)(neighbors.objs[i]);
            if (other != this) {
                Double2D newVec;
                if (other.adhoc) {
                    newVec = lastd;
                } else {
                    newVec = nonAdhocNewDirection(other,
                        flockers.getNeighborsExactlyWithinDistance(
                            other.loc, theFlock.neighborhood, theFlock.toroidal),
                        flockers, theFlock);
                }
                avgDx += newVec.x;
                avgDy += newVec.y;
                count++;
            }
        }

        if (count != 0) {
            avgDx /= count;
            avgDy /= count;
        }

        return new Double2D(avgDx, avgDy);
    }

    public Double2D calcMinAvgDirNeighborhood(Bag neighbors, Double2D goal) {
        double bestAngle = 0;
        Double2D bestVec = goal;
        double bestAngleResult = 360;
        Double2D lastdOrig = lastd;

        if (neighbors.numObjs == 0) {
            return goal;
        }

        for (double angle = 0; angle < 360; angle += angleIncrement) {
            lastd = normalize(new Double2D(Math.cos(Math.toRadians(angle)),
									Math.sin(Math.toRadians(angle))),
							theFlock.jump);
           
            Double2D avgOrientation = averageOrientation(neighbors); 
            double newAngle = Helpers.calcAngle(lastd, avgOrientation);

            if (newAngle < bestAngleResult) {
                bestAngle = angle;
                bestVec = lastd;
                bestAngleResult = newAngle;
            }
        }

        lastd = lastdOrig;

        return bestVec;
    }

    public Double2D calcMinAvgDirFlock(Bag neighbors, Double2D goal) {
        double bestAngle = 0;
        Double2D bestVec = goal;
        double bestAngleResult = 360;
        Double2D lastdOrig = lastd;

        if (neighbors.numObjs == 0) {
            return goal;
        }

        ArrayList<Flocker> flock = theFlock.getFlocks(this);

        // go through the possible angles, choose the right one
        for (double angle = 0; angle < 360; angle += angleIncrement) {
            lastd = normalize(new Double2D(Math.cos(Math.toRadians(angle)),
									Math.sin(Math.toRadians(angle))), theFlock.jump);
            double avgDx = 0;
            double avgDy = 0;

            int count = 0;
            
            for (Flocker flocker : flock) {
                if (flocker != this) {
                    Double2D newVec;
                    if (flocker.adhoc) {
                        newVec = lastd;
                    } else {
                        newVec = nonAdhocNewDirection(flocker,
                            flockers.getNeighborsExactlyWithinDistance(
                                flocker.loc, theFlock.neighborhood, theFlock.toroidal),
                            flockers, theFlock);
                    }
                    avgDx += newVec.x;
                    avgDy += newVec.y;
                    count++;
                }
            }
           
            if (count != 0) {
                avgDx /= count;
                avgDy /= count;
            }

            double newAngle = Helpers.calcAngle(lastd, new Double2D(avgDx, avgDy));

            if (newAngle < bestAngleResult) {
                bestAngle = angle;
                bestVec = lastd;
                bestAngleResult = newAngle;
            }
        }

        lastd = lastdOrig;

        return bestVec;
    }

    public Double2D calcCouzin(Bag neighbors, Double2D goal) {
        Double2D regularVec = nonAdhocNewDirection(this, neighbors, flockers, theFlock);

        double omega = 0.5;

        return new Double2D(regularVec.x + omega * goal.x, regularVec.y + omega * goal.y);
    }

    public Double2D calcPolygon(Bag neighbors, Double2D goal) {
        double angleFromOrigin = loc.subtract(origin).angle();
        if (angleFromOrigin < 0) {
            angleFromOrigin += 2 * Math.PI;
        }

        int curSector = (int)(angleFromOrigin / (2 * Math.PI / polygonSides)) % polygonSides;

        double newAngleFromOrigin = (curSector + 1) * (2 * Math.PI / polygonSides);
        Double2D goalPos = new Double2D(Math.cos(newAngleFromOrigin) * polygonRadius,
            Math.sin(newAngleFromOrigin) * polygonRadius).add(origin);
        Double2D bestVec = goalPos.subtract(loc);

        return bestVec;
    }

    // public Double2D calcCurveOrigin() {
    //     double r_s = multiHerdParams.stopFollowingPoint;
    //     double r_n = multiHerdParams.finalBehaviorRadius - r_s;
    //     double theta = 2 * Math.asin(r_n / 2 / r_s);
    //     double newAngle = this.loc.subtract(center_origin).angle() + theta;
    //     Double2D o = new Double2D(Math.cos(newAngle) * r_s, Math.sin(newAngle) * r_s);
    //     return o.add(center_origin);
    // }

    // inch along both sides to the vector perpendicular to the current orientation of the flock
    public Double2D calcCurveOrigin() {
    	// clockwise [y, -x] (positive perpendicular)
    	// Double2D vecPositive = new Double2D(lastd.y, -lastd.x);
   
    	// counterclockwise [-y, x] (negative perpendicular direction)
    	Double2D vecNegative = new Double2D(-lastd.y, lastd.x);

    	// Double2D nextLocPos = loc;
    	Double2D nextLocNeg = loc;

    	double r_big = multiHerdParams.finalBehaviorRadius;

    	// distance from the mini origin to the central origin 
    	// double r_dist_pos = 0;
    	double r_dist_neg = 0;

    	// // radius of the small circle 
    	// double r_pos = 0; 
    	double r_neg = 0;
    	double dx = 1.0;
    	// while (r_pos + r_dist_pos < r_big) {
    	// 	nextLocPos = new Double2D(nextLocPos.x + vecPositive.x * dx, nextLocPos.y + vecPositive.y * dx);
     //        r_pos = loc.subtract(nextLocPos).length();
     //        r_dist_pos = nextLocPos.subtract(center_origin).length();
    	// }

    	while (r_neg + r_dist_neg < r_big) {
            nextLocNeg = new Double2D(nextLocNeg.x + vecNegative.x * dx, nextLocNeg.y + vecNegative.y * dx);
            r_neg = loc.subtract(nextLocNeg).length();
            r_dist_neg = nextLocNeg.subtract(center_origin).length();
    	}

    	return nextLocNeg;
    }

    public Double2D calcMultiLarge(Bag b, Double2D goal) {  
        Double2D newVec;
        if (multiLargeParams.stageNum == 0) {
            newVec = chooseBehavior(Behavior.GlobalBehavior.FACE, multiLargeParams.behavior1, b, goal);
        } else {
            newVec = chooseBehavior(Behavior.GlobalBehavior.FACE, multiLargeParams.behavior2, b,
                multiLargeParams.direction == null ? goal : multiLargeParams.direction);
        }
        return newVec;
    }

    public Double2D calcMultiHerd(Bag neighbors, Double2D goal) {
        Double2D newVec = goal;

        // Patrol
        if (multiHerdParams.stageNum == 0) {
            if (multiHerdParams.initialBehavior == Behavior.GlobalBehavior.POLYGON) {
                polygonSides = multiHerdParams.initialBehaviorSides;
            }
            if (neighbors.numObjs == 1) {
                newVec = chooseBehavior(multiHerdParams.initialBehavior, localBehavior, neighbors, goal);
            } else {
                multiHerdParams.stageNum = 1;
            }
        }

        // Follow
        if (multiHerdParams.stageNum == 1) {
            double distance = loc.subtract(center_origin).length();
            if (distance < multiHerdParams.stopFollowingPoint) {
                newVec = chooseBehavior(Behavior.GlobalBehavior.FACE, multiHerdParams.followBehavior, neighbors, goal);
            } else {
                multiHerdParams.stageNum = 2;
                origin = calcCurveOrigin();
            }
        }

        // smooth curve until final behavior radius 
        if (multiHerdParams.stageNum == 2) {
            double distance = loc.subtract(center_origin).length();
            if (distance < multiHerdParams.finalBehaviorRadius) {
                newVec = calcCircle(neighbors, goal);
            } else {
                multiHerdParams.stageNum = 3;
                origin = center_origin;
                if (multiHerdParams.finalBehavior == Behavior.GlobalBehavior.POLYGON) {
                    polygonSides = multiHerdParams.finalBehaviorSides;
                    polygonRadius = multiHerdParams.finalBehaviorRadius;
                }
            }
        }

        // Final circle
        if (multiHerdParams.stageNum == 3) {
            newVec = chooseBehavior(multiHerdParams.finalBehavior, localBehavior, neighbors, goal);
        }

        return newVec;
    }

}

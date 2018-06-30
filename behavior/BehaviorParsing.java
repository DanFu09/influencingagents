package sim.app.flockers.behavior;

/**
 * Class to parse Global and Local Behaviors
 */
public class BehaviorParsing {
    /**************************************************************************
     * PRIVATE METHODS TO HELP PARSE BEHAVIORS
     *************************************************************************/

    private static int parsePolygon(String exp) {
        if (exp.startsWith("POLYGON")) {
            if (exp.equals("POLYGON")) {
                return 10;
            } else {
                try {
                    int sides = Integer.parseInt(exp.substring("POLYGON".length()));
                    if (sides > 1) {
                        return sides;
                    } else {
                        throw new Exception("1-sided polygon");
                    }
                } catch (Exception e) {
                    return 10;
                }
            }
        }
        return -1;
    }

    /**************************************************************************
     * PUBLIC METHODS FOR PROCESSING BEHAVIORS
     *************************************************************************/

    public static int getPolygonSides(String exp) {
        return parsePolygon(exp);
    }

    public static Behavior.MultiLargeParams parseMultiLarge(String input) {
        String[] parameters = input.split("-");
        if (parameters.length != 4) {
            return null;
        }
        if (!parameters[0].equals("MULTILARGE")) {
            return null;
        }

        Behavior.MultiLargeParams params = new Behavior.MultiLargeParams();
        try {
            params.switchingPoint = Integer.parseInt(parameters[1]);
            params.behavior1 = stringToLocalBehavior(parameters[2]);
            if (params.behavior1 == Behavior.LocalBehavior.GENETIC) {
                params.behavior1Genetics = parseGenetic(parameters[2]);
            }
            params.behavior2 = stringToLocalBehavior(parameters[3]);
            if (params.behavior2 == Behavior.LocalBehavior.GENETIC) {
                params.behavior2Genetics = parseGenetic(parameters[3]);
            }
            if (params.behavior1 == Behavior.LocalBehavior.UNKNOWN ||
                params.behavior2 == Behavior.LocalBehavior.UNKNOWN) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        return params;
    }

    public static Behavior.MultiLargeParams parseMultiLargeFixed(String input) {
        String[] parameters = input.split("-");
        if (parameters.length != 4) {
            return null;
        }
        if (!parameters[0].equals("MULTILARGEFIXED")) {
            return null;
        }

        Behavior.MultiLargeParams params = new Behavior.MultiLargeParams();
        try {
            params.switchingPoint = Integer.parseInt(parameters[1]);
            params.behavior1 = stringToLocalBehavior(parameters[2]);
            if (params.behavior1 == Behavior.LocalBehavior.GENETIC) {
                params.behavior1Genetics = parseGenetic(parameters[2]);
            }
            params.behavior2 = stringToLocalBehavior(parameters[3]);
            if (params.behavior2 == Behavior.LocalBehavior.GENETIC) {
                params.behavior2Genetics = parseGenetic(parameters[3]);
            }
            if (params.behavior1 == Behavior.LocalBehavior.UNKNOWN ||
                    params.behavior2 == Behavior.LocalBehavior.UNKNOWN) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        return params;
    }

    public static Behavior.MultiLargeParams parseMultiLargeLocal(String input) {
        String[] parameters = input.split("-");
        if (parameters.length != 4) {
            return null;
        }
        if (!parameters[0].equals("MULTILARGELOCAL")) {
            return null;
        }

        Behavior.MultiLargeParams params = new Behavior.MultiLargeParams();
        try {
            params.switchingPoint = Integer.parseInt(parameters[1]);
            params.behavior1 = stringToLocalBehavior(parameters[2]);
            if (params.behavior1 == Behavior.LocalBehavior.GENETIC) {
                params.behavior1Genetics = parseGenetic(parameters[2]);
            }
            params.behavior2 = stringToLocalBehavior(parameters[3]);
            if (params.behavior2 == Behavior.LocalBehavior.GENETIC) {
                params.behavior2Genetics = parseGenetic(parameters[3]);
            }
            if (params.behavior1 == Behavior.LocalBehavior.UNKNOWN ||
                    params.behavior2 == Behavior.LocalBehavior.UNKNOWN) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        return params;
    }

    public static Behavior.MultiHerdParams parseMultiHerd(String input) {
        String[] parameters = input.split("-");
        if (parameters.length != 6) {
            return null;
        }
        if (!parameters[0].equals("MULTIHERD")) {
            return null;
        }

        Behavior.MultiHerdParams params = new Behavior.MultiHerdParams();
        try {
            params.initialBehavior = stringToGlobalBehavior(parameters[1]);
            if (params.initialBehavior == Behavior.GlobalBehavior.POLYGON) {
                params.initialBehaviorSides = parsePolygon(parameters[1]);
            }
            params.followBehavior = stringToLocalBehavior(parameters[2]);
            params.stopFollowingPoint = Double.parseDouble(parameters[3]);
            params.finalBehavior = stringToGlobalBehavior(parameters[4]);
            if (params.finalBehavior == Behavior.GlobalBehavior.POLYGON) {
                params.finalBehaviorSides = parsePolygon(parameters[4]);
            }
            params.finalBehaviorRadius = Double.parseDouble(parameters[5]);

            if (params.initialBehavior == Behavior.GlobalBehavior.UNKNOWN ||
                params.followBehavior == Behavior.LocalBehavior.UNKNOWN ||
                params.finalBehavior == Behavior.GlobalBehavior.UNKNOWN ||
                params.initialBehaviorSides == -1 ||
                params.finalBehaviorSides == -1) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        return params;
    }

    public static Behavior.GeneticLocalBehaviorParams parseGenetic(String input) {
        String[] parameters = input.split("_");
        if (parameters.length != 2) {
            return null;
        }
        if (!parameters[0].equals("GENETIC")) {
            return null;
        }

        Behavior.GeneticLocalBehaviorParams params = new Behavior.GeneticLocalBehaviorParams();
        params.genomeFile = parameters[1];
        params.localBehavior = GenomeFileParser.read(params.genomeFile);
        if (params.localBehavior == null) {
            return null;
        }

        return params;
    }

    public static String multiLargeToString(Behavior.MultiLargeParams params) {
        if (params == null) {
            return "Unknown";
        }
        return "MULTILARGE-" + params.switchingPoint + "-" +
            localBehaviorToString(params.behavior1, params.behavior1Genetics) + "-" +
            localBehaviorToString(params.behavior2, params.behavior2Genetics);
    }

    public static String multiLargeFixedToString(Behavior.MultiLargeParams params) {
        if (params == null) {
            return "Unknown";
        }
        return "MULTILARGEFIXED-" + params.switchingPoint + "-" +
                localBehaviorToString(params.behavior1, params.behavior1Genetics) + "-" +
                localBehaviorToString(params.behavior2, params.behavior2Genetics);
    }

    public static String multiLargeLocalToString(Behavior.MultiLargeParams params) {
        if (params == null) {
            return "Unknown";
        }
        return "MULTILARGELOCAL-" + params.switchingPoint + "-" +
                localBehaviorToString(params.behavior1, params.behavior1Genetics) + "-" +
                localBehaviorToString(params.behavior2, params.behavior2Genetics);
    }

    public static String multiHerdToString(Behavior.MultiHerdParams params) {
        if (params == null) {
            return "Unknown";
        }
        return "MULTIHERD-" +
            globalBehaviorToString(params.initialBehavior, params.initialBehaviorSides, null, null) + "-" +
            localBehaviorToString(params.followBehavior, null) + "-" +
            params.stopFollowingPoint + "-" +
            globalBehaviorToString(params.finalBehavior, params.finalBehaviorSides, null, null) + "-" +
            params.finalBehaviorRadius;
    }

    public static Behavior.LocalBehavior stringToLocalBehavior(String exp) {
        if (exp.equals("FACE_EAST")) {
            return Behavior.LocalBehavior.FACE_EAST;
        } else if (exp.equals("OFFSET_MOMENTUM")) {
            return Behavior.LocalBehavior.OFFSET_MOMENTUM;
        } else if (exp.equals("ONE_STEP_LOOKAHEAD")) {
            return Behavior.LocalBehavior.ONE_STEP_LOOKAHEAD;
        } else if (exp.equals("TWO_STEP_LOOKAHEAD")) {
            return Behavior.LocalBehavior.TWO_STEP_LOOKAHEAD;
        } else if (exp.equals("COORDINATED")) {
            return Behavior.LocalBehavior.COORDINATED;
        } else if (exp.equals("MIN_AVG_DIR_FLOCK")) {
            return Behavior.LocalBehavior.MIN_AVG_DIR_FLOCK;
        } else if (exp.equals("MIN_AVG_DIR_NEIGH")) {
            return Behavior.LocalBehavior.MIN_AVG_DIR_NEIGH;
        } else if (exp.equals("COUZIN")) {
            return Behavior.LocalBehavior.COUZIN;
        } else if (parseGenetic(exp) != null) {
            return Behavior.LocalBehavior.GENETIC;
        } else {
            return Behavior.LocalBehavior.UNKNOWN;
        }
    }

    public static Behavior.GlobalBehavior stringToGlobalBehavior(String exp) {
        if (exp.equals("FACE")) {
            return Behavior.GlobalBehavior.FACE;
        } else if (exp.equals("RANDOM")) {
            return Behavior.GlobalBehavior.RANDOM;
        } else if (exp.equals("CIRCLE")) {
            return Behavior.GlobalBehavior.CIRCLE;
        } else if (parsePolygon(exp) > -1) {
            return Behavior.GlobalBehavior.POLYGON;
        } else if (parseMultiLarge(exp) != null) {
            return Behavior.GlobalBehavior.MULTILARGE;
        } else if (parseMultiLargeFixed(exp) != null) {
            return Behavior.GlobalBehavior.MULTILARGEFIXED;
        } else if (parseMultiLargeLocal(exp) != null) {
            return Behavior.GlobalBehavior.MULTILARGELOCAL;
        } else if (parseMultiHerd(exp) != null) {
            return Behavior.GlobalBehavior.MULTIHERD;
        } else {
            return Behavior.GlobalBehavior.UNKNOWN;
        }
    }

    public static String localBehaviorToString(Behavior.LocalBehavior behavior,
                                               Behavior.GeneticLocalBehaviorParams geneticParams) {
        switch (behavior) {
            case FACE_EAST: return "FACE_EAST";
            case OFFSET_MOMENTUM: return "OFFSET_MOMENTUM";
            case ONE_STEP_LOOKAHEAD: return "ONE_STEP_LOOKAHEAD";
            case TWO_STEP_LOOKAHEAD: return "TWO_STEP_LOOKAHEAD";
            case COORDINATED: return "COORDINATED";
            case MIN_AVG_DIR_FLOCK: return "MIN_AVG_DIR_FLOCK";
            case MIN_AVG_DIR_NEIGH: return "MIN_AVG_DIR_NEIGH";
            case COUZIN: return "COUZIN";
            case GENETIC: return "GENETIC_" + geneticParams.genomeFile;
            default: return "Unknown";
        }
    }

    public static String globalBehaviorToString(
            Behavior.GlobalBehavior behavior, int polygonSides, Behavior.MultiLargeParams multiLargeParams,
            Behavior.MultiHerdParams multiHerdParams) {
        switch (behavior) {
            case FACE: return "FACE";
            case RANDOM: return "RANDOM";
            case CIRCLE: return "CIRCLE";
            case POLYGON: return "POLYGON" + polygonSides;
            case MULTILARGE: return multiLargeToString(multiLargeParams);
            case MULTILARGEFIXED: return multiLargeFixedToString(multiLargeParams);
            case MULTILARGELOCAL: return multiLargeLocalToString(multiLargeParams);
            case MULTIHERD: return multiHerdToString(multiHerdParams);
            default: return "Unknown";
        }
    }
}

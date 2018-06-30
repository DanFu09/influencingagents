package sim.app.flockers.placement;

public class PlacementParsing {

    public static String getPlacement(Placement.PlacementFlag flag) {
        switch (flag) {
            case RANDOM_RECT: return "RANDOM_RECT";
            case GRID_RECT: return "GRID_RECT";
            case BORDER_RECT: return "BORDER_RECT";
            case RANDOM_CIRCLE: return "RANDOM_CIRCLE";
            case GRID_CIRCLE: return "GRID_CIRCLE";
            case BORDER_CIRCLE: return "BORDER_CIRCLE";
            case K_MEANS: return "K_MEANS";
            default: return "Unknown";
        }
    }

    public static Placement.PlacementFlag readPlacement(String exp) {
        if (exp.equals("RANDOM_RECT")) {
            return Placement.PlacementFlag.RANDOM_RECT;
        } else if (exp.equals("GRID_RECT")) {
            return Placement.PlacementFlag.GRID_RECT;
        } else if (exp.equals("BORDER_RECT")) {
            return Placement.PlacementFlag.BORDER_RECT;
        } else if (exp.equals("RANDOM_CIRCLE")) {
            return Placement.PlacementFlag.RANDOM_CIRCLE;
        } else if (exp.equals("GRID_CIRCLE")) {
            return Placement.PlacementFlag.GRID_CIRCLE;
        } else if (exp.equals("BORDER_CIRCLE")) {
            return Placement.PlacementFlag.BORDER_CIRCLE;
        } else if (exp.equals("K_MEANS")) {
            return Placement.PlacementFlag.K_MEANS;
        } else {
            return Placement.PlacementFlag.UNKNOWN;
        }
    }
}

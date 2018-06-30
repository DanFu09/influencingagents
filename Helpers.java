package sim.app.flockers;
import sim.util.*;

public class Helpers {
	// Return the angle in radians
	public static double calcAngle(Double2D v1, Double2D v2) {
		double angle = 0;
		if (v1.lengthSq() != 0 && v2.lengthSq() != 0) {
            double cosAngle =
                (v1.x*v2.x + v1.y*v2.y)/(Math.sqrt(v1.lengthSq() * v2.lengthSq()));
            if (cosAngle > 1) {
                angle = 0;
            } else {
                angle = Math.acos(cosAngle);
            }
        }

        return angle;
	}
}

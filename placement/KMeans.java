package sim.app.flockers.placement;

import sim.app.flockers.Flocker;
import sim.app.flockers.Flockers;
import sim.util.Double2D;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * Created by danfu on 11/25/17.
 */
public class KMeans extends Placement {

    // need to choose k natural birds, where k = # of adhoc agents
    // later assign each adhoc agent to a different cluster
    public KMeans(PlacementParams params) {
        this.params = params;
    }

    // place the adhoc agents
    // This code largely taken from Katie Genter's code
    public void setup(List<Flocker> boids) {
        if (params.numAdhoc == 0) {
            return;
        }

        try {

            StringWriter sw1 = new StringWriter();
            sw1.write("@relation location\n");
            sw1.write("@attribute x real\n");
            sw1.write("@attribute y real\n");
            sw1.write("@data\n");

            // storing locations of each one of the non-adhoc agents
            for (int i = 0; i < params.state.numFlockers - params.numAdhoc; i++) {
                Double2D loc = boids.get(i).loc;
                sw1.write(loc.x + "," + loc.y + "\n");
            }

            String dataString = sw1.toString();

            Instances data = new Instances(new BufferedReader(new StringReader(dataString)));
            SimpleKMeans clusterer = new SimpleKMeans();

            // choose k clusters, where k = # adhoc agents
            int cluster_num = params.numAdhoc;
            if (params.state.numFlockers < params.numAdhoc * 2) {
                cluster_num = params.state.numFlockers - params.numAdhoc;
            }

            clusterer.setNumClusters(cluster_num);
            clusterer.buildClusterer(data);
            Instances clusterCentroids = clusterer.getClusterCentroids();

            // add adhoc agents to each centroid (cluster_num centroids)

            for (int i = 0; i < clusterCentroids.numInstances(); i++) {
                double[] values = clusterCentroids.instance(i).toDoubleArray();
                addNewAdhocFlocker(new Double2D(values[0], values[1]), boids);
                // System.out.println("Adhoc #, Should Reach 90");
                // System.out.println(i + 1);
                // System.out.println("Location Values");
                // System.out.println(values[0]);
                // System.out.println(values[1]);
            }

            int leftover = params.numAdhoc - clusterCentroids.numInstances();

            // add the
            while (leftover > 0) {
                int count = clusterCentroids.numInstances();

                if (leftover < count) {
                    count = leftover;
                }

                for (int i = 0; i < count; i++) {
                    double[] values = clusterCentroids.instance(i).toDoubleArray();
                    // System.out.println("Leftover Number of Adhoc Agents");
                    // System.out.println(leftover);
                    // System.out.println("Location Values");
                    // System.out.println(values[0]);
                    // System.out.println(values[1]);
                    addNewAdhocFlocker(new Double2D(values[0], values[1]), boids);
                }

                leftover -= count;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

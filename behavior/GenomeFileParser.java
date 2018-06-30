package sim.app.flockers.behavior;

import java.io.*;

public class GenomeFileParser {
    /**
     * Expects a file whose first line indicates the type of genome (AST for the AST behavior)
     * The rest of the lines indicate the genome; new lines are concatenated without line breaks
     *
     * Examples:
     *     AST
     *     (exp (mul index heading) 0.5)
     *
     *     AST
     *     (asin (cos (sin (acos 0))))
     */
    public static GeneticLocalBehavior read(String genomeFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(genomeFile))) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("File empty!");
            }

            String genomeType = line;
            String genome = "";
            while ((line = reader.readLine()) != null) {
                genome += line;
            }

            if (genomeType.equals("AST")) {
                try {
                    return new GeneticLocalBehaviorASTImpl(genome);
                } catch (GeneticLocalBehaviorASTImpl.ParseException ex) {
                    System.err.println(ex.getMessage());
                }
            } else {
                throw new IOException("Genome type not recognized!");
            }
        } catch (IOException x) {
            System.err.println(x.getMessage());
        }

        return null;
    }
}

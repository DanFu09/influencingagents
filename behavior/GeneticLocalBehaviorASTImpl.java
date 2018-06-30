package sim.app.flockers.behavior;

import sim.util.Double2D;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GeneticLocalBehaviorASTImpl implements GeneticLocalBehavior {
    private class Genome {
        public Node ast;
        public boolean accumulatorInitToGoal;

        public Genome(String genome) throws ParseException {
            String accumulatorGoal = "acc=goal";
            String accumulatorCurrent = "acc=current";

            String[] parts = genome.split(";");
            if (parts.length != 2 || !(parts[0].equals(accumulatorCurrent) ||
                    parts[0].equals(accumulatorGoal))) {
                throw new ParseException("Expecting genome of form \"acc=[current|goal];PROG\"");
            }
            accumulatorInitToGoal = parts[0].equals(accumulatorGoal);
            ast = parseProg(parseSExp(parts[1]));
        }
    }

    private Genome genome;

    public GeneticLocalBehaviorASTImpl(String prog) throws ParseException {
        genome = new Genome(prog);
    }

    private ExecArgs convertNeighborToNeighborBoid(Behavior.NeighborBoid neighbor,
                                                   Double2D myDirection,
                                                   Double2D goal,
                                                   int numNeighbors,
                                                   double neighborhoodSize) {
        ExecArgs args = new ExecArgs(
                (neighbor.relativePosition.angle() - myDirection.angle()) / Math.PI, // direction
                neighbor.relativePosition.length() / neighborhoodSize, // distance
                (neighbor.direction.angle() - myDirection.angle()) / Math.PI, // heading
                1.0 / numNeighbors, // influence
                0, // index
                goal.angle() - myDirection.angle(), // goal
                0 // acc
        );
        return args;
    }

    private double fold(List<ExecArgs> args, double initAcc) {
        double acc = initAcc;

        for (ExecArgs arg : args) {
            arg.setAccumulator(acc);
            acc = genome.ast.execute(arg);
        }

        return acc;
    }

    @Override
    public Double2D calcNewDirection(List<Behavior.NeighborBoid> neighbors,
                                     Double2D myDirection, Double2D goal,
                                     double neighborhoodSize) {
        List<ExecArgs> args = IntStream.range(0, neighbors.size())
            .mapToObj(i -> convertNeighborToNeighborBoid(neighbors.get(i),
                    myDirection,
                    goal,
                    neighbors.size(),
                    neighborhoodSize))
            .collect(Collectors.toList());
        Collections.sort(args, (ExecArgs arg1, ExecArgs arg2) ->
                         Double.compare(arg1.getDistance(), arg2.getDistance()));

        for (int i = 0; i < args.size(); i++) {
            args.get(i).setIndex((double) i / args.size());
        }
        Collections.reverse(args);

        Double2D initDirection = genome.accumulatorInitToGoal ? goal : myDirection;
        double initAcc = (initDirection.angle() - myDirection.angle()) / Math.PI;
        double finalAngleRelativeToMe = fold(args, initAcc) * Math.PI;
        double finalAngle = myDirection.angle() + finalAngleRelativeToMe;

        return new Double2D(Math.cos(finalAngle), Math.sin(finalAngle));
    }

    private class ExecArgs {
        private double direction;
        private double distance;
        private double heading;
        private double influence;
        private double index;
        private double goal;
        private double acc;

        public ExecArgs(double direction, double distance, double heading,
                        double influence, double index, double goal, double acc) {
            this.direction = direction;
            this.distance = distance;
            this.heading = heading;
            this.influence = influence;
            this.index = index;
            this.goal = goal;
            this.acc = acc;
        }

        public double getDirection() {
            return direction;
        }

        public double getDistance() {
            return distance;
        }

        public double getHeading() {
            return heading;
        }

        public double getInfluence() {
            return influence;
        }

        public double getIndex() {
            return index;
        }

        public double getGoal() {
            return goal;
        }

        public double getAccumulator() {
            return acc;
        }

        public void setDirection(double arg) {
            direction = arg;
        }

        public void setDistance(double arg) {
            distance = arg;
        }

        public void setHeading(double arg) {
            heading = arg;
        }

        public void setIndex(double arg) {
            index = arg;
        }

        public void setGoal(double arg) {
            goal = arg;
        }

        public void setAccumulator(double arg) {
            acc = arg;
        }

        @Override
        public String toString() {
            return String.format("dir: %f, dist: %f, heading: %f, inf: %f, ind: %f, goal: %f, acc: %f", direction,
                    distance, heading, influence, index, goal, acc);
        }
    }

    private static interface Node {
        public double execute(ExecArgs args);
    }

    private static class DirectionNode implements Node {
        public double execute(ExecArgs args) {
            return args.getDirection();
        }
    }

    private static class DistanceNode implements Node {
        public double execute(ExecArgs args) {
            return args.getDistance();
        }
    }

    private static class HeadingNode implements Node {
        public double execute(ExecArgs args) {
            return args.getHeading();
        }
    }

    private static class InfluenceNode implements Node {
        public double execute(ExecArgs args) {
            return args.getInfluence();
        }
    }

    private static class IndexNode implements Node {
        public double execute(ExecArgs args) {
            return args.getIndex();
        }
    }

    private static class GoalNode implements Node {
        public double execute(ExecArgs args) {
            return args.getGoal();
        }
    }

    private static class AccumulatorNode implements Node {
        public double execute(ExecArgs args) {
            return args.getAccumulator();
        }
    }

    private static class ConstNode implements Node {
        private double f;

        public ConstNode(double f) {
            this.f = f;
        }

        public double execute(ExecArgs args) {
            return f;
        }
    }

    private static double wrap(double f) {
        if (f < -1) {
            f += 2;
        }
        else if (f > 1) {
            f -= 2;
        }
        assert(-1 <= f && f <= 1);
        return f;
    }

    private static class GezNode implements Node {
        private Node cond, e1, e2;

        public GezNode(Node cond, Node e1, Node e2) {
            this.cond = cond;
            this.e1 = e1;
            this.e2 = e2;
        }

        public double execute(ExecArgs args) {
            return (cond.execute(args) >= 0)
                ? e1.execute(args)
                : e2.execute(args);
        }
    }

    private static class AddNode implements Node {
        private Node rhs, lhs;

        public AddNode(Node rhs, Node lhs) {
            this.rhs = rhs;
            this.lhs = lhs;
        }

        public double execute(ExecArgs args) {
            return wrap(lhs.execute(args) + rhs.execute(args));
        }
    }

    private static class SubNode implements Node {
        private Node rhs, lhs;

        public SubNode(Node rhs, Node lhs) {
            this.rhs = rhs;
            this.lhs = lhs;
        }

        public double execute(ExecArgs args) {
            return wrap(lhs.execute(args) - rhs.execute(args));
        }
    }

    private static class MulNode implements Node {
        private Node rhs, lhs;

        public MulNode(Node rhs, Node lhs) {
            this.rhs = rhs;
            this.lhs = lhs;
        }

        public double execute(ExecArgs args) {
            return lhs.execute(args) * rhs.execute(args);
        }
    }

    private static class ExpNode implements Node {
        private Node rhs, lhs;

        public ExpNode(Node rhs, Node lhs) {
            this.rhs = rhs;
            this.lhs = lhs;
        }

        public double execute(ExecArgs args) {
            return Math.pow(lhs.execute(args), 1 + rhs.execute(args));
        }
    }

    private static class SinNode implements Node {
        private Node arg;

        public SinNode(Node arg) {
            this.arg = arg;
        }

        public double execute(ExecArgs args) {
            return Math.sin(Math.PI * arg.execute(args));
        }
    }

    private static class CosNode implements Node {
        private Node arg;

        public CosNode(Node arg) {
            this.arg = arg;
        }

        public double execute(ExecArgs args) {
            return Math.cos(Math.PI * arg.execute(args));
        }
    }

    private static class AsinNode implements Node {
        private Node arg;

        public AsinNode(Node arg) {
            this.arg = arg;
        }

        // adjusted to return values in the range [-1, 1]
        public double execute(ExecArgs args) {
            return 2 * Math.asin(arg.execute(args)) / Math.PI;
        }
    }

    private static class AcosNode implements Node {
        private Node arg;

        public AcosNode(Node arg) {
            this.arg = arg;
        }

        // adjusted to return values in the range [-1, 1]
        public double execute(ExecArgs args) {
            return 2 * (Math.acos(arg.execute(args)) / Math.PI) - 1;
        }
    }

    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }

    private static class SExp {
        public boolean isList;
        public ArrayList<SExp> list = null;
        public String str = null;

        public SExp(ArrayList<SExp> list) {
            isList = true;
            this.list = list;
        }

        public SExp(String str) {
            isList = false;
            this.str = str;
        }

        @Override
        public String toString() {
            if (isList) {
                StringBuilder builder = new StringBuilder();
                builder.append('(');
                for (int i = 0; i < list.size() - 1; i++) {
                    builder.append(list.get(i).toString());
                    builder.append(' ');
                }
                if (!list.isEmpty()) {
                    builder.append(list.get(list.size() - 1).toString());
                }
                builder.append(')');
                return builder.toString();
            } else {
                return str;
            }
        }
    }

    private static SExp parseSExp(String prog) throws ParseException {
        try {
            Stack<ArrayList<SExp>> stack = new Stack<>();
            StringBuilder builder = new StringBuilder();
            boolean isBuilding = false;

            // root list
            stack.push(new ArrayList<>());

            // parse sexp
            for (char c : prog.toCharArray()) {
                // end of string
                if (isBuilding &&
                    (Character.isWhitespace(c) || c == ')' || c == '(')) {
                    stack.peek().add(new SExp(builder.toString()));
                    builder = new StringBuilder();
                    isBuilding = false;
                }

                // end of list
                if (c == ')') {
                    ArrayList<SExp> l = stack.pop();
                    stack.peek().add(new SExp(l));
                }

                // start of list
                else if (c == '(') {
                    stack.push(new ArrayList<>());
                }

                // part of a string
                else if (!Character.isWhitespace(c)) {
                    builder.append(c);
                    isBuilding = true;
                }
            }

            // end of string if no lists
            if (isBuilding) {
                stack.peek().add(new SExp(builder.toString()));
            }

            ArrayList<SExp> rootList = stack.pop();

            // check that there is nothing left
            if (!stack.empty() || rootList.size() != 1) {
                throw new ParseException("Invalid SExp");
            }
            return rootList.get(0);

        } catch (EmptyStackException e) {
            throw new ParseException("Invalid SExp");
        }
    }

    private static Node parseProg(SExp prog) throws ParseException {
        if (!prog.isList) {
            try {
                double f = Double.parseDouble(prog.str);
                if (f < -1 || f > 1) {
                    throw new ParseException("Constants must be in [-1, 1]");
                }
                return new ConstNode(f);
            } catch (NumberFormatException e) {}

            switch (prog.str) {
            case "direction":
                return new DirectionNode();
            case "distance":
                return new DistanceNode();
            case "heading":
                return new HeadingNode();
            case "influence":
                return new InfluenceNode();
            case "index":
                return new IndexNode();
            case "goal":
                return new GoalNode();
            case "acc":
                return new AccumulatorNode();
            default:
                throw new ParseException("Unknown variable: " + prog.str);
            }
        }

        if (prog.list.isEmpty()) {
            throw new ParseException("Empty expression");
        }
        SExp cmd = prog.list.get(0);
        if (cmd.isList) {
            throw new ParseException("Expression without constructor");
        }
        switch (cmd.str) {
        case "gez":
            if (prog.list.size() != 4) {
                throw new ParseException("gez needs three arguments");
            }
            return new GezNode(parseProg(prog.list.get(1)),
                               parseProg(prog.list.get(2)),
                               parseProg(prog.list.get(3)));
        case "add":
            if (prog.list.size() != 3) {
                throw new ParseException("add needs two arguments");
            }
            return new AddNode(parseProg(prog.list.get(1)),
                               parseProg(prog.list.get(2)));
        case "sub":
            if (prog.list.size() != 3) {
                throw new ParseException("sub needs two arguments");
            }
            return new SubNode(parseProg(prog.list.get(1)),
                               parseProg(prog.list.get(2)));
        case "mul":
            if (prog.list.size() != 3) {
                throw new ParseException("mul needs two arguments");
            }
            return new MulNode(parseProg(prog.list.get(1)),
                               parseProg(prog.list.get(2)));
        case "exp":
            if (prog.list.size() != 3) {
                throw new ParseException("exp needs two arguments");
            }
            return new ExpNode(parseProg(prog.list.get(1)),
                               parseProg(prog.list.get(2)));
        case "sin":
            if (prog.list.size() != 2) {
                throw new ParseException("sin needs one argument");
            }
            return new SinNode(parseProg(prog.list.get(1)));
        case "cos":
            if (prog.list.size() != 2) {
                throw new ParseException("cos needs one argument");
            }
            return new CosNode(parseProg(prog.list.get(1)));
        case "asin":
            if (prog.list.size() != 2) {
                throw new ParseException("asin needs one argument");
            }
            return new AsinNode(parseProg(prog.list.get(1)));
        case "acos":
            if (prog.list.size() != 2) {
                throw new ParseException("acos needs one argument");
            }
            return new AcosNode(parseProg(prog.list.get(1)));
        default:
            throw new ParseException("Unknown constructor: " + cmd.str);
        }
    }

    // TESTING

    private static List<String> sexpValid = Arrays.asList(
            "()", "()",
            "(  )", "()",
            " ( ) ", "()",
            "hello", "hello",
            " hello ", "hello",
            "(hello)", "(hello)",
            "( hello )", "(hello)",
            "(hello world)", "(hello world)",
            "(hello (inner list))", "(hello (inner list))",
            "(hello( inner  list ) )", "(hello (inner list))",
            "((hello inner) list)", "((hello inner) list)",
            "( (hello inner)list)", "((hello inner) list)",
            "(((hi)))", "(((hi)))",
            "( ((a ) ( b c) ) (d(e)f)g)", "(((a) (b c)) (d (e) f) g)"
    );

    private static List<String> sexpInvalid = Arrays.asList(
            "(",
            ")",
            ")(",
            "(()",
            "())",
            "hi bye",
            "(hi) bye",
            "hi (bye)",
            "hi)",
            "(hi"
    );

    private static List<String> progValid = Arrays.asList(
            "0",
            "-1",
            "1",
            "-0.5",
            "direction",
            "distance",
            "influence",
            "heading",
            "index",
            "goal",
            "acc",
            "(sin .5)",
            "(asin (cos (sin (acos 0))))",
            "(add acc (sin direction))",
            "(exp (mul index heading) 0.5)"
    );

    private static List<String> progInvalid = Arrays.asList(
            "-1.1",
            "1.1",
            "0.5b",
            "abc",
            "(sin 0 1)",
            "(add acc heading index)",
            "(exp .5)"
    );

    private static List<String> genomeValid = Stream.concat(
            progValid.stream()
                    .map(elt -> "acc=current;" + elt),
            progValid.stream()
                    .map(elt -> "acc=goal;" + elt))
            .collect(Collectors.toList());

    private static List<String> genomeInvalid = Stream.concat(
            progInvalid.stream(),
            Stream.concat(progValid.stream()
                    .map(elt -> "acc=nada;" + elt),
            progInvalid.stream()
                    .map(elt -> "acc=current;" + elt)))
            .collect(Collectors.toList());

    public static void main(String[] argv) {
        // NOTE: run with java -ea to enable assertions

        // valid s-expressions
        for (int i = 0; i < sexpValid.size(); i += 2) {
            System.out.printf("Testing valid: %s\n", sexpValid.get(i));
            try {
                String input = sexpValid.get(i);
                String correct = sexpValid.get(i+1);
                String output = parseSExp(input).toString();
                assert output.equals(correct) : output;
            }
            catch (ParseException e) {
                System.out.println(e.toString());
                assert false : sexpValid.get(i);
            }
        }

        // invalid s-expressions
        for (String s : sexpInvalid) {
            System.out.printf("Testing invalid: %s\n", s);
            try {
                parseSExp(s);
                assert false : s;
            } catch (ParseException e) {}
        }

        // valid programs
        for (String s : progValid) {
            System.out.printf("Testing valid: %s\n", s);
            try {
                parseProg(parseSExp(s));
            } catch (ParseException e) {
                System.out.println(e.toString());
                assert false : s;
            }
        }

        // invalid programs
        for (String s : progInvalid) {
            System.out.printf("Testing invalid: %s\n", s);
            try {
                parseProg(parseSExp(s));
                assert false : s;
            } catch (ParseException e) {}
        }

        // valid genomes
        for (String s: genomeValid) {
            System.out.printf("Testing valid genome: %s\n", s);
            try {
                new GeneticLocalBehaviorASTImpl(s);
            } catch (ParseException e) {
                System.out.println(e.toString());
                assert false : s;
            }
        }

        // invalid genomes
        for (String s: genomeInvalid) {
            System.out.printf("Testing invalid genome: %s\n", s);
            try {
                new GeneticLocalBehaviorASTImpl(s);
            } catch (ParseException e) {}
        }

        System.out.println("ok");
    }
}

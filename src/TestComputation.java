import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class TestComputation {

    public static int runGreedy(ProblemInstance instance) {
        Greedy greedy = new Greedy(instance);
        Schedule greedySchedule = greedy.getSchedule();
        return greedySchedule.getTardiness();
    }

    public static int runBestFirst(ProblemInstance instance) {
        BestFirst bestFirst = new BestFirst(instance);
        Schedule bestFirstSchedule = bestFirst.getSchedule();
        return bestFirstSchedule.getTardiness();
    }

    public static void testInstance (String src, int answer, boolean doGreedy, boolean doBestFirst) {

        ProblemInstance instance = ComputeTardiness.readInstance(src);
        System.out.println(src);

        if(doGreedy) {
            long t0 = System.nanoTime();
            int result = runGreedy(instance);
            long t1 = System.nanoTime();
            long time = (t1 - t0) / 1000000000;
            String status = (result == answer) ? "ok" : "should be " + answer;
            System.out.println("- Greedy: " + answer + "; " + status + "; " + time + " sec");
        }

        if(doBestFirst) {
            long t0 = System.nanoTime();
            int result = runBestFirst(instance);
            long t1 = System.nanoTime();
            long time = (t1 - t0) / 1000000000;
            String status = (result == answer) ? "ok" : " should be " + answer;
            System.out.println("- BestFirst: " + answer + "; " + status + "; " + time + " sec");
        }

    }

    public static void main (String args[]) {

        if(args.length != 4) {
            System.out.println("Usage: path/to/answer.file path/to/instance/dir doGreedy doBestFirst");
            System.out.println("  where doGreedy, doBestFirst = 0|1");
            return;
        }

        String answersFile = args[0];
        String instanceRoot = args[1];
        boolean doGreedy = args[2].equals("1");
        boolean doBestFirst = args[3].equals("1");

        try {
            Scanner sc = new Scanner(new BufferedReader(new FileReader(answersFile)));

            while(sc.hasNext()) {

                String path = sc.next();
                int ans = sc.nextInt();
                testInstance(instanceRoot + '/' + path, ans, doGreedy, doBestFirst);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }
}

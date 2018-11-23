import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

public class TestComputation {

    public static int runGreedy(ProblemInstance instance) {
        Greedy greedy = new Greedy(instance);
        Schedule schedule = greedy.getSchedule();
        return schedule.getTardiness();
    }

    public static int runDynamic(ProblemInstance instance) {

        Dynamic dynamic = new Dynamic(instance);
//        Schedule schedule = dynamic.getSchedule();
        try {
            return dynamic.calculateTardiness();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1000;
    }
    public static int runDynamicSq(ProblemInstance instance) {
        int[][] jobs = instance.getJobs();
        float[][] jobsDouble = new float[jobs.length][2];
        for (int i = 0; i < jobs.length; i++) {
            jobsDouble[i] = new float[]{jobs[i][0], jobs[i][1]};
        }
        DynamicSequence dynamic = new DynamicSequence(jobsDouble);
//        Schedule schedule = dynamic.getSchedule();
        try {
            return (int) dynamic.calculateTardiness();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1000;
    }

    public static int runApprox(ProblemInstance instance, float epsilon) {
        Approx approx = new Approx(instance, epsilon);
        try {
            return approx.calculateTardiness();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1000;
    }

    public static void testInstance(String src, int answer, boolean doGreedy, boolean doDynamic) throws Exception {

        ProblemInstance instance = ComputeTardiness.readInstance(src + ".dat");

        System.out.println(src + ".dat");
//        System.out.println(MeasureHardness.measure(instance));

        if (doGreedy) {
            long t0 = System.nanoTime();
            int result = runGreedy(instance);
            long t1 = System.nanoTime();
            long time = (t1 - t0) / 1000000;
            if (result != answer)
                System.out.println("- Greedy: Wrong answer: " + result + " should be " + answer);
//            else
            System.out.println("- Greedy: " + time + " ms");
        }

//        Thread.sleep(2000);
        if (doDynamic) {
            long t0 = System.nanoTime();
//            int result = runApprox(instance, (float)0.3);
            int result = runDynamicSq(instance);
//            int result = runDynamic(instance);
            long t1 = System.nanoTime();
            long time = (t1 - t0) / 1000000;
           if (result != answer)
               throw new Exception("- Dynamic: Wrong answer: " + result + " should be " + answer);
           else
                System.out.println("- Dynamic: " + time + " ms");
        }

    }

    public static void main(String args[]) {


        if (args.length != 6) {
            System.out.println("Usage: path/to/answer.file path/to/instance/dir doGreedy doBestFirst start limit");
            System.out.println("  where doGreedy, doDynamic = 0|1");
            return;
        }

        String answersFile = args[0];
        String instanceRoot = args[1];
        boolean doGreedy = args[2].equals("1");
        boolean doDynamic = args[3].equals("1");
        int start = Integer.parseInt(args[4]);
        int limit = Integer.parseInt(args[5]);
        int x = 0;

        try {
            Scanner sc = new Scanner(new BufferedReader(new FileReader(answersFile)));

            while (sc.hasNext() && x++ < (start + limit)) {
                String path = sc.next();
                int ans = sc.nextInt();


                if (x >= start) {
//                    Thread.sleep(2000);
                    testInstance(instanceRoot + '/' + path, ans, doGreedy, doDynamic);
//                    Thread.sleep(2000);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}

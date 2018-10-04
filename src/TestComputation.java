import java.io.BufferedReader;
import java.io.FileNotFoundException;
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
            return dynamic.getMinTard();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1000;
    }

    public static void testInstance (String src, int answer, boolean doGreedy, boolean doDynamic) {

        ProblemInstance instance = ComputeTardiness.readInstance(src);
        System.out.println(src);

        if(doGreedy) {
            long t0 = System.nanoTime();
            int result = runGreedy(instance);
            long t1 = System.nanoTime();
            long time = (t1 - t0) / 1000000000;
            String status = (result == answer) ? "ok" : "should be " + answer;
            System.out.println("- Greedy: " + result + "; " + status + "; " + time + " sec");
        }

        if(doDynamic) {
            long t0 = System.nanoTime();
            int result = runDynamic(instance);
            long t1 = System.nanoTime();
            long time = (t1 - t0) / 1000000000;
            String status = (result == answer) ? "ok" : " should be " + answer;
            System.out.println("- Dynamic: " + result + "; " + status + "; " + time + " sec");
        }

    }

    public static void main (String args[]) {

        if(args.length != 5) {
            System.out.println("Usage: path/to/answer.file path/to/instance/dir doGreedy doBestFirst limit");
            System.out.println("  where doGreedy, doDynamic = 0|1");
            return;
        }

        String answersFile = args[0];
        String instanceRoot = args[1];
        boolean doGreedy = args[2].equals("1");
        boolean doDynamic = args[3].equals("1");
        int limit = Integer.parseInt(args[4]);
        int x = 0;

        try {
            Scanner sc = new Scanner(new BufferedReader(new FileReader(answersFile)));

            while(sc.hasNext() && x++ < limit) {

                String path = sc.next();
                int ans = sc.nextInt();
                testInstance(instanceRoot + '/' + path, ans, doGreedy, doDynamic);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }
}

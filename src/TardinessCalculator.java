import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class TardinessCalculator {

    /*
    First row is processing time, second row is due dates
     */
    private int[][] jobs;

    public static void main(String args[]) {
        if (args.length == 0) {
            System.exit(1);
        }
        FileInputStream in;
        try {
            in = new FileInputStream(args[1]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initJobs() {

    }

    private int calculateTardiness(int i, int j, int k, int t) {

        int minimalTardiness = Integer.MAX_VALUE;
        for (int delta = k; delta < jobs.length; delta++) {

            int kprime = findBiggestJob(getSubset(i, j, k));
            int cprime = calculateCompletionTime(
                    t,
                    getSubset(i, k + delta, kprime));

            int subsetTardiness = calculateTardiness(i, k + delta, kprime, t)
                    + Integer.max(0, cprime - jobs[1][kprime])
                    + calculateTardiness(kprime + delta + 1, j, kprime, cprime);
            if (minimalTardiness > subsetTardiness)
                minimalTardiness = subsetTardiness;
        }
        return minimalTardiness;
    }

    private int[] calculateDeltas() {
        return new int[]{};
    }

    /*
    S
     */
    private int[] getSubset(int i, int j, int k) {
        return new int[]{1};
    }

    private int calculateCompletionTime(int startingTime, int[] set) {

        int productionTimeSum = 0;
        for (int id : set)
            productionTimeSum += jobs[0][id];

        return startingTime + productionTimeSum;
    }

    private int findBiggestJob(int[] set) {
        int max = 0;
        int idOfMax = 0;

        for (int id : set) {
            if (max < jobs[0][id]) {
                max = jobs[0][id];
                idOfMax = id;
            }
        }

        return idOfMax;
    }
}

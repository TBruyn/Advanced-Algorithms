import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;


public class TardinessCalculator {

    /*
    First row is processing time, second row is due dates
     */
    private int[][] jobs;
    private int[][] testjobs = new int[][]{
            {98, 314},
            {26, 287},
            {82, 285},
            {67, 253},
            {85, 256}
    };

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

        if (i == j) return jobs[0][i];

        int minimalTardiness = Integer.MAX_VALUE;
        for (int delta = k; delta < jobs.length; delta++) {

            int kPrime = findBiggestJob(getSubset(i, j, k));
            int cPrime = calculateCompletionTime(
                    t, getSubset(i, k + delta, kPrime));

            int subsetTardiness = calculateTardiness(i, k + delta, kPrime, t)
                    + Integer.max(0, cPrime - jobs[1][kPrime])
                    + calculateTardiness(kPrime + delta + 1, j, kPrime, cPrime);
            if (minimalTardiness > subsetTardiness)
                minimalTardiness = subsetTardiness;
        }
        return minimalTardiness;
    }

    private ArrayList<Integer> calculateDeltas(ArrayList<Integer> set) {
        int k = findBiggestJob(set);
        return new ArrayList<Integer>();
    }

    /*
    S
     */
    private ArrayList<Integer> getSubset(int i, int j, int k) {
        ArrayList<Integer> subset = new ArrayList<>();

        for (int id = i; id <= j; id++) {
            if (jobs[0][id] < jobs[0][k]) {
                subset.add(id);
            }
        }
        return subset;
    }

    private int calculateCompletionTime(int startingTime, ArrayList<Integer> set) {

        int productionTimeSum = 0;
        for (int id : set)
            productionTimeSum += jobs[0][id];

        return startingTime + productionTimeSum;
    }

    private int findBiggestJob(ArrayList<Integer> set) {
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

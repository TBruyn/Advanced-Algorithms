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

    private int calculateTardiness(int[] set) {
        return 0;
    }

    /*
    S
     */
    private int[] getSubset(int i, int j, int k) {
        return null;
    }

    private int calculateCompletionTime(int startingTime, int[] set) {

        int productionTimeSum = 0;
        for (int id : set)
            productionTimeSum += jobs[0][id];

        return startingTime + productionTimeSum;
    }

    private int findMaxProcessingTime(int[] set) {
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

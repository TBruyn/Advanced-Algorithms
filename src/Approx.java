import java.util.Arrays;
import java.util.Comparator;

public class Approx {

    /**
     * The original problem data
     */
    private int[][] jobs;

    private float epsilon;

    public Approx(ProblemInstance instance, float epsilon) {

        this.jobs = instance.getJobs();

        this.epsilon = epsilon;

        // Earliest Due Date order
        Arrays.sort(this.jobs, new SortByDeadline()); // O(n log n)
    }

    /**
     * Given EDD order, calculate the maximum tardiness
     */
    public int getTmax() {
        int completionTime = 0;
        int maxTardiness = 0;

        for (int[] job : jobs) {
            completionTime += job[0];

            maxTardiness = Math.max(maxTardiness, completionTime - job[1]);
        }

        return maxTardiness;
    }

    /**
     * Given a list of job indices, compute the total tardiness of that sequence
     */
    public int computeTotalTardiness(int[] sequence) {
        int completionTime = 0;
        int totalTardiness = 0;

        for(int index: sequence) {
            completionTime += jobs[index][0];

            totalTardiness += Math.max(0, completionTime - jobs[index][1]);
        }

        return totalTardiness;
    }

    /**
     * Calculate the total tardiness on this problem instance
     */
    public int calculateTardiness() throws Exception {

        int maxTardiness = getTmax();

        if (maxTardiness == 0)
            return 0;

        // Compute K value according to Lawler (1982)
        int n = jobs.length;
        float K = maxTardiness * 2 * epsilon / (n * (n + 1));

        // Scale all jobs
        float[][] jobsScaled = new float[n][2];
        for (int i = 0; i < n; i++) {
            jobsScaled[i] = new float[]{ (float) Math.max(Math.floor(jobs[i][0] / K),1), (jobs[i][1] / K) };
        }

        // Run the exact algorithm on the scaled jobs
        DynamicSequence dyn = new DynamicSequence(jobsScaled, true);
        int[] seq = dyn.calculateSequence();

        // Return the total tardiness of the resulting sequence
        return computeTotalTardiness(seq);

    }

    /**
     * Sort the 2D jobs array by deadline (2nd element of each pair)
     */
    class SortByDeadline implements Comparator<int[]> {
        public int compare(int[] a, int[] b) {
            return Integer.compare(a[1], b[1]);
        }
    }

}

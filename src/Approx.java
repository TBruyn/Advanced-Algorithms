import java.util.Arrays;
import java.util.Comparator;

public class Approx {


    /**
     * The original problem data
     */
    private int[][] jobs;
    private float[][] jobsScaled;

    private float epsilon;

    public Approx(ProblemInstance instance, float epsilon) {

        jobs = instance.getJobs();
        this.epsilon = epsilon;

        // Earliest Due Date order
        Arrays.sort(jobs, new SortByDeadline()); // O(n log n)

    }

    public int getTmax() {
        int n = jobs.length;
        int t = 0;
        int Tmax = 0;

        for (int i = 0; i < n; i++) {
            t += jobs[i][0];
            Tmax = Math.max(Tmax, t - jobs[i][1]);
        }

        return Tmax;
    }

    public int calculateTardiness() throws Exception {

        int n = jobs.length;
        int Tmax = getTmax();

        if (Tmax == 0)
            return 0;

        float K = Tmax * 2 * epsilon / (n * (n + 1));

        jobsScaled = new float[n][2];

        for (int i = 0; i < n; i++) {
            jobsScaled[i] = new float[]{ (float) Math.floor(jobs[i][0] / K), (jobs[i][1] / K) };
        }

        DynamicSequence dyn = new DynamicSequence(jobsScaled);

        int[] seq = dyn.calculateSequence();

        int t = 0;
        int T = 0;
        for (int i = 0; i < n; i++) {
            int index = seq[i];
            t += jobs[index][0];
            T += Math.max(0, t - jobs[index][1]);
        }
        return T;

    }


    /**
     * Sort the 2D jobs array by deadline (2nd element of each pair)
     */
    class SortByDeadline implements Comparator<int[]> {
        public int compare(int[] a, int[] b) {
            return a[1] - b[1];
        }
    }

}

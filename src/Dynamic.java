import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class Dynamic {

    private int[][] jobs;

    /**
     * Store some performance data for evaluation
     */
    private MetricsBag metrics;

    /**
     * Store all calculated results for sub-problems (i,j,k,t)
     */
    private Store store;

    public Dynamic(ProblemInstance instance) {

        jobs = instance.getJobs();
        store = new Store(jobs.length);
        metrics = new MetricsBag();

        Arrays.sort(jobs, new SortByDeadline()); // O(n log n)
    }

    public int calculateTardiness() throws Exception {

        // Create a list of all jobs
        JobList list = JobList.fromArray(jobs);

        int result = calculateTardiness(list, -1, 0, 0);

        System.out.println(metrics.calls + "," + metrics.computations);

        return result;
    }

    public int calculateTardiness(JobList list, int k, int t, int depth) throws Exception {

        metrics.calls++;

        metrics.depth = Math.max(metrics.depth, depth);

        if(depth > jobs.length)
            throw new Exception("Depth cannot exceed number of jobs");

        // Limit i and j to the remaining elements in the list
        int i = list.start.index;
        int j = list.end.index;

        // Base case: empty set
        if (list.length == 0)
            return 0;

        // Base case: single element
        if (list.length == 1)
            return Math.max(0, t + jobs[list.start.index][0] - jobs[list.start.index][1]);

        // Check whether this sub-problem has been calculated before
        if (k >= 0) {
            int res = store.get(i, j, k, t);
            if (res >= 0)
                return res;
        }

        metrics.computations++;

        // Take the largest job from the list
        // - list: no longer contains kPrime
        int kPrime = list.extractMaxP(); // Runs O(n)
        if(kPrime == k) {
            throw new Exception("K = k'");
        }
        int lowestTardiness = Integer.MAX_VALUE;


        // Split the list at k', results in:
        // - list: the left side of the split
        // - right: the right side of the split
        JobList right = list.split(kPrime); // Runs O(n)

        // Original length of the list
        int originalLength = right.length;

        for (int d = 0; d <= originalLength; d++) {

            // The time until the left hand side is complete.
            int leftComplete = t + list.totalP;

            // Only compute when d_x > leftComplete, for x the first element in `right`
            if (right.start == null || !(jobs[right.start.index][1] <= leftComplete)) {

                // Recurse over the left list (if not empty)
                int tardinessLeft = list.length == 0 ? 0 :
                        calculateTardiness(list, kPrime, t, depth+1);

                int kPrimeDone = leftComplete + jobs[kPrime][0];
                int tardinessKPrime = Math.max(0, kPrimeDone - jobs[kPrime][1]);

                // Recurse over the right list (if not empty)
                int tardinessRight = right.length == 0 ? 0 :
                        calculateTardiness(right, kPrime, kPrimeDone, depth+1);

                int total = tardinessLeft + tardinessKPrime + tardinessRight;

                if (total < lowestTardiness) {
                    lowestTardiness = total;
                }
            }

            // Move over one item from the right to the left list
            if (right.length > 0)
                list.push(right.removeFirst()); // Runs O(1)

        }

        list.insert(kPrime); // Runs O(n), can improve by remembering beforeK node?

        // Store the result for this computation, except the root (k = -1)
        if (k >= 0)
            store.set(i, j, k, t, lowestTardiness);

        return lowestTardiness;
    }

    /**
     * Sort the 2D jobs array by deadline (2nd element of each pair)
     */
    class SortByDeadline implements Comparator<int[]> {
        public int compare(int[] a, int[] b) {
            return a[1] - b[1];
        }
    }

    /**
     * This class is used for memoization of all computations.
     */
    class Store {

        int x = 0;

        private HashMap<Integer, Integer>[][][] store;

        public Store(int size) {
            store = new HashMap[size][size][size];
        }

        public void set(int i, int j, int k, int t, int tardiness) {
            x++;
            if(x > 40000000) return;
            if (store[i][j][k] == null)
                store[i][j][k] = new HashMap<Integer, Integer>();

            store[i][j][k].put(t, tardiness);
        }

        /**
         * Return the tardiness of problem (i,j,k,t) or -1 if not available
         */
        public int get(int i, int j, int k, int t) {
            if (store[i][j][k] == null)
                return -1;

            Integer result = store[i][j][k].get(t);
            return result == null ? -1 : result;
        }

    }

    /**
     * Keep some metrics for performance tracking
     */
    class MetricsBag {
        public int calls;
        public int computations;
    }


}

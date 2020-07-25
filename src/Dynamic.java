import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Our implementation of the Minimum Tardiness algorithm by Lawler
 */
public class Dynamic {

    /**
     * The original problem data
     */
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

        return calculateTardiness(list, 0, 0);
    }

    /**
     * Given a list of jobs, a k-index and a starting time t, calculate the minimum tardiness.
     *
     * Note: depth is only passed for performance analysis.
     */
    public int calculateTardiness(JobList list, int t, int depth) throws Exception {

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

        int kPrime = list.extractMaxP(); // Runs O(n)

        // Check whether this sub-problem has been calculated before
        int res = store.get(i, j, kPrime, t);
        if (res >= 0) {
            // Restore list
            list.insert(kPrime);
            return res;
        }

        // Take the largest job from the list
        // - list: no longer contains kPrime

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
            if (right.start == null || jobs[right.start.index][1] > leftComplete) {

                // Recurse over the left list (if not empty)
                int tardinessLeft = list.length == 0 ? 0 :
                        calculateTardiness(list, t, depth+1);

                int kPrimeDone = leftComplete + jobs[kPrime][0];
                int tardinessKPrime = Math.max(0, kPrimeDone - jobs[kPrime][1]);

                // Recurse over the right list (if not empty)
                int tardinessRight = right.length == 0 ? 0 :
                        calculateTardiness(right, kPrimeDone, depth+1);

                int total = tardinessLeft + tardinessKPrime + tardinessRight;

                if (total < lowestTardiness) {
                    lowestTardiness = total;
                }
            }

            // Move over one item from the right to the left list.
            //   This ensures `list` is restored to its original state
            //   after all iterations.
            if (right.length > 0)
                list.push(right.removeFirst()); // Runs O(1)

        }

        // Re-insert node kPrime to restore the list
        list.insert(kPrime); // Runs O(n), can improve by remembering beforeK node?

        // Store the result for this computation, except the root (k = -1)
        store.set(i, j, kPrime, t, lowestTardiness);

        return lowestTardiness;
    }

    /**
     * Sort the 2D jobs array by deadline (2nd element of each pair)
     */
    class SortByDeadline implements Comparator<int[]> {
        public int compare(int[] a, int[] b) {
            return Integer.compare(a[1], b[1]);
        }
    }

    /**
     * Caching of computations is done using a 3 dimensional array
     * of HashMaps. The dimensions are `i`, `j` and `k` which range
     * from 0 to n (number of jobs). The time `t` can range from
     * 0 to n * pMax (the largest processing time).
     */
    class Store {

        private HashMap<Integer, Integer>[][][] store;

        public Store(int size) {
            store = new HashMap[size][size][size];
        }

        /**
         * Save the solution to a problem (i,j,k,t) in the store.
         */
        public void set(int i, int j, int k, int t, int tardiness) {
            if (store[i][j][k] == null)
                store[i][j][k] = new HashMap<Integer, Integer>();

            store[i][j][k].put(t, tardiness);
        }

        /**
         * Return the tardiness of problem (i,j,k,t) or -1 if not available.
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
        public int depth;
        public int calls;
        public int computations;
    }


}

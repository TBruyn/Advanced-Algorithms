import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Our implementation of the Minimum Tardiness algorithm by Lawler
 */
public class DynamicSequence {

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

    public DynamicSequence(ProblemInstance instance) {

        jobs = instance.getJobs();
        store = new Store(jobs.length);
        metrics = new MetricsBag();

        Arrays.sort(jobs, new SortByDeadline()); // O(n log n)
    }

    public int[] calculateSequence() throws Exception {

        // Create a list of all jobs
        JobList list = JobList.fromArray(jobs);

        Result r = calculateTardiness(list, -1, 0, 0);

        return r.toArray();
    }


    public int calculateTardiness() throws Exception {
        // Calculate the sequence
        int[] seq = calculateSequence();

        int t = 0;
        int T = 0;
        for(int i = 0; i < seq.length; i++ ){
            int index = seq[i];
            t += jobs[index][0];
            int Ti = Math.max(0, t - jobs[index][1]);

            T += Ti;
        }

        return T;
    }

    /**
     * Given a list of jobs, a k-index and a starting time t, calculate the minimum tardiness.
     *
     * Note: depth is only passed for performance analysis.
     */
    public Result calculateTardiness(JobList list, int k, int t, int depth) throws Exception {

        metrics.calls++;
        metrics.depth = Math.max(metrics.depth, depth);

        if(depth > jobs.length)
            throw new Exception("Depth cannot exceed number of jobs");

        // Limit i and j to the remaining elements in the list
        int i = list.start.index;
        int j = list.end.index;

        // Base case: empty set
        if (list.length == 0)
            return null;

        // Base case: single element
        if (list.length == 1)
            return new Result(list.start.index,  Math.max(0, t + jobs[list.start.index][0] - jobs[list.start.index][1]), null, null);

        // Check whether this sub-problem has been calculated before
        if (k >= 0) {
            Result result = store.get(i, j, k, t);
            if (result != null)
                return result;
        }

        int[] seq = new int[list.length];

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
        Result bestL = null;
        Result bestR = null;

        for (int d = 0; d <= originalLength; d++) {

            // The time until the left hand side is complete.
            int leftComplete = t + list.totalP;

            // Only compute when d_x > leftComplete, for x the first element in `right`
            if (right.start == null || jobs[right.start.index][1] > leftComplete) {

                // Recurse over the left list (if not empty)
                Result leftRes = list.length == 0 ? null :
                        calculateTardiness(list, kPrime, t, depth+1);
                int tardinessLeft = leftRes == null ? 0 : leftRes.tardiness;

                int kPrimeDone = leftComplete + jobs[kPrime][0];
                int tardinessKPrime = Math.max(0, kPrimeDone - jobs[kPrime][1]);

                // Recurse over the right list (if not empty)
                Result rightRes = right.length == 0 ? null :
                        calculateTardiness(right, kPrime, kPrimeDone, depth+1);
                int tardinessRight = rightRes == null ? 0 : rightRes.tardiness;

                int total = tardinessLeft + tardinessKPrime + tardinessRight;

                if (total < lowestTardiness) {
                    lowestTardiness = total;
                    bestL = leftRes;
                    bestR = rightRes;
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


        Result result = new Result(kPrime, lowestTardiness, bestL, bestR);

        // Store the result for this computation, except the root (k = -1)
        if (k >= 0)
            store.set(i, j, k, t, result);

        return result;
    }

    /**
     * Sort the 2D jobs array by deadline (2nd element of each pair)
     */
    class SortByDeadline implements Comparator<int[]> {
        public int compare(int[] a, int[] b) {
            return a[1] - b[1];
        }
    }

    class Result {
        public int k;
        public int tardiness;
        public Result left;
        public Result right;

        public Result(int k, int tardiness, Result left, Result right) {
            this.k = k;
            this.tardiness = tardiness;
            this.left = left;
            this.right = right;
        }

        public int[] toArray() {
            int[] l = this.left == null ? new int[0] : this.left.toArray();
            int[] r = this.right == null ? new int[0] : this.right.toArray();
            int[] out = new int[l.length + r.length + 1];
            for(int i = 0; i < l.length; i++) {
                out[i] = l[i];
            }
            for(int i = 0; i < r.length; i++) {
                out[l.length + 1 + i] = r[i];
            }
            out[l.length] = k;
            return out;
        }
    }

    /**
     * Caching of computations is done using a 3 dimensional array
     * of HashMaps. The dimensions are `i`, `j` and `k` which range
     * from 0 to n (number of jobs). The time `t` can range from
     * 0 to n * pMax (the largest processing time).
     */
    class Store {

        private HashMap<Integer, Result>[][][] store;

        public Store(int size) {
            store = new HashMap[size][size][size];
        }

        /**
         * Save the solution to a problem (i,j,k,t) in the store.
         */
        public void set(int i, int j, int k, int t, Result result) {
            if (store[i][j][k] == null)
                store[i][j][k] = new HashMap<>();

            store[i][j][k].put(t, result);
        }

        /**
         * Return the tardiness of problem (i,j,k,t) or -1 if not available.
         */
        public Result get(int i, int j, int k, int t) {
            if (store[i][j][k] == null)
                return null;
//                return -1;

            Result result = store[i][j][k].get(t);
            return result;
//            return result == null ? -1 : result.tardiness;
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

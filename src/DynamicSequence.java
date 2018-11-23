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
    private float[][] jobs;

    /**
     * Store some performance data for evaluation
     */
    private MetricsBag metrics;

    private boolean maintainSequence;

    /**
     * Store all calculated results for sub-problems (i,j,k,t)
     */
    private Store<Float> store;
    private Store<Integer> indexStore;


    public DynamicSequence(float[][] jobs) {
        this(jobs, false);
    }

    public DynamicSequence(float[][] jobs, boolean maintainSequence) {

        this.jobs = jobs;
        store = new Store(jobs.length, (float) -1);

        if(maintainSequence)
            indexStore = new Store(jobs.length, -1);

        metrics = new MetricsBag();

        this.maintainSequence = maintainSequence;

        Arrays.sort(jobs, new SortByDeadline()); // O(n log n)
    }

    /** Calculate the total Tardiness of this problem instance */
    public float calculateTardiness() throws Exception {

        // Create a list of all jobs
        JobList<Float> list = JobList.fromArray(jobs);

        // Return the total tardiness
        return calculateTardiness(list, 0, 0);

    }

    /** Calculate the sequence with the lowest tardiness on this problem instance */
    public int[] calculateSequence() throws Exception {

        if(!maintainSequence)
            throw new Exception("Sequence was not maintained, initialize with maintainSequence: true");

        // First let the tardiness computation run to fill the store
        calculateTardiness();

        // Reconstruct the sequence from the cached results.
        return reconstructSequence();

    }

    /** Reconstruct the sequence after computation is done */
    private int[] reconstructSequence() throws Exception {
        int[] sequence = new int[jobs.length];

        JobList list = JobList.fromArray(jobs);
        reconstructSequence(list, 0, 0, sequence);

        return sequence;
    }

    /**
     * Reconstruct a subset of the original job-list, starting at time t and index p0.
     * Put everything in-place in the provided sequence array.
     *
     * @param list The subset of the jobs to reconstruct the sequence from
     * @param t The starting time of these jobs
     * @param p0 The starting index within the sequence
     * @param seq The sequence to fill
     * @return The sequence
     * @throws Exception
     */
    private int[] reconstructSequence(JobList list, int t, int p0, int[] seq) throws Exception {

        if (list.length == 0) {
            return seq;
        }

        // If only one job in the list, put it at position p0
        if (list.length == 1) {
            seq[p0] = list.start.index;
            return seq;
        }

        int i = list.start.index;
        int j = list.end.index;

        // Extract the job with the largest processing time from the list
        int k = list.extractMaxP();

        // Fetch the optimal position for k given this list
        int deltaOfK = indexStore.get(i, j, k, t);

        // Split the list at k
        JobList right = list.split(k + 1);

        // Move over d elements
        // NOTE: we cannot directly split the list at (k + 1 + delta) since our delta means
        // the number of position shifts within the list, which may differ from the original
        // job indices.
        for (int x = 0; x < deltaOfK; x++) {
            list.push(right.removeFirst());
        }

        // Put k in that position (shifted delta to the right from its original position)
        int indexOfK = p0 + list.length;
        seq[indexOfK] = k;

        int leftCompletionTime = t + list.totalP + (int) jobs[k][0];

        // Compute the new starting position for the elements in the right list.
        int rightStartingPosition = p0 + list.length + 1;

        // Recursively reconstruct the left and right parts of the list without k
        reconstructSequence(list, t, p0, seq);
        reconstructSequence(right, leftCompletionTime, rightStartingPosition, seq);

        return seq;
    }

    /**
     * Given a list of jobs, a k-index and a starting time t, calculate the minimum tardiness.
     * <p>
     * Note: depth is only passed for performance analysis.
     */
    public float calculateTardiness(JobList list, int t, int depth) throws Exception {

        metrics.calls++;
        metrics.depth = Math.max(metrics.depth, depth);

        if (depth > jobs.length)
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
        float res = store.get(i, j, kPrime, t);
        if (res >= 0) {
            // Restore list
            list.insert(kPrime);
            return res;
        }

        metrics.computations++;

        float lowestTardiness = Float.MAX_VALUE;

        // Split the list at k', results in:
        // - list: the left side of the split
        // - right: the right side of the split
        JobList right = list.split(kPrime + 1); // Runs O(n)

        // Original length of the list
        int originalLength = right.length;
        int bestD = 0;

        for (int d = 0; d <= originalLength; d++) {

            // The time until the left hand side is complete.
            int leftComplete = t + list.totalP;

            // Only compute when d_x > leftComplete, for x the first element in `right`
            if (right.start == null || jobs[right.start.index][1] > leftComplete) {

                // Recurse over the left list (if not empty)
                float tardinessLeft = list.length == 0 ? 0 :
                        calculateTardiness(list, t, depth + 1);

                int kPrimeDone = leftComplete + (int) jobs[kPrime][0];
                float tardinessKPrime = Math.max(0, kPrimeDone - jobs[kPrime][1]);

                // Recurse over the right list (if not empty)
                float tardinessRight = right.length == 0 ? 0 :
                        calculateTardiness(right, kPrimeDone, depth + 1);

                float total = tardinessLeft + tardinessKPrime + tardinessRight;

                if (total < lowestTardiness) {
                    lowestTardiness = total;
                    bestD = d;
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

        // We store the number of elements k' was moved to the right.
        if(maintainSequence)
            indexStore.set(i, j, kPrime, t, bestD);

        return lowestTardiness;
    }

    class SortByDeadline implements Comparator<float[]> {
        public int compare(float[] a, float[] b) {
            float d = a[1] - b[1];
            return d == 0 ? 0 : d < 0 ? -1 : 1;
        }
    }

    /**
     * Caching of computations is done using a 3 dimensional array
     * of HashMaps. The dimensions are `i`, `j` and `k` which range
     * from 0 to n (number of jobs). The time `t` can range from
     * 0 to n * pMax (the largest processing time).
     *
     * @param <T> The type of value to be stored.
     */
    class Store<T> {

        private HashMap<Integer, T>[][][] store;
        T onEmpty;

        public Store(int size, T onEmpty) {
            store = new HashMap[size][size][size];
            this.onEmpty = onEmpty;
        }

        /**
         * Save the solution to a problem (i,j,k,t) in the store.
         */
        public void set(int i, int j, int k, int t, T result) {
            if (store[i][j][k] == null)
                store[i][j][k] = new HashMap<>();

            store[i][j][k].put(t, result);
        }

        /**
         * Return the tardiness of problem (i,j,k,t) or -1 if not available.
         */
        public T get(int i, int j, int k, int t) {
            if (store[i][j][k] == null)
                return onEmpty;
            T result = store[i][j][k].get(t);
            return result == null ? onEmpty : result;
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

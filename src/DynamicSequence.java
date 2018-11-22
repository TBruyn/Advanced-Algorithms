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

    /**
     * Store all calculated results for sub-problems (i,j,k,t)
     */
    private Store<Float> store;
    private Store<Integer> indexStore;


    public DynamicSequence(float[][] jobs) {

        this.jobs = jobs;
        store = new Store(jobs.length, (float) -1);
        indexStore = new Store(jobs.length, -1);
        metrics = new MetricsBag();

        Arrays.sort(jobs, new SortByDeadline()); // O(n log n)
    }

    public int[] calculateSequence() throws Exception {

        float original = calculateTardinessX();


        int[] seq = new int[jobs.length];
        JobListDecimal list = JobListDecimal.fromArray(jobs);
        reconstructSequence(list, 0, 0, seq);

//        System.out.println("Original: " + original);

        return seq;
    }

    public int[] reconstructSequence(JobListDecimal list, int t, int p0, int[] seq) throws Exception {

        if (list.length == 0) {
            return seq;
        }

//        System.out.println(String.format("Recon list: %s .. %s @ p0: %s", list.getI(), list.getJ(), p0));

        int i = list.start.index;
        int j = list.end.index;

        int originalLength = list.length;


        // Extract the largest k from the list
        int kPrime = list.extractMaxP();


        if (list.length == 0) {
//            System.out.println(String.format("%s -> %s", p0, kPrime));
            if(seq[p0]> 0) {
                throw new Exception( "Overwrite..");
            }
            seq[p0] = kPrime;
            return seq;
        }

        // Fetch the optimal position for kPrime given this list
        int deltaOfKPrime = indexStore.get(i, j, kPrime, t);

//        System.out.println(String.format("k: %s, delta: %s, p0: %s", kPrime, deltaOfKPrime, p0));

        if (deltaOfKPrime < 0)
            System.out.println("NEG!");

        // Split the list at that position
        JobListDecimal right = list.split(kPrime +1);

        // Move over d elements
        for(int x  = 0; x < deltaOfKPrime; x++){
            list.push(right.removeFirst());
        }

//        System.out.println(String.format("%s .. %s | %s .. %s ", list.getI(), list.getJ(), right.getI(), right.getJ()));

        // Put K in that position
//        int index = p0 + list.length + deltaOfKPrime;
        int index = p0 + list.length;
        if(seq[index]> 0) {
            throw new Exception( "Overwrite..");
        }
        seq[index] = kPrime;

//        System.out.println(String.format("%s -> %s", index, kPrime));

        int t1 = t + list.totalP + (int) jobs[kPrime][0];
        int p1 = p0+ originalLength - right.length;

        // Recursively reconstruct the left and right
        reconstructSequence(list, t, p0, seq);
        reconstructSequence(right, t1, p1, seq);

        return seq;
    }


    public float calculateTardiness() throws Exception {
        boolean fromSequence = true;

        if (!fromSequence) {
            return calculateTardinessX();

        } else {

            // Create a list of all jobs
            int[] seq = calculateSequence();

            int t = 0;
            int T = 0;
            for (int i = 0; i < jobs.length; i++) {
                int index = seq[i];
                t += jobs[index][0];
                T += Math.max(0, t - jobs[index][1]);
            }
            return T;
        }
    }

    private float calculateTardinessX() throws Exception {

        // Create a list of all jobs
        JobListDecimal list = JobListDecimal.fromArray(jobs);

        return calculateTardiness(list, 0, 0);

    }

    /**
     * Given a list of jobs, a k-index and a starting time t, calculate the minimum tardiness.
     * <p>
     * Note: depth is only passed for performance analysis.
     */
    public float calculateTardiness(JobListDecimal list, int t, int depth) throws Exception {

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
        JobListDecimal right = list.split(kPrime + 1); // Runs O(n)

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
            else if(d != originalLength)
                throw new Exception("wha"); // TODO Remove

        }

        // Re-insert node kPrime to restore the list
        list.insert(kPrime); // Runs O(n), can improve by remembering beforeK node?

        // Store the result for this computation, except the root (k = -1)
        store.set(i, j, kPrime, t, lowestTardiness);
        indexStore.set(i, j, kPrime, t, /*kPrime +*/ bestD);

        return lowestTardiness;
    }

    class SortByDeadline implements Comparator<float[]> {
        public int compare(float[] a, float[] b) {
            float d = a[1] - b[1];
            return d == 0 ? 0 : d < 0 ? -1 : 1;
        }
    }

    class Result {
        public int k;
        public float tardiness;
        //        public Result left;
//        public Result right;
        public int[] indices;

        public Result(int k, float tardiness, Result left, Result right) {
            this.k = k;
            this.tardiness = tardiness;
//            this.left = left;
//            this.right = right;
            this.indices = this.toArray(k, left, right);
        }

//        public int[] toArray() {
//            int[] l = this.left == null ? new int[0] : this.left.toArray();
//            int[] r = this.right == null ? new int[0] : this.right.toArray();
//            int[] out = new int[l.length + r.length + 1];
//            for(int i = 0; i < l.length; i++) {
//                out[i] = l[i];
//            }
//            for(int i = 0; i < r.length; i++) {
//                out[l.length + 1 + i] = r[i];
//            }
//            out[l.length] = k;
//            return out;
//        }

        public int[] toArray(int k, Result leftRes, Result rightRes) {
            int[] left = leftRes == null ? new int[0] : leftRes.indices;
            int[] right = rightRes == null ? new int[0] : rightRes.indices;
            int[] out = new int[left.length + right.length + 1];
            for (int i = 0; i < left.length; i++) {
                out[i] = left[i];
            }
            for (int i = 0; i < right.length; i++) {
                out[left.length + 1 + i] = right[i];
            }
            out[left.length] = k;
            return out;
        }
    }

    /**
     * Caching of computations is done using a 3 dimensional array
     * of HashMaps. The dimensions are `i`, `j` and `k` which range
     * from 0 to n (number of jobs). The time `t` can range from
     * 0 to n * pMax (the largest processing time).
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

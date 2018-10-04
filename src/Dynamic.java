import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class Dynamic {

    private boolean log = false;

    private int maxP;
    private int numJobs;
    private int[][] jobs;
    private StoreSkipList store;

    private int calls =0;
    private int comps = 0;

    public Dynamic(ProblemInstance instance) {
        numJobs = instance.getNumJobs();
        jobs = instance.getJobs();

        int maxP = 0;
        for(int[] job: jobs) maxP = Math.max(maxP, job[0]);
        this.maxP = maxP;

        store = new StoreSkipList(numJobs, maxP);

        Arrays.sort(jobs, new SortByDeadline()); // O(n log n)
    }

    public int getMinTard() throws Exception {

//        System.out.println("Jobs: ");
//        for (int i = 0; i < numJobs; i++) {
//            System.out.println("- " + i + ": " + jobs[i][0] + ", " + jobs[i][1]);
//        }
//        System.out.println("--------------");

        JobList list = new JobList(jobs, false);

        int min = minTard(list,0, numJobs - 1, -1, 0, 0);
        System.out.println(String.format("Calls: %s, n^5*maxP: %s", calls, Math.pow(numJobs,5) * maxP));

        double n3 = Math.pow(numJobs, 3);
        System.out.println("Number of ijks: " + store.ijks + "/" + n3 + " or " + Math.round((((float) store.ijks) / n3) * 100) + '%');
//        System.out.println("Lookup iterations: " + store.lookupIterations);
//        System.out.println("Completed with " + comps + "/" + calls + " comps/calls: " + Math.round((((float) comps) / calls) * 100) + '%');

        return min;
    }

    public int minTard(JobList list, int i, int j, int k, int t, int depth) throws Exception {

        if(depth > 200)
            throw new Exception("Depth exceeded max");

//        if(list.start != null && i > list.start.index)
//            throw new Exception("i > list.start.index");
//        if(list.end != null && j < list.end.index)
//            throw new Exception("j < list.end.index");

//            String list0 = list.toString();
        int result = _minTard(list, i, j, k, t, depth);

//        if(!list.toString().equals(list0))
//            throw new Exception("Call manipulated list");
        return result;
    }

    public int _minTard(JobList list, int i, int j, int k, int t, int depth) throws Exception {

        calls++;

        // Base case: empty set
        if(list.length == 0)
            return 0;

        // Base case: single element
        if(list.length == 1)
            return Math.max(0, t + jobs[list.start.index][0] - jobs[list.start.index][1]);

        if(k >= 0) {
            int res = store.get(i, j, k, t);
            if (res >= 0) {
                return res;
            }
        }

        comps++;

        // Take the largest job from the list
        int kPrime = list.extractMaxP(); // Runs O(n)

        int len = list.length; // store

        int min = Integer.MAX_VALUE;

        // just take left list?
        JobList right = list.split(kPrime); // Runs O(n)

        for (int d = 0; d <= len; d++) { // O( ?? )

            int leftTotalP = list.totalP;

            // Review the left side of the split
            int TLeft = minTard(list, i, list.end != null ? list.end.index : i, kPrime, t, depth+1);

            // How tardy is k'?
            int kPrimeDone = t + leftTotalP + jobs[kPrime][0];

            int tardKPrime = Math.max(0, kPrimeDone - jobs[kPrime][1]);

            // Review the right side of the split
            int TRight = minTard(right, right.start != null ? right.start.index : i, j, kPrime, kPrimeDone, depth+1);

            int trd = TLeft + tardKPrime + TRight;

            if (trd < min) {
                min = trd;
            }

            // Move over one item
            if(right.length > 0)
                list.push(right.removeFirst()); // Runs O(1)

        }

        // Rejoin lists with k in position?
        if(right != null)
            list.concat(right); // Runs O(1)

        list.insert(kPrime); // Runs O(n), can improve by remembering beforeK node?

        if(min < 0 || min > 100000)
            throw new Exception("Min outside reasonable range " + min);

        if(k >= 0)
            store.set(i,j,k,t,min);

        return min;
    }

    class SortByDeadline implements Comparator<int[]> {
        public int compare(int[] a, int[] b) {
            return a[1] - b[1];
        }
    }

    /**
     * Which i,j,k are used?
     *
     * i, j ->
     *    1 2 3 4
     * 1  * * * *
     * 2    * * *
     * 3      * *
     * 4        *
     *
     * Can only reduce memory by half
     *
     * k: all, but in order of ascending p
     * Can we optimize this somehow?
     *
     * kx: n-x >= |I| >= |J|
     *
     *
     */


    /**
     * This class is used for memoization of all computations.
     */
    class Store {

        public int lookupIterations = 0;

        private ArrayList<Pair>[][][] store;

        public Store(int size) {
            store = new ArrayList[size][size][size];
        }

        public void set(int i, int j, int k, int t, int tardiness) {
            if (store[i][j][k] == null) {
                store[i][j][k] = new ArrayList<Pair>();
            }
            store[i][j][k].add(new Pair(t, tardiness));
        }

        public int get(int i, int j, int k, int t) {
            if (store[i][j][k] == null)
                return -1;
            ArrayList<Pair> pairs = store[i][j][k];

            for (Pair pair : pairs) {
                lookupIterations++;
                if (pair.time == t)
                    return pair.tardiness;
            }
            return -1;
        }


        class Pair {
            public int time;
            public int tardiness;

            public Pair(int time, int tardiness) {
                this.time = time;
                this.tardiness = tardiness;
            }
        }
    }


    /**
     * This class is used for memoization of all computations.
     */
    class StoreMaxP {

        public int lookupIterations = 0;

        private int[][][][] store;

        public StoreMaxP(int size, int maxP) {
            store = new int[size][size][size][size*maxP];
        }

        public void set(int i, int j, int k, int t, int tardiness) {
            store[i][j][k][t] = tardiness + 1;
        }

        public int get(int i, int j, int k, int t) {
            if (store[i][j][k][t] == 0)
                return -1;

            return store[i][j][k][t] - 1;
        }

    }



    /**
     * This class is used for memoization of all computations.
     */
    class StoreHashMap {

        private HashMap<Integer, Integer>[][][] store;

        public StoreHashMap(int size, int maxP) {
            store = new HashMap[size][size][size];
        }

        public void set(int i, int j, int k, int t, int tardiness) {
            if(store[i][j][k] == null)
                store[i][j][k] = new HashMap<Integer, Integer>();

            store[i][j][k].put(t, tardiness);
        }

        public int get(int i, int j, int k, int t) {
            if (store[i][j][k] == null)
                return -1;

            Integer result = store[i][j][k].get(t);
            return result == null ? -1 :  result;
        }

    }



    /**
     * This class is used for memoization of all computations.
     */
    class StoreSkipList {

        public int ijks = 0;
        private ConcurrentSkipListMap<Integer, Integer>[][][] store;

        public StoreSkipList(int size, int maxP) {
            store = new ConcurrentSkipListMap[size][size][size];
        }

        public void set(int i, int j, int k, int t, int tardiness) {
            if(store[i][j][k] == null) {
                ijks++;
                store[i][j][k] = new ConcurrentSkipListMap<Integer, Integer>();
            }

            store[i][j][k].put(t, tardiness);
        }

        public int get(int i, int j, int k, int t) {
            if (store[i][j][k] == null)
                return -1;

            Integer result = store[i][j][k].get(t);
            return result == null ? -1 :  result;
        }

    }



}

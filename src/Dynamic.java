import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Dynamic {

    private boolean log = false;

    private int numJobs;
    private int[][] jobs;
    private Store store;

    private int calls =0;
    private int comps = 0;

    public Dynamic(ProblemInstance instance) {
        numJobs = instance.getNumJobs();
        jobs = instance.getJobs();
        store = new Store(numJobs);

        Arrays.sort(jobs, new SortByDeadline()); // O(n log n)
    }

    public int getMinTard() throws Exception {

//        System.out.println("Jobs: ");
//        for (int i = 0; i < numJobs; i++) {
//            System.out.println("- " + i + ": " + jobs[i][0] + ", " + jobs[i][1]);
//        }
//        System.out.println("--------------");

        JobList list = new JobList(jobs, false);

        R r = minTard(list,0, numJobs - 1, -1, 0, 0);

//        System.out.println("Completed with " + comps + "/" + calls + " comps/calls: " + Math.round((((float) comps) / calls) * 100) + '%');

        int min = r.result;

        return min;
    }

    public R minTard(JobList list, int i, int j, int k, int t, int depth) throws Exception {

        if(depth > 200)
            throw new Exception("Depth exceeded max");

//        if(list.start != null && i > list.start.index)
//            throw new Exception("i > list.start.index");
//        if(list.end != null && j < list.end.index)
//            throw new Exception("j < list.end.index");

//            String list0 = list.toString();
        R result = _minTard(list, i, j, k, t, depth);

//        if(!list.toString().equals(list0))
//            throw new Exception("Call manipulated list");
        return result;
    }

    public R _minTard(JobList list, int i, int j, int k, int t, int depth) throws Exception {

        R r = new R(i, j, k, t);
        calls++;

//        r.set = list.toString();

        // Base case: empty set
        if(list.length == 0)
            return r.done(0);

        // Base case: single element
        if(list.length == 1)
            return r.done(Math.max(0, t + jobs[list.start.index][0] - jobs[list.start.index][1]));

        if(k >= 0) {
            int res = store.get(i, j, k, t);
            if (res >= 0) {
                return r.done(res);
            }
        }

        comps++;

        // Take the largest job from the list
        int kPrime = list.extractMaxP(); // Runs O(n)
        r.kPrime = kPrime;

        // Deltas [ 0, 1, .., j - kPrime ]
//        int[] deltas = generateRange(0, list.length);
//        r.setDeltas(deltas);

        int len = list.length; // store


        int min = Integer.MAX_VALUE;

        // just take left list?
        JobList right = list.split(list.start.index); // Runs O(n)

//        if(deltas.length == 0)
//            System.out.println("No Deltas!");


        for (int d = 0; d <= len; d++) { // O( ?? )
//        for(int d: deltas){

            // Where to split the current list
//            int split = kPrime + d; // i <= kPrime <= split <= j
//
//            if(right == null) {
//                right = list.split(split);
//            } else {
//                if(right.length > 0) // CORRECT?
//                    list.push(right.removeFirst());
//            }

//            if(list.start != null && i > list.start.index)
//                throw new Exception("i > list.start.index");
//            if(list.end != null && split < list.end.index)
//                throw new Exception("split < list.end.index");


            int leftTotalP = list.totalP;

            // Review the left side of the split
            R rLeft = minTard(list, i, list.end != null ? list.end.index : i, kPrime, t, depth+1);
            int TLeft = rLeft.result;

            // How tardy is k'?
            int kPrimeDone = t + leftTotalP + jobs[kPrime][0];

            int tardKPrime = Math.max(0, kPrimeDone - jobs[kPrime][1]);

            // Review the right side of the split
            R rRight = minTard(right, right.start != null ? right.start.index : i, j, kPrime, kPrimeDone, depth+1);
            int TRight = rRight.result;

            int trd = TLeft + tardKPrime + TRight;

            if (trd < min) {
                min = trd;
                r.minD = d;
            }

            // Move over one item
            if(right.length > 0)
                list.push(right.removeFirst()); // Runs O(1)

//            r.tardKPrime[d] = tardKPrime;
//            r.subs[d][0] = rLeft;
//            r.subs[d][1] = rRight;

        }

        // Rejoin lists with k in position?
        if(right != null)
            list.concat(right); // Runs O(1)

        list.insert(kPrime); // Runs O(n), can improve by remembering beforeK node?

        if(min < 0 || min > 100000)
            throw new Exception("Min outside reasonable range " + min);

        if(k >= 0)
            store.set(i,j,k,t,min);

        return r.done(min);
    }

    private int[] generateRange(int i, int j) {
        int size = Math.max(j - i + 1, 0);
        int[] deltas = new int[size];
        for (int x = i; x <= j; x++) {
            deltas[x] = x;
        }
        return deltas;
    }

    class SortByDeadline implements Comparator<int[]> {
        public int compare(int[] a, int[] b) {
            return a[1] - b[1];
        }
    }


    class R {
        public int i, j, k, t, kPrime, size, result, minD;
        public int[] deltas;
        public int[] sumL, sumR, tardKPrime;
        public R[][] subs;
        public String set;


        public R(int i, int j, int k, int t) {
            this.i = i;
            this.j = j;
            this.k = k;
            this.t = t;
            this.minD = -1;
            kPrime = -1;
        }

        public void setDeltas(int[] ds) {
            deltas = ds;
            sumL = new int[ds.length];
            sumR = new int[ds.length];
            tardKPrime = new int[ds.length];
            subs = new R[ds.length][2];
        }

        public R done(int res) {
            result = res;
            return this;
        }

        public String order() {
            String s = "";
            if (subs != null && minD >= 0 && subs[minD][0] != null)
                s += subs[minD][0].order();

            if (kPrime >= 0)
                s += kPrime + ", ";

            if (subs != null && minD >= 0 && subs[minD][1] != null)
                s += subs[minD][1].order();

            return s;
        }

        public String toString() {
            String s = set + "@" + t + " (d: " + minD + ", k': " + kPrime + ", i: " + i + ", j: " + j + ", k: " + k + ") => " + result + "" +
                    "\n";
            if (deltas != null) {
                for (int d : deltas) {

                    s += " d " + d + " {\n";
                    R l = subs[d][0];
                    s += String.format("    " + (l != null ? l.toString() : "").replace("\n", "\n    ") + "\n");
                    s += "    + " + tardKPrime[d] + "\n";
                    R r = subs[d][1];
                    s += String.format("    " + (r != null ? r.toString() : "{}").replace("\n", "\n    ") + "\n");

                }
            }
            s += "}";
            return s;
        }

    }

    /**
     * This class is used for memoization of all computations.
     */
    class Store {

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


}

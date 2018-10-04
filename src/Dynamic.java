import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Dynamic {

    private boolean log = false;

    private int numJobs;
    private int[][] jobs;
    private Store store;

    public int comps = 0;
    public int calls = 0;

    public Dynamic(ProblemInstance instance) {
        numJobs = instance.getNumJobs();
        jobs = instance.getJobs();
        store = new Store(numJobs);

        Arrays.sort(jobs, new SortByDeadline());
    }

    // returns the earliest deadline first schedule
    // sorting is a little quicker, but then it is more tricky
    // to use this as a subroutine for a search method
    public Schedule getSchedule() {

        Schedule s = new Schedule();


        return s;
    }

    public int setSize(int i, int j, int k) {
        int max = k < 0 ? Integer.MAX_VALUE : jobs[k][0];
        int size = 0;
        if (i == 3 && j == 3 && k == 2) {
            size = size;
        }
        for (int x = i; x <= j; x++) {
            if (jobs[x][0] < max)
                size++;
        }
        return size;
    }

    public String setStr(int i, int j, int k) {
        int max = k < 0 ? Integer.MAX_VALUE : jobs[k][0];
        String s = "{";
        for (int x = i; x <= j; x++) {
            if (jobs[x][0] < max)
                s += x + ",";
        }
        return s + "}";
    }

    public int findK(int i, int j, int maxP) {
        int bestIndex = -1;
        int bestP = -1;

        for (int x = i; x <= j; x++) {
            if (jobs[x][0] < maxP) {
                if (jobs[x][0] > bestP) {
                    bestIndex = x;
                    bestP = jobs[x][0];
                }
            }
        }

        return bestIndex;
    }

    public int sumOfP(int i, int j, int k) {
        int sum = 0;
        for (int x = i; x <= j; x++) {
            if (jobs[x][0] < jobs[k][0]) {
                sum += jobs[x][0];
            }
        }
        return sum;
    }

    public int getMinTard() throws Exception {
        if (log) System.out.println("Computing min tard with " + numJobs + " jobs");
        if (log) System.out.println("  i: p, d");
        for (int i = 0; i < numJobs; i++) {
            System.out.println("- " + i + ": " + jobs[i][0] + ", " + jobs[i][1]);
        }

        //  findK(0, numJobs-1, Integer.MAX_VALUE)
//        int min = procedure(0, numJobs - 1, -1, 0, "");
        JobList list = new JobList(jobs, false);
        R r = minTard(list,0, numJobs - 1, -1, 0);
        int min = r.result;
//        System.out.println(r);
//        if (log) System.out.println("Completed with " + comps + "/" + calls + " comps/calls");
        return min;
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

        public String toString3() {
            String s = String.format("R(%s, %s, %s, %s): ", i, j, k, t)
                    + setStr(i, j, k)
                    + String.format(", |S|: %s, k': %s => %s", size, kPrime, result)
                    + "\n";

            if (deltas != null) {
                s += "| deltas: ";
                for (int d : deltas) {
                    s += d + ", ";
                }
                s += "\n";

                for (int d : deltas) {
                    R l = subs[d][0];
                    s += String.format("| L: " + (l != null ? l.toString() : "").replace("\n", "\n-----") + "\n");
                    R r = subs[d][1];
                    s += String.format("| R: " + (r != null ? r.toString() : "").replace("\n", "\n-----") + "\n");
                    s += String.format("| sumL: %s, sumR: %s, tardKPrime: %s \n", sumL[d], sumR[d], tardKPrime[d]);
                }
            }
            return s;
        }

        public String toString5() {
            String s = setStr(i, j, k) + "@" + t + " (d: " + minD + ", k': " + kPrime + ") => " + result;
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

        public String toString4() {
            String s = setStr(i, j, k) + "@" + t + " (d: " + minD + ", k': " + kPrime + ") => " + result;
            if (deltas != null) {
                s += " {\n";
                R l = subs[minD][0];
                s += String.format("    " + (l != null ? l.toString() : "").replace("\n", "\n    ") + "\n");
                s += "    + " + tardKPrime[minD] + "\n";
                R r = subs[minD][1];
                s += String.format("    " + (r != null ? r.toString() : "{}").replace("\n", "\n    ") + "\n");

            }
            s += "}";
            return s;
        }

        public String toString2() {
            String s = setStr(i, j, k) + "@" + t + " => " + result;
            if (deltas != null) {
                s += "\n";
                for (int d : deltas) {
                    R l = subs[d][0];
                    s += String.format("| L: " + (l != null ? l.toString() : "").replace("\n", "\n-----") + "\n");
                    R r = subs[d][1];
                    s += String.format("| R: " + (r != null ? r.toString() : "").replace("\n", "\n-----") + "\n");
                }
            }
            return s;
        }

    }

    // L0 - [k] = L1 & L2
    // R(L1) -> L1
    // R(L2) -> L2
    // L1 <-1- L2
    // R...
    // L1 & L2 + [k]
    // Lx: { length, processingtime }

    // [ i, i+1, ... j ]
    // [ .. ] - k
    // [ i .. k'+d1] [ k'+d1+1 .. j]
    // [ i .. k'+d2] [ k'+d2+1 .. j]
    // [ i .. k'+d1] k [ k'+d1+1 .. j]
    // [ 1, 2, 3, 4 ]
    // [ 1, 2,    4 ]
    // [ 1,       4 ]
    // [ 1
   /* class List {

        public Node start;
        public Node end;

        public int totalProcessing = 0;
        public int length = 0;

        public List(int i, int j) {
            start = new Node(i);
            Node n = start;
            for(int x = i+1; x <= j; x++){
                n = n.join(new Node(x));
                length++;
            }
        }

        public List(Node start, Node end, int size){
            this.start = start;
            this.end = end;
            this.length = size;
        }

        public void push(int x) {
            Node n = new Node(x);
            if(start == null) {
                start = n;
                end = n;
            } else {
                end.join(n);
            }
            length++;
            totalProcessing += 0;
        }

        public void concat(List list) {
            end = end.join(list.start);
            length += list.length;
        }

        public List split(int x) {
            Node n = start;
            Node prev = null;
            while(n != null) {
                if(n.index == x) {
//                    List l = new List(n, end);
                    this.end = prev;
                }
                prev = n;
            }
        }

        class Node {
            int index;
            Node next;

            public Node(int index) {
                this.index = index;
            }

            public Node join(Node n) {
                next = n;
                return n;
            }
        }

    }*/

    public R minTard(JobList list, int i, int j, int k, int t) throws Exception {

        R r = new R(i, j, k, t);

        r.set = list.toString();

        // Base case: empty set
        if(list.length == 0)
            return r.done(0);

        // Base case: single element
        if(list.length == 1)
            return r.done(Math.max(0, t + jobs[list.start.index][0] - jobs[list.start.index][1]));

        // The largest job
        int kPrime = list.extractMaxP(); // list manipulated
        r.kPrime = kPrime;

        // The length of the set
        r.size = list.length;

        // Deltas [ 0, 1, .., j - kPrime ]
        int[] deltas = generateRange(0, j - kPrime);
        r.setDeltas(deltas);

        int min = Integer.MAX_VALUE;

        JobList right = null;

        for (int d : deltas) {

            // Where to split the current list
            int split = kPrime + d; // i <= kPrime <= split <= j

            if(right == null) {
                System.out.println("Split " + list + " at " + split);
                right = list.split(split);
                System.out.println("gives: " + right);

            } else {
                if(right.length > 0) // CORRECT?
                    list.push(right.removeFirst());
            }

            System.out.println(kPrime  + "+" + d + " || " + list + " || " + right);

            // Total length of S(i, split, k')
            int sumLeft = list.totalP;// sumOfP(i, split, kPrime);
            r.sumL[d] = sumLeft;

            // Total length of S(split+1, j, k')
            int sumRight = right.totalP; // sumOfP(split + 1, j, kPrime);
            r.sumR[d] = sumRight;

            // Review the left side of the split
            String a0 = list.toString();
            R rLeft = minTard(list, i, split, kPrime, t);
            if(!list.toString().equals(a0))
                throw new Exception("List changed by recursion");

            int TLeft = rLeft.result;
            r.subs[d][0] = rLeft;

            // How tardy is k'?
            int kPrimeDone = t + sumLeft + jobs[kPrime][0];

            int tardKPrime = Math.max(0, kPrimeDone - jobs[kPrime][1]);
            r.tardKPrime[d] = tardKPrime;

            // Review the right side of the split
            String a1 = right.toString();
            R rRight = split >= j ? null : minTard(right,split + 1, j, kPrime, kPrimeDone);
            if(!right.toString().equals(a1))
                throw new Exception("List changed by recursion");
            int TRight = rRight == null ? 0 : rRight.result;
            r.subs[d][1] = rRight;

            int trd = TLeft + tardKPrime + TRight;

            if (trd < min) {
                min = trd;
                r.minD = d;
            }
        }

        // Rejoin lists with k in position?
        if(right != null)
            list.concat(right);

        list.insert(kPrime);

        // R(i,j,k,t) => result

        return r.done(min);
    }


//    public R minTardX(int i, int j, int k, int t) throws Exception {
//
//        // Node(i, Node(i+1, Node(..., Node(j, ))))
//
//        R r = new R(i, j, k, t);
//
//        // The current job length limit
//        int pLim = k < 0 ? Integer.MAX_VALUE : jobs[k][0];
//
//        // The largest job
//        int kPrime = findK(i, j, pLim); // one pass over Node list, get List without K
//        r.kPrime = kPrime;
//
//        // If the set is empty, the tardiness is zero.
//        if (kPrime < 0)
//            return r.done(0);
//
//        // The length of the set
//        int size = setSize(i, j, k); // O(1)
//        r.size = size;
//
//        if (size == 1)
//            return r.done(Math.max(0, t + jobs[kPrime][0] - jobs[kPrime][1]));
//
//        // Deltas [ 0, 1, .., j - kPrime ]
//        int[] deltas = generateRange(0, j - kPrime);
//        r.setDeltas(deltas);
//
//        int min = Integer.MAX_VALUE;
//
//        for (int d : deltas) {
//
//            // Where to split the current list
//            int split = kPrime + d; // i <= kPrime <= split <= j
//
//            // first d: Split List at split: List L, List R
//            // second d: move first node of R to L
//
//            // Total length of S(i, split, k')
//            int sumLeft = sumOfP(i, split, kPrime);
//            r.sumL[d] = sumLeft;
//
//            // Total length of S(split+1, j, k')
//            int sumRight = sumOfP(split + 1, j, kPrime);
//            r.sumR[d] = sumRight;
//
//            // Review the left side of the split
//            R rLeft = minTard(i, split, kPrime, t);
//            int TLeft = rLeft.result;
//            r.subs[d][0] = rLeft;
//
//            // How tardy is k'?
//            int kPrimeDone = t + sumLeft + jobs[kPrime][0];
//            int tardKPrime = Math.max(0, kPrimeDone - jobs[kPrime][1]);
//            r.tardKPrime[d] = tardKPrime;
//
//            // Review the right side of the split
//            R rRight = split >= j ? null : minTard(split + 1, j, kPrime, kPrimeDone);
//            int TRight = rRight == null ? 0 : rRight.result;
//            r.subs[d][1] = rRight;
//
//            int sumAll = sumOfP(i, j, kPrime);
//            int totalP = sumLeft + sumRight;
//
//            int trd = TLeft + tardKPrime + TRight;
//
//            if (trd < min) {
//                min = trd;
//                r.minD = d;
//            }
//        }
//
//        // Rejoin lists with k in position?
//        // R(i,j,k,t) => result
//
//        return r.done(min);
//    }

    // Walk from k to j
    public int[] generateDeltas(int j, int k) {
        int size = Math.max(j - k + 1, 0);
        int[] deltas = new int[size];
        for (int x = 0; x < deltas.length; x++) {
            deltas[x] = x;
        }
        return deltas;
    }

    public int[] generateRange(int i, int j) {
        int size = Math.max(j - i + 1, 0);
        int[] deltas = new int[size];
        for (int x = i; x <= j; x++) {
            deltas[x] = x;
        }
        return deltas;
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

    class SortByDeadline implements Comparator<int[]> {
        public int compare(int[] a, int[] b) {
            return a[1] - b[1];
        }
    }


}

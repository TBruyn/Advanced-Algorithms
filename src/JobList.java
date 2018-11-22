/**
 * Efficient implementation to manage the sub-problem instance.
 *
 * Implemented as a singly-linked sorted list.
 */
public class JobList {

    /**
     * Keep all n jobs of the entire problem in memory, no need
     * to create new JobNode objects later-on which reduces run-time
     * and garbage-collection overhead.
     *
     * Careful: All JobList instances share the same set of nodes!
     */
    public JobNode[] jobs;

    public JobNode start;
    public JobNode end;

    /**
     * Total processing time of all jobs in the list
     */
    public int totalP = 0;

    /**
     * The number of jobs in the list
     */
    public int length = 0;

    /**
     * Create a JobList from a 2D jobs array
     */
    public static JobList fromArray(int[][] jobs) throws Exception {
        JobNode[] nodes = new JobNode[jobs.length];

        for (int x = 0; x < jobs.length; x++)
            nodes[x] = new JobNode(x, jobs[x][0], jobs[x][1]);

        return new JobList(nodes, false);
    }

    public int[] toArray() {
        int[] arr = new int[length];
        JobNode x = start;
        for(int i = 0; i < length; i++) {
            arr[i] = x.index;
            x = x.next;
        }
        return arr;
    }

    /**
     * Create a new JobList based on the set of all jobs
     * Runs O( empty ? 1 : n )
     */
    public JobList(JobNode[] jobs, boolean empty) throws Exception {
        this.jobs = jobs;

        if (!empty)
            for (int x = 0; x < jobs.length; x++) this.push(x);

    }

    /**
     * Push job with index x to the end of the list.
     * Runs O(1)
     */
    public JobNode push(int x) throws Exception {
        return push(jobs[x].reset());
    }

    /**
     * Push a given node to the end of the list.
     * Runs O(1)
     */
    public JobNode push(JobNode n) throws Exception {
        if (length == 0) {
            start = end = n;
        } else {
            end = end.join(n);
        }

        n.join(null); // since our end

        length++;
        totalP += n.p;

        return n;
    }

    /**
     * Insert job with index x in this (sorted) list
     * Runs O(n)
     */
    public JobNode insert(int x) throws Exception {

        if (length == 0)
            return this.push(x);

        JobNode newNode = jobs[x].reset();
        JobNode current = start;

        if (current.index > x) {
            newNode.join(start);
            start = newNode;
        } else {
            while (current != null) {
                if (current.next == null) // end
                    return this.push(newNode);
                else if (current.next.index > x)
                    break;

                current = current.next;
            }
            newNode.join(current.next);
            current.join(newNode);
        }

        length++;
        totalP += jobs[x].p;


        return newNode;
    }

    /**
     * Remove the first job with the largest processing time from the list and
     * return its index.
     * Runs O(n)
     */
    public int extractMaxP() throws Exception {

        if (length == 0)
            throw new Exception("No maxP in empty list");

        JobNode current = start;
        JobNode prev = null;
        int currP = 0;

        int maxP = 0;
        JobNode k = null;
        JobNode beforeK = null;

        // Find k (first JobNode with max processing time)
        while (current != null) {
            currP = current.p;

            if (currP > maxP) {
                k = current;
                beforeK = prev;
                maxP = currP;
            }

            prev = current;
            current = current.next;
        }

        // Remove k
        remove(beforeK, k); // O(1)


        return k.index;
    }

    /**
     * Remove a given JobNode from the list, passing its predecessor for speed-up.
     * Runs O(1)
     */
    private void remove(JobNode predecessor, JobNode target) throws Exception { // List must contain these!
        if (predecessor == null) // start
            start = target.next;
        else
            predecessor.join(target.next);

        if (target.next == null) // end
            end = predecessor;

        length--;
        totalP -= target.p;


    }

    /**
     * Split this list at a job with index x
     * Runs O(n)
     */
    public JobList split(int x) throws Exception {
        int lengthL = 0;
        int totalPL = 0;

        JobNode current = start;
        JobNode prev = null;

        while (current != null) {
            if (current.index >= x) {

                // Build the right list
                JobList right = new JobList(jobs, true);
                right.start = current;
                right.end = end;
                right.length = length - lengthL;
                right.totalP = totalP - totalPL;

                // Correct the left list
                if (current == start) {
                    start = null;
                    end = null;
                } else {
                    this.end = prev;
                    prev.join(null);
                }
                length = lengthL;
                totalP = totalPL;

                return right;
            }

            lengthL += 1;
            totalPL += current.p;
            prev = current;
            current = current.next;
        }

        return new JobList(jobs, true); // O(1)
    }

    /**
     * Remove the first element from this list.
     * Runs O(1)
     */
    public JobNode removeFirst() throws Exception {
        if (this.length == 0)
            throw new Exception("List is empty");

        if (this.length == 1)
            this.end = null;

        JobNode first = start;
        start = start.next;
        length -= 1;
        totalP -= first.p;

        first.join(null);

        return first;
    }

    /**
     * This class represents a node in the singly-linked list.
     */
    static class JobNode {

        /** The index in the original input (in increasing deadline order). */
        int index;

        /** The processing time of this job */
        int p;

        /** The deadline of this job */
        int d;

        /** The next job currently linked to */
        JobNode next;

        public JobNode(int index, int p, int d) {
            this.index = index;
            this.p = p;
            this.d = d;
        }

        /**
         * Link this node to a given node
         * Runs O(1)
         */
        public JobNode join(JobNode n) {
            next = n;
            return n;
        }

        /**
         * Unlink this node. This makes the node re-usable for optimization purposes.
         */
        public JobNode reset() {
            this.next = null;
            return this;
        }
    }

}

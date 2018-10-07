public class JobList {

    public boolean checkInv = false;

    public Node[] Nodes;

    public Node start;
    public Node end;

    public int totalP = 0;
    public int length = 0;
    public int maxP = 0;

    public static JobList fromArray (int [][] jobs) throws Exception {
        Node[] Nodes = new Node[jobs.length];
        for(int x = 0; x < jobs.length; x++) Nodes[x] = new Node(x, jobs[x][0], jobs[x][1]);
        return new JobList(Nodes, false);
    }

    /**
     * Runs empty ? O(1) : O(n)
     */
    public JobList(Node[] Nodes, boolean empty) throws Exception {
        this.Nodes = Nodes;
        if (!empty)
            for (int x = 0; x < Nodes.length; x++) this.push(x);
        checkInvariant();
    }

    public JobList(Node[] Nodes, Node start, Node end, int length, int totalP) throws Exception {
        this.Nodes = Nodes;
        this.start = start;
        this.end = end;
        this.length = length;
        this.totalP = totalP;

        checkInvariant();
    }

    /**
     * Checks the invariants of the list
     * @throws Exception
     */
    public void checkInvariant() throws Exception {
        if (checkInv) {

            int i = 0;
            int p = 0;
            int ind = -1;
            Node curr = start;
            while (curr != null) {
                i += 1;
                p += curr.p;

                if (curr.index <= ind)
                    throw new Exception("Invariant: indices in wrong order");
                ind = curr.index;

                curr = curr.next;
            }
            if (i != length)
                throw new Exception("Invariant: length is incorrect");

            if (p != totalP)
                throw new Exception("Invariant: totalP is incorrect");

            if (length != 0 && start == null)
                throw new Exception("Invarian: start null, length > 0");

            if (length != 0 && end == null)
                throw new Exception("Invarian: end null, length > 0");

            if (end != null && end.next != null)
                throw new Exception("Invarian: end.next != null");

            if (length == 1 && start != end)
                throw new Exception("Invarian: start != end && length == 1");

        }
    }

    /**
     * Runs O(1)
     */
    public Node push(int x) throws Exception {
        return push(Nodes[x].reset());
    }

    /**
     * Runs O(1)
     */
    public Node push(Node n) throws Exception {
        if (length == 0) {
            start = end = n;
        } else {
            end = end.join(n);
        }

        n.join(null); // since our end

        length++;
        totalP += n.p;
        maxP = Math.max(maxP, n.p); // store the maximum P
        checkInvariant();
        return n;
    }

    /**
     * Runs O(n)
     */
    public Node insert(int x) throws Exception {

        if (length == 0)
            return this.push(x);

        Node newNode = Nodes[x].reset();
        Node current = start;

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
        totalP += Nodes[x].p;

        checkInvariant();
        return newNode;
    }

    /**
     * Runs O(n)
     */
    public int extractMaxP() throws Exception {

        if (length == 0)
            throw new Exception("No maxP in empty list");

        Node current = start;
        Node prev = null;
        int currP = 0;

        int maxP = 0;
        Node k = null;
        Node beforeK = null;

        // Find k (first Node with max processing time)
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

        checkInvariant();
        return k.index;
    }

    /**
     * Runs O(1)
     */
    private void remove(Node predecessor, Node target) throws Exception { // List must contain these!
        if (predecessor == null) // start
            start = target.next;
        else
            predecessor.join(target.next);

        if (target.next == null) // end
            end = predecessor;

        length--;
        totalP -= target.p;

        checkInvariant();
    }

    /**
     * Runs O(1)
     */
    public JobList concat(JobList list) throws Exception {
        if (list == null)
            throw new Exception("Right list cannot be null");

        if (length == 0) { // |left| is 0, copy right list
            start = list.start;
            end = list.end;
            length = list.length;
            totalP = list.totalP;

        } else if (list.length > 0) { //
            end.join(list.start);
            end = list.end;

            length += list.length;
            totalP += list.totalP;
        }

        checkInvariant();
        return this;
    }

    /**
     * Runs O(n)
     */
    public JobList split(int x) throws Exception {
        int lengthL = 0;
        int totalPL = 0;

        Node current = start;
        Node prev = null;

        while (current != null) {
            if (current.index >= x) {
                // Build the right list
                JobList right = new JobList(
                        Nodes,
                        current,
                        end,
                        length - lengthL,
                        totalP - totalPL); // O(1)

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
        checkInvariant();
        return new JobList(Nodes, true); // O(1)
    }

    /**
     * Runs O(1)
     */
    public Node removeFirst() throws Exception {
        if (this.length == 0)
            throw new Exception("List is empty");

        if (this.length == 1)
            this.end = null;

        Node first = start;
        start = start.next;
        length -= 1;
        totalP -= first.p;

        first.join(null);
        checkInvariant();
        return first;
    }

    public String toString() {
        String s = "{";
        Node current = start;
        while (current != null) {
            s += current.index + ",";
            current = current.next;
        }
        return s + "} " + String.format("[ len: %s, totalP: %s ]", length, totalP);
    }

    static class Node {

        int index;
        int p;
        int d;
        Node next;

        public Node(int index, int p, int d)
        {
            this.index = index;
            this.p = p;
            this.d = d;
        }

        /**
         * Runs O(1)
         */
        public Node join(Node n) {
            next = n;
            return n;
        }

        public Node reset(){
            this.next = null;
            return this;
        }
    }

}
